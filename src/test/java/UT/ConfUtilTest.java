package UT;

import mqtt.MqttConfig;
import mqtt.tool.ConfUtil;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author iwant
 * @date 19-5-15 16:37
 * @desc ConfUtil 测试类
 */
public class ConfUtilTest {

    @Test
    public void confUtilTest() {
        int oldValue = 100;
        MqttConfig.KEEP_ALIVE = oldValue;
        ConfUtil.open("src/main/resources/conf.properties");
        ConfUtil.setting(MqttConfig.class);
        Assert.assertNotEquals(oldValue, MqttConfig.KEEP_ALIVE);
        Assert.assertEquals(10, MqttConfig.KEEP_ALIVE);
        ConfUtil.close();
    }

}
