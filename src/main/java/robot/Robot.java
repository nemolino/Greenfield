package robot;

import static utils.Printer.*;

import admin_server.RobotPosition;
import admin_server.RobotRepresentation;
import admin_server.services.RegistrationResponse;
import com.google.gson.Gson;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import utils.exceptions.RegistrationFailureException;
import utils.exceptions.RemovalFailureException;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

public class Robot {

    private String id;
    private int listeningPort;
    private String adminServerAddress;
    private RobotPosition position;
    private List<RobotRepresentation> otherRobots;

    public Robot(String id, int listeningPort, String adminServerAddress) {
        this.id = id;
        this.listeningPort = listeningPort;
        this.adminServerAddress = adminServerAddress;
    }

    public RobotPosition getPosition() {
        return position;
    }

    public List<RobotRepresentation> getOtherRobots() {
        return otherRobots;
    }

    public void registration() throws RegistrationFailureException {

        Client client = Client.create();
        ClientResponse clientResponse = null;

        String postPath = "/robots/register";
        clientResponse = postRegistrationRequest(client, adminServerAddress + postPath,
                new RobotRepresentation(id, "localhost", listeningPort));

        //logln("Registration response: " + clientResponse.toString());

        if (clientResponse.getStatus() == 200) {

            RegistrationResponse r = clientResponse.getEntity(RegistrationResponse.class);
            this.position = r.getPosition();
            this.otherRobots = r.getOtherRobots();
            if (this.otherRobots == null)
                this.otherRobots = new ArrayList<>();
        } else {
            throw new RegistrationFailureException("Registration failure");
        }
    }

    public void removal() throws RemovalFailureException {

        Client client = Client.create();
        ClientResponse clientResponse = null;

        String postPath = "/robots/remove";
        clientResponse = deleteRemovalRequest(client,adminServerAddress + postPath, this.id);

        logln("Removal response: " + clientResponse.toString());

        if (clientResponse.getStatus() != 200)
            throw new RemovalFailureException("Removal failure");
    }

    public static ClientResponse postRegistrationRequest(Client client, String url, RobotRepresentation req) {
        WebResource webResource = client.resource(url);
        String input = new Gson().toJson(req);
        try {
            return webResource.type("application/json").post(ClientResponse.class, input);
        } catch (ClientHandlerException e) {
            errorln("Server non disponibile");
            return null;
        }
    }

    public static ClientResponse deleteRemovalRequest(Client client, String url, String id) {
        WebResource webResource = client.resource(url);
        String input = id;
        try {
            return webResource.type("application/json").delete(ClientResponse.class, input);
        } catch (ClientHandlerException e) {
            errorln("Server non disponibile");
            return null;
        }
    }
}
