package robot;

import admin_server.RobotRepresentation;
import com.google.gson.Gson;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class Robot {

    private String id;
    private int listeningPort;
    private String adminServerAddress;

    public Robot(String id, int listeningPort, String adminServerAddress){
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
                new RobotRepresentation(id, "localhost", listeningPort));

        System.out.println(clientResponse.toString());
    }

    public static ClientResponse postRegistrationRequest(Client client, String url, RobotRepresentation req){
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
