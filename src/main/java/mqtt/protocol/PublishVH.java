package mqtt.protocol;

/**
 * @author iwant
 * @date 19-5-12 16:17
 * @desc PUBLISH 可变报头
 */
public final class PublishVH implements VariableHeader{

    private String topicName;
    // 报文标识符
    private int messageId;

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }
}
