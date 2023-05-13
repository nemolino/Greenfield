package robot.MQTT_pollution;

import java.util.List;

public class PollutionMessageWithID extends admin_server.PollutionMessage {

    private final String id;

    public PollutionMessageWithID(String id, long timestamp, List<Double> averages) {
        super(timestamp, averages);
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
