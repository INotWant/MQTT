package mqtt.tool;

import java.util.ArrayList;
import java.util.List;
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

    /**
     * 判断 topic filter 是否有效
     *
     * @param topicFilter topic filter
     */
    public static boolean isValidTopicFilter(String topicFilter) {
        if (topicFilter == null || topicFilter.length() == 0)
            return false;
        for (int i = 0; i < topicFilter.length(); i++) {
            char c = topicFilter.charAt(i);
            if (c == '+') {
                if (i != 0 && topicFilter.charAt(i - 1) != '/')
                    return false;
                if (i != topicFilter.length() - 1 && topicFilter.charAt(i + 1) != '/')
                    return false;
            }
            if (c == '#')
                if ((i != 0 && topicFilter.charAt(i - 1) != '/') || i != topicFilter.length() - 1)
                    return false;
        }
        return true;
    }

    /**
     * 划分 topic filter
     *
     * @param topicFilter topic filter
     * @return 如果时合法 topic filter 则返回 String 数组（一个元素表示一层），否则返回 null
     */
    public static String[] splitTopicFilter(String topicFilter) {
        if (!isValidTopicFilter(topicFilter))
            return null;
        List<String> words = new ArrayList<>();
        int i = 0;
        char c = topicFilter.charAt(i);
        if (c == '/') {
            words.add("");
            ++i;
        } else if (c == '#' || c == '+') {
            words.add(String.valueOf(c));
            i += 2;
        }
        int lastI = i;
        for (; i < topicFilter.length(); i++) {
            c = topicFilter.charAt(i);
            if (c == '/') {
                if (lastI == i)
                    words.add("");
                else
                    words.add(topicFilter.substring(lastI, i));
                lastI = i + 1;
            } else if (c == '+' || c == '#') {
                words.add(String.valueOf(c));
                ++i;
                lastI = i + 1;
            }
        }
        // for /finance/ (以/结尾，还需要添加最后一个 “” 空串主题)
        if (topicFilter.length() > 1 && topicFilter.endsWith("/"))
            words.add("");
        // for /finance (不以/结尾)
        if (lastI != i)
            words.add(topicFilter.substring(lastI, i));
        String[] result = new String[words.size()];
        words.toArray(result);
        return result;
    }

    /**
     * 判断 topic name 与 topic filter 是否匹配
     *
     * @param topicName   topic name
     * @param topicFilter topic filter
     * @return true or false
     */
    public static boolean topicMatchTopicFilter(String topicName, String topicFilter) {
        String[] wordsForName = splitTopicFilter(topicName);
        String[] wordsForFilter = splitTopicFilter(topicFilter);
        if (wordsForName == null || wordsForFilter == null)
            return false;
        if (wordsForFilter.length > wordsForName.length) {
            if (wordsForFilter.length - 1 == wordsForName.length && !wordsForFilter[wordsForFilter.length - 1].equals("#"))
                return false;
            else if (wordsForFilter.length - 1 != wordsForName.length)
                return false;
        }
        for (int i = 0; i < wordsForName.length; i++) {
            if (i >= wordsForFilter.length)
                return false;
            if (wordsForFilter[i].equals("#"))
                return true;
            if (wordsForFilter[i].equals("+"))
                continue;
            if (!wordsForFilter[i].equals(wordsForName[i]))
                return false;
        }
        return true;
    }

}
