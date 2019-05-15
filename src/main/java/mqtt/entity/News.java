package mqtt.entity;

import java.util.List;

/**
 * @author iwant
 * @date 19-5-11 16:36
 * @desc 消息
 */
public final class News {

    /*
     * 设计为线程安全的类：
     * 构造完成后不允许发生改变！
     */

    private final String topic;
    private final List<Byte> content;
    // 注意仅为服务器接受到此消息时对应的服务质量
    private final int qos;

    public News(String topic, List<Byte> content, int qos) {
        this.topic = topic;
        this.content = content;
        this.qos = qos;
    }

    public String getTopic() {
        return topic;
    }

    public List<Byte> getContent() {
        return content;
    }

    public int getQos() {
        return qos;
    }

}
