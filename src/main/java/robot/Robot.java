package robot;

import common.District;
import common.Position;
import admin_server.REST_response_formats.RobotRepresentation;
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
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import robot.maintenance.Maintenance;
import robot.network.LeavingServiceImpl;
import robot.maintenance.MaintenanceServiceImpl;
import robot.network.PresentationServiceImpl;
import utils.exceptions.RegistrationFailureException;
import utils.exceptions.RemovalFailureException;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static robot.network.RequestsHTTP.*;
import static utils.Printer.*;

public class Robot {

    private final String id;
    private final int listeningPort;
    private final String adminServerAddress;

    private Position position;
    private District district;
    private List<RobotRepresentation> otherRobots;
    private final Object otherRobotsLock = new Object();

    private Server serverGRPC;
    private Maintenance m;

    public Robot(String id, int listeningPort, String adminServerAddress) {
        this.id = id;
        this.listeningPort = listeningPort;
        this.adminServerAddress = adminServerAddress;
    }

    public void robotMain() {

        /* -------------------------------------------------- registration of this robot to Administration Server --- */
        try {
            registration();
        } catch (RegistrationFailureException e) {
            errorln(e.toString());
            return;
        }
        successln("... registration to AdminServer succeeded " +
                "--> Position: " + position + " , otherRobots: " + otherRobots);

        /* --- (thread start) ------------------------------------ starting to get data from air pollution sensor --- */
        //PollutionMonitoring p = new PollutionMonitoring(this);
        //p.turnOnPollutionProcessing();

        /* ------------------------------------------------------------------------------- setting up gRPC server --- */
        try {
            startServerGRPC();
        } catch (Exception e) {
            errorln(e.toString());
            return;
        }
        logln("... gRPC server on");


        /* -------------------------------------- presentation to the other robots already in Greenfield via gRPC --- */
        presentation();

        /* --- (thread start) ----------- starting to publish pollution levels into the MQTT topic of my district --- */
        //p.turnOnPollutionPublishing();

        /* --- (thread start) ---------- starting to organize with the other robots the access to the maintenance --- */
        //m = new Maintenance(this);
        //m.turnOnMaintenance();

        /* ------------------------------------------------------------------- CLI to give commands to this robot --- */
        Scanner s = new Scanner(System.in);
        String menu = "Insert:\t\t\"quit\" to remove this robot from the smart city\n" +
                           "\t\t\t \"fix\" to request the maintenance for this robot";
        cliln(menu);
        while (true) {
            String input = s.next();

            if (input.equals("quit")) {

                /* --- (thread stop in a blocking way) ------------------------ completing maintenance operations --- */
                //m.turnOffMaintenance();

                /* ------------------------------ notifying the other robots that I'm leaving Greenfield via gRPC --- */
                leaving(id);

                /* --- (thread stop) -------------------------------------- finishing pollution levels publishing --- */
                //p.turnOffPollutionPublishing();

                /* --- (thread stop) ---------------------------- finishing to get data from air pollution sensor --- */
                //p.turnOffPollutionProcessing();

                /* --------------------------------------------- removal of this robot from Administration Server --- */
                try {
                    removal(id);
                } catch (RemovalFailureException e) {
                    errorln(e.toString());
                    return;
                }
                /*catch (Exception e) {
                    e.printStackTrace();
                    errorln("BAD ERROR in removal from AdminServer");
                    errorln(e.toString());
                    return;
                }*/
                successln("... removal of this robot from AdminServer succeeded");

                /* -------------------------------------------------------------------- shutting down gRPC server --- */
                try {
                    shutdownServerGRPC();
                } catch (Exception e) {
                    errorln(e.toString());
                    return;
                }
                logln("... gRPC server off");
                break;
            } else if (input.equals("fix")) {
                logln("... fix ...");
            } else
                errorln("INVALID INPUT");
        }
    }

    // ------------------------------------------------------------------------------------- useful Getters & Setters ---

    public String getId() {
        return id;
    }
    public Object getOtherRobotsLock() {
        return otherRobotsLock;
    }
    public List<RobotRepresentation> getOtherRobots() {
        return otherRobots;
    }
    public District getDistrict() {
        return district;
    }
    public Maintenance getMaintenance() {
        return m;
    }

    // --------------------------------------------------------------------------------------- gRPC Server utilities ---

    private void startServerGRPC() {
        try {
            serverGRPC = ServerBuilder.forPort(listeningPort)
                    .addService(new PresentationServiceImpl(this))
                    .addService(new LeavingServiceImpl(this))
                    .addService(new MaintenanceServiceImpl(this))
                    .build();
            serverGRPC.start();
        } catch (Exception e) {
            throw new RuntimeException("Unable to start gRPC server properly");
        }
    }

