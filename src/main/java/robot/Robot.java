package robot;

import admin_server.RobotPosition;
import admin_server.RobotRepresentation;
import admin_server.services.RegistrationResponse;

import com.example.grpc.PresentationServiceGrpc;
import com.example.grpc.PresentationServiceGrpc.PresentationServiceStub;
import com.example.grpc.PresentationServiceOuterClass.PresentationRequest;
import com.example.grpc.PresentationServiceOuterClass.PresentationResponse;

import com.example.grpc.LeavingServiceGrpc;
import com.example.grpc.LeavingServiceGrpc.LeavingServiceStub;
import com.example.grpc.LeavingServiceOuterClass.LeavingRequest;
import com.example.grpc.LeavingServiceOuterClass.LeavingResponse;

import com.google.gson.Gson;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import utils.exceptions.RegistrationFailureException;
import utils.exceptions.RemovalFailureException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static utils.Printer.errorln;
import static utils.Printer.successln;

public class Robot {

    private String id;
    private int listeningPort;
    private String adminServerAddress;
    private RobotPosition position;
    private List<RobotRepresentation> otherRobots;
    private Object otherRobotsLock = new Object();

    public Robot(String id, int listeningPort, String adminServerAddress) {
        this.id = id;
        this.listeningPort = listeningPort;
        this.adminServerAddress = adminServerAddress;
    }

    public Object getOtherRobotsLock() {
        return otherRobotsLock;
    }

    public RobotPosition getPosition() {
        return position;
    }

    public int getListeningPort() {
        return listeningPort;
    }

    public List<RobotRepresentation> getOtherRobots() {
        return otherRobots;
    }

    public void registration() throws RegistrationFailureException {

        Client client = Client.create();
        ClientResponse clientResponse = null;

        String postPath = "/robots/register";
        clientResponse = postRegistrationRequest(client, adminServerAddress + postPath,
                new RobotRepresentation(id, "localhost", listeningPort));

        //logln("Registration response: " + clientResponse.toString());

        if (clientResponse.getStatus() == 200) {

            RegistrationResponse r = clientResponse.getEntity(RegistrationResponse.class);
            this.position = r.getPosition();
            this.otherRobots = r.getOtherRobots();
            if (this.otherRobots == null)
                this.otherRobots = new ArrayList<>();
        } else {
            throw new RegistrationFailureException("Registration failure");
        }
    }

    public void removal() throws RemovalFailureException {

        Client client = Client.create();
        ClientResponse clientResponse = null;

        String postPath = "/robots/remove";
        clientResponse = deleteRemovalRequest(client, adminServerAddress + postPath, this.id);

        //logln("Removal response: " + clientResponse.toString());

        if (clientResponse.getStatus() != 200)
            throw new RemovalFailureException("Removal failure");
    }

    public void presentation() {

        List<ManagedChannel> channels = new ArrayList<>();

        synchronized (this.otherRobotsLock) {
            for (RobotRepresentation x : this.otherRobots) {

                final ManagedChannel channel = ManagedChannelBuilder
                        .forTarget("localhost:" + x.getPort()).usePlaintext().build();

                channels.add(channel);

                PresentationServiceStub stub = PresentationServiceGrpc.newStub(channel);
                PresentationRequest request = PresentationRequest.newBuilder()
                        .setId(this.id)
                        .setPort(this.listeningPort)
                        .setPosition(PresentationRequest.Position.newBuilder()
                                .setX(this.position.getX())
                                .setY(this.position.getY())
                                .build())
                        .build();

                stub.presentation(request, new StreamObserver<PresentationResponse>() {

                    public void onNext(PresentationResponse response) {
                        successln("Presentation to " + x + " succeded");
                    }

                    public void onError(Throwable throwable) {
                        errorln("Error! " + throwable.getMessage());
                    }

                    public void onCompleted() {
                        channel.shutdownNow();
                    }
                });
            }
        }

        /*
        for (ManagedChannel ch : channels) {
            try {
                ch.awaitTermination(10, TimeUnit.SECONDS);
                System.out.println("fine canale");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        */
    }

    public void leaving() {

        List<ManagedChannel> channels = new ArrayList<>();

        synchronized (this.otherRobotsLock) {
            for (RobotRepresentation x : this.otherRobots) {

                final ManagedChannel channel = ManagedChannelBuilder
                        .forTarget("localhost:" + x.getPort()).usePlaintext().build();

                channels.add(channel);

                LeavingServiceStub stub = LeavingServiceGrpc.newStub(channel);
                LeavingRequest request = LeavingRequest.newBuilder()
                        .setId(this.id)
                        .build();

                stub.leaving(request, new StreamObserver<LeavingResponse>() {

                    public void onNext(LeavingResponse response) {
                        successln("I notified " + x + " that I'm leaving");
                    }

                    public void onError(Throwable throwable) {
                        errorln("Error! " + throwable.getMessage());
                    }

                    public void onCompleted() {
                        channel.shutdownNow();
                    }
                });
            }
        }

        /*
        for (ManagedChannel ch : channels) {
            try {
                ch.awaitTermination(10, TimeUnit.SECONDS);
                System.out.println("fine canale");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        */
    }

    public static ClientResponse postRegistrationRequest(Client client, String url, RobotRepresentation req) {
        WebResource webResource = client.resource(url);
        String input = new Gson().toJson(req);
        try {
            return webResource.type("application/json").post(ClientResponse.class, input);
        } catch (ClientHandlerException e) {
            errorln("Server non disponibile");
            return null;
        }
    }

    public static ClientResponse deleteRemovalRequest(Client client, String url, String id) {
        WebResource webResource = client.resource(url);
        String input = id;
        try {
            return webResource.type("application/json").delete(ClientResponse.class, input);
        } catch (ClientHandlerException e) {
            errorln("Server non disponibile");
            return null;
        }
    }
}
