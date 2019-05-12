package protocol;

import java.util.List;

/**
 * @author iwant
 * @date 19-5-12 17:08
 * @desc UNSUBSCRIBE 报文的有效载荷
 */
public final class UnsubscribePayload implements Payload {

    private List<String> topicFilters;

    public List<String> getTopicFilters() {
        return topicFilters;
    }

    public void setTopicFilters(List<String> topicFilters) {
        this.topicFilters = topicFilters;
    }

}
