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
        m.stopMeGently();
        try {
            m.join();
            warnln("... maintenance operations are finished");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    // GETTER
    public MaintenanceThread getThread() {
        return m;
    }
}
