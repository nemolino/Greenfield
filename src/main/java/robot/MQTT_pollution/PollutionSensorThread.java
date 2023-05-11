package robot.MQTT_pollution;

import simulator.PM10Simulator;

public class PollutionSensorThread extends Thread {

    protected volatile boolean stopCondition = false;
    private final BufferImpl b;
    private final PM10Simulator s;

    public PollutionSensorThread(){
        this.b = new BufferImpl();
        this.s = new PM10Simulator(b);
    }

    @Override
    public void run() {
        s.start();
        while (!stopCondition) {
            System.out.println(b.readAllAndClean().toString());
        }
        s.stopMeGently();
    }

    public void stopMeGently() {
        stopCondition = true;
    }
}
