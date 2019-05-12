package logic;

import exception.IllegalStateException;
import exception.MessageFormatException;
import exception.UnknownControlTypeException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import protocol.Message;
import tool.ByteUtil;

import java.util.List;

import static logic.ParseMessage.parseMessage;

/**
 * @author iwant
 * @date 19-5-12 09:28
 * @desc 将字节流解析为 Message
 */
public class BytesToMessageDecoder extends ByteToMessageDecoder {

    private int remainLength = -1;
    private Message message = null;

    /**
     * 将字节流解析为 {@link protocol.Message}。
     * 主要逻辑：
     * 1）获取完整报文
     * 2）将完整报文解析成 Message
     */
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws MessageFormatException, UnknownControlTypeException, IllegalStateException {
        if (remainLength == -1) {
            if (in.readableBytes() >= 2) {
                // 获取 remain length 字段的长度
                int len = 1;
                byte b = in.getByte(in.readerIndex() + 1);
                if (ByteUtil.getSpecialBinaryBit(b, 7) == 1) {
                    ++len;
                    if (in.readableBytes() < 3)
                        return;
                    else {
                        b = in.getByte(in.readerIndex() + 2);
                        if (ByteUtil.getSpecialBinaryBit(b, 7) == 1) {
                            ++len;
                            if (in.readableBytes() < 4)
                                return;
                            else {
                                b = in.getByte(in.readerIndex() + 3);
                                if (ByteUtil.getSpecialBinaryBit(b, 7) == 1) {
                                    ++len;
                                    if (in.readableBytes() < 5)
                                        return;
                                    else {
                                        if (ByteUtil.getSpecialBinaryBit(b, 7) == 1)
                                            throw new MessageFormatException("attr of remain length error!");
                                    }
                                }
                            }
                        }
                    }
                }
                // 构建 message 实例，并获取 remain length
                this.message = new Message(in.readByte());
                byte[] bytes = new byte[len];
                in.readBytes(bytes);
                this.remainLength = ByteUtil.getRemainLength(bytes);
                this.message.setRemainLength(this.remainLength);

                // 解析报文并填充 message
                if (in.readableBytes() >= this.remainLength) {
                    bytes = new byte[this.remainLength];
                    in.readBytes(bytes);
                    parseMessage(this.message, bytes);
                    out.add(this.message);
                    this.remainLength = -1;
                    this.message = null;
                }
            }
        } else {
            // 解析报文并填充 message
            if (in.readableBytes() >= this.remainLength) {
                byte[] bytes = new byte[this.remainLength];
                in.readBytes(bytes);
                parseMessage(this.message, bytes);
                out.add(this.message);
                this.remainLength = -1;
                this.message = null;
            }
        }
    }
}
