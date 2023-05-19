package robot.maintenance;

import admin_server.REST_response_formats.RobotRepresentation;
import com.example.grpc.MaintenanceServiceGrpc;
import com.example.grpc.MaintenanceServiceOuterClass.MaintenanceRequest;
import com.example.grpc.MaintenanceServiceOuterClass.MaintenanceResponse;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import robot.Robot;
import utils.exceptions.RemovalFailureException;

import java.time.LocalTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static utils.Printer.*;

public class MaintenanceThread extends Thread {

    protected volatile boolean stopCondition = false;

    public static Long maintenanceRequestTimestamp = null;

    private static Set<RobotRepresentation> pendingMaintenanceRequests = null;

    private static final Object pendingMaintenanceRequestsLock = new Object();

    public static final Object maintenanceResponseLock = new Object();
    public static final Object lock = new Object();

    private final Robot r;

    public MaintenanceThread(Robot r){
        this.r = r;
    }

    @Override
    public void run() {
        while (!stopCondition) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if (Math.random() < 0.3){
                System.out.println(LocalTime.now() + " - I need maintenance!");
                accessMaintenance();
            }
        }
    }

    public void stopMeGently() {
        stopCondition = true;
    }

    public void accessMaintenance() {

        synchronized (r.getOtherRobotsLock()) {

            synchronized (lock){
                maintenanceRequestTimestamp = System.currentTimeMillis();
            }

            pendingMaintenanceRequests = new HashSet<>(r.getOtherRobots());

            System.out.println(LocalTime.now() + " - pending: " + pendingMaintenanceRequests);

            for (RobotRepresentation x : r.getOtherRobots()) {

                final ManagedChannel channel = ManagedChannelBuilder
                        .forTarget("localhost:" + x.getPort()).usePlaintext().build();

                MaintenanceServiceGrpc.MaintenanceServiceStub stub = MaintenanceServiceGrpc.newStub(channel);
                MaintenanceRequest request = MaintenanceRequest.newBuilder()
                        .setId(r.getId())
                        .setTimestamp(maintenanceRequestTimestamp.toString())
                        .build();

                stub.maintenance(request, new StreamObserver<MaintenanceResponse>() {

                    public void onNext(MaintenanceResponse response) {
                        updatePendingMaintenanceRequests(x);
                    }

                    public void onError(Throwable throwable) {
                        errorln(throwable.getMessage() + " | Notifying otherRobots that " + x + " left the city!");
                        r.removeDeadRobot(x);
                    }

                    public void onCompleted() {
                        channel.shutdownNow();
                    }
                });
            }
        }

        // waiting for all the responses
        synchronized (pendingMaintenanceRequestsLock){
            while (pendingMaintenanceRequests.size() > 0){
                try {
                    pendingMaintenanceRequestsLock.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        successln(LocalTime.now() + " ... ENTER maintenance");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        successln(LocalTime.now() + " ...  EXIT maintenance");

        synchronized (lock) {
            maintenanceRequestTimestamp = null;
        }
        pendingMaintenanceRequests = null;
        synchronized (maintenanceResponseLock) {
            maintenanceResponseLock.notifyAll();
        }
    }

    synchronized public void updatePendingMaintenanceRequests(RobotRepresentation x){
        if (pendingMaintenanceRequests != null){
            if (pendingMaintenanceRequests.contains(x)){
                pendingMaintenanceRequests.remove(x);
                System.out.println(LocalTime.now() + " - pending - { " + x + " }: " + pendingMaintenanceRequests);
                synchronized (pendingMaintenanceRequestsLock) {
                    pendingMaintenanceRequestsLock.notify();
                }
            }
            else errorln("WARN - robot not in pendingMaintenanceRequests");
        }
        else errorln("WARN - null pendingMaintenanceRequests");
    }

    synchronized public void updatePendingMaintenanceRequestsById(String id){
        if (pendingMaintenanceRequests != null){
            for (RobotRepresentation x : pendingMaintenanceRequests) {
                if (x.getId() == id) {
                    pendingMaintenanceRequests.remove(x);
                    System.out.println(LocalTime.now() + " - pending - { " + x + " }: " + pendingMaintenanceRequests);
                    synchronized (pendingMaintenanceRequestsLock) {
                        pendingMaintenanceRequestsLock.notify();
                    }
                    return;
                }
            }
            errorln("WARN - robot not in pendingMaintenanceRequests");
        }
        else errorln("WARN - null pendingMaintenanceRequests");
    }
}
