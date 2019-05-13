package mqtt.protocol;

import mqtt.tool.ByteUtil;

/**
 * @author iwant
 * @date 19-5-11 17:33
 * @desc 报文
 */
public class Message {

    private byte controlAndFlag;
    private int remainLength;

    // 可变报头
    private VariableHeader variableHeader;
    // 有效载荷
    private Payload payload;

    public Message(byte controlAndFlag) {
        this.controlAndFlag = controlAndFlag;
    }

    public Message(int controlType, int flag) {
        this.controlAndFlag = (byte) (((controlType & 0xf) << 4) + (flag & 0xf));
    }

    public byte getControlAndFlag() {
        return controlAndFlag;
    }

    public void setControlAndFlag(int controlType, int flag) {
        this.controlAndFlag = (byte) (((controlType & 0xf) << 4) + (flag & 0xf));
    }

    public void setControlAndFlag(byte controlAndFlag) {
        this.controlAndFlag = controlAndFlag;
    }

    /**
     * 获取报文控制类型
     */
    public byte getControlType() {
        return (byte) (this.controlAndFlag >> 4);
    }

    /**
     * 设置报文控制类型
     */
    public void setControlType(int controlType) {
        this.controlAndFlag = (byte) (this.controlAndFlag & 0xf + ((controlType & 0xf) << 4));
    }

    /**
     * 获取标志位
     */
    public byte getFlag() {
        return (byte) (this.controlAndFlag & 0xf);
    }

    /**
     * 设置标志位
     */
    public void setFlag(int flag) {
        this.controlAndFlag = (byte) (this.controlAndFlag & 0xf0 + (flag & 0xf));
    }

    public int getRemainLength() {
        return remainLength;
    }

    public void setRemainLength(int remainLength) {
        this.remainLength = remainLength;
    }

    public VariableHeader getVariableHeader() {
        return variableHeader;
    }

    public void setVariableHeader(VariableHeader variableHeader) {
        this.variableHeader = variableHeader;
    }

    public Payload getPayload() {
        return payload;
    }

    public void setPayload(Payload payload) {
        this.payload = payload;
    }


    /*-------------------------------------------
     *          注：以下方法仅针对 PUBLISH 报文
     *-------------------------------------------
     */

    /**
     * 获取 QOS
     */
    public int getQos() {
        return (getFlag() >> 1) & 0x03;
    }

    /**
     * 设置 QOS
     */
    public void setQos(int qos) {
        if (qos >= 0 && qos <= 2)
            setFlag((getFlag() & ~0x06) + (qos << 1));
    }

    /**
     * 获取是否是 消息重发
     */
    public boolean isDup() {
        return ByteUtil.getSpecialBinaryBit(getFlag(), 3) == 1;
    }

    /**
     * 设置 DUP 字段
     */
    public void setDup(boolean isDup) {
        if (isDup)
            setFlag((getFlag() & ~0x08) + 8);
        else
            setFlag(getFlag() & ~0x08);
    }

    /**
     * 获取是否需 保留消息
     */
    public boolean isRetain() {
        return ByteUtil.getSpecialBinaryBit(getFlag(), 0) == 1;
    }

    /**
     * 设置 RETAIN 字段
     */
    public void setRetain(boolean isRetain) {
        if (isRetain)
            setFlag((getFlag() & ~0x01) + 1);
        else
            setFlag(getFlag() & ~0x01);
    }

}
