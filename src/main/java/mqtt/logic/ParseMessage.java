package mqtt.logic;

import mqtt.MqttConfig;
import mqtt.exception.IllegalStateException;
import mqtt.exception.MessageFormatException;
import mqtt.exception.UnknownControlTypeException;
import mqtt.protocol.*;
import mqtt.tool.ByteUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author iwant
 * @date 19-5-12 10:36
 * @desc 解析报文静态类
 */
public final class ParseMessage {

    /**
     * 解析报文并填充 {@link Message} --> 拆包
     *
     * @param message 将解析的结果填充至该实例
     * @param bytes   报文字节流
     */
    public static void parseMessage(Message message, byte[] bytes) throws UnknownControlTypeException, MessageFormatException, IllegalStateException {
        byte controlType = message.getControlType();
        switch (controlType) {
            case ControlType.CONNECT:
                parseConnect(message, bytes);
                break;
            case ControlType.PUBLISH:
                parsePublish(message, bytes);
                break;
            case ControlType.PUBACK:
            case ControlType.PUBREC:
            case ControlType.PUBREL:
            case ControlType.PUBCOMP:
                parsePublishVerify(message, bytes);
                break;
            case ControlType.SUBSCRIBE:
                parseSubscribe(message, bytes);
                break;
            case ControlType.UNSUBSCRIBE:
                parseUnsubscribe(message, bytes);
                break;
            case ControlType.PINGREQ:
                parsePingReq(message, bytes);
                break;
            case ControlType.DISCONNECT:
                parseDisconnect(message, bytes);
                break;
            default:
                throw new UnknownControlTypeException("unknown control type: " + Integer.toHexString(controlType));
        }
    }

    /*
     * 解析 CONNECT 报文
     */
    private static void parseConnect(Message message, byte[] bytes) throws MessageFormatException, IllegalStateException {
        int index = 1 + ByteUtil.getRemainLengthLen(message.getRemainLength());

        // 可变报头
        ConnectVH connectVH = new ConnectVH();
        int protocolLen = ByteUtil.twoByteToInt(bytes[index], bytes[index + 1]);
        index += 2;
        connectVH.setProtocolName(ByteUtil.getStringFromBytes(bytes, index, protocolLen));
        index += protocolLen;
        connectVH.setLevel(bytes[index]);
        // level --> 假设向下兼容
        if (connectVH.getLevel() > MqttConfig.PROTOCOL_LEVEL)
            throw new IllegalStateException("Unsupported mqtt.protocol level!",
                    IllegalStateException.ILLEGAL_PROTOCOL_LEVEL);
        index += 1;
        connectVH.setUserNameFlag(ByteUtil.getSpecialBinaryBit(bytes[index], 7) == 1);
        connectVH.setPassWordFlag(ByteUtil.getSpecialBinaryBit(bytes[index], 6) == 1);
        if (!connectVH.isUserNameFlag() && connectVH.isPassWordFlag())
            throw new IllegalStateException("if want to use password, must use username!",
                    IllegalStateException.ILLEGAL_USERNAME_PASSWORD_FLAG);
        connectVH.setWillRetainFlag(ByteUtil.getSpecialBinaryBit(bytes[index], 5) == 1);
        int willQos = ByteUtil.getSpecialBinaryBit(bytes[index], 4) * 2;
        willQos += ByteUtil.getSpecialBinaryBit(bytes[index], 3);
        if (willQos > 2)
            throw new MessageFormatException("connect message's  variable header, will qos error");
        connectVH.setWillQos(willQos);
        connectVH.setWillFlag(ByteUtil.getSpecialBinaryBit(bytes[index], 2) == 1);
        connectVH.setCleanSessionFlag(ByteUtil.getSpecialBinaryBit(bytes[index], 1) == 1);
        if (ByteUtil.getSpecialBinaryBit(bytes[index], 0) == 1)
            throw new MessageFormatException("connect message's variable header, reserved error!");
        index += 1;
        connectVH.setKeepAlive(ByteUtil.twoByteToInt(bytes[index], bytes[index + 1]));
        index += 2;
        message.setVariableHeader(connectVH);

        // 有效载荷
        ConnectPayload connectPayload = new ConnectPayload();
        int clientIdLen = ByteUtil.twoByteToInt(bytes[index], bytes[index + 1]);
        index += 2;
        if (clientIdLen == 0) {
            if (!connectVH.isCleanSessionFlag())
                throw new IllegalStateException("illegal clientId!",
                        IllegalStateException.ILLEGAL_CLIENT_ID);
            connectPayload.setClientId("");
        } else {
            connectPayload.setClientId(ByteUtil.getStringFromBytes(bytes, index, clientIdLen));
            index += clientIdLen;
        }
        if (connectVH.isWillFlag()) {
            int topicLen = ByteUtil.twoByteToInt(bytes[index], bytes[index + 1]);
            index += 2;
            connectPayload.setWillTopic(ByteUtil.getStringFromBytes(bytes, index, topicLen));
            index += topicLen;
            int contentLen = ByteUtil.twoByteToInt(bytes[index], bytes[index + 1]);
            index += 2;
            List<Byte> willContent = new ArrayList<>();
            for (int i = 0; i < contentLen; i++)
                willContent.add(bytes[index + i]);
            connectPayload.setWillContent(willContent);
            index += contentLen;
        }
        if (connectVH.isUserNameFlag()) {
            int userNameLen = ByteUtil.twoByteToInt(bytes[index], bytes[index + 1]);
            index += 2;
            connectPayload.setUserName(ByteUtil.getStringFromBytes(bytes, index, userNameLen));
            index += userNameLen;
        }
        if (connectVH.isPassWordFlag()) {
            int passWordLen = ByteUtil.twoByteToInt(bytes[index], bytes[index + 1]);
            index += 2;
            connectPayload.setPassWord(ByteUtil.getStringFromBytes(bytes, index, passWordLen));
            index += passWordLen;
        }
        message.setPayload(connectPayload);

        assert index == bytes.length;
    }

