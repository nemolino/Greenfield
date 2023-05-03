package admin_server;

import com.sun.jersey.api.container.httpserver.HttpServerFactory;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;

import static utils.Utils.ADMIN_SERVER_ADDRESS;

public class StartServer {

    public static void main(String[] args) throws IOException {

        HttpServer server = HttpServerFactory.create(ADMIN_SERVER_ADDRESS+"/");
        server.start();

        System.out.println("Server running!");
        System.out.println("Server started on: " + ADMIN_SERVER_ADDRESS);

        System.out.println("Hit return to stop...");
        System.in.read();
        System.out.println("Stopping server");
        server.stop(0);
        System.out.println("Server stopped");
    }
}
