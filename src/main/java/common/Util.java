package common;

import java.util.Random;

public class Util {

    public static String generateRobotID() {
        return String.format("%05d", new Random().nextInt(100000));
    }
}
