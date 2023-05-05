package robot;

import utils.exceptions.RegistrationFailureException;

import static utils.Printer.*;
import static utils.Utils.generateRobotID;
import static utils.Utils.ADMIN_SERVER_ADDRESS;

public class RobotMainCLI {

    public static void main(String[] args) {

        /*
        Scanner s = new Scanner(System.in);
        System.out.println("Insert ID of new cleaning robot: ");
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

        logln("Position: " + r.getPosition().toString());
        logln("Other robots: " + r.getOtherRobots().toString());
    }
}
