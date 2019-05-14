package mqtt.tool;

import java.nio.charset.Charset;

/**
 * @author iwant
 * @date 19-5-12 09:45
 * @desc 字节操作相关的工具类
 */
public final class ByteUtil {

    /**
     * 获取操作数指定二进制位
     * 注：指定位从 0 开始
     *
     * @param optNum 操作数
     * @param pos    指定位
     * @return 1 or 0
     */
    public static int getSpecialBinaryBit(int optNum, int pos) {
        return (optNum >> pos) & 0x1;
    }

    /**
     * 获取 remain length
     *
     * @param bytes remain length 字段
     * @return remain length
     */
    public static int getRemainLength(byte[] bytes) {
        int p = 0;
        int remainLength = 0;
        for (byte b : bytes)
            for (int j = 0; j < 7; j++)
                remainLength += (getSpecialBinaryBit(b, j) * Math.pow(2, p++));
        return remainLength;
    }

    /**
     * 将 remainLength 转为二进制形式
     *
     * @param remainLength remainLength
     * @return remainLength 对应的二进制形式
     */
    public static byte[] remainLengthToBytes(int remainLength) {
        int len = getRemainLengthLen(remainLength);
        byte[] bytes = new byte[len];
        for (int i = 0; i < bytes.length - 1; i++) {
            bytes[i] = (byte) ((remainLength & 0x0000007f) + 0x80);
            remainLength >>= 7;
        }
        bytes[len - 1] = (byte) remainLength;
        return bytes;
    }

    /**
     * h获取 remain length 字段的长度
     */
    public static int getRemainLengthLen(int remainLength) {
        if (remainLength < 0)
            return 0;
        if (remainLength < 128)
            return 1;
        if (remainLength < 16384)
            return 2;
        if (remainLength < 2097152)
            return 3;
        if (remainLength < 268435456)
            return 4;
        return 0;
    }

    /**
     * 将 2个byte 换成 int
     *
     * @param msb 高有效位
     * @param lsb 第有效位
     */
    public static int twoByteToInt(byte msb, byte lsb) {
        return (msb << 8) + lsb;
    }

    /**
     * 将 byte 数组 {@param startIndex} 开始长为 {@param length} 的转为字符串
     *
     * @param bytes      byte 数组
     * @param startIndex 开始索引（包含）
     * @param length     长度
     * @return 字符串
     */
    public static String getStringFromBytes(byte[] bytes, int startIndex, int length) {
        if (length <= 0)
            return "";
        return new String(bytes, startIndex, length, Charset.forName("UTF-8"));
    }

}
