package mqtt.entity;

import mqtt.tool.OtherUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author iwant
 * @date 19-5-11 16:37
 * @desc 订阅树
 * 05.14 10:47 初稿
 */
public final class SubscribeTree {

    /* 线程安全：
     *  1) 订阅构建与匹配无需用锁 --> 弱一致性
     *  2) 订阅删除与匹配无需用锁 --> 弱一致性
     *  3) 订阅构建与订阅删除需要锁 --> synchronized
     *  4) 订阅构建之间 or 订阅删除之间 亦需要锁 --> synchronized
     *  注：1、2 的满足需要并发容器配合
     * 数据结构：
     *  类似“字典树”
     * 设计模式：
     *  单例模式
     */

    private class TreeNode {
        private String word;
        private TreeNode parentTreeNode;
        // 记录有哪些 session 订阅 --> key: Session, value: qos
        private Map<Session, Integer> sessionAndQosMap = new ConcurrentHashMap<>();
        // 记录下层（字节点）
        private Map<String, TreeNode> childTreeNodes = new ConcurrentHashMap<>();

        TreeNode(String word, TreeNode parentTreeNode) {
            this.word = word;
            this.parentTreeNode = parentTreeNode;
        }

        String getWord() {
            return word;
        }

        /**
         * 添加一个订阅
         *
         * @param session session
         * @param qos     订阅的质量
         */
        void addSubscribe(Session session, int qos) {
            this.sessionAndQosMap.put(session, qos);
        }

        Map<Session, Integer> getSessionAndQosMap() {
            return sessionAndQosMap;
        }

        Map<String, TreeNode> getChildTreeNodes() {
            return childTreeNodes;
        }

        /**
         * 添加一个新的字节点
         */
        void addChildTreeNode(String word, TreeNode childTreeNode) {
            this.childTreeNodes.put(word, childTreeNode);
        }

        TreeNode getParentTreeNode() {
            return parentTreeNode;
        }
    }

    // 订阅树根节点 :: 无对应 topic filter 中的实际意义
    private TreeNode root = new TreeNode(null, null);
    private static SubscribeTree subscribeTree = new SubscribeTree();

    private SubscribeTree() {
    }

    /**
     * 获取 subscribeTree 单例
     *
     * @return 单例实例
     */
    public synchronized static SubscribeTree getInstance() {
        return subscribeTree;
    }

    /**
     * 添加新的订阅关系
     *
     * @param topicFilter topic filter
     * @param session     topic filter 相关的会话
     * @param qos         订阅的质量
     * @return 是否添加订阅成功
     */
    public synchronized boolean addTopicFilter(String topicFilter, Session session, int qos) {
        String[] words = OtherUtil.splitTopicFilter(topicFilter);
        if (words == null)
            return false;
        TreeNode node = this.root;
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            TreeNode childTreeNode = node.getChildTreeNodes().get(word);
            if (childTreeNode == null) {
                childTreeNode = new TreeNode(word, node);
                node.addChildTreeNode(word, childTreeNode);
            }
            if (i == words.length - 1)
                childTreeNode.addSubscribe(session, qos);
            node = childTreeNode;
        }
        return true;
    }

    /**
     * 删除某一订阅关系
     *
     * @param topicFilter topic filter
     * @param session     session
     */
    public synchronized void removeTopicFilter(String topicFilter, Session session) {
        String[] words = OtherUtil.splitTopicFilter(topicFilter);
        if (words == null)
            return;
        // 先走到最底部，从下往上删除
        TreeNode node = this.root;
        for (String word : words) {
            TreeNode childTreeNode = node.getChildTreeNodes().get(word);
            if (childTreeNode == null)
                return;
            node = childTreeNode;
        }
        // 现在 node 指向最后一层
        Map<Session, Integer> sessionAndQosMap = node.getSessionAndQosMap();
        Map<String, TreeNode> childTreeNodes = node.getChildTreeNodes();
        // 删除对应订阅
        sessionAndQosMap.remove(session);
        while (sessionAndQosMap.size() == 0 && childTreeNodes.size() == 0) {
            TreeNode parentTreeNode = node.getParentTreeNode();
            if (parentTreeNode == null)
                return;
            parentTreeNode.getChildTreeNodes().remove(node.getWord());
            node = parentTreeNode;
            sessionAndQosMap = node.getSessionAndQosMap();
            childTreeNodes = node.getChildTreeNodes();
        }
    }

    /**
     * 匹配
     *
     * @param topic 消息主题
     * @return 所有订阅给定主题的会话，key : 会话，value ： qos
     */
    public Map<Session, Integer> match(String topic) {
        Map<Session, Integer> result = new HashMap<>();
        String[] words = OtherUtil.splitTopicFilter(topic);
        if (words == null)
            return result;
        matchHelper(words, 0, this.root, result);
        return result;
    }

    /*
     * 匹配 helper ，用于对 ‘+’ 的递归匹配
     */
    private void matchHelper(String[] words, int start, TreeNode root, Map<Session, Integer> saveMap) {
        if (root != null) {
            // 多层通配符
            Map<String, TreeNode> childTreeNodes = root.getChildTreeNodes();
            TreeNode node = childTreeNodes.get("#");
            if (node != null)
                save(node, saveMap);

            // 单层通配符
            node = childTreeNodes.get("+");
            if (node != null)
                if (start == words.length - 1)
                    // 最后一层，直接添加
                    save(node, saveMap);
                else
                    // 非最后一层，递归
                    matchHelper(words, start + 1, node, saveMap);

            // 非通配符
            String word = words[start];
            node = childTreeNodes.get(word);
            if (node != null)
                if (start == words.length - 1)
                    // 最后一层，直接添加
                    save(node, saveMap);
                else
                    // 非最后一层，递归
                    matchHelper(words, start + 1, node, saveMap);
        }
    }

    // 保存匹配的订阅
    private void save(TreeNode node, Map<Session, Integer> saveMap) {
        for (Map.Entry<Session, Integer> entry : node.getSessionAndQosMap().entrySet()) {
            Session session = entry.getKey();
            Integer qos = entry.getValue();
            Integer oldQos = saveMap.get(session);
            if (oldQos == null)
                oldQos = qos;
            // 若对同一个 session 匹配多此，则保存最大 qos
            saveMap.put(session, Math.max(qos, oldQos));
        }
    }

}
