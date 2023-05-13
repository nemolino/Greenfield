package robot;

import admin_server.District;
import admin_server.RobotPosition;
import admin_server.RobotRepresentation;
import admin_server.REST_response_formats.RegistrationResponse;

import com.example.grpc.PresentationServiceGrpc;
import com.example.grpc.PresentationServiceGrpc.PresentationServiceStub;
import com.example.grpc.PresentationServiceOuterClass.PresentationRequest;
import com.example.grpc.PresentationServiceOuterClass.PresentationResponse;

import com.example.grpc.LeavingServiceGrpc;
import com.example.grpc.LeavingServiceGrpc.LeavingServiceStub;
import com.example.grpc.LeavingServiceOuterClass.LeavingRequest;
import com.example.grpc.LeavingServiceOuterClass.LeavingResponse;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.eclipse.paho.client.mqttv3.*;
import robot.MQTT_pollution.BufferAverages;
import robot.MQTT_pollution.SensorDataProcessingThread;
import robot.MQTT_pollution.SensorDataPublishingThread;
import utils.exceptions.RegistrationFailureException;
import utils.exceptions.RemovalFailureException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static robot.RequestsHTTP.*;
import static utils.Printer.*;
import static utils.Utils.MQTT_BROKER_ADDRESS;

public class Robot {

    private final String id;
    private final int listeningPort;
    private final String adminServerAddress;
    private RobotPosition position;
    private District district;
    private List<RobotRepresentation> otherRobots;
    private final Object otherRobotsLock = new Object();

    // POLLUTION
    private BufferAverages b;
    private SensorDataProcessingThread processing;
    private SensorDataPublishingThread publishing;
    private MqttClient client;
    private String clientId;

    public Robot(String id, int listeningPort, String adminServerAddress) {
        this.id = id;
        this.listeningPort = listeningPort;
        this.adminServerAddress = adminServerAddress;
    }

    public BufferAverages getBufferAverages() {
        return b;
    }

    public MqttClient getMqttClient() {
        return client;
    }

    public String getMqttClientId() {
        return clientId;
    }

    public District getDistrict() {
        return district;
    }

    public String getId() { return id; }

    public Object getOtherRobotsLock() {
        return otherRobotsLock;
    }

    public RobotPosition getPosition() {
        return position;
    }

    public int getListeningPort() {
        return listeningPort;
    }

    public List<RobotRepresentation> getOtherRobots() { return otherRobots; }

    public void registration() throws RegistrationFailureException {

        Client client = Client.create();
        ClientResponse clientResponse;

        clientResponse = postRegistrationRequest(client, adminServerAddress + "/robots/register",
                new RobotRepresentation(id, "localhost", listeningPort));

        //logln("Registration response: " + clientResponse.toString());

        if (clientResponse.getStatus() == 200) {

            RegistrationResponse r = clientResponse.getEntity(RegistrationResponse.class);
            this.position = r.getPosition();
            this.district = this.position.getDistrict();
            this.otherRobots = r.getOtherRobots();
            if (this.otherRobots == null)
                this.otherRobots = new ArrayList<>();
        } else {
            throw new RegistrationFailureException("Registration failure");
        }
    }

    public void removal(String leavingRobotId) throws RemovalFailureException {

        Client client = Client.create();
        ClientResponse clientResponse;

        clientResponse = deleteRemovalRequest(client, adminServerAddress + "/robots/remove", leavingRobotId);

        //logln("Removal response: " + clientResponse.toString());

        if (clientResponse.getStatus() != 200)
            throw new RemovalFailureException("Removal failure");
    }

