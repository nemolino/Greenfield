package robot.network;

import admin_server.REST_response_formats.RobotRepresentation;
import com.example.grpc.PresentationServiceGrpc.PresentationServiceImplBase;
import com.example.grpc.PresentationServiceOuterClass.*;
import io.grpc.stub.StreamObserver;
import robot.Robot;

import java.util.Objects;
import java.util.List;

import static common.Printer.log;
import static common.Printer.logln;

public class PresentationServiceImpl extends PresentationServiceImplBase {

    private final Robot r;

    public PresentationServiceImpl(Robot r){
            this.r = r;
    }

    @Override
    public void presentation(PresentationRequest request, StreamObserver<PresentationResponse> responseObserver){

        log("... receveid presentation of R_" + request.getId() +
                " at port " + request.getPort() +
                " placed in (" + request.getPosition().getX() + "," + request.getPosition().getY() + ")" );

        // updating otherRobots, if needed
        synchronized (r.getOtherRobotsLock()) {
            List<RobotRepresentation> others = r.getOtherRobots();
            boolean alreadyKnown = false;
            for (RobotRepresentation x : others){
                if (Objects.equals(x.getId(), request.getId())){
                    alreadyKnown = true;
                    break;
                }
            }
            if (!alreadyKnown){
                others.add(new RobotRepresentation(request.getId(), "localhost", request.getPort()));
            }
            logln("  | otherRobots: " + others);
        }

        PresentationResponse response = PresentationResponse.newBuilder().build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

}