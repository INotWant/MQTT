package mqtt.entity;

import java.util.List;

/**
 * @author iwant
 * @date 19-5-11 16:38
 * @desc 遗嘱
 */
public final class Will {

    /*
     * 设计为线程安全类：
     * 在初始化完成后不发生改变！
     */

    private final News willNews;
    // 保留消息标志
    private final boolean retainFlag;

    public Will(boolean retainFlag, String topic, List<Byte> content, int qos) {
        this.retainFlag = retainFlag;
        this.willNews = new News(topic, content, qos);
    }

    public News getWillNews() {
        return willNews;
    }

    public boolean isRetainFlag() {
        return retainFlag;
    }

}
