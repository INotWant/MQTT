package UT;

import org.junit.Assert;
import org.junit.Test;
import tool.ByteUtil;

/**
 * @author iwant
 * @date 19-5-12 09:20
 * @desc 解析相关单元测试
 */
public class ParseTest {

    /**
     * 测试 remainLength 字段的解析
     */
    @Test
    public void getRemainLengthTest() {
        byte[] bytes = new byte[1];

        bytes[0] = 0x00;
        int remainLength = 0;
        Assert.assertEquals(remainLength, ByteUtil.getRemainLength(bytes));

        bytes[0] = 0x7F;
        remainLength = 127;
        Assert.assertEquals(remainLength, ByteUtil.getRemainLength(bytes));

        bytes = new byte[4];
        bytes[0] = (byte) 0x80;
        bytes[1] = (byte) 0x80;
        bytes[2] = (byte) 0x80;
        bytes[3] = 0x01;
        remainLength = 2097152;
        Assert.assertEquals(remainLength, ByteUtil.getRemainLength(bytes));

        bytes = new byte[4];
        bytes[0] = (byte) 0xFF;
        bytes[1] = (byte) 0xFF;
        bytes[2] = (byte) 0xFF;
        bytes[3] = 0x7F;
        remainLength = 268435455;
        Assert.assertEquals(remainLength, ByteUtil.getRemainLength(bytes));
    }


}
