package robot.maintenance;

import admin_server.REST_response_formats.RobotRepresentation;
import com.example.grpc.MaintenanceServiceGrpc;
import com.example.grpc.MaintenanceServiceOuterClass.MaintenanceRequest;
import com.example.grpc.MaintenanceServiceOuterClass.MaintenanceResponse;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import robot.Robot;

import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static common.Printer.*;

/*
 * Se si aggiunge un nuovo robot e io ho già mandato le richieste senza considerare lui
 * perchè non lo avevo in otherRobots non è un problema :
 * se non lo avevo in otherRobots non può aver ancora mandato richieste con un timestamp < del mio.
 *
 * Se un robot termina in modo controllato (con "quit") non succede nulla
 * perchè prima di terminare manda tutte le risposte che ha in sospeso.
 *
 */

public class MaintenanceThread extends Thread {

    protected volatile boolean stopCondition = false;
    protected volatile boolean fixCommand = false;

    private final Robot r;
    private List<RobotRepresentation> otherRobotsCopy;

    private Long requestTimestamp = null;
    private Set<RobotRepresentation> pendingRequests = null;
    private boolean usingMaintenance;

    public final Object fixLock = new Object();
    public final Object sendResponseLock = new Object();
    private final Object accessMaintenanceLock = new Object();
    private final Object structuresLock = new Object();

    public MaintenanceThread(Robot r) {
        this.r = r;
    }

    @Override
    public void run() {
        while (!stopCondition) {

            try {
                synchronized (fixLock){
                    fixLock.wait(10000);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            if (stopCondition){
                synchronized (sendResponseLock) {
                    sendResponseLock.notifyAll();
                }
            }
            else if (Math.random() < 0.1 || fixCommand) {
                System.out.println(LocalTime.now() + " - I need maintenance!");
                accessMaintenance();
                fixCommand = false;
            }
        }
    }

    public void stopMeGently() {
        stopCondition = true;
    }

    public void fixCommand() {
        fixCommand = true;
    }

    public void accessMaintenance() {

        requestTimestamp = System.currentTimeMillis();
        synchronized (r.getOtherRobotsLock()) {
            otherRobotsCopy = new ArrayList<>(r.getOtherRobots());
        }
        pendingRequests = new HashSet<>(otherRobotsCopy);

        sendMaintenanceRequests();


        HeartbeatThread h = new HeartbeatThread(r, structuresLock, pendingRequests);
        h.start();

        // waiting for all the responses
        while (pendingRequests.size() > 0) {
            try {
                synchronized (accessMaintenanceLock){
                    accessMaintenanceLock.wait();
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        h.stopMeGently();

        maintenanceOperation(); // CRITICAL SECTION

        pendingRequests = null;
        requestTimestamp = null;

        synchronized (sendResponseLock) {
            sendResponseLock.notifyAll();
        }
    }

    private void sendMaintenanceRequests() {

        System.out.println(LocalTime.now() + " - pending = " + pendingRequests);

        for (RobotRepresentation x : otherRobotsCopy) {

            final ManagedChannel channel = ManagedChannelBuilder
                    .forTarget("localhost:" + x.getPort()).usePlaintext().build();

            MaintenanceServiceGrpc.MaintenanceServiceStub stub = MaintenanceServiceGrpc.newStub(channel);
            MaintenanceRequest request = MaintenanceRequest.newBuilder()
                    .setId(r.getId())
                    .setTimestamp(requestTimestamp.toString())
                    .build();

            stub.maintenance(request, new StreamObserver<MaintenanceResponse>() {

                public void onNext(MaintenanceResponse response) {
                    updatePendingMaintenanceRequests(x);
                }

                public void onError(Throwable throwable) {
                    errorln(x + " is dead [sendMaintenanceRequests]");
                    r.removeDeadRobot(x);
                }

                public void onCompleted() {
                    channel.shutdownNow();
                }
            });
        }
    }

    private void maintenanceOperation() {
        usingMaintenance = true;
        successln(LocalTime.now() + " ... ENTER maintenance");
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        successln(LocalTime.now() + " ...  EXIT maintenance");
        usingMaintenance = false;
    }

    public void updatePendingMaintenanceRequests(RobotRepresentation x) {
        synchronized (structuresLock) {
            if (pendingRequests != null && pendingRequests.contains(x))
                removePendingRequest(x);
        }
    }

    public void updatePendingMaintenanceRequestsById(String id) {
        synchronized (structuresLock) {
            if (pendingRequests != null)
                for (RobotRepresentation x : pendingRequests)
                    if (Objects.equals(x.getId(), id)) {
                        removePendingRequest(x);
                        break;
                    }
        }
    }

    private void removePendingRequest(RobotRepresentation x){
        pendingRequests.remove(x);
        System.out.println(LocalTime.now() + " - pending - { " + x + " } = " + pendingRequests);
        if (pendingRequests.size() == 0){
            synchronized (accessMaintenanceLock){
                accessMaintenanceLock.notify();
            }
        }
    }

    public boolean hasToWait(String otherRequestTimestamp){
        synchronized (structuresLock) {
            return usingMaintenance || (requestTimestamp != null &&
                                        requestTimestamp < Long.parseLong(otherRequestTimestamp));
        }
    }

}
