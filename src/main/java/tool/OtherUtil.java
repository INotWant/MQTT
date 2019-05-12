package tool;

import java.util.Random;

/**
 * @author iwant
 * @date 19-5-12 12:11
 * @desc 其他工具类
 */
public final class OtherUtil {

    private static final String CHARS_STR = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    /**
     * 随机生成 clientId
     */
    public static String generateClientId() {
        Random random = new Random();

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 23; i++)
            sb.append(CHARS_STR.charAt(random.nextInt(CHARS_STR.length())));
        return sb.toString();
    }

}
