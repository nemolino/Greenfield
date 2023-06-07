package robot.maintenance;

import admin_server.rest_response_formats.RobotRepresentation;
import common.printer.Type;
import robot.Robot;

import java.util.List;

import static common.printer.Printer.log;

public class Maintenance {

    private final Robot r;
    private MaintenanceThread t;

    public Maintenance(Robot r){
        this.r = r;
    }

    public void turnOnMaintenance() {
        t = new MaintenanceThread(r);
        t.start();
    }

    public void turnOffMaintenance() {
        t.stopMeGently();
        try {
            t.join();
            log(Type.M, "... maintenance operations are finished");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    // wrapping some MaintenanceThread methods

    public void fixCommand() {
        t.fixCommand();
    }

    public void updatePendingMaintenanceRequests(RobotRepresentation x) {
        t.updatePendingMaintenanceRequests(x);
    }

    public void updatePendingMaintenanceRequestsById(String id) {
        t.updatePendingMaintenanceRequestsById(id);
    }

    public boolean hasToWait(String otherRequestTimestamp){
        return t.hasToWait(otherRequestTimestamp);
    }

    public List<RobotRepresentation> getPendingRequestsCopy(){
        return t.getPendingRequestsCopy();
    }

    public Object getSendResponseLock() {
        return t.getSendResponseLock();
    }
}
