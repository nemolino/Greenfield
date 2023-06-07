package robot;

import static common.Util.*;
import static common.printer.Printer.cli;

public class Main {

    public static void main(String[] args) {

        String id;
        int listeningPort;

        if (args.length == 2) {
            id = args[0];
            listeningPort = Integer.parseInt(args[1]);
        } else {
            id = getSomeID();
            while (true) {
                try {
                    listeningPort = getSomePort();
                } catch (RuntimeException e) {
                    continue;
                }
                break;
            }
        }

        cli("robotID: " + id + " , port: " + listeningPort);

        Robot r = new Robot(id, listeningPort, ADMIN_SERVER_ADDRESS);
        r.robotMain();
    }
}
