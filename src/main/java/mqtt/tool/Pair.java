package mqtt.tool;

/**
 * @author iwant
 * @date 19-5-15 13:44
 * @desc 带两个参数类型
 */
public class Pair<F, S> {

    private F f;
    private S s;

    public Pair(F f, S s) {
        this.f = f;
        this.s = s;
    }

    public F getF() {
        return f;
    }

    public void setF(F f) {
        this.f = f;
    }

    public S getS() {
        return s;
    }

    public void setS(S s) {
        this.s = s;
    }
}
