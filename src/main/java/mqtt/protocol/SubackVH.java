package mqtt.protocol;

/**
 * @author iwant
 * @date 19-5-12 16:41
 * @desc SUBACK 相关的可变报头
 */
public final class SubackVH implements VariableHeader {

    private int messageId;

    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }
}
