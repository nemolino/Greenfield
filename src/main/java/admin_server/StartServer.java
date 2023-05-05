package admin_server;

import static utils.Printer.*;

import com.sun.jersey.api.container.httpserver.HttpServerFactory;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;

import static utils.Utils.ADMIN_SERVER_ADDRESS;

public class StartServer {

    public static void main(String[] args) throws IOException {

        HttpServer server = HttpServerFactory.create(ADMIN_SERVER_ADDRESS + "/");
        server.start();

        warnln("Server running!");
        warnln("Server started on: " + ADMIN_SERVER_ADDRESS);

        warnln("Hit return to stop...");
        System.in.read();
        warnln("Stopping server");
        server.stop(0);
        warnln("Server stopped");
    }
}
