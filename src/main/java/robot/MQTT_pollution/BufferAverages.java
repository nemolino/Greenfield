package robot.MQTT_pollution;

import java.util.ArrayList;
import java.util.List;

public class BufferAverages {

    private final List<Double> l;

    public BufferAverages() {
        this.l = new ArrayList<>();
    }

    synchronized public void addAverage(double x) {
        l.add(x);
    }

    synchronized public List<Double> readAveragesAndClean() {
        List<Double> averages = new ArrayList<>(l);
        l.clear();
        return averages;
    }
}