    /* ?? calls propagation */
    private void shutdownServerGRPC() {
        try {
            // waiting 5 seconds for all the calls to be propagated
            serverGRPC.awaitTermination(5, TimeUnit.SECONDS);
            serverGRPC.shutdown();
        } catch (Exception e) {
            throw new RuntimeException("Unable to shutdown gRPC server properly");
        }
    }

    // ------------------------------------------------------------------- Administration Server REST services calls ---

    // registration of this robot to Administration Server
    private void registration() throws RegistrationFailureException {

        Client client = Client.create();
        ClientResponse response = postRegistrationRequest(client, adminServerAddress + "/robots/register",
                new RobotRepresentation(id, "localhost", listeningPort));

        if (response == null)
            throw new RegistrationFailureException("Server is unavailable");

        //logln("Registration response: " + clientResponse.toString());

        if (response.getStatus() == 200) {

            RegistrationResponse r = response.getEntity(RegistrationResponse.class);
            position = r.getPosition();
            district = position.getDistrict();
            otherRobots = r.getOtherRobots();
            if (otherRobots == null)
                otherRobots = new ArrayList<>();
        } else
            throw new RegistrationFailureException("Registration failure");
    }

    // removal of a robot by its id from Administration Server
    private void removal(String id) throws RemovalFailureException {

        Client client = Client.create();
        ClientResponse response = deleteRemovalRequest(client, adminServerAddress + "/robots/remove", id);

        if (response == null)
            throw new RemovalFailureException("Server is unavailable");

        //logln("Removal response: " + clientResponse.toString());

        if (response.getStatus() != 200)
            throw new RemovalFailureException("Removal failure");
    }

    // --------------------------------------------------------------------------- robot network management via gRPC ---

    // presentation to the other robots already in Greenfield via gRPC
    private void presentation() {

        List<RobotRepresentation> otherRobotsCopy;
        synchronized (otherRobotsLock) {
            otherRobotsCopy = new ArrayList<>(otherRobots);
        }

        for (RobotRepresentation x : otherRobotsCopy) {

            final ManagedChannel channel = ManagedChannelBuilder
                    .forTarget("localhost:" + x.getPort()).usePlaintext().build();

            PresentationServiceStub stub = PresentationServiceGrpc.newStub(channel);
            PresentationRequest request = PresentationRequest.newBuilder()
                    .setId(id)
                    .setPort(listeningPort)
                    .setPosition(PresentationRequest.Position.newBuilder()
                            .setX(position.getX())
                            .setY(position.getY())
                            .build())
                    .build();

            stub.presentation(request, new StreamObserver<PresentationResponse>() {

                public void onNext(PresentationResponse response) {
                    successln("Presentation to " + x + " succeeded");
                }

                public void onError(Throwable throwable) { removeDeadRobot(x); }

                public void onCompleted() {
                    channel.shutdownNow();
                }
            });
        }
    }

    // notifying other robots that the robot with a certain id is leaving Greenfield via gRPC
    private void leaving(String leavingRobotId) {

        List<RobotRepresentation> others;
        synchronized (otherRobotsLock) {
            others = new ArrayList<>(otherRobots);
        }

        for (RobotRepresentation x : others) {

            final ManagedChannel channel = ManagedChannelBuilder
                    .forTarget("localhost:" + x.getPort()).usePlaintext().build();

            LeavingServiceStub stub = LeavingServiceGrpc.newStub(channel);
            LeavingRequest request;
            if (Objects.equals(leavingRobotId, id))
                request = LeavingRequest.newBuilder().setId(leavingRobotId).build();
            else
                request = LeavingRequest.newBuilder().setId(leavingRobotId).setSender(id).build();

            stub.leaving(request, new StreamObserver<LeavingResponse>() {

                public void onNext(LeavingResponse response) {
                    successln("I successfully notified " + x + " that " + leavingRobotId + " is leaving");
                }

                public void onError(Throwable throwable) { removeDeadRobot(x); }

                public void onCompleted() {
                    channel.shutdownNow();
                }
            });
        }

    }

    public void removeDeadRobot(RobotRepresentation x) {

        error("... notifying otherRobots that " + x + " is dead | ");

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
            errorln("... removed " + x + " also from AdminServer");
        } catch (RemovalFailureException e) {
            warnln("... someone already removed " + x + " from AdminServer");
            errorln(e.toString());
        }

        /* --- update maintenance pending requests */
        //m.getThread().updatePendingMaintenanceRequests(x);
        /* --- */

        // notifying other robots that x left the city (recursive call)
        leaving(x.getId());
    }
}
