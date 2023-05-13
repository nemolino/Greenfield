package admin_server.REST_services;

import admin_server.REST_response_formats.ListRobotsResponse;
import admin_server.RobotRepresentation;
import admin_server.SmartCity;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

import static utils.Printer.*;

@Path("query")
public class AdminClientServices {

    @Path("list")
    @GET
    @Produces({"application/json", "application/xml"})
    public Response robotsList() {

        logln("Query1 : AdminClient wants the list of all robots");

        List<RobotRepresentation> robots = SmartCity.getInstance().getRobotsList();
        successln("Query1 succeeded\n");
        return Response.ok(new ListRobotsResponse(robots)).build();
    }

    @Path("avg_last_n_of_id/{id}/{n}")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response avgLastNOfId(@PathParam("id") String id, @PathParam("n") int n) {

        logln("Query2 : AdminClient wants the average of last " + n + " pollution levels of robot " + id);

        double result;
        try {
            result = SmartCity.getInstance().getAvgLastNOfId(n, id);
        } catch (Exception e) {
            warn(e.getMessage());
            return Response.status(404).build();
        }
        String response = String.valueOf(result);
        successln("Query2 succeeded\n");
        return Response.ok(response).build();
    }
}