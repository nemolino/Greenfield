package robot.maintenance;

import com.example.grpc.MaintenanceServiceGrpc.MaintenanceServiceImplBase;
import com.example.grpc.MaintenanceServiceOuterClass.*;
import common.printer.Type;
import io.grpc.stub.StreamObserver;
import robot.Robot;

import static common.printer.Printer.log;

public class MaintenanceServiceImpl extends MaintenanceServiceImplBase {

    private final Robot r;

    public MaintenanceServiceImpl(Robot r) {
        this.r = r;
    }

    @Override
    public void maintenance(MaintenanceRequest request, StreamObserver<MaintenanceResponse> responseObserver) {

        log(Type.M, "... R_" + request.getId() + " needs maintenance");

        while (r.maintenance().hasToWait(request.getTimestamp())){
            //log(Type.M_LOW, "... blocking maintenance response to R_" + request.getId());
            synchronized (r.maintenance().getSendResponseLock()){
                try {
                    r.maintenance().getSendResponseLock().wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        //log(Type.M_LOW, "... sending maintenance response to R_" + request.getId());

        MaintenanceResponse response = MaintenanceResponse.newBuilder().build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
