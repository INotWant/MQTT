package mqtt.logic;

import io.netty.buffer.ByteBuf;
import mqtt.exception.UnknownControlTypeException;
import mqtt.protocol.*;
import mqtt.tool.ByteUtil;

import java.nio.charset.Charset;

/**
 * @author iwant
 * @date 19-5-13 16:48
 * @desc 打包报文类
 */
public final class PackageMessage {

    /**
     * 封包 {@link Message}
     *
     * @param message 源 message 实例
     * @param byteBuf 存储封包结果（字节流）
     * @throws UnknownControlTypeException 未知控制报文类型异常
     */
    public static void packageMessage(Message message, ByteBuf byteBuf) throws UnknownControlTypeException {
        byte controlType = message.getControlType();
        switch (controlType) {
            case ControlType.CONNACK:
                packageConnack(message, byteBuf);
                break;
            case ControlType.PUBLISH:
                packagePublish(message, byteBuf);
                break;
            case ControlType.PUBACK:
            case ControlType.PUBREC:
            case ControlType.PUBREL:
            case ControlType.PUBCOMP:
            case ControlType.UNSUBACK:
                packagePublishVerify(message, byteBuf);
                break;
            case ControlType.SUBACK:
                packageSuback(message, byteBuf);
                break;
            case ControlType.PINGRESP:
                packagePingresp(message, byteBuf);
                break;
            default:
                throw new UnknownControlTypeException("unknown control type: " + Integer.toHexString(controlType));
        }
    }

    /*
     * 封包 CONNACK
     */
    private static void packageConnack(Message message, ByteBuf byteBuf) {
        byteBuf.writeByte(message.getControlAndFlag());
        byteBuf.writeByte(message.getRemainLength());
        ConnackVH connackVH = (ConnackVH) message.getVariableHeader();
        byteBuf.writeByte(connackVH.getSp());
        byteBuf.writeByte(connackVH.getStateCode());
    }

    /*
     * 封包 PUBLISH
     */
    private static void packagePublish(Message message, ByteBuf byteBuf) {
        byteBuf.writeByte(message.getControlAndFlag());
        byteBuf.writeBytes(ByteUtil.remainLengthToBytes(message.getRemainLength()));

        PublishVH publishVH = (PublishVH) message.getVariableHeader();
        int topicNameLen = publishVH.getTopicName().length();
        byteBuf.writeByte(topicNameLen >> 8);
        byteBuf.writeByte(topicNameLen);
        byteBuf.writeBytes(publishVH.getTopicName().getBytes(Charset.forName("UTF-8")));
        int messageId = publishVH.getMessageId();
        byteBuf.writeByte(messageId >> 8);
        byteBuf.writeByte(messageId);

        PublishPayload publishPayload = (PublishPayload) message.getPayload();
        for (byte b : publishPayload.getContent())
            byteBuf.writeByte(b);
    }

    /*
     * 封包 PUBACK or PUBREC or PUBREL or PUBCOMP or UNSUBACK
     */
    private static void packagePublishVerify(Message message, ByteBuf byteBuf) {
        byteBuf.writeByte(message.getControlAndFlag());
        byteBuf.writeByte(message.getRemainLength());

        PublishVerifyVH publishVerifyVH = (PublishVerifyVH) message.getVariableHeader();
        int messageId = publishVerifyVH.getMessageId();
        byteBuf.writeByte(messageId >> 8);
        byteBuf.writeByte(messageId);
    }

    /*
     * 封包 SUBACK
     */
    private static void packageSuback(Message message, ByteBuf byteBuf) {
        byteBuf.writeByte(message.getControlAndFlag());
        byteBuf.writeBytes(ByteUtil.remainLengthToBytes(message.getRemainLength()));

        SubackVH subackVH = (SubackVH) message.getVariableHeader();
        int messageId = subackVH.getMessageId();
        byteBuf.writeByte(messageId >> 8);
        byteBuf.writeByte(messageId);

        SubackPayload subackPayload = (SubackPayload) message.getPayload();
        for (byte b : subackPayload.getReturnCodes())
            byteBuf.writeByte(b);
    }

    /*
     * 封包 PINGRESP
     */
    private static void packagePingresp(Message message, ByteBuf byteBuf) {
        byteBuf.writeByte(message.getControlAndFlag());
        byteBuf.writeByte(message.getRemainLength());
    }

}
