package mqtt.logic;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import mqtt.protocol.Message;

/**
 * @author iwant
 * @date 19-5-13 10:46
 * @desc 将响应报文转换成字节流 --> 封包
 */
public class MqttMessageToBytesEncoder extends MessageToByteEncoder<Message> {
    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {
        PackageMessage.packageMessage(msg, out);
    }
}
