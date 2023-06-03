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
 */

public class MaintenanceThread extends Thread {

    protected volatile boolean stopCondition = false;
    protected volatile boolean fixCommand = false;

    private final Robot r;

    private List<RobotRepresentation> otherRobotsCopy;
    private Long maintenanceRequestTimestamp = null;
    private Set<RobotRepresentation> pendingMaintenanceRequests = null;
    private boolean requirinqMaintenance;
    private boolean usingMaintenance;

    public final Object fixLock = new Object();

    private final Object accessMaintenanceLock = new Object();
    private final Object structuresLock = new Object();
    private final Object maintenanceResponsesLock = new Object();

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
                synchronized (maintenanceResponsesLock) {
                    maintenanceResponsesLock.notifyAll();
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

        requirinqMaintenance = true;
        maintenanceRequestTimestamp = System.currentTimeMillis();
        synchronized (r.getOtherRobotsLock()) {
            otherRobotsCopy = new ArrayList<>(r.getOtherRobots());
        }
        pendingMaintenanceRequests = new HashSet<>(otherRobotsCopy);

        sendMaintenanceRequests();

        // waiting for all the responses
        while (pendingMaintenanceRequests.size() > 0) {
            try {
                //logln("PRIMA sveglio " + pendingMaintenanceRequests.size());
                synchronized (accessMaintenanceLock){
                    accessMaintenanceLock.wait();
                }
                //logln("DOPO  sveglio " + pendingMaintenanceRequests.size());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        doMaintenance(); // CRITICAL SECTION

        pendingMaintenanceRequests = null;
        maintenanceRequestTimestamp = null;
        requirinqMaintenance = false;

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
                    errorln("QUI  " + throwable.getMessage() + " | Notifying otherRobots that " + x + " left the city!");
                    r.removeDeadRobot(x);
                }

                public void onCompleted() {
                    channel.shutdownNow();
                }
            });
        }
    }

    private void doMaintenance() {
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

    private void removePendingRequest(RobotRepresentation x){
        pendingMaintenanceRequests.remove(x);
        System.out.println(LocalTime.now() + " - pending - { " + x + " } = " + pendingMaintenanceRequests);
        if (pendingMaintenanceRequests.size() == 0){
            synchronized (accessMaintenanceLock){
                accessMaintenanceLock.notify();
            }
        }
    }

    public void updatePendingMaintenanceRequests(RobotRepresentation x) {
        synchronized (structuresLock) {
            if (pendingMaintenanceRequests != null) {
                if (pendingMaintenanceRequests.contains(x)) {
                    removePendingRequest(x);
                }
                //else errorln("WARN - robot not in pendingMaintenanceRequests");
            }
            //else errorln("WARN - null pendingMaintenanceRequests");
        }
    }

    public void updatePendingMaintenanceRequestsById(String id) {
        synchronized (structuresLock) {
            if (pendingMaintenanceRequests != null) {
                for (RobotRepresentation x : pendingMaintenanceRequests) {
                    if (Objects.equals(x.getId(), id)) {
                        removePendingRequest(x);
                        return;
                    }
                }
                //errorln("WARN by id - robot not in pendingMaintenanceRequests");
            }
            //else errorln("WARN by id - null pendingMaintenanceRequests");
        }
    }

    public boolean hasToWait(String requestTimestamp){
        synchronized (structuresLock) {
            return usingMaintenance || (requirinqMaintenance &&
                                        maintenanceRequestTimestamp != null &&
                                        maintenanceRequestTimestamp < Long.parseLong(requestTimestamp));
        }
    }

    // getter
    public Object getMaintenanceResponsesLock() {
        return maintenanceResponsesLock;
    }
}
