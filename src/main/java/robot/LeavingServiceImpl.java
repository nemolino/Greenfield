package robot;

import admin_server.RobotRepresentation;
import com.example.grpc.LeavingServiceGrpc.LeavingServiceImplBase;
import com.example.grpc.LeavingServiceOuterClass.*;
import io.grpc.stub.StreamObserver;

import java.util.List;
import java.util.Objects;

import static utils.Printer.log;
import static utils.Printer.logln;

public class LeavingServiceImpl extends LeavingServiceImplBase {

    private final Robot r;

    public LeavingServiceImpl(Robot r){
        this.r = r;
    }

    @Override
    public void leaving(LeavingRequest request, StreamObserver<LeavingResponse> responseObserver){

        log("Leaving notification for R_" + request.getId() +
                " from R_" + (request.getSender().equals("") ? request.getId() : request.getSender()));


        // update data structure
        synchronized (r.getOtherRobotsLock()) {
            List<RobotRepresentation> others = r.getOtherRobots();
            for (RobotRepresentation x : others){
                if (Objects.equals(x.getId(), request.getId())){
                    others.remove(x);
                    break;
                }
            }
            logln(" | otherRobots: " + others);
        }

        LeavingResponse response = LeavingResponse.newBuilder().build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

}
