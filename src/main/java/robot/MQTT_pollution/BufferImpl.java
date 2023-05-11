package robot.MQTT_pollution;

import simulator.Measurement;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class BufferImpl implements simulator.Buffer {

    private final static int windowSize = 8;
    private final static int windowOverlap = 4;

    private final List<Measurement> l;

    public BufferImpl() {
        this.l = new LinkedList<>();
    }

    @Override
    synchronized public void addMeasurement(Measurement m) {
        l.add(m);
        notify();
    }

    @Override
    synchronized public List<Measurement> readAllAndClean() {
        while (l.size() < windowSize) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        List<Measurement> window = new ArrayList<>(l.subList(0, windowSize));
        for (int i = 0; i < windowOverlap; i++)
            l.remove(0);
        return window;
    }
}
