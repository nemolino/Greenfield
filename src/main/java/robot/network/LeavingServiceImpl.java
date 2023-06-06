package robot.network;

import com.example.grpc.LeavingServiceGrpc.LeavingServiceImplBase;
import com.example.grpc.LeavingServiceOuterClass.LeavingRequest;
import com.example.grpc.LeavingServiceOuterClass.LeavingResponse;
import common.printer.Type;
import io.grpc.stub.StreamObserver;
import robot.Robot;

import static common.printer.Printer.info;

public class LeavingServiceImpl extends LeavingServiceImplBase {

    private final Robot r;

    public LeavingServiceImpl(Robot r) {
        this.r = r;
    }

    @Override
    public void leaving(LeavingRequest request, StreamObserver<LeavingResponse> responseObserver) {

        info(Type.N, "... R_" + request.getId() + " is leaving Greenfield " +
                "( warned by R_" + (request.getSender().equals("") ? request.getId() : request.getSender()) + " )");

        // updating otherRobots
        r.removeFromOtherRobotsById(request.getId());

        // update maintenance pending requests
        r.getMaintenance().getThread().updatePendingMaintenanceRequestsById(request.getId());

        LeavingResponse response = LeavingResponse.newBuilder().build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
