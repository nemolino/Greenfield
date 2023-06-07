package admin_client;

import admin_server.rest_response_formats.ListRobotsResponse;
import admin_server.rest_response_formats.RobotRepresentation;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import common.printer.Type;

import java.util.List;
import java.util.Scanner;

import static common.Util.ADMIN_SERVER_ADDRESS;
import static common.printer.Printer.*;

public class AdminClientCLI {

    public static void main(String[] args) {

        Scanner s = new Scanner(System.in);
        String menu =
                "Insert:\t\t\"a\" to get the list of the cleaning robots currently located in Greenfield\n" +
                        "\t\t\t\"b\" to get the average of the last n air pollution levels sent to the server by a given robot\n" +
                        "\t\t\t\"c\" to get the average of the air pollution levels sent by all the robots to the server and occurred from timestamps t1 and t2\n" +
                        "\t\t\t\"q\" to quit\n";
        boolean quit = false;

        Client client = Client.create();
        ClientResponse clientResponse;

        while (!quit) {
            cli(menu);
            logInline(Type.B, "Input: ");
            String input = s.next();
            log(Type.B, "");

            switch (input) {

                case "a":
                    clientResponse = getRequest(client, ADMIN_SERVER_ADDRESS + "/query/list_all_robots");
                    if (clientResponse == null)
                        error(Type.Q, "query1 FAILED, server is unavailable");
                    else if (clientResponse.getStatus() == 200) {
                        ListRobotsResponse response = clientResponse.getEntity(ListRobotsResponse.class);
                        List<RobotRepresentation> r = response.getRobots();
                        if (r != null) {
                            logInline(Type.Q, "Robots currently in Greenfield :\t");
                            for (RobotRepresentation x : r)
                                logInline(Type.Q, "Robot with ID " + x.getId() + " at address " + x.getAddress() + " and port " + x.getPort() + "\n\t\t\t\t\t\t\t\t\t");
                        } else log(Type.Q, "No robots currently in Greenfield");
                    } else error(Type.Q, "query1 FAILED, arguments are not correct");
                    break;

                case "b":
                    logInline(Type.B, "Insert robot ID: ");
                    String id = s.next();
                    String n;
                    while (true) {
                        logInline(Type.B, "Insert n > 0: ");
                        n = s.next();
                        try {
                            Integer.parseInt(n);
                        } catch (Exception e) {
                            continue;
                        }
                        break;
                    }
                    log(Type.B, "");
                    clientResponse = getRequest(client, ADMIN_SERVER_ADDRESS + "/query/avg_last_n_of_id/" + id + "/" + n);
                    if (clientResponse == null)
                        error(Type.Q, "query2 FAILED, server is unavailable");
                    else if (clientResponse.getStatus() == 200) {
                        String response = clientResponse.getEntity(String.class);
                        log(Type.Q, "Response: " + response);
                    } else error(Type.Q, "query2 FAILED, arguments are not correct");
                    break;

                case "c":
                    String t1, t2;
                    while (true) {
                        logInline(Type.B, "Insert t1: ");
                        t1 = s.next();
                        try {
                            Long.parseLong(t1);
                        } catch (Exception e) {
                            continue;
                        }
                        break;
                    }
                    while (true) {
                        logInline(Type.B, "Insert t2: ");
                        t2 = s.next();
                        try {
                            Long.parseLong(t2);
                        } catch (Exception e) {
                            continue;
                        }
                        break;
                    }
                    log(Type.B, "");
                    clientResponse = getRequest(client, ADMIN_SERVER_ADDRESS + "/query/avg_between_t1_and_t2/" + t1 + "/" + t2);
                    if (clientResponse == null)
                        error(Type.Q, "query3 FAILED, server is unavailable");
                    else if (clientResponse.getStatus() == 200) {
                        String response = clientResponse.getEntity(String.class);
                        log(Type.Q, "Response: " + response);
                    } else error(Type.Q, "query3 FAILED, arguments are not correct");
                    break;

                case "q":
                    quit = true;
                    log(Type.Q, "Goodbye");
                    break;

                default:
                    error(Type.Q, "INVALID INPUT");
                    break;
            }
            log(Type.B, "");
        }
    }

    private static ClientResponse getRequest(Client client, String url) {
        WebResource webResource = client.resource(url);
        try {
            return webResource.type("application/json").get(ClientResponse.class);
        } catch (ClientHandlerException e) {
            return null;
        }
    }
}