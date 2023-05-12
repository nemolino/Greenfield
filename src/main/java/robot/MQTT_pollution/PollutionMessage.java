package robot.MQTT_pollution;

import java.util.ArrayList;
import java.util.List;

public class PollutionMessage {

    private final String id;
    private final long timestamp;
    private final List<Double> averages;

    public PollutionMessage(String id, long timestamp, List<Double> averages) {
        this.id = id;
        this.timestamp = timestamp;
        this.averages = averages;
    }

    public String getId() {
        return id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public List<Double> getAverages() {
        return new ArrayList<>(averages);
    }
}
