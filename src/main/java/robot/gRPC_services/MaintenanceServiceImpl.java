package robot.gRPC_services;

import com.example.grpc.MaintenanceServiceGrpc.MaintenanceServiceImplBase;
import com.example.grpc.MaintenanceServiceOuterClass.*;
import io.grpc.stub.StreamObserver;
import robot.Robot;
import robot.maintenance.MaintenanceThread;

import static utils.Printer.logln;

public class MaintenanceServiceImpl extends MaintenanceServiceImplBase {

    private final Robot r;

    public MaintenanceServiceImpl(Robot r) {
        this.r = r;
    }

    @Override
    public void maintenance(MaintenanceRequest request, StreamObserver<MaintenanceResponse> responseObserver) {

        logln("Maintenance access request from R_" + request.getId() + " with timestamp " + request.getTimestamp());

        while (cannotAnswer(request.getTimestamp())){
            System.out.println("... block response to " + request.getId());
            synchronized (MaintenanceThread.maintenanceResponseLock){
                try {
                    MaintenanceThread.maintenanceResponseLock.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        System.out.println("... sending response to " + request.getId());

        MaintenanceResponse response = MaintenanceResponse.newBuilder().build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    public static boolean cannotAnswer(String requestTimestamp){
        synchronized (MaintenanceThread.lock) {
            return MaintenanceThread.maintenanceRequestTimestamp != null && MaintenanceThread.maintenanceRequestTimestamp < Long.parseLong(requestTimestamp);
        }
    }
}
