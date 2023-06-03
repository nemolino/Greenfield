package robot.maintenance;

import com.example.grpc.MaintenanceServiceGrpc.MaintenanceServiceImplBase;
import com.example.grpc.MaintenanceServiceOuterClass.*;
import io.grpc.stub.StreamObserver;
import robot.Robot;
import robot.maintenance.MaintenanceThread;

import static utils.Printer.logln;

public class MaintenanceServiceImpl extends MaintenanceServiceImplBase {

    private final Robot r;
    private MaintenanceThread mt;

    public MaintenanceServiceImpl(Robot r) {
        this.r = r;
    }

    @Override
    public void maintenance(MaintenanceRequest request, StreamObserver<MaintenanceResponse> responseObserver) {

        mt = r.getMaintenance().getThread();
        Object responsesLock = mt.getMaintenanceResponsesLock();

        logln("Maintenance request from R_" + request.getId());// + " with timestamp " + request.getTimestamp());

        while (mt.hasToWait(request.getTimestamp())){
            //System.out.println("... blocking maintenance response to " + request.getId());
            synchronized (responsesLock){
                try {
                    responsesLock.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        //System.out.println("... sending maintenance response to " + request.getId());

        MaintenanceResponse response = MaintenanceResponse.newBuilder().build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
