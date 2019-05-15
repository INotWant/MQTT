package mqtt.entity;

import io.netty.channel.Channel;
import mqtt.tool.Pair;

import java.util.HashMap;
import java.util.Map;

/**
 * @author iwant
 * @date 19-5-11 16:36
 * @desc 会话
 */
public final class Session {

    private String clientId;
    // 协议级别
    private int level;
    // false 不清理会话， True 时清理
    private boolean cleanSessionFlag;
    private int keepAlive;

    private String userName = null;
    private String passWord = null;

    // 遗嘱
    private Will will = null;
    // 用于与客户端通信
    private Channel channel;
    // 上一次请求时间
    private long lastReqTime;
    // 未确认的消息（在重新连接后可能重传的消息） --> key: 报文标识符; value: 对应的消息
    // qos2 的确认未完成，作为接受者
    private Map<Integer, News> unconfirmedMessages = new HashMap<>();
    // 作为发送者，未确认的 qos 1 和 qos 2 级别的消息
    // 注意：与 unconfirmedMessages 的区别
    // key --> messageId, value --> news & qos
    private Map<Integer, Pair<News, Integer>> unconfirmedMessagesForSender = new HashMap<>();
    // 当保留会话状态时，在连接断开期间，此会话订阅匹配的消息（即恢复连接后将要传送给客户端的 Qos1 和 Qos2 消息）
    // key --> news, value --> qos
    private Map<News, Integer> unsentMessages = new HashMap<>();
    // 所有的订阅信息：key： topic filter， value： qos
    private Map<String, Integer> subscribes = new HashMap<>();

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public boolean isCleanSessionFlag() {
        return cleanSessionFlag;
    }

    public void setCleanSessionFlag(boolean cleanSessionFlag) {
        this.cleanSessionFlag = cleanSessionFlag;
    }

    public int getKeepAlive() {
        return keepAlive;
    }

    public void setKeepAlive(int keepAlive) {
        this.keepAlive = keepAlive;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassWord() {
        return passWord;
    }

    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }

    public Will getWill() {
        return will;
    }

    public void setWill(Will will) {
        this.will = will;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public long getLastReqTime() {
        return lastReqTime;
    }

    public void setLastReqTime(long lastReqTime) {
        this.lastReqTime = lastReqTime;
    }

    public Map<Integer, News> getUnconfirmedMessages() {
        return unconfirmedMessages;
    }

    public void removeUnconfirmedMessage(int messageId) {
        this.unconfirmedMessages.remove(messageId);
    }

    public News getUnconfirmedMessage(int messageId) {
        return this.unconfirmedMessages.get(messageId);
    }

    public void addUnconfirmedMessage(int messageId, News news) {
        this.unconfirmedMessages.put(messageId, news);
    }

    public Map<Integer, Pair<News, Integer>> getUnconfirmedMessagesForSender() {
        return unconfirmedMessagesForSender;
    }

    public News getUnconfirmedMessageForSender(int messageId) {
        return this.unconfirmedMessagesForSender.get(messageId).getF();
    }

    public void addUnconfirmedMessageForSender(int messageId, News news, int qos) {
        this.unconfirmedMessagesForSender.put(messageId, new Pair<>(news, qos));
    }

    public void removeUnconfirmedMessageForSender(int messageId) {
        this.unconfirmedMessagesForSender.remove(messageId);
    }


    public Map<News, Integer> getUnsentMessages() {
        return this.unsentMessages;
    }

    public void addUnsentMessage(News news, int qos) {
        unsentMessages.put(news, qos);
    }

    public Map<String, Integer> getSubscribes() {
        return subscribes;
    }

    public void addSubscribe(String topicFilter, int qos) {
        this.subscribes.put(topicFilter, qos);
    }

    public void removeSubscribe(String topicFilter) {
        this.subscribes.remove(topicFilter);
    }
}