    /*
     * 解析 PUBLISH 报文
     */
    private static void parsePublish(Message message, byte[] bytes) {
        int index = 1 + ByteUtil.getRemainLengthLen(message.getRemainLength());

        // 可变报头
        PublishVH publishVH = new PublishVH();
        int topicLen = ByteUtil.twoByteToInt(bytes[index], bytes[index + 1]);
        index += 2;
        publishVH.setTopicName(ByteUtil.getStringFromBytes(bytes, index, topicLen));
        index += topicLen;
        publishVH.setMessageId(ByteUtil.twoByteToInt(bytes[index], bytes[index + 1]));
        index += 2;
        message.setVariableHeader(publishVH);

        // 有效载荷
        PublishPayload publishPayload = new PublishPayload();
        List<Byte> content = new ArrayList<>();
        for (int i = index; i < bytes.length; i++)
            content.add(bytes[index]);
        publishPayload.setContent(content);
        index = bytes.length;
        message.setPayload(publishPayload);

        assert index == bytes.length;
    }

    /*
     * 解析 PUBACK or PUBREC or PUBREL or PUBCOMP 报文
     */
    private static void parsePublishVerify(Message message, byte[] bytes) throws MessageFormatException {
        assert message.getRemainLength() == 2;

        if (message.getControlType() == ControlType.PUBREL)
            if (message.getFlag() != 0x02)
                throw new MessageFormatException("pubrel message's fixed header, flag error");

        PublishVerifyVH publishVerifyVH = new PublishVerifyVH();
        publishVerifyVH.setMessageId(ByteUtil.twoByteToInt(bytes[2], bytes[3]));
        message.setVariableHeader(publishVerifyVH);
    }

    /*
     * 解析 SUBSCRIBE 报文
     */
    private static void parseSubscribe(Message message, byte[] bytes) throws MessageFormatException {
        if (message.getFlag() != 0x02)
            throw new MessageFormatException("subscribe message's fixed header, flag error");

        int index = 1 + ByteUtil.getRemainLengthLen(message.getRemainLength());

        // 可变报头
        SubscribeVH subscribeVH = new SubscribeVH();
        subscribeVH.setMessageId(ByteUtil.twoByteToInt(bytes[index], bytes[index + 1]));
        message.setVariableHeader(subscribeVH);
        index += 2;

        // 有效载荷
        SubscribePayload subscribePayload = new SubscribePayload();
        List<String> topicFilters = new ArrayList<>();
        List<Integer> qoss = new ArrayList<>();
        int topicFilterLen;
        while (index < bytes.length) {
            topicFilterLen = ByteUtil.twoByteToInt(bytes[index], bytes[index + 1]);
            index += 2;
            topicFilters.add(ByteUtil.getStringFromBytes(bytes, index, topicFilterLen));
            index += topicFilterLen;
            if (bytes[index] >> 2 != 0)
                throw new MessageFormatException("subscribe message's payload, reserved error");
            int qos = bytes[index] & 0x03;
            if (qos > 2)
                throw new MessageFormatException("subscribe message's payload, qos error");
            qoss.add(qos);
            index += 1;
        }
        subscribePayload.setTopicFilters(topicFilters);
        subscribePayload.setQoss(qoss);
        message.setPayload(subscribePayload);
    }

    /*
     * 解析 UNSUBSCRIBE 报文
     */
    private static void parseUnsubscribe(Message message, byte[] bytes) throws MessageFormatException {
        if (message.getFlag() != 0x02)
            throw new MessageFormatException("subscribe message's fixed header, flag error");

        int index = 1 + ByteUtil.getRemainLengthLen(message.getRemainLength());

        // 可变报头
        UnsubscribeVH unsubscribeVH = new UnsubscribeVH();
        unsubscribeVH.setMessageId(ByteUtil.twoByteToInt(bytes[index], bytes[index + 1]));
        message.setVariableHeader(unsubscribeVH);
        index += 2;

        // 有效载荷
        UnsubscribePayload unsubscribePayload = new UnsubscribePayload();
        List<String> topicFilters = new ArrayList<>();
        int topicFilterLen;
        while (index < bytes.length) {
            topicFilterLen = ByteUtil.twoByteToInt(bytes[index], bytes[index + 1]);
            index += 2;
            topicFilters.add(ByteUtil.getStringFromBytes(bytes, index, topicFilterLen));
            index += topicFilterLen;
        }
        unsubscribePayload.setTopicFilters(topicFilters);
        message.setPayload(unsubscribePayload);
    }

    /*
     * 解析 PINGREQ 报文
     */
    private static void parsePingReq(Message message, byte[] bytes) {
        assert message.getRemainLength() == 0;
    }

    /*
     * 解析 DISCONNECT 报文
     */
    private static void parseDisconnect(Message message, byte[] bytes) {
        assert message.getRemainLength() == 0;
    }
}
