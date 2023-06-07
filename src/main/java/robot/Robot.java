package robot;

import admin_server.rest_response_formats.RobotRepresentation;
import common.District;
import common.Position;
import common.printer.Type;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import robot.maintenance.Maintenance;
import robot.maintenance.MaintenanceServiceImpl;
import robot.network.*;
import robot.pollution.Pollution;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static common.printer.Printer.*;

public class Robot {

    private final String id;
    private final int listeningPort;
    private final String adminServerAddress;

    private Position position;
    private District district;
    private List<RobotRepresentation> otherRobots;
    private final Object otherRobotsLock = new Object();

    private Network n;
    private Maintenance m;

    public Robot(String id, int listeningPort, String adminServerAddress) {
        this.id = id;
        this.listeningPort = listeningPort;
        this.adminServerAddress = adminServerAddress;
    }

    public void robotMain() {

        n = new Network(this);

        /* -------------------------------------------------- registration of this robot to Administration Server --- */
        try {
            n.registration();
        } catch (Exception e) {
            error(Type.N, "... registration to AdminServer failed - " + e.getMessage());
            return;
        }
        log(Type.N, "... registration to AdminServer succeeded - " +
                "position: " + position + " , otherRobots: " + otherRobots);

        /* --- (thread start) ------------------------------------ starting to get data from air pollution sensor --- */
        Pollution p = new Pollution(this);
        p.turnOnPollutionProcessing();

        /* ------------------------------------------------------------------------------- setting up gRPC server --- */
        Server serverGRPC;
        try {
            serverGRPC = startServerGRPC();
        } catch (Exception e) {
            error(Type.B, "... " + e.getMessage());
            return;
        }
        log(Type.B, "... gRPC server on");


        /* -------------------------------------- presentation to the other robots already in Greenfield via gRPC --- */
        n.presentation();

        /* --- (thread start) ----------- starting to publish pollution levels into the MQTT topic of my district --- */
        p.turnOnPollutionPublishing();

        /* --- (thread start) ---------- starting to organize with the other robots the access to the maintenance --- */
        m = new Maintenance(this);
        m.turnOnMaintenance();
        HeartbeatThread h = new HeartbeatThread(this);
        h.start();

        /* ------------------------------------------------------------------- CLI to give commands to this robot --- */

        Scanner s = new Scanner(System.in);
        cli("Insert:\t\t\"quit\" to remove this robot from the smart city   ");
        cli("\t\t\t \"fix\" to request the maintenance for this robot  ");

        while (true) {
            String input = s.next();

            if (input.equals("quit")) {
                log(Type.B, "... quit command received");

                /* --- (thread stop in a blocking way) ------------------------ completing maintenance operations --- */
                m.turnOffMaintenance();
                h.stopMeGently();

                /* ------------------------------ notifying the other robots that I'm leaving Greenfield via gRPC --- */
                n.leaving(id);

                /* --- (thread stop) -------------------------------------- finishing pollution levels publishing --- */
                p.turnOffPollutionPublishing();

                /* --- (thread stop) ---------------------------- finishing to get data from air pollution sensor --- */
                p.turnOffPollutionProcessing();

                /* --------------------------------------------- removal of this robot from Administration Server --- */
                try {
                    n.removal(id);
                    log(Type.N, "... removal of this robot from AdminServer succeeded");
                } catch (Exception e) {
                    error(Type.N, "... removal of this robot from AdminServer failed - " + e.getMessage());
                }

                /* -------------------------------------------------------------------- shutting down gRPC server --- */
                try {
                    shutdownServerGRPC(serverGRPC);
                } catch (Exception e) {
                    error(Type.B, "... " + e.getMessage());
                    return;
                }
                log(Type.B, "... gRPC server off");
                break;
            } else if (input.equals("fix")) {
                log(Type.B, "... fix command received");
                m.fixCommand();
            } else
                error(Type.B, "... invalid command received");
        }
    }

    // -------------------------------------------------------------------------------------------- getters, setters ---

    public String getId() {
        return id;
    }

    public int getListeningPort() {
        return listeningPort;
    }

    public String getAdminServerAddress() {
        return adminServerAddress;
    }

    public Position getPosition() {
        return position;
    }

    public District getDistrict() {
        return district;
    }

    public List<RobotRepresentation> getOtherRobotsCopy() {
        synchronized (otherRobotsLock) {
            return new ArrayList<>(otherRobots);
        }
    }

    public Network network() {
        return n;
    }

    public Maintenance maintenance() {
        return m;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public void setDistrict(District district) {
        this.district = district;
    }

    public void setOtherRobots(List<RobotRepresentation> otherRobots) {
        this.otherRobots = otherRobots;
    }

    // ---------------------------------------------------------------------------------------- otherRobots updating ---

    // add robot x to otherRobots, if it does not contain already a robot with the same id as x
    public void addToOtherRobots(RobotRepresentation x) {
        synchronized (otherRobotsLock) {
            for (RobotRepresentation y : otherRobots) {
                if (Objects.equals(y.getId(), x.getId()))
                    return;
            }
            otherRobots.add(x);
        }
    }

    // remove from otherRobots a robot by its id, if present
    public void removeFromOtherRobotsById(String id) {
        synchronized (otherRobotsLock) {
            for (RobotRepresentation y : otherRobots)
                if (Objects.equals(y.getId(), id)) {
                    otherRobots.remove(y);
                    return;
                }
        }
    }

    // --------------------------------------------------------------------------------------- gRPC Server utilities ---

    private Server startServerGRPC() {
        try {
            Logger grpcExecutorLogger = Logger.getLogger("io.grpc.internal.SerializingExecutor");
            grpcExecutorLogger.setLevel(Level.OFF);
            Server s = ServerBuilder.forPort(listeningPort)
                    .addService(new PresentationServiceImpl(this))
                    .addService(new LeavingServiceImpl(this))
                    .addService(new MaintenanceServiceImpl(this))
                    .addService(new HeartbeatServiceImpl())
                    .build();
            s.start();
            return s;
        } catch (Exception e) {
            throw new RuntimeException("Unable to start gRPC server properly");
        }
    }

    private void shutdownServerGRPC(Server s) {
        try {
            // waiting 3 seconds for all the calls to be propagated
            s.awaitTermination(3, TimeUnit.SECONDS);
            s.shutdown();
        } catch (Exception e) {
            throw new RuntimeException("Unable to shutdown gRPC server properly");
        }
    }
}
