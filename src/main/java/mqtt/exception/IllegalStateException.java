package mqtt.exception;

/**
 * @author iwant
 * @date 19-5-12 12:24
 * @desc 报文错误状态（逻辑矛盾）异常
 */
public class IllegalStateException extends Exception {

    public final static int ILLEGAL_PROTOCOL_LEVEL = 0x01;
    public final static int ILLEGAL_CLIENT_ID = 0x02;
    public final static int ILLEGAL_BROKER_UNAVAILABLE = 0x03;
    public final static int ILLEGAL_USERNAME_PASSWORD_FLAG = 0x04;
    public final static int ILLEGAL_UNAUTHORIZED = 0x05;

    private int errorno;

    public IllegalStateException(int errorno) {
        this.errorno = errorno;
    }

    public IllegalStateException(String s, int errorno) {
        super(s);
        this.errorno = errorno;
    }

    public IllegalStateException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public IllegalStateException(Throwable throwable) {
        super(throwable);
    }

    public int getErrorno() {
        return errorno;
    }
}
