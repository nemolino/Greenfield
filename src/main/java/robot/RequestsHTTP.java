package robot;

import admin_server.REST_response_formats.RobotRepresentation;
import com.google.gson.Gson;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import static utils.Printer.errorln;

public class RequestsHTTP {

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
