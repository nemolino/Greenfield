package robot.maintenance;

import com.example.grpc.MaintenanceServiceGrpc.MaintenanceServiceImplBase;
import com.example.grpc.MaintenanceServiceOuterClass.*;
import io.grpc.stub.StreamObserver;
import robot.Robot;

import static common.Printer.logln;

public class MaintenanceServiceImpl extends MaintenanceServiceImplBase {

    private final Robot r;

    public MaintenanceServiceImpl(Robot r) {
        this.r = r;
    }

    @Override
    public void maintenance(MaintenanceRequest request, StreamObserver<MaintenanceResponse> responseObserver) {

        logln("R_" + request.getId() + " wants to access maintenance");// + " with timestamp " + request.getTimestamp());

        MaintenanceThread m = r.getMaintenance().getThread();

        while (m.hasToWait(request.getTimestamp())){
            //System.out.println("... blocking maintenance response to R_" + request.getId());
            synchronized (m.sendResponseLock){
                try {
                    m.sendResponseLock.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        //System.out.println("... sending maintenance response to R_" + request.getId());

        MaintenanceResponse response = MaintenanceResponse.newBuilder().build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void heartbeat(HeartbeatRequest request, StreamObserver<HeartbeatResponse> responseObserver) {

        HeartbeatResponse response = HeartbeatResponse.newBuilder().build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
