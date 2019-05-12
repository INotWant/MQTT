package protocol;

/**
 * @author iwant
 * @date 19-5-12 11:04
 * @desc CONNECT 报文可变头部
 */
public final class ConnectVH implements VariableHeader {

    private String protocolName;
    // 协议级别
    private int level;
    private boolean userNameFlag;
    private boolean passWordFlag;
    private boolean willRetainFlag;
    private int willQos;
    private boolean willFlag;
    // false 不清理会话， True 时清理
    private boolean cleanSessionFlag;
    private int keepAlive;

    public String getProtocolName() {
        return protocolName;
    }

    public void setProtocolName(String protocolName) {
        this.protocolName = protocolName;
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

    public boolean isWillRetainFlag() {
        return willRetainFlag;
    }

    public void setWillRetainFlag(boolean willRetainFlag) {
        this.willRetainFlag = willRetainFlag;
    }

    public int getWillQos() {
        return willQos;
    }

    public void setWillQos(int willQos) {
        this.willQos = willQos;
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
}
