package mqtt.exception;

/**
 * @author iwant
 * @date 19-5-12 10:43
 * @desc 未知报文控制类型异常
 */
public class UnknownControlTypeException extends Exception {

    public UnknownControlTypeException() {
    }

    public UnknownControlTypeException(String s) {
        super(s);
    }

    public UnknownControlTypeException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public UnknownControlTypeException(Throwable throwable) {
        super(throwable);
    }
}
