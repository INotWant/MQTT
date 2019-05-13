package mqtt.protocol;

/**
 * @author iwant
 * @date 19-5-12 16:59
 * @desc UNSUBSCRIBE 报文的可变报头
 */
public final class UnsubscribeVH implements VariableHeader {

    private int messageId;

    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }
}
