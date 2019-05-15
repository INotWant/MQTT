package UT;

import mqtt.tool.OtherUtil;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author iwant
 * @date 19-5-13 20:00
 * @desc OtherUtil 测试
 */
public class OtherUtilTest {

    @Test
    public void isValidTopicFilterTest() {
        String topicFilter;

        topicFilter = "";
        Assert.assertFalse(OtherUtil.isValidTopicFilter(topicFilter));

        topicFilter = "#";
        Assert.assertTrue(OtherUtil.isValidTopicFilter(topicFilter));

        topicFilter = "sport/#";
        Assert.assertTrue(OtherUtil.isValidTopicFilter(topicFilter));

        topicFilter = "sport/tennis#";
        Assert.assertFalse(OtherUtil.isValidTopicFilter(topicFilter));

        topicFilter = "sport/tennis/#/ranking";
        Assert.assertFalse(OtherUtil.isValidTopicFilter(topicFilter));

        topicFilter = "+";
        Assert.assertTrue(OtherUtil.isValidTopicFilter(topicFilter));

        topicFilter = "+/tennis/#";
        Assert.assertTrue(OtherUtil.isValidTopicFilter(topicFilter));

        topicFilter = "sport+";
        Assert.assertFalse(OtherUtil.isValidTopicFilter(topicFilter));

        topicFilter = "sport/+/player1";
        Assert.assertTrue(OtherUtil.isValidTopicFilter(topicFilter));

        topicFilter = "/finance";
        Assert.assertTrue(OtherUtil.isValidTopicFilter(topicFilter));

        topicFilter = "/sport/+a/";
        Assert.assertFalse(OtherUtil.isValidTopicFilter(topicFilter));
    }

    @Test
    public void splitTopicFilterTest() {
        String topicFilter;
        String[] words;

        topicFilter = "";
        Assert.assertNull(OtherUtil.splitTopicFilter(""));

        topicFilter = "/";
        words = new String[1];
        words[0] = "";
        Assert.assertArrayEquals(words, OtherUtil.splitTopicFilter(topicFilter));

        topicFilter = "#";
        words = new String[1];
        words[0] = "#";
        Assert.assertArrayEquals(words, OtherUtil.splitTopicFilter(topicFilter));

        topicFilter = "/#";
        words = new String[2];
        words[0] = "";
        words[1] = "#";
        Assert.assertArrayEquals(words, OtherUtil.splitTopicFilter(topicFilter));

        topicFilter = "+";
        words = new String[1];
        words[0] = "+";
        Assert.assertArrayEquals(words, OtherUtil.splitTopicFilter(topicFilter));

        topicFilter = "/+";
        words = new String[2];
        words[0] = "";
        words[1] = "+";
        Assert.assertArrayEquals(words, OtherUtil.splitTopicFilter(topicFilter));

        topicFilter = "/finance";
        words = new String[2];
        words[0] = "";
        words[1] = "finance";
        Assert.assertArrayEquals(words, OtherUtil.splitTopicFilter(topicFilter));

        topicFilter = "+/+";
        words = new String[2];
        words[0] = "+";
        words[1] = "+";
        Assert.assertArrayEquals(words, OtherUtil.splitTopicFilter(topicFilter));

        topicFilter = "sport/+/player1";
        words = new String[3];
        words[0] = "sport";
        words[1] = "+";
        words[2] = "player1";
        Assert.assertArrayEquals(words, OtherUtil.splitTopicFilter(topicFilter));

        topicFilter = "/finance/";
        words = new String[3];
        words[0] = "";
        words[1] = "finance";
        words[2] = "";
        Assert.assertArrayEquals(words, OtherUtil.splitTopicFilter(topicFilter));

        topicFilter = "+/+/";
        words = new String[3];
        words[0] = "+";
        words[1] = "+";
        words[2] = "";
        Assert.assertArrayEquals(words, OtherUtil.splitTopicFilter(topicFilter));

        topicFilter = "+/tennis/#";
        words = new String[3];
        words[0] = "+";
        words[1] = "tennis";
        words[2] = "#";
        Assert.assertArrayEquals(words, OtherUtil.splitTopicFilter(topicFilter));

        topicFilter = "//";
        words = new String[3];
        words[0] = "";
        words[1] = "";
        words[2] = "";
        Assert.assertArrayEquals(words, OtherUtil.splitTopicFilter(topicFilter));

        topicFilter = "中国/山东/济南";
        words = new String[3];
        words[0] = "中国";
        words[1] = "山东";
        words[2] = "济南";
        Assert.assertArrayEquals(words, OtherUtil.splitTopicFilter(topicFilter));

        topicFilter = "中国/山东/济南/";
        words = new String[4];
        words[0] = "中国";
        words[1] = "山东";
        words[2] = "济南";
        words[3] = "";
        Assert.assertArrayEquals(words, OtherUtil.splitTopicFilter(topicFilter));
    }

    @Test
    public void topicMatchTopicFilterTest() {
        String topicName;
        String topicFilter;

        topicName = "";
        topicFilter = "";
        Assert.assertFalse(OtherUtil.topicMatchTopicFilter(topicName, topicFilter));

        topicName = "中国";
        topicFilter = "#";
        Assert.assertTrue(OtherUtil.topicMatchTopicFilter(topicName, topicFilter));

        topicName = "/中国/山东";
        topicFilter = "#";
        Assert.assertTrue(OtherUtil.topicMatchTopicFilter(topicName, topicFilter));

        topicName = "/中国";
        topicFilter = "/+";
        Assert.assertTrue(OtherUtil.topicMatchTopicFilter(topicName, topicFilter));

        topicName = "/中国/山东";
        topicFilter = "+/+/#";
        Assert.assertTrue(OtherUtil.topicMatchTopicFilter(topicName, topicFilter));

        topicName = "/中国/山东/济南";
        topicFilter = "+/+/#";
        Assert.assertTrue(OtherUtil.topicMatchTopicFilter(topicName, topicFilter));

        topicName = "/中国/山东/济南";
        topicFilter = "/中国/山东/济南/";
        Assert.assertFalse(OtherUtil.topicMatchTopicFilter(topicName, topicFilter));

        topicName = "/中国/山东/济南";
        topicFilter = "/中国/山东/济南/#";
        Assert.assertTrue(OtherUtil.topicMatchTopicFilter(topicName, topicFilter));

        topicName = "/中国/山东/济南";
        topicFilter = "/中国/山东/济南/县城";
        Assert.assertFalse(OtherUtil.topicMatchTopicFilter(topicName, topicFilter));

        topicName = "/中国/山东/济南";
        topicFilter = "/中国/山东/济南";
        Assert.assertTrue(OtherUtil.topicMatchTopicFilter(topicName, topicFilter));
    }

}
