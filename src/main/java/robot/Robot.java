package robot;

import admin_server.beans.RobotRepr;
import com.google.gson.Gson;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import java.util.Scanner;

public class Robot {

    public static void main(String[] args) {

        Scanner s = new Scanner(System.in);

        System.out.println("Insert ID of new cleaning robot: ");
        int id = s.nextInt();
        System.out.println("Insert listening port of new cleaning robot: ");
        int listeningPort = s.nextInt();

        Robot r = new Robot(id, listeningPort, "http://localhost:1337");
        r.registration();
    }

    private int id;
    private int listeningPort;
    private String adminServerAddress;

    public Robot(int id, int listeningPort, String adminServerAddress){
        this.id = id;
        this.listeningPort = listeningPort;
        this.adminServerAddress = adminServerAddress;
    }

    public void registration() {

        Client client = Client.create();
        ClientResponse clientResponse = null;

        // registration request
        String postPath = "/robots/register";
        clientResponse = postRegistrationRequest(client,
                adminServerAddress + postPath,
                new RobotRepr(id, "localhost", listeningPort));

        System.out.println(clientResponse.toString());
    }

    public static ClientResponse postRegistrationRequest(Client client, String url, RobotRepr req){
        WebResource webResource = client.resource(url);
        String input = new Gson().toJson(req);
        try {
            return webResource.type("application/json").post(ClientResponse.class, input);
        } catch (ClientHandlerException e) {
            System.out.println("Server non disponibile");
            return null;
        }
    }
}
