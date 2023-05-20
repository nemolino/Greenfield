package admin_client;

import admin_server.REST_response_formats.ListRobotsResponse;
import admin_server.REST_response_formats.RobotRepresentation;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import java.util.List;
import java.util.Scanner;

import static utils.Printer.*;
import static common.Configuration.ADMIN_SERVER_ADDRESS;

public class AdminClientCLI {

    public static void main(String[] args) {

        Scanner s = new Scanner(System.in);
        cliln("HELLO AdminClient\n");
        String menu =
                "Insert:\t\t\"a\" to get the list of the cleaning robots currently located in Greenfield\n" +
                     "\t\t\t\"b\" to get the average of the last n air pollution levels sent to the server by a given (?CONNECTED?) robot\n" +
                     "\t\t\t\"c\" to get the average of the air pollution levels sent by all the (?CONNECTED?) robots to the server and occurred from timestamps t1 and t2\n" +
                     "\t\t\t\"q\" to quit\n";
        boolean quit = false;

        Client client = Client.create();;
        ClientResponse clientResponse;

        while (!quit){
            cliln(menu);
            System.out.print("Input: ");
            String input = s.next();
            System.out.println();

            switch (input) {

                case "a":
                    clientResponse = getRequest(client, ADMIN_SERVER_ADDRESS + "/query/list_all_robots");
                    //logln("query1 response: " + clientResponse.toString());
                    if (clientResponse.getStatus() == 200) {
                        ListRobotsResponse response = clientResponse.getEntity(ListRobotsResponse.class);
                        List<RobotRepresentation> r = response.getRobots();
                        if (r != null){
                            log("Robots currently in Greenfield :\t");
                            for (RobotRepresentation x : r)
                                log("Robot with ID " + x.getId() + " at address " + x.getAddress() + " and port " + x.getPort() + "\n\t\t\t\t\t\t\t\t\t");
                        }
                        else logln("No robots currently in Greenfield");
                    }
                    else
                        errorln("query1 FAILURE");
                    break;

                case "b":
                    System.out.print("Insert robot ID: ");
                    String id = s.next();
                    System.out.print("Insert n > 0: ");
                    String n = String.valueOf(s.nextInt());
                    System.out.println();
                    clientResponse = getRequest(client, ADMIN_SERVER_ADDRESS + "/query/avg_last_n_of_id/" + id + "/" + n);
                    //logln("query2 response: " + clientResponse.toString());
                    if (clientResponse.getStatus() == 200) {
                        String response = clientResponse.getEntity(String.class);
                        logln("Response: " + response);
                    }
                    else
                        errorln("query2 FAILURE");
                    break;

                case "c":
                    System.out.print("Insert t1: ");
                    String t1 = String.valueOf(s.nextLong());
                    System.out.print("Insert t2: ");
                    String t2 = String.valueOf(s.nextLong());
                    System.out.println();
                    clientResponse = getRequest(client, ADMIN_SERVER_ADDRESS + "/query/avg_between_t1_and_t2/" + t1 + "/" + t2);
                    //logln("query3 response: " + clientResponse.toString());
                    if (clientResponse.getStatus() == 200) {
                        String response = clientResponse.getEntity(String.class);
                        logln("Response: " + response);
                    }
                    else
                        errorln("query3 FAILURE");
                    break;

                case "q":
                    logln("GOODBYE AdminClient");
                    quit = true;
                    break;

                default:
                    errorln("INVALID INPUT");
                    break;
            }
            System.out.println();
        }


    }

    public static ClientResponse getRequest(Client client, String url) {
        WebResource webResource = client.resource(url);
        try {
            return webResource.type("application/json").get(ClientResponse.class);
        } catch (ClientHandlerException e) {
            errorln("Server non disponibile");
            return null;
        }
    }
}