    public void presentation() {

        synchronized (this.otherRobotsLock) {
            for (RobotRepresentation x : this.otherRobots) {

                final ManagedChannel channel = ManagedChannelBuilder
                        .forTarget("localhost:" + x.getPort()).usePlaintext().build();

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

                        errorln(throwable.getMessage() + " | Notifying otherRobots that " + x + " left the city!");

                        // removing x from otherRobots
                        synchronized (otherRobotsLock) {
                            for (RobotRepresentation y : otherRobots) {
                                if (Objects.equals(y.getId(), x.getId())) {
                                    otherRobots.remove(x);
                                    break;
                                }
                            }
                            errorln("otherRobots: " + otherRobots);
                        }

                        // removing x from AdminServer
                        try {
                            removal(x.getId());
                            successln("Removing " + x + " also from AdminServer");
                        } catch (RemovalFailureException e) {
                            warnln("Someone already removed " + x + " from AdminServer");
                        }

                        // notifying remaining otherRobots that x left the city
                        leaving(x.getId());
                    }

                    public void onCompleted() {
                        channel.shutdownNow();
                    }
                });
            }
        }
    }

    public void leaving(String leavingRobotId) {

        synchronized (this.otherRobotsLock) {
            for (RobotRepresentation x : this.otherRobots) {

                final ManagedChannel channel = ManagedChannelBuilder
                        .forTarget("localhost:" + x.getPort()).usePlaintext().build();

                LeavingServiceStub stub = LeavingServiceGrpc.newStub(channel);
                LeavingRequest request;
                if (Objects.equals(leavingRobotId, this.id))
                    request = LeavingRequest.newBuilder().setId(leavingRobotId).build();
                else
                    request = LeavingRequest.newBuilder().setId(leavingRobotId).setSender(this.id).build();

                stub.leaving(request, new StreamObserver<LeavingResponse>() {

                    public void onNext(LeavingResponse response) {
                        successln("I successfully notified " + x + " that " + leavingRobotId + " is leaving");
                    }

                    public void onError(Throwable throwable) {

                        errorln(throwable.getMessage() + " | Notifying otherRobots that " + x + " left the city!");

                        // removing x from otherRobots
                        synchronized (otherRobotsLock) {
                            for (RobotRepresentation y : otherRobots) {
                                if (Objects.equals(y.getId(), x.getId())) {
                                    otherRobots.remove(x);
                                    break;
                                }
                            }
                            errorln("otherRobots: " + otherRobots);
                        }

                        // removing x from AdminServer
                        try {
                            removal(x.getId());
                            errorln("Removing " + x + " also from AdminServer");
                        } catch (RemovalFailureException e) {
                            warnln("Someone already removed " + x + " from AdminServer");
                        }

                        // notifying remaining otherRobots that x left the city (recursive)
                        leaving(x.getId());
                    }

                    public void onCompleted() { channel.shutdownNow(); }
                });
            }
        }
    }

    // POLLUTION

    public void turnOnPollutionProcessing(){
        this.b = new BufferAverages();
        processing = new SensorDataProcessingThread(this.b);
        processing.start();
    }

    public void turnOnPollutionPublishing(){

        clientId = MqttClient.generateClientId();
        try {
            client = new MqttClient(MQTT_BROKER_ADDRESS, clientId);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);

            logln(clientId + " Connecting Broker " + MQTT_BROKER_ADDRESS);
            client.connect(connOpts);
            logln(clientId + " Connected - Thread PID: " + Thread.currentThread().getId());

            client.setCallback(new MqttCallback() {
                public void messageArrived(String topic, MqttMessage message) {
                    // Not used Here
                }

                public void connectionLost(Throwable cause) {
                    errorln(clientId + " Connectionlost! cause:" + cause.getMessage());
                }

                public void deliveryComplete(IMqttDeliveryToken token) {
                    // Until the delivery is completed, messages with QoS 1 or 2 are retained from the client
                    // Delivery for a message is completed when all acknowledgments have been received
                    // When the callback returns from deliveryComplete to the main thread, the client removes the retained messages with QoS 1 or 2.
                    if (token.isComplete()) {
                       logln(clientId + " Message delivered - Thread PID: " + Thread.currentThread().getId());
                    }
                }
            });

        } catch (MqttException me ) {
            errorln("ERROR IN MQTT CONNECTION");
            errorln("reason " + me.getReasonCode());
            errorln("msg " + me.getMessage());
            errorln("loc " + me.getLocalizedMessage());
            errorln("cause " + me.getCause());
            errorln("excep " + me);
            me.printStackTrace();
        }

        publishing = new SensorDataPublishingThread(this);
        publishing.start();
    }

    public void turnOffPollutionProcessing(){
        processing.stopMeGently();
    }

    public void turnOffPollutionPublishing(){

        publishing.stopMeGently();
        try{
            if (client.isConnected())
                client.disconnect();
            logln("Publisher " + clientId + " disconnected - Thread PID: " + Thread.currentThread().getId());
        } catch (MqttException me) {
            errorln("ERROR IN MQTT DISCONNECTION");
            errorln("reason " + me.getReasonCode());
            errorln("msg " + me.getMessage());
            errorln("loc " + me.getLocalizedMessage());
            errorln("cause " + me.getCause());
            errorln("excep " + me);
            me.printStackTrace();
        }
    }
}
