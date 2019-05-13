package mqtt.entity;

import io.netty.channel.Channel;

import java.util.HashMap;
import java.util.Map;

/**
 * @author iwant
 * @date 19-5-11 16:36
 * @desc 会话
 */
public final class Session {

    private String clientId;
    // 协议级别
    private int level;
    private boolean userNameFlag;
    private boolean passWordFlag;
    private boolean willFlag;
    // false 不清理会话， True 时清理
    private boolean cleanSessionFlag;
    private int keepAlive;

    private String userName;
    private String passWord;

    // 遗嘱
    private Will will;
    // 用于与客户端通信
    private Channel channel;
    // 上一次请求时间
    private long lastReqTime;
    // 未确认的消息（在重新连接后可能重传的消息） --> key: 报文标识符; value: 对应的消息
    private Map<Integer, News> unconfirmedMessages = new HashMap<>();

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public boolean isUserNameFlag() {
        return userNameFlag;
    }

    public void setUserNameFlag(boolean userNameFlag) {
        this.userNameFlag = userNameFlag;
    }

    public boolean isPassWordFlag() {
        return passWordFlag;
    }

    public void setPassWordFlag(boolean passWordFlag) {
        this.passWordFlag = passWordFlag;
    }

    public boolean isWillFlag() {
        return willFlag;
    }

    public void setWillFlag(boolean willFlag) {
        this.willFlag = willFlag;
    }

    public boolean isCleanSessionFlag() {
        return cleanSessionFlag;
    }

    public void setCleanSessionFlag(boolean cleanSessionFlag) {
        this.cleanSessionFlag = cleanSessionFlag;
    }

    public int getKeepAlive() {
        return keepAlive;
    }

    public void setKeepAlive(int keepAlive) {
        this.keepAlive = keepAlive;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassWord() {
        return passWord;
    }

    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }

    public Will getWill() {
        return will;
    }

    public void setWill(Will will) {
        this.will = will;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public long getLastReqTime() {
        return lastReqTime;
    }

    public void setLastReqTime(long lastReqTime) {
        this.lastReqTime = lastReqTime;
    }

    public Map<Integer, News> getUnconfirmedMessages() {
        return unconfirmedMessages;
    }
}
