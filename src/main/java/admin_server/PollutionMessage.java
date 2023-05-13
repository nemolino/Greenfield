package admin_server;

import java.util.ArrayList;
import java.util.List;

public class PollutionMessage {

    private final long timestamp;
    private final List<Double> averages;

    public PollutionMessage(long timestamp, List<Double> averages) {
        this.timestamp = timestamp;
        this.averages = averages;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public List<Double> getAverages() {
        return new ArrayList<>(averages);
    }

    @Override
    public String toString() {
        return "PollutionMessage : time " + timestamp + " , averages " + averages;
    }
}
