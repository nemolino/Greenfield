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

import static utils.Printer.*;

/*
 * Se si aggiunge un nuovo robot e io ho già mandato le richieste senza considerare lui
 * perchè non lo avevo in otherRobots non è un problema :
 * se non lo avevo in otherRobots non può aver ancora mandato richieste con un timestamp < del mio.
 *
 * Se un robot termina in modo controllato (con "quit") non succede nulla
 * perchè prima di terminare manda tutte le risposte che ha in sospeso.
 *
 * Se un robot termina in modo non controllato (con "quit") non succede nulla
 * perchè prima di terminare manda tutte le risposte che ha in sospeso.
 *
 */

public class MaintenanceThread extends Thread {

    protected volatile boolean stopCondition = false;

    private final Robot r;

    private List<RobotRepresentation> otherRobotsCopy;
    private Long maintenanceRequestTimestamp = null;
    private Set<RobotRepresentation> pendingMaintenanceRequests = null;

    private final Object maintenanceResponsesLock = new Object();

    public MaintenanceThread(Robot r) {
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
            if (Math.random() < 0.3) {
                System.out.println(LocalTime.now() + " - I need maintenance!");
                accessMaintenance();
            }
        }
    }

    public void stopMeGently() {
        stopCondition = true;
    }

    synchronized public void accessMaintenance() {

        maintenanceRequestTimestamp = System.currentTimeMillis();
        synchronized (r.getOtherRobotsLock()) {
            otherRobotsCopy = new ArrayList<>(r.getOtherRobots());
        }
        pendingMaintenanceRequests = new HashSet<>(otherRobotsCopy);

        sendMaintenanceRequests();

        // waiting for all the responses
        while (pendingMaintenanceRequests.size() > 0) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        doMaintenance(); // CRITICAL SECTION

        maintenanceRequestTimestamp = null;
        pendingMaintenanceRequests = null;

        synchronized (maintenanceResponsesLock) {
            maintenanceResponsesLock.notifyAll();
        }
    }

    private void sendMaintenanceRequests() {

        System.out.println(LocalTime.now() + " - pending = " + pendingMaintenanceRequests);

        for (RobotRepresentation x : otherRobotsCopy) {

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

    private void doMaintenance() {
        successln(LocalTime.now() + " ... ENTER maintenance");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        successln(LocalTime.now() + " ...  EXIT maintenance");
    }

    synchronized public void updatePendingMaintenanceRequests(RobotRepresentation x) {
        if (pendingMaintenanceRequests != null) {
            if (pendingMaintenanceRequests.contains(x)) {
                pendingMaintenanceRequests.remove(x);
                System.out.println(LocalTime.now() + " - pending - { " + x + " } = " + pendingMaintenanceRequests);
                successln("GOOD - rimozione corretta");
                notify();
            }
            else errorln("WARN - robot not in pendingMaintenanceRequests");
        }
        else errorln("WARN - null pendingMaintenanceRequests");
    }

   synchronized public void updatePendingMaintenanceRequestsById(String id) {
        if (pendingMaintenanceRequests != null) {
            for (RobotRepresentation x : pendingMaintenanceRequests) {
                if (x.getId() == id) {
                    pendingMaintenanceRequests.remove(x);
                    System.out.println(LocalTime.now() + " - pending - { " + x + " } = " + pendingMaintenanceRequests);
                    successln("GOOD by id - rimozione corretta");
                    notify();
                    return;
                }
            }
            errorln("WARN by id - robot not in pendingMaintenanceRequests");
        }
        else errorln("WARN by id - null pendingMaintenanceRequests");
    }

    synchronized public boolean hasPriority(String requestTimestamp){
       return maintenanceRequestTimestamp != null && maintenanceRequestTimestamp < Long.parseLong(requestTimestamp);
    }

    // GETTER
    public Object getMaintenanceResponsesLock() {
        return maintenanceResponsesLock;
    }
}
