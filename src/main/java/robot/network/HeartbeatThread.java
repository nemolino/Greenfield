package robot.network;

import admin_server.rest_response_formats.RobotRepresentation;
import com.example.grpc.HeartbeatServiceGrpc;
import com.example.grpc.HeartbeatServiceOuterClass.HeartbeatRequest;
import com.example.grpc.HeartbeatServiceOuterClass.HeartbeatResponse;
import common.printer.Type;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import robot.Robot;

import java.util.List;

import static common.printer.Printer.*;

public class HeartbeatThread extends Thread {

    protected volatile boolean stopCondition = false;

    private final Robot r;
    private final Object stopLock = new Object();

    public HeartbeatThread(Robot r) {
        this.r = r;
    }

    @Override
    public void run() {

        while (!stopCondition) {

            try {
                synchronized (stopLock){
                    stopLock.wait(15000);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            if (stopCondition) break;

            List<RobotRepresentation> p = r.maintenance().getPendingRequestsCopy();
            log(Type.M_LOW, "... pending maintenance requests: " + p);

            if (p == null || p.size() == 0) continue;

            for (RobotRepresentation x : p) {

                final ManagedChannel channel = ManagedChannelBuilder
                        .forTarget("localhost:" + x.getPort()).usePlaintext().build();

                HeartbeatServiceGrpc.HeartbeatServiceStub stub = HeartbeatServiceGrpc.newStub(channel);
                HeartbeatRequest request = HeartbeatRequest.newBuilder().build();

                stub.heartbeat(request, new StreamObserver<HeartbeatResponse>() {

                    public void onNext(HeartbeatResponse response) {
                        log(Type.M_LOW, "... " + x + " is still alive");
                    }

                    public void onError(Throwable throwable) {
                        error(Type.N, "... " + x + " is dead [HeartbeatThread]");
                        r.network().removeDeadRobot(x);
                        channel.shutdownNow();
                    }

                    public void onCompleted() {
                        channel.shutdownNow();
                    }
                });
            }

        }
    }

    public void stopMeGently() {
        stopCondition = true;
        synchronized (stopLock){
            stopLock.notify();
        }
    }
}
