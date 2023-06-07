package robot.pollution;

import common.printer.Type;
import simulator.Measurement;
import simulator.PM10Simulator;

import java.util.List;

import static common.printer.Printer.log;

public class SensorDataProcessingThread extends Thread {

    protected volatile boolean stopCondition = false;

    private final PM10Simulator s;
    private final BufferAverages b;

    public SensorDataProcessingThread(BufferAverages b){
        this.s = new PM10Simulator(new BufferWindow());
        this.b = b;
    }

    @Override
    public void run() {
        s.start();

        while (!stopCondition) {

            List<Measurement> window = s.getBuffer().readAllAndClean();
            log(Type.P_LOW, "... measurements window: " + window.toString());

            double avg = 0;
            for (Measurement m : window)
                avg += m.getValue();
            avg /= 8;
            b.addAverage(avg);
        }
        s.stopMeGently();
    }

    public void stopMeGently() {
        stopCondition = true;
    }
}
