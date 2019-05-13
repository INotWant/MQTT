package mqtt.exception;

/**
 * @author iwant
 * @date 19-5-12 10:06
 * @desc 报文格式错误异常
 */
public class MessageFormatException extends Exception {

    public MessageFormatException() {
    }

    public MessageFormatException(String s) {
        super(s);
    }

    public MessageFormatException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public MessageFormatException(Throwable throwable) {
        super(throwable);
    }
}
