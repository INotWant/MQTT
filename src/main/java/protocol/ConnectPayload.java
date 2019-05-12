package protocol;

import java.util.List;

/**
 * @author iwant
 * @date 19-5-12 11:08
 * @desc CONNECT 报文有效载荷
 */
public final class ConnectPayload implements Payload {

    // 客户端标识符
    private String clientId;
    private String willTopic;
    private List<Byte> willContent;
    private String userName;
    private String passWord;

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getWillTopic() {
        return willTopic;
    }

    public void setWillTopic(String willTopic) {
        this.willTopic = willTopic;
    }

    public List<Byte> getWillContent() {
        return willContent;
    }

    public void setWillContent(List<Byte> willContent) {
        this.willContent = willContent;
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
}
