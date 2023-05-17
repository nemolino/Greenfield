package robot.gRPC_services;

import com.example.grpc.MaintenanceServiceGrpc.MaintenanceServiceImplBase;
import com.example.grpc.MaintenanceServiceOuterClass.*;
import io.grpc.stub.StreamObserver;
import robot.Robot;

import static utils.Printer.logln;

public class MaintenanceServiceImpl extends MaintenanceServiceImplBase {

    private final Robot r;

    public MaintenanceServiceImpl(Robot r) {
        this.r = r;
    }

    @Override
    public void maintenance(MaintenanceRequest request, StreamObserver<MaintenanceResponse> responseObserver) {

        logln("Maintenance notification from R_" + request.getId() + " with timestamp " + request.getTimestamp());

        while (r.needMaintenance() && r.getMaintenanceRequestTimestamp() < Long.parseLong(request.getTimestamp())){
            System.out.println("... block response");
            synchronized (r.maintenanceResponseLock){
                try {
                    r.maintenanceResponseLock.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        System.out.println("... sending response");

        MaintenanceResponse response = MaintenanceResponse.newBuilder().build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

}
