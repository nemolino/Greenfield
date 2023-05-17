package robot;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import robot.gRPC_services.*;
import utils.exceptions.RegistrationFailureException;
import utils.exceptions.RemovalFailureException;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import static utils.Printer.*;
import static utils.Utils.ADMIN_SERVER_ADDRESS;

public class RobotMainCLI {

    public static void main(String[] args) {

        Scanner s = new Scanner(System.in);

        String id = args[0]; //generateRobotID();
        int listeningPort = Integer.valueOf(args[1]);
        cliln("robotID: " + id + " , port: " + listeningPort);

        Robot r = new Robot(id, listeningPort, ADMIN_SERVER_ADDRESS);
        try {
            r.registration();
        } catch (RegistrationFailureException e) {
            errorln(e.toString());
            System.exit(-1);
        }
        success("Registration of this robot to AdminServer succeded");
        logln(" --> Position: " + r.getPosition() + " , otherRobots: " + r.getOtherRobots());

        // starts acquiring data from its pollution sensor
        r.turnOnPollutionProcessing();

        // setting up gRPC server
        Server serverGRPC = ServerBuilder.forPort(r.getListeningPort())
                .addService(new PresentationServiceImpl(r))
                .addService(new LeavingServiceImpl(r))
                .addService(new MaintenanceServiceImpl(r))
                .build();
        try {
            serverGRPC.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        logln("... gRPC server started!");
        
        // if there are other robots in Greenfield, presents itself to the other ones by sending them its position
        r.presentation();

        // connects as a publisher to the MQTT topic of its district
        r.turnOnPollutionPublishing();

        // maintenance
        r.turnOnMaintenance();

        cliln("Insert:\t\t\"quit\" to remove the robot from the smart city");
        while (true){
            String input = s.next();
            if (input.equals("quit")){

                // TODO ... complete any operation at the mechanic

                // notify the other robots of Greenfield
                r.leaving(r.getId());

                // (my choice)
                r.turnOffPollutionPublishing();
                r.turnOffPollutionProcessing();

                // request the Administrator Server to leave Greenfield
                try {
                    r.removal(r.getId());
                } catch (RemovalFailureException e) {
                    errorln(e.toString());
                    System.exit(-1);
                }
                successln("Removal of this robot from AdminServer succeded");

                try {
                    // waiting 5 seconds for all the calls to be propagated
                    serverGRPC.awaitTermination(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                serverGRPC.shutdown();
                break;
            }

            // fix command
        }
    }
}
