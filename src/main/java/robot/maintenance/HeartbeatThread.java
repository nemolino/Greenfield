package robot.maintenance;

import admin_server.REST_response_formats.RobotRepresentation;
import com.example.grpc.MaintenanceServiceGrpc;
import com.example.grpc.MaintenanceServiceOuterClass.HeartbeatRequest;
import com.example.grpc.MaintenanceServiceOuterClass.HeartbeatResponse;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import robot.Robot;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static common.Printer.errorln;
import static common.Printer.logln;

public class HeartbeatThread extends Thread {

    protected volatile boolean stopCondition = false;

    private final Robot r;
    private final Object structuresLock;
    private Set<RobotRepresentation> pendingRequests;

    public HeartbeatThread(Robot r, Object structuresLock, Set<RobotRepresentation> pendingRequests) {
        this.r = r;
        this.structuresLock = structuresLock;
        this.pendingRequests = pendingRequests;
    }

    @Override
    public void run() {

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        List<RobotRepresentation> remainingPendingRequests;

        while (!stopCondition) {

            synchronized (structuresLock) {
                if (pendingRequests == null || pendingRequests.size() == 0) {
                    stopCondition = true;
                    continue;
                }
                remainingPendingRequests = new ArrayList<>(pendingRequests);
            }

            for (RobotRepresentation x : remainingPendingRequests) {

                final ManagedChannel channel = ManagedChannelBuilder
                        .forTarget("localhost:" + x.getPort()).usePlaintext().build();

                MaintenanceServiceGrpc.MaintenanceServiceStub stub = MaintenanceServiceGrpc.newStub(channel);
                HeartbeatRequest request = HeartbeatRequest.newBuilder().build();

                stub.heartbeat(request, new StreamObserver<HeartbeatResponse>() {

                    public void onNext(HeartbeatResponse response) {
                        logln("... " + x + " is still alive");
                    }

                    public void onError(Throwable throwable) {
                        errorln(x + " is dead [HeartbeatThread] " + throwable.getMessage());
                        r.removeDeadRobot(x);
                    }

                    public void onCompleted() {
                        channel.shutdownNow();
                    }
                });
            }

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void stopMeGently() {
        stopCondition = true;
    }
}
