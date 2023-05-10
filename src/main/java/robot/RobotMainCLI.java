package robot;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import utils.exceptions.RegistrationFailureException;
import utils.exceptions.RemovalFailureException;

import java.io.IOException;
import java.util.Scanner;

import static utils.Printer.*;
import static utils.Utils.generateRobotID;
import static utils.Utils.ADMIN_SERVER_ADDRESS;

public class RobotMainCLI {

    public static void main(String[] args) {

        Scanner s = new Scanner(System.in);

        String id = args[0]; //generateRobotID();
        logln("ID of new cleaning robot: " + id);
        int listeningPort = Integer.valueOf(args[1]);
        logln("listening port of new cleaning robot: " + listeningPort);

        Robot r = new Robot(id, listeningPort, ADMIN_SERVER_ADDRESS);
        try {
            r.registration();
        } catch (RegistrationFailureException e) {
            errorln(e.toString());
            System.exit(-1);
        }
        successln("Registration succeded");
        logln("Position: " + r.getPosition().toString());
        logln("Other robots: " + r.getOtherRobots().toString());

        // ... starts acquiring data from its pollution sensor

        // setting up gRPC server
        Server server = ServerBuilder.forPort(r.getListeningPort())
                .addService(new PresentationServiceImpl(r))
                .addService(new LeavingServiceImpl(r))
                .build();
        try {
            server.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        successln("gRPC server started!");
        
        // if there are other robots in Greenfield, presents itself to the other ones by sending them its position
        r.presentation();

        // ... connects as a publisher to the MQTT topic of its district

        System.out.println("Insert:\t\t\"quit\" to remove the robot from the smart city");
        while (true){
            if (s.next().equals("quit")){

                // TODO ... complete any operation at the mechanic

                // notify the other robots of Greenfield
                r.leaving();

                // request the Administrator Server to leave Greenfield
                try {
                    r.removal();
                } catch (RemovalFailureException e) {
                    errorln(e.toString());
                    System.exit(-1);
                }
                successln("Removal succeded");
                break;
            }
        }

        try {
            server.awaitTermination();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
