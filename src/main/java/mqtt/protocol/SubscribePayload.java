package mqtt.protocol;

import java.util.List;

/**
 * @author iwant
 * @date 19-5-12 17:08
 * @desc SUBSCRIBE 报文的有效载荷
 */
public final class SubscribePayload implements Payload {

    private List<String> topicFilters;
    private List<Integer> qoss;

    public List<String> getTopicFilters() {
        return topicFilters;
    }

    public void setTopicFilters(List<String> topicFilters) {
        this.topicFilters = topicFilters;
    }

    public List<Integer> getQoss() {
        return qoss;
    }

    public void setQoss(List<Integer> qoss) {
        this.qoss = qoss;
    }
}
