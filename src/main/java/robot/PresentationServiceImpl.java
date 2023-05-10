package robot;

import admin_server.RobotRepresentation;
import com.example.grpc.PresentationServiceGrpc.PresentationServiceImplBase;
import com.example.grpc.PresentationServiceOuterClass.*;
import io.grpc.stub.StreamObserver;

import static utils.Printer.logln;

public class PresentationServiceImpl extends PresentationServiceImplBase {

    private final Robot r;

    public PresentationServiceImpl(Robot r){
            this.r = r;
    }

    @Override
    public void presentation(PresentationRequest request, StreamObserver<PresentationResponse> responseObserver){

        logln("New registered robot R_" + request.getId() +
                " , listening at " + request.getPort() +
                " and placed in (" + request.getPosition().getX() + "," + request.getPosition().getY() + ")" );

        // update data structure
        synchronized (r.getOtherRobotsLock()) {
            r.getOtherRobots().add(new RobotRepresentation(request.getId(), "localhost", request.getPort()));
            logln(r.getOtherRobots().toString());
        }

        PresentationResponse response = PresentationResponse.newBuilder().build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

}