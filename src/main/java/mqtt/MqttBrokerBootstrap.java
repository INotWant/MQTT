package mqtt;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Log4JLoggerFactory;
import mqtt.entity.News;
import mqtt.entity.Session;
import mqtt.logic.MqttBytesToMessageDecoder;
import mqtt.logic.MqttMessageToBytesEncoder;
import mqtt.logic.ProcessMessageChannelInboundHandler;
import mqtt.tool.ConfUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author iwant
 * @date 19-5-11 16:34
 * @desc Mqtt 中间件启动类
 */
public class MqttBrokerBootstrap {

    private static final InternalLogger log = InternalLoggerFactory.getInstance(MqttBrokerBootstrap.class);

    // 含有所有的当下存活 or 未清理 的 session
    public static Map<String, Session> sessions = new ConcurrentHashMap<>();
    // 保留消息集：key -> topic name; value -> news
    public static Map<String, News> retainNews = new ConcurrentHashMap<>();
    // 维护服务端的报文标识符
    public static ConcurrentHashMap<Integer, Integer> messageIds = new ConcurrentHashMap<>();

    public static void main(String[] args) throws InterruptedException {
        // 设置日志
        InternalLoggerFactory.setDefaultFactory(Log4JLoggerFactory.INSTANCE);
        // 读取配置
        ConfUtil.open(MqttBrokerBootstrap.class.getResource("/conf.properties").getFile());
        ConfUtil.setting(MqttConfig.class);
        ConfUtil.close();
        // 启动 broker
        log.info("Start to bootstrap broker...");
        NioEventLoopGroup nioEventLoopGroup = new NioEventLoopGroup();
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        try {
            serverBootstrap.group(nioEventLoopGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            // 添加编码（出站）
                            pipeline.addLast(new MqttMessageToBytesEncoder());
                            // 添加解码（入站）
                            pipeline.addLast(new MqttBytesToMessageDecoder());
                            // 添加处理报文逻辑（入站）
                            pipeline.addLast(new ProcessMessageChannelInboundHandler());
                        }
                    });
            // 绑定 IP & port
            ChannelFuture f = serverBootstrap.bind(MqttConfig.IP, MqttConfig.PORT).sync().
                    addListener((ChannelFutureListener) future -> log.info("Bind successfully!"));
            f.channel().closeFuture().sync().
                    addListener((ChannelFutureListener) future -> log.info("Close broker successfully!"));
        } finally {
            nioEventLoopGroup.shutdownGracefully().sync();
        }
    }

}
