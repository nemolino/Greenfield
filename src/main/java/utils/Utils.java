package utils;

import java.util.Random;

public final class Utils {

    public static String ADMIN_SERVER_ADDRESS = "http://localhost:1337";
    public static String generateRobotID(){
        return String.format("%05d", new Random().nextInt(100000));
    }

}
