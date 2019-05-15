package mqtt.logic;

import io.netty.channel.*;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import mqtt.MqttBrokerBootstrap;
import mqtt.entity.News;
import mqtt.entity.Session;
import mqtt.entity.SubscribeTree;
import mqtt.entity.Will;
import mqtt.exception.IllegalStateException;
import mqtt.exception.MessageFormatException;
import mqtt.exception.UnknownControlTypeException;
import mqtt.protocol.*;
import mqtt.tool.OtherUtil;
import mqtt.tool.Pair;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static mqtt.MqttConfig.DEFAULT_KEEP_ALIVE;

/**
 * @author iwant
 * @date 19-5-13 08:30
 * @desc 处理 Message 逻辑
 */
// TODO 日志输出
public class ProcessMessageChannelInboundHandler extends SimpleChannelInboundHandler<Message> {

    private static final InternalLogger log = InternalLoggerFactory.getInstance(ProcessMessageChannelInboundHandler.class);
    private ScheduledFuture<?> future = null;
    private Session session = null;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) {
        // 接收到新的 req 关闭上一个 keep alive 定时器
        if (this.future != null)
            this.future.cancel(true);

        switch (msg.getControlType()) {
            case ControlType.CONNECT:
                processConnect(msg, ctx);
                break;
            case ControlType.PUBLISH:
                processPublish(msg, ctx);
                break;
            case ControlType.PUBACK:
                processPuback(msg, ctx);
                break;
            case ControlType.PUBREC:
                processPubrec(msg, ctx);
                break;
            case ControlType.PUBREL:
                processPubrel(msg, ctx);
                break;
            case ControlType.PUBCOMP:
                processPubcomp(msg, ctx);
                break;
            case ControlType.SUBSCRIBE:
                processSubscribe(msg, ctx);
                break;
            case ControlType.UNSUBSCRIBE:
                processUnsubscribe(msg, ctx);
                break;
            case ControlType.PINGREQ:
                processPingreq(msg, ctx);
                break;
            case ControlType.DISCONNECT:
                processDisconnect(msg, ctx);
                break;
            default:
                break;
        }

        if (this.session != null)
            this.session.setLastReqTime(System.currentTimeMillis());
        if (msg.getControlType() != ControlType.DISCONNECT)
            // keep alive
            this.future = createKeepAliveTimer(ctx.channel(), session.getKeepAlive());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // 打印捕获的异常
        log.error("Get a exception: " + cause.toString());

        if (cause instanceof UnknownControlTypeException) {
            ChannelFuture channelFuture = ctx.close();
            channelFuture.addListener((ChannelFutureListener) future ->
                    log.error("Close, because of unknown control type!")
            );
        } else if (cause instanceof IllegalStateException) {
            IllegalStateException exp = (IllegalStateException) cause;
            ctx.writeAndFlush(getConnackMessage(0, exp.getErrorno())).
                    addListener((ChannelFutureListener) future -> ctx.close().addListener((ChannelFutureListener) f ->
                            log.error("Close, because of illegal state: %x!", exp.getErrorno())));
        } else if (cause instanceof MessageFormatException) {
            ChannelFuture channelFuture = ctx.close();
            channelFuture.addListener((ChannelFutureListener) future ->
                    log.error("Close, because of message format exception!")
            );
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        /*
         * 在此方法中统一做如下事：
         * 1）清理非保留 session;
         * 2）发布遗嘱
         */

        if (this.session != null) {
            Will will = this.session.getWill();
            if (will != null)
                // 客户端未发送 DISCONNECT 直接关闭了网络连接，发布遗嘱
                // 获取其他异常关闭连接且 will 仍存在的情况
                sendPublish(generateMessageId(), will.getWillNews());
            // 清理非保留会话
            if (this.session.isCleanSessionFlag())
                MqttBrokerBootstrap.sessions.remove(this.session.getClientId());
            // 清理遗嘱
            this.session.setWill(null);
        }
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
                // 保留遗嘱
                if (will.isRetainFlag())
                    MqttBrokerBootstrap.retainNews.put(will.getWillNews().getTopic(), will.getWillNews());
            }
            session.setChannel(ctx.channel());
            session.setLastReqTime(System.currentTimeMillis());
        } else {
            // 恢复会话后要更新 channel
            session.setChannel(ctx.channel());
            // 恢复会话时重置 遗嘱
            if (connectVH.isWillFlag()) {
                Will will = new Will(connectVH.isWillRetainFlag(), connectPayload.getWillTopic(),
                        connectPayload.getWillContent(), connectVH.getWillQos());
                session.setWill(will);
            } else
                session.setWill(null);
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

        // 成功恢复会话的发送 unconfirmedMessages （此时服务端作为接收者，针对 qos2 消息）
        for (Map.Entry<Integer, News> entry : this.session.getUnconfirmedMessages().entrySet()) {
            int messageId = entry.getKey();
            ctx.writeAndFlush(generatePublishVertify(ControlType.PUBREC, 0x00, messageId));
        }
        // 成功恢复会话的发送 unconfirmedMessagesForSender （此时服务端作为发送者，针对 qos1、2 消息）
        for (Map.Entry<Integer, Pair<News, Integer>> entry : this.session.getUnconfirmedMessagesForSender().entrySet()) {
            Integer messageId = entry.getKey();
            News news = entry.getValue().getF();
            int qos = entry.getValue().getS();
            ctx.writeAndFlush(generatePublish(true, qos, false, messageId, news));
        }
        // 成功恢复会话的发送
        for (Map.Entry<News, Integer> entry : this.session.getUnsentMessages().entrySet()) {
            News news = entry.getKey();
            int qos = entry.getValue();
            ctx.writeAndFlush(generatePublish(false, qos, false, generateMessageId(), news));
        }
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
                    log.error("Close, because of timeout(keep alive), username:%s, clientId:%s!", session.getUserName(), session.getClientId())
            );
        }, keepAlive, TimeUnit.SECONDS);
    }

    /*
     * 处理 SUBSCRIBE
     */
    private void processSubscribe(Message msg, ChannelHandlerContext ctx) {
        SubscribeVH subscribeVH = (SubscribeVH) msg.getVariableHeader();
        int messageId = subscribeVH.getMessageId();
        SubscribePayload subscribePayload = (SubscribePayload) msg.getPayload();
        List<String> topicFilters = subscribePayload.getTopicFilters();
        List<Integer> qoss = subscribePayload.getQoss();

        // 生成 SUBACK
        Message subackMessage = new Message(ControlType.SUBACK, 0x00);
        subackMessage.setRemainLength(2 + topicFilters.size());
        SubackVH subackVH = new SubackVH();
        subackVH.setMessageId(messageId);
        subackMessage.setVariableHeader(subackVH);
        SubackPayload subackPayload = new SubackPayload();
        subackMessage.setPayload(subackPayload);

        // 根据报文内容添加新的订阅
        SubscribeTree subscribeTree = SubscribeTree.getInstance();
        for (int i = 0; i < topicFilters.size(); i++) {
            String topicFilter = topicFilters.get(i);
            int qos = qoss.get(i);
            assert this.session != null;
            boolean result = subscribeTree.addTopicFilter(topicFilter, this.session, qos);
            if (!result)
                subackPayload.addReturnCode((byte) 0x80);
            else {
                this.session.addSubscribe(topicFilter, qos);
                subackPayload.addReturnCode((byte) qos);
            }
        }

        // 响应，写 SUBACK message
        ctx.writeAndFlush(subackMessage);

        // 发布保留消息
        for (Map.Entry<String, News> entry : MqttBrokerBootstrap.retainNews.entrySet()) {
            String topicName = entry.getKey();
            int qos = -1;
            for (int i = 0; i < topicFilters.size(); i++) {
                String topicFilter = topicFilters.get(i);
                if (OtherUtil.topicMatchTopicFilter(topicName, topicFilter))
                    qos = qoss.get(i) > qos ? qoss.get(i) : qos;
            }
            if (qos >= 0)
                ctx.writeAndFlush(generatePublish(false, qos, true, generateMessageId(), entry.getValue()));
        }
    }

    /*
     * 处理 UNSUBSCRIBE
     */
    private void processUnsubscribe(Message msg, ChannelHandlerContext ctx) {
        UnsubscribeVH unsubscribeVH = (UnsubscribeVH) msg.getVariableHeader();
        UnsubscribePayload unsubscribePayload = (UnsubscribePayload) msg.getPayload();

        // 根据报文内容删除部分订阅
        SubscribeTree subscribeTree = SubscribeTree.getInstance();
        for (String topicFilter : unsubscribePayload.getTopicFilters()) {
            assert this.session != null;
            subscribeTree.removeTopicFilter(topicFilter, this.session);
            this.session.removeSubscribe(topicFilter);
        }

        // 生成 UNSUBACK
        Message unsuback = new Message(ControlType.UNSUBACK, 0x00);
        msg.setRemainLength(2);
        PublishVerifyVH publishVerifyVH = new PublishVerifyVH();
        publishVerifyVH.setMessageId(unsubscribeVH.getMessageId());
        msg.setVariableHeader(publishVerifyVH);

        // 响应
        ctx.writeAndFlush(unsuback);
    }

    /*
     * 处理 PINGREQ
     */
    private void processPingreq(Message msg, ChannelHandlerContext ctx) {
        Message pingresqMessage = new Message(ControlType.PINGRESP, 0x00);
        pingresqMessage.setRemainLength(0);

        // 响应
        ctx.writeAndFlush(pingresqMessage);
    }

    /*
     * 处理 DISCONNECT
     */
    private void processDisconnect(Message msg, ChannelHandlerContext ctx) {
        assert this.session != null;
        if (msg.getFlag() != 0x00) {
            log.error("Get a error in the flag of disconnect, username:%s, clientId:%s!", session.getUserName(), session.getClientId());
            return;
        }
        if (this.session.isCleanSessionFlag())
            MqttBrokerBootstrap.sessions.remove(session.getClientId());
        this.session.setWill(null);
        ctx.close().addListener((ChannelFutureListener) future -> log.info("Normal Disconnect, username:%s, clientId:%s!", session.getUserName(), session.getClientId()));
    }

    /*
     * 处理 PUBLISH
     */
    private void processPublish(Message msg, ChannelHandlerContext ctx) {
        PublishVH publishVH = (PublishVH) msg.getVariableHeader();
        PublishPayload publishPayload = (PublishPayload) msg.getPayload();
        int messageId = publishVH.getMessageId();
        String topicName = publishVH.getTopicName();
        int qos = msg.getQos();
        boolean isRetain = msg.isRetain();
        List<Byte> content = publishPayload.getContent();

        // 根据报文构建 news
        News news = new News(topicName, content, qos);

        // 保留消息处理
        if (isRetain) {
            if (content.size() == 0)
                // 清除已存保留消息
                MqttBrokerBootstrap.retainNews.remove(topicName);
            else
                MqttBrokerBootstrap.retainNews.put(topicName, news);
        }

        assert this.session != null;

        // 响应
        if (qos == 0x01) {
            // 响应服务质量为 1 的
            Message pubackMessage = generatePublishVertify(ControlType.PUBACK, 0x00, messageId);
            ctx.writeAndFlush(pubackMessage);
        } else if (qos == 0x02) {
            // 响应服务质量为 2 的
            if (!this.session.getUnconfirmedMessages().containsKey(messageId)) {
                // 如果不是之前未确定的消息，则作为新的消息处理
                // 1）存储消息
                this.session.addUnconfirmedMessage(messageId, news);
                // 2）发送 PUBREC
                Message pubrecMessage = generatePublishVertify(ControlType.PUBREC, 0x00, messageId);
                ctx.writeAndFlush(pubrecMessage);
            }
        }

        // 发布（当 qos 为 0、1 时）
        if (qos != 2)
            sendPublish(generateMessageId(), news);
    }

    /*
     * 生成 PUBACK or PUBREC or PUBREL or PUBCOMP 报文
     */
    private Message generatePublishVertify(int controlType, int flag, int messageId) {
        Message publishVertifyMessage = new Message(controlType, flag);
        PublishVerifyVH publishVerifyVH = new PublishVerifyVH();
        publishVerifyVH.setMessageId(messageId);
        return publishVertifyMessage;
    }

    /*
     * 生成 PUBLISH
     */
    private Message generatePublish(boolean isDup, int qos, boolean isRetain, int messageId, News news) {
        Message publishMessage = new Message(ControlType.PUBLISH, 0x00);
        publishMessage.setDup(isDup);
        publishMessage.setQos(Math.min(news.getQos(), qos));
        publishMessage.setRetain(isRetain);
        PublishVH publishVH = new PublishVH();
        publishVH.setTopicName(news.getTopic());
        publishVH.setMessageId(messageId);
        PublishPayload publishPayload = new PublishPayload();
        publishPayload.setContent(news.getContent());
        return publishMessage;
    }

    /*
     * 发布
     */
    private void sendPublish(int messageId, News news) {
        SubscribeTree subscribeTree = SubscribeTree.getInstance();
        Map<Session, Integer> match = subscribeTree.match(news.getTopic());
        for (Map.Entry<Session, Integer> entry : match.entrySet()) {
            Session session = entry.getKey();
            Integer qosInt = entry.getValue();
            if (session != null) {
                int qos = Math.min(qosInt, news.getQos());
                if (session.getChannel().isActive()) {
                    // for 存活中的会话
                    Message publishMessage = generatePublish(false, qosInt, false, messageId, news);
                    if (qos > 0)
                        // 存储相应的消息
                        session.addUnconfirmedMessageForSender(messageId, news, qos);
                    session.getChannel().writeAndFlush(publishMessage);
                } else {
                    // for 非存活的会话，即保留状态的会话
                    session.addUnsentMessage(news, qos);
                }
            }
        }
    }

    /*
     * 处理 PUBACK
     */
    private void processPuback(Message msg, ChannelHandlerContext ctx) {
        PublishVerifyVH publishVerifyVH = (PublishVerifyVH) msg.getVariableHeader();
        int messageId = publishVerifyVH.getMessageId();

        assert this.session != null;
        // 丢弃相应的消息
        this.session.removeUnconfirmedMessageForSender(messageId);
        // 释放相应的报文标识符
        MqttBrokerBootstrap.messageIds.remove(messageId);
    }

    /*
     * 处理 PUBREC
     */
    private void processPubrec(Message msg, ChannelHandlerContext ctx) {
        PublishVerifyVH publishVerifyVH = (PublishVerifyVH) msg.getVariableHeader();
        int messageId = publishVerifyVH.getMessageId();

        assert this.session != null;
        // 丢弃相应的消息
        this.session.removeUnconfirmedMessageForSender(messageId);

        // 发送 PUBREL
        Message pubrelMessage = generatePublishVertify(ControlType.PUBREL, 0x02, messageId);
        ctx.writeAndFlush(pubrelMessage);
    }

    /*
     * 处理 PUBREL
     */
    private void processPubrel(Message msg, ChannelHandlerContext ctx) {
        assert this.session != null;

        // 发布对应的 qos2 消息
        PublishVerifyVH publishVerifyVH = (PublishVerifyVH) msg.getVariableHeader();
        int messageId = publishVerifyVH.getMessageId();
        News news = this.session.getUnconfirmedMessage(messageId);
        if (news != null)
            sendPublish(generateMessageId(), news);

        // 丢弃对应的 qos2 消息
        this.session.removeUnconfirmedMessage(messageId);

        // 发送响应 PUBCOMP
        Message pubcompMessage = generatePublishVertify(ControlType.PUBCOMP, 0x00, messageId);
        ctx.writeAndFlush(pubcompMessage);
    }

    /*
     * 生成一个未被占用的报文标识符
     */
    private int generateMessageId() {
        ConcurrentHashMap<Integer, Integer> messageIds = MqttBrokerBootstrap.messageIds;
        Random random = new Random();
        int messageId;
        do {
            messageId = random.nextInt(0xffff);
        } while (messageIds.putIfAbsent(messageId, 0) != null);
        return messageId;
    }

    /*
     * 处理 PUBCOMP
     */
    private void processPubcomp(Message msg, ChannelHandlerContext ctx) {
        assert this.session != null;

        PublishVerifyVH publishVerifyVH = (PublishVerifyVH) msg.getVariableHeader();
        int messageId = publishVerifyVH.getMessageId();

        // 释放相应的报文标识符
        this.session.removeUnconfirmedMessageForSender(messageId);
    }

}
