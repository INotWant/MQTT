package protocol;

import java.util.List;

/**
 * @author iwant
 * @date 19-5-12 16:19
 * @desc PUBLISH 有效载荷
 */
public class PublishPayload implements Payload {

    private List<Byte> content;

    public List<Byte> getContent() {
        return content;
    }

    public void setContent(List<Byte> content) {
        this.content = content;
    }
}
