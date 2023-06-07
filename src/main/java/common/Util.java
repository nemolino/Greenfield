package common;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Random;

public final class Util {

    public static final String ADMIN_SERVER_ADDRESS = "http://localhost:1337";
    public static final String MQTT_BROKER_ADDRESS = "tcp://localhost:1883";

    public static String getSomeID() {
        return String.format("%05d", new Random().nextInt(100000));
    }

    public static int getSomePort(){
        int port;
        try (ServerSocket socket = new ServerSocket(0)) {
            socket.setReuseAddress(true);
            port = socket.getLocalPort();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (port > 0)
            return port;
        throw new RuntimeException("Could not find a free port");
    }
}

