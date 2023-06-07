package robot.maintenance;

import admin_server.rest_response_formats.RobotRepresentation;
import com.example.grpc.MaintenanceServiceGrpc;
import com.example.grpc.MaintenanceServiceOuterClass.MaintenanceRequest;
import com.example.grpc.MaintenanceServiceOuterClass.MaintenanceResponse;
import common.printer.Type;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import robot.Robot;

import java.time.LocalTime;
import java.util.*;

import static common.printer.Printer.*;

public class MaintenanceThread extends Thread {

    protected volatile boolean stopCondition = false;
    protected volatile boolean fixCommand = false;

    private final Robot r;
    private List<RobotRepresentation> otherRobots;

    private Long requestTimestamp = null;
    private Set<RobotRepresentation> pendingRequests = null;
    private boolean usingMaintenance;

    private final Object fixLock = new Object();
    private final Object sendResponseLock = new Object();
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
                log(Type.M, "... " + LocalTime.now() + " - ⚠️  NEED maintenance");
                accessMaintenance();
                fixCommand = false;
            }
        }
    }

    public void stopMeGently() {
        stopCondition = true;
        synchronized (fixLock){
            fixLock.notify();
        }
    }

    public void fixCommand() {
        fixCommand = true;
        synchronized (fixLock){
            fixLock.notify();
        }
    }

    private void accessMaintenance() {

        requestTimestamp = System.currentTimeMillis();
        otherRobots = r.getOtherRobotsCopy();
        pendingRequests = new HashSet<>(otherRobots);

        sendMaintenanceRequests();

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

        maintenanceOperation(); // CRITICAL SECTION

        pendingRequests = null;
        requestTimestamp = null;

        synchronized (sendResponseLock) {
            sendResponseLock.notifyAll();
        }
    }

    private void sendMaintenanceRequests() {

        log(Type.M_LOW, "... pending = " + pendingRequests);

        for (RobotRepresentation x : otherRobots) {

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
                    // not an error, but I want it red to make it visible
                    error(Type.N, "... " + x + " is dead [sendMaintenanceRequests]");
                    r.network().removeDeadRobot(x);
                    channel.shutdownNow();
                }

                public void onCompleted() {
                    channel.shutdownNow();
                }
            });
        }
    }

    private void maintenanceOperation() {
        usingMaintenance = true;
        log(Type.M, "... " + LocalTime.now() + " - \uD83D\uDC68\uD83C\uDFFB\u200D\uD83D\uDD27 ENTER maintenance");
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        log(Type.M, "... " + LocalTime.now() + " - \uD83D\uDC68\uD83C\uDFFB\u200D\uD83D\uDD27  EXIT maintenance");
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
                        return;
                    }
        }
    }

    private void removePendingRequest(RobotRepresentation x){
        pendingRequests.remove(x);
        log(Type.M_LOW, "... pending \\ { " + x + " } = " + pendingRequests);
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

    public List<RobotRepresentation> getPendingRequestsCopy(){
        synchronized (structuresLock) {
            return (pendingRequests != null) ? new ArrayList<>(pendingRequests) : null;
        }
    }

    public Object getSendResponseLock() {
        return sendResponseLock;
    }
}
