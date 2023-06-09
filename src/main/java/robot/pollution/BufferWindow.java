package robot.pollution;

import simulator.Measurement;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class BufferWindow implements simulator.Buffer {

    private final static int windowSize = 8;
    private final static int windowOverlap = 4;

    private final List<Measurement> l;

    public BufferWindow() {
        this.l = new LinkedList<>();
    }

    @Override
    synchronized public void addMeasurement(Measurement m) {
        l.add(m);
        if (l.size() >= windowSize)
            notify();
    }

    @Override
    synchronized public List<Measurement> readAllAndClean() {
        while (l.size() < windowSize) {
            try {
                wait();
                System.out.println("notified - " + l);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        List<Measurement> window = new ArrayList<>(l.subList(0, windowSize));
        l.subList(0, windowOverlap).clear();
        return window;
    }
}
