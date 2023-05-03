package robot;

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
        System.out.println("ID of new cleaning robot: " + id);
        int listeningPort = 2; /* s.nextInt(); */
        System.out.println("listening port of new cleaning robot: " + listeningPort);

        Robot r = new Robot(id, listeningPort, ADMIN_SERVER_ADDRESS);
        r.registration();
    }
}
