package robot;

import static common.Printer.*;
import static common.Configuration.ADMIN_SERVER_ADDRESS;

public class Main {

    public static void main(String[] args) {

        String id = args[0]; //generateRobotID();
        int listeningPort = Integer.parseInt(args[1]);

        cliln("robotID: " + id + " , port: " + listeningPort);

        Robot r = new Robot(id, listeningPort, ADMIN_SERVER_ADDRESS);
        try{
            r.robotMain();
        }
        catch (Exception e){
            errorln("BAD ERROR - some exception has not been caught");
        }
    }
}
