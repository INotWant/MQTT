package mqtt;

import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Log4JLoggerFactory;
import mqtt.entity.News;
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
    // 保留消息集：key -> topic name; value -> news
    public static Map<String, News> retainNews = new ConcurrentHashMap<>();
    // 维护服务端的报文标识符
    public static ConcurrentHashMap<Integer, Integer> messageIds = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        // 设置日志
        InternalLoggerFactory.setDefaultFactory(Log4JLoggerFactory.INSTANCE);
    }

}
