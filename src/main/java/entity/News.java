package entity;

import java.util.List;

/**
 * @author iwant
 * @date 19-5-11 16:36
 * @desc 消息
 */
public final class News {

    private String topic;
    private List<Byte> content;
    private int qos;

    public News(String topic, List<Byte> content, int qos) {
        this.topic = topic;
        this.content = content;
        this.qos = qos;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public List<Byte> getContent() {
        return content;
    }

    public void setContent(List<Byte> content) {
        this.content = content;
    }

    public int getQos() {
        return qos;
    }

    public void setQos(int qos) {
        this.qos = qos;
    }
}
