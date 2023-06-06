package robot.network;

import com.example.grpc.HeartbeatServiceGrpc.HeartbeatServiceImplBase;
import com.example.grpc.HeartbeatServiceOuterClass.HeartbeatRequest;
import com.example.grpc.HeartbeatServiceOuterClass.HeartbeatResponse;
import io.grpc.stub.StreamObserver;

public class HeartbeatServiceImpl extends HeartbeatServiceImplBase {

    @Override
    public void heartbeat(HeartbeatRequest request, StreamObserver<HeartbeatResponse> responseObserver) {

        HeartbeatResponse response = HeartbeatResponse.newBuilder().build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
