package mqtt.protocol;

import java.util.ArrayList;
import java.util.List;

/**
 * @author iwant
 * @date 19-5-13 17:45
 * @desc SUBACK 相关的有效载荷
 */
public final class SubackPayload implements Payload {

    private List<Byte> returnCodes = new ArrayList<>();

    public List<Byte> getReturnCodes() {
        return returnCodes;
    }

    public void addReturnCode(Byte b) {
        this.returnCodes.add(b);
    }

    public void setReturnCodes(List<Byte> returnCodes) {
        this.returnCodes = returnCodes;
    }
}
