package robot;

import utils.exceptions.RegistrationFailureException;
import utils.exceptions.RemovalFailureException;

import java.util.Scanner;

import static utils.Printer.*;
import static utils.Utils.generateRobotID;
import static utils.Utils.ADMIN_SERVER_ADDRESS;

public class RobotMainCLI {

    public static void main(String[] args) {

        Scanner s = new Scanner(System.in);
        /* System.out.println("Insert ID of new cleaning robot: ");
        int id = s.nextInt();
        */
        String id = generateRobotID();
        logln("ID of new cleaning robot: " + id);
        int listeningPort = 2; /* s.nextInt(); */
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

        // ... qua avvierò i vari thread nel robot ...

        System.out.println("Insert:\n\t\"quit\" to remove the robot from the smart city");
        while (true){
            if (s.next().equals("quit")){
                // TODO ... complete any operation at the mechanic
                // TODO ... notify the other robots of Greenfield

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
    }
}
