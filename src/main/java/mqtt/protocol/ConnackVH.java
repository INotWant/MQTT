package mqtt.protocol;

/**
 * @author iwant
 * @date 19-5-13 11:03
 * @desc
 */
public class ConnackVH implements VariableHeader {

    private int sp;
    private int stateCode;

    public ConnackVH(int sp, int stateCode) {
        this.sp = sp;
        this.stateCode = stateCode;
    }

    public int getSp() {
        return sp;
    }

    public void setSp(int sp) {
        this.sp = sp;
    }

    public int getStateCode() {
        return stateCode;
    }

    public void setStateCode(int stateCode) {
        this.stateCode = stateCode;
    }
}
