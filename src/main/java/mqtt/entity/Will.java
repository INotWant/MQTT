package mqtt.entity;

import java.util.List;

/**
 * @author iwant
 * @date 19-5-11 16:38
 * @desc 遗嘱
 */
public final class Will {

    private News willNews;
    // 保留消息标志
    private boolean retainFlag;

    public Will(boolean retainFlag, String topic, List<Byte> content, int qos) {
        this.retainFlag = retainFlag;
        this.willNews = new News(topic, content, qos);
    }

    public News getWillNews() {
        return willNews;
    }

    public void setWillNews(News willNews) {
        this.willNews = willNews;
    }

    public boolean isRetainFlag() {
        return retainFlag;
    }

    public void setRetainFlag(boolean retainFlag) {
        this.retainFlag = retainFlag;
    }
}
