package robot.gRPC_services;

import admin_server.REST_response_formats.RobotRepresentation;
import com.example.grpc.LeavingServiceGrpc.LeavingServiceImplBase;
import com.example.grpc.LeavingServiceOuterClass.*;
import io.grpc.stub.StreamObserver;
import robot.Robot;

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


        // updating otherRobots
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
