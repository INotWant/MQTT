package mqtt;

import mqtt.entity.Session;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author iwant
 * @date 19-5-11 16:34
 * @desc Mqtt 中间件启动类
 */
public class MqttBrokerBootstrap {

    // 含有所有的当下存活 or 未清理 的 session
    public static Map<String, Session> sessions = new ConcurrentHashMap<>();

    public static void main(String[] args) {
    }

}