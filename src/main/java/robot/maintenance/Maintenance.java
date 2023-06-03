package robot.maintenance;

import robot.Robot;

import static utils.Printer.warnln;

public class Maintenance {

    private final Robot r;
    private MaintenanceThread m;

    public Maintenance(Robot r){
        this.r = r;
    }

    public void turnOnMaintenance() {
        m = new MaintenanceThread(r);
        m.start();
    }

    public void turnOffMaintenance() {
        synchronized (m.fixLock){
            m.stopMeGently();
            m.fixLock.notify();
        }
        try {
            m.join();
            warnln("... maintenance operations are finished");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    // getters
    public MaintenanceThread getThread() {
        return m;
    }
}
