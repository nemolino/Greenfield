package robot.gRPC_services;

import admin_server.REST_response_formats.RobotRepresentation;
import com.example.grpc.PresentationServiceGrpc.PresentationServiceImplBase;
import com.example.grpc.PresentationServiceOuterClass.*;
import io.grpc.stub.StreamObserver;
import robot.Robot;

import static utils.Printer.log;
import static utils.Printer.logln;

public class PresentationServiceImpl extends PresentationServiceImplBase {

    private final Robot r;

    public PresentationServiceImpl(Robot r){
            this.r = r;
    }

    @Override
    public void presentation(PresentationRequest request, StreamObserver<PresentationResponse> responseObserver){

        log("New registered R_" + request.getId() +
                " at port " + request.getPort() +
                " placed in (" + request.getPosition().getX() + "," + request.getPosition().getY() + ")" );

        // updating otherRobots
        synchronized (r.getOtherRobotsLock()) {
            r.getOtherRobots().add(new RobotRepresentation(request.getId(), "localhost", request.getPort()));
            logln("  | otherRobots: " + r.getOtherRobots());
        }

        PresentationResponse response = PresentationResponse.newBuilder().build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

}