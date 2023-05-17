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

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static utils.Printer.*;

public class MaintenanceThread extends Thread {

    protected volatile boolean stopCondition = false;

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
            if (Math.random() < 0.1){
                System.out.println("I need maintenance!");
                accessMaintenance();
            }
        }
    }

    public void stopMeGently() {
        stopCondition = true;
    }

    public void accessMaintenance() {

        r.setNeedMaintenance(true);
        r.setMaintenanceRequestTimestamp(System.currentTimeMillis());

        synchronized (r.getOtherRobotsLock()) {

            r.setPendingMaintenanceRequests(new HashSet<>(r.getOtherRobots()));
            System.out.println("pending: " + r.getPendingMaintenanceRequests());

            for (RobotRepresentation x : r.getOtherRobots()) {

                final ManagedChannel channel = ManagedChannelBuilder
                        .forTarget("localhost:" + x.getPort()).usePlaintext().build();

                MaintenanceServiceGrpc.MaintenanceServiceStub stub = MaintenanceServiceGrpc.newStub(channel);
                MaintenanceRequest request = MaintenanceRequest.newBuilder()
                        .setId(r.getId())
                        .setTimestamp(r.getMaintenanceRequestTimestamp().toString())
                        .build();

                stub.maintenance(request, new StreamObserver<MaintenanceResponse>() {

                    public void onNext(MaintenanceResponse response) {
                        if (r.getPendingMaintenanceRequests().contains(x)){
                            r.getPendingMaintenanceRequests().remove(x);
                            System.out.println("pending - { " + x + " }: " + r.getPendingMaintenanceRequests());
                            synchronized (r.pendingMaintenanceRequestsLock) {
                                r.pendingMaintenanceRequestsLock.notify();
                            }
                        }
                        else{
                            errorln("aiuto, dovrebbe contenerlo");
                        }
                    }

                    public void onError(Throwable throwable) {
                        /*
                        errorln(throwable.getMessage() + " | Notifying otherRobots that " + x + " left the city!");

                        // removing x from otherRobots
                        synchronized (r.getOtherRobotsLock()) {
                            for (RobotRepresentation y : r.getOtherRobots()) {
                                if (Objects.equals(y.getId(), x.getId())) {
                                    r.getOtherRobots().remove(x);
                                    break;
                                }
                            }
                            errorln("otherRobots: " + r.getOtherRobots());
                        }

                        // removing x from AdminServer
                        try {
                            r.removal(x.getId());
                            successln("Removing " + x + " also from AdminServer");
                        } catch (RemovalFailureException e) {
                            warnln("Someone already removed " + x + " from AdminServer");
                        }

                        // notifying remaining otherRobots that x left the city
                        r.leaving(x.getId());*/
                    }

                    public void onCompleted() {
                        channel.shutdownNow();
                    }
                });
            }
        }


        // waiting for all the responses
        synchronized (r.pendingMaintenanceRequestsLock){
            while (r.getPendingMaintenanceRequests().size() > 0){
                try {
                    r.pendingMaintenanceRequestsLock.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        System.out.println("... entering mechanic");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("... exiting mechanic");

        r.setNeedMaintenance(false);
        r.setMaintenanceRequestTimestamp(null);
        r.setPendingMaintenanceRequests(null);
        synchronized (r.maintenanceResponseLock) {
            r.maintenanceResponseLock.notify();
        }
    }
}
