package robot.maintenance;

import common.printer.Type;
import robot.Robot;

import static common.printer.Printer.log;
import static common.printer.Printer.warn;

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
            log(Type.M, "... maintenance operations are finished");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    // getter
    public MaintenanceThread getThread() {
        return m;
    }
}
