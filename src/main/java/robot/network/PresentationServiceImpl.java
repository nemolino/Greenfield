package robot.network;

import admin_server.REST_response_formats.RobotRepresentation;
import com.example.grpc.PresentationServiceGrpc.PresentationServiceImplBase;
import com.example.grpc.PresentationServiceOuterClass.PresentationRequest;
import com.example.grpc.PresentationServiceOuterClass.PresentationResponse;
import common.printer.Type;
import io.grpc.stub.StreamObserver;
import robot.Robot;

import static common.printer.Printer.info;

public class PresentationServiceImpl extends PresentationServiceImplBase {

    private final Robot r;

    public PresentationServiceImpl(Robot r) {
        this.r = r;
    }

    @Override
    public void presentation(PresentationRequest request, StreamObserver<PresentationResponse> responseObserver) {

        info(Type.N, "... R_" + request.getId() + " is entering Greenfield ( port: " + request.getPort() +
                " , position: (" + request.getPosition().getX() + "," + request.getPosition().getY() + ") )");

        // updating otherRobots
        r.addToOtherRobots(new RobotRepresentation(request.getId(), "localhost", request.getPort()));

        PresentationResponse response = PresentationResponse.newBuilder().build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}