package UT;

import mqtt.entity.Session;
import mqtt.entity.SubscribeTree;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author iwant
 * @date 19-5-14 10:49
 * @desc 订阅树测试
 */
public class SubscribeTreeTest {

    private static Session session1 = new Session();
    private static Session session2 = new Session();
    private static Session session3 = new Session();
    private static Session session4 = new Session();
    private static Session session5 = new Session();
    private static Session session6 = new Session();


    /**
     * 单例测试
     */
    @Test
    public void singleInstanceTest() {
        SubscribeTree instance1 = SubscribeTree.getInstance();
        SubscribeTree instance2 = SubscribeTree.getInstance();
        Assert.assertEquals(instance1, instance2);
    }

    /**
     * 添加 订阅 测试
     */
    @Test
    public void addTest() {
        SubscribeTree subscribeTree = SubscribeTree.getInstance();
        String topicFilter;

        topicFilter = "";
        Assert.assertFalse(subscribeTree.addTopicFilter(topicFilter, new Session(), 0));

        topicFilter = "/tennis+";
        Assert.assertFalse(subscribeTree.addTopicFilter(topicFilter, new Session(), 0));

        topicFilter = "/中国/北京";
        Assert.assertTrue(subscribeTree.addTopicFilter(topicFilter, new Session(), 0));
    }

    /**
     * 添加&匹配订阅测试
     */
    @Test
    public void addAndMathTest() {
        SubscribeTree subscribeTree = SubscribeTree.getInstance();
        String topicFilter;
        String topic;

        topicFilter = "#";
        Assert.assertTrue(subscribeTree.addTopicFilter(topicFilter, session1, 0));
        topic = "/中国";
        Assert.assertEquals(1, subscribeTree.match(topic).size());

        topicFilter = "/+";
        Assert.assertTrue(subscribeTree.addTopicFilter(topicFilter, session2, 0));
        topic = "/中国";
        Assert.assertEquals(2, subscribeTree.match(topic).size());

        topicFilter = "/#";
        Assert.assertTrue(subscribeTree.addTopicFilter(topicFilter, session3, 0));
        topic = "/中国";
        Assert.assertEquals(3, subscribeTree.match(topic).size());

        topicFilter = "+/+";
        Assert.assertTrue(subscribeTree.addTopicFilter(topicFilter, session4, 0));
        topic = "/中国";
        Assert.assertEquals(4, subscribeTree.match(topic).size());

        topicFilter = "+/+/#";
        Assert.assertTrue(subscribeTree.addTopicFilter(topicFilter, session5, 0));
        topic = "/中国";
        Assert.assertEquals(4, subscribeTree.match(topic).size());

        topicFilter = "/中国/山东/济南";
        Assert.assertTrue(subscribeTree.addTopicFilter(topicFilter, session6, 0));
        topic = "/中国/山东/济南";
        Assert.assertEquals(4, subscribeTree.match(topic).size());
    }

    /**
     * 删除&匹配订阅测试
     */
    @Test
    public void removeAndMath() {
        SubscribeTree subscribeTree = SubscribeTree.getInstance();
        String topicFilter;
        String topic;

        topicFilter = "/中国/山东/济南";
        subscribeTree.removeTopicFilter(topicFilter, new Session());
        topic = "/中国/山东/济南";
        Assert.assertEquals(4, subscribeTree.match(topic).size());

        topicFilter = "/中国/山东/济南";
        subscribeTree.removeTopicFilter(topicFilter, session6);
        topic = "/中国/山东/济南";
        Assert.assertEquals(3, subscribeTree.match(topic).size());

        topicFilter = "+/+";
        subscribeTree.removeTopicFilter(topicFilter, session4);
        topic = "/中国";
        Assert.assertEquals(3, subscribeTree.match(topic).size());

        topicFilter = "/#";
        subscribeTree.removeTopicFilter(topicFilter, session3);
        topic = "/中国";
        Assert.assertEquals(2, subscribeTree.match(topic).size());

        topicFilter = "/+";
        subscribeTree.removeTopicFilter(topicFilter, session2);
        topic = "/中国";
        Assert.assertEquals(1, subscribeTree.match(topic).size());

        topicFilter = "#";
        subscribeTree.removeTopicFilter(topicFilter, session2);
        topic = "/中国";
        Assert.assertEquals(1, subscribeTree.match(topic).size());

        topicFilter = "#";
        subscribeTree.removeTopicFilter(topicFilter, session1);
        topic = "/中国";
        Assert.assertEquals(0, subscribeTree.match(topic).size());
    }

}
