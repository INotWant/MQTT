package mqtt;

import java.net.URL;

/**
 * @author iwant
 * @date 19-5-13 08:48
 * @desc 含一些配置信息
 */
public final class MqttConfig {

    // 默认支持的协议等级
    public static int PROTOCOL_LEVEL = 0x04;
    // 默认 KeepAlive 时间
    public static int KEEP_ALIVE = 100;
    // 默认IP
    public static String IP = "127.0.0.1";
    // 默认端口号
    public static int PORT = 1883;

    public static void PROTOCOL_LEVEL(int level) {
        PROTOCOL_LEVEL = level;
    }

    public static void KEEP_ALIVE(int keepAlive) {
        KEEP_ALIVE = keepAlive;
    }

    public static void IP(String ip) {
        IP = ip;
    }

    public static void PORT(int port) {
        PORT = port;
    }

}
