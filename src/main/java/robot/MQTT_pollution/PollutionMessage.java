package robot.MQTT_pollution;

import java.util.List;

public class PollutionMessage {

    private String id;
    private long timestamp;
    private List<Double> averages;

    public PollutionMessage(String id, long timestamp, List<Double> averages) {
        this.id = id;
        this.timestamp = timestamp;
        this.averages = averages;
    }
}
