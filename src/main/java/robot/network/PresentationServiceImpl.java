package robot.network;

import admin_server.rest_response_formats.RobotRepresentation;
import com.example.grpc.PresentationServiceGrpc.PresentationServiceImplBase;
import com.example.grpc.PresentationServiceOuterClass.PresentationRequest;
import com.example.grpc.PresentationServiceOuterClass.PresentationResponse;
import common.Position;
import common.printer.Type;
import io.grpc.stub.StreamObserver;
import robot.Robot;

import static common.printer.Printer.log;

public class PresentationServiceImpl extends PresentationServiceImplBase {

    private final Robot r;

    public PresentationServiceImpl(Robot r) {
        this.r = r;
    }

    @Override
    public void presentation(PresentationRequest request, StreamObserver<PresentationResponse> responseObserver) {

        Position p = new Position(request.getPosition().getX(), request.getPosition().getY());
        log(Type.N, "... R_" + request.getId() + " is entering Greenfield ( port: " + request.getPort() +
                " , position: " + p + ", district : " + p.getDistrict() + " )");

        // updating otherRobots
        r.addToOtherRobots(new RobotRepresentation(request.getId(), "localhost", request.getPort()));

        PresentationResponse response = PresentationResponse.newBuilder().build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}