package robot.network;

import admin_server.rest_response_formats.RegistrationResponse;
import admin_server.rest_response_formats.RobotRepresentation;
import com.example.grpc.LeavingServiceGrpc;
import com.example.grpc.LeavingServiceOuterClass.LeavingRequest;
import com.example.grpc.LeavingServiceOuterClass.LeavingResponse;
import com.example.grpc.PresentationServiceGrpc;
import com.example.grpc.PresentationServiceOuterClass.PresentationRequest;
import com.example.grpc.PresentationServiceOuterClass.PresentationResponse;
import com.google.gson.Gson;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import common.printer.Type;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import robot.Robot;
import robot.network.exceptions.RegistrationFailureException;
import robot.network.exceptions.RemovalFailureException;

import java.rmi.server.ServerNotActiveException;
import java.util.ArrayList;
import java.util.Objects;

import static common.printer.Printer.*;

public class Network {

    private final Robot r;

    public Network(Robot r) {
        this.r = r;
    }

    /* ------------------------------------------------------------- calls to Administration Server REST services --- */

    // registration of this robot to Administration Server
    public void registration() throws RegistrationFailureException, ServerNotActiveException {

        Client client = Client.create();
        ClientResponse response = postRegistrationRequest(client, r.getAdminServerAddress() + "/robots/register",
                new RobotRepresentation(r.getId(), "localhost", r.getListeningPort()));

        if (response == null)
            throw new ServerNotActiveException("Server is unavailable");

        if (response.getStatus() == 200) {
            RegistrationResponse res = response.getEntity(RegistrationResponse.class);
            r.setPosition(res.getPosition());
            r.setDistrict(res.getPosition().getDistrict());
            r.setOtherRobots(res.getOtherRobots() != null ? res.getOtherRobots() : new ArrayList<>());
        } else
            throw new RegistrationFailureException("Duplicated ID");
    }

    private static ClientResponse postRegistrationRequest(Client client, String url, RobotRepresentation req) {
        WebResource webResource = client.resource(url);
        String input = new Gson().toJson(req);
        try {
            return webResource.type("application/json").post(ClientResponse.class, input);
        } catch (ClientHandlerException e) {
            return null;
        }
    }

    // removal of a robot by its id from Administration Server
    public void removal(String id) throws RemovalFailureException, ServerNotActiveException {

        Client client = Client.create();
        ClientResponse response = deleteRemovalRequest(client, r.getAdminServerAddress() + "/robots/remove", id);

        if (response == null)
            throw new ServerNotActiveException("Server is unavailable");

        if (response.getStatus() != 200)
            throw new RemovalFailureException("Missing ID");
    }

    private static ClientResponse deleteRemovalRequest(Client client, String url, String id) {
        WebResource webResource = client.resource(url);
        try {
            return webResource.type("application/json").delete(ClientResponse.class, id);
        } catch (ClientHandlerException e) {
            return null;
        }
    }

    /* ------------------------------------------------------------------------ robot network management via gRPC --- */

    // presentation to the other robots already in Greenfield
    public void presentation() {

        for (RobotRepresentation x : r.getOtherRobotsCopy()) {

            final ManagedChannel channel = ManagedChannelBuilder
                    .forTarget("localhost:" + x.getPort()).usePlaintext().build();

            PresentationServiceGrpc.PresentationServiceStub stub = PresentationServiceGrpc.newStub(channel);
            PresentationRequest request = PresentationRequest.newBuilder()
                    .setId(r.getId())
                    .setPort(r.getListeningPort())
                    .setPosition(PresentationRequest.Position.newBuilder()
                            .setX(r.getPosition().getX())
                            .setY(r.getPosition().getY())
                            .build())
                    .build();

            stub.presentation(request, new StreamObserver<PresentationResponse>() {

                public void onNext(PresentationResponse response) {
                    log(Type.N, "... presentation to " + x + " done");
                }

                public void onError(Throwable throwable) {
                    // not an error, but I want it red to make it visible
                    error(Type.N, "... " + x + " is dead [Network, presentation]");
                    removeDeadRobot(x);
                    channel.shutdownNow();
                }

                public void onCompleted() {
                    channel.shutdownNow();
                }
            });
        }
    }

    // notifying other robots that the robot with a certain id is leaving Greenfield
    public void leaving(String leavingRobotId) {

        for (RobotRepresentation x : r.getOtherRobotsCopy()) {

            final ManagedChannel channel = ManagedChannelBuilder
                    .forTarget("localhost:" + x.getPort()).usePlaintext().build();

            LeavingServiceGrpc.LeavingServiceStub stub = LeavingServiceGrpc.newStub(channel);
            LeavingRequest request;
            if (Objects.equals(r.getId(), leavingRobotId))
                request = LeavingRequest.newBuilder().setId(leavingRobotId).build();
            else
                request = LeavingRequest.newBuilder().setId(leavingRobotId).setSender(r.getId()).build();

            stub.leaving(request, new StreamObserver<LeavingResponse>() {

                public void onNext(LeavingResponse response) {
                    if (Objects.equals(r.getId(), leavingRobotId))
                        log(Type.N, "... notified " + x + " that I'm leaving Greenfield");
                    else
                        log(Type.N, "... notified " + x + " that R_" + leavingRobotId + " has left Greenfield");
                }

                public void onError(Throwable throwable) {
                    // not an error, but I want it red to make it visible
                    error(Type.N, "... " + x + " is dead [Network, leaving]");
                    removeDeadRobot(x);
                    channel.shutdownNow();
                }

                public void onCompleted() {
                    channel.shutdownNow();
                }
            });
        }
    }

    // removing robot x from the network
    public void removeDeadRobot(RobotRepresentation x) {

        // removing x from otherRobots
        r.removeFromOtherRobotsById(x.getId());

        // removing x from AdminServer
        try {
            removal(x.getId());
            log(Type.N, "... removal of " + x + " from AdminServer succeeded");
        } catch (RemovalFailureException e) {
            warn(Type.N, "... removal of " + x + " from AdminServer failed - " + e.getMessage());
        } catch (ServerNotActiveException e) {
            error(Type.N, "... removal of " + x + " from AdminServer failed - " + e.getMessage());
        }

        // update maintenance pending requests
        r.maintenance().updatePendingMaintenanceRequests(x);

        // notifying other robots that x is dead
        leaving(x.getId());
    }
}
