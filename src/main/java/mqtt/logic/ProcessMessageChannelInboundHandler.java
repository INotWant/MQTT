package mqtt.logic;

import io.netty.channel.*;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import mqtt.MqttBrokerBootstrap;
import mqtt.entity.Session;
import mqtt.entity.Will;
import mqtt.exception.UnknownControlTypeException;
import mqtt.protocol.*;
import mqtt.tool.OtherUtil;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static mqtt.MqttConfig.DEFAULT_KEEP_ALIVE;

/**
 * @author iwant
 * @date 19-5-13 08:30
 * @desc 处理 Message 逻辑
 */
public class ProcessMessageChannelInboundHandler extends SimpleChannelInboundHandler<Message> {

    private static final InternalLogger log = InternalLoggerFactory.getInstance(ProcessMessageChannelInboundHandler.class);
    private ScheduledFuture<?> future = null;
    private Session session = null;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        // 接收到新的 req 关闭上一个 keep alive 定时器
        if (this.future != null)
            this.future.cancel(true);

        switch (msg.getControlType()) {
            case ControlType.CONNECT:
                processConnect(msg, ctx);
                break;
            default:
                throw new UnknownControlTypeException("unknown control type: " + Integer.toHexString(msg.getControlType()));
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // TODO
    }

    /*
     * 处理 CONNECT 报文
     */
    private void processConnect(Message msg, ChannelHandlerContext ctx) {
        ConnectVH connectVH = (ConnectVH) msg.getVariableHeader();
        ConnectPayload connectPayload = (ConnectPayload) msg.getPayload();
        Session session = null;

        int sp = 0;
        if (!connectVH.isCleanSessionFlag()) {
            // 恢复原有会话
            session = MqttBrokerBootstrap.sessions.get(connectPayload.getClientId());
            if (session != null)
                sp = 1;
        } else if (!"".equals(connectPayload.getClientId()))
            // 清理原有会话
            MqttBrokerBootstrap.sessions.remove(connectPayload.getClientId());
        if (session == null) {
            // 创建新会话 --> 清理会话 or 恢复失败
            session = new Session();
            session.setLevel(connectVH.getLevel());
            session.setCleanSessionFlag(connectVH.isCleanSessionFlag());
            int keepAlive = connectVH.getKeepAlive();
            if (keepAlive == 0)
                keepAlive = DEFAULT_KEEP_ALIVE;
            session.setKeepAlive(keepAlive);
            if (connectVH.isUserNameFlag())
                session.setUserName(connectPayload.getUserName());
            if (connectVH.isPassWordFlag())
                session.setPassWord(connectPayload.getPassWord());
            if (connectVH.isWillFlag()) {
                Will will = new Will(connectVH.isWillRetainFlag(), connectPayload.getWillTopic(),
                        connectPayload.getWillContent(), connectVH.getWillQos());
                session.setWill(will);
            }
            session.setChannel(ctx.channel());
            session.setLastReqTime(System.currentTimeMillis());
        }

        // 设置 客户端标识符 并 保存 session
        String clientId;
        if ("".equals(connectPayload.getClientId())) {
            // 服务端生成客户端标识符
            do {
                clientId = OtherUtil.generateClientId();
                session.setClientId(clientId);
            } while (MqttBrokerBootstrap.sessions.putIfAbsent(clientId, session) != null);
        } else {
            /* 进入此的情况：
             * 1）清理会话（无需服务端生成客户端标识符）--> putIfAbsent 成功
             * 2）非清理会话（已恢复会话）--> putIfAbsent 因已经存在故失败
             * 3）非清理会话（恢复会话失败）--> putIfAbsent 成功
             * 故，存在客户端携带的客户端标识符是唯一的假设！
             * 推荐，由服务端分配客户端标识符。(注意此时不能保存会话状态)
             */
            clientId = connectPayload.getClientId();
            session.setClientId(clientId);
            MqttBrokerBootstrap.sessions.putIfAbsent(clientId, session);
        }
        this.session = session;

        // 生成并发送 CONNACK 响应
        Message connackMessage = getConnackMessage(sp, 0x00);
        ctx.writeAndFlush(connackMessage);
        // TODO 成功恢复会话的要发送 “unconfirmedMessages” & “unsentMessages”

        // keep alive
        this.future = createKeepAliveTimer(ctx.channel(), session.getKeepAlive());
    }

    /*
     * 生成 CONNACK 报文
     */
    private Message getConnackMessage(int sp, int stateCode) {
        Message connackMessage = new Message(ControlType.CONNACK, 0x00);
        connackMessage.setRemainLength(2);
        connackMessage.setVariableHeader(new ConnackVH(sp, stateCode));
        return connackMessage;
    }

    /*
     * 创建 KeepAlive 定时器
     */
    private ScheduledFuture createKeepAliveTimer(final Channel channel, int keepAlive) {
        return channel.eventLoop().schedule(() -> {
            ChannelFuture channelFuture = channel.closeFuture();
            channelFuture.addListener((ChannelFutureListener) future ->
                    log.error("Disconnect, username:%s, clientId:%s!", session.getUserName(), session.getClientId()));
        }, keepAlive, TimeUnit.SECONDS);
    }

}
