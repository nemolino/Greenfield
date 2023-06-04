package admin_server.REST_services;

import admin_server.REST_response_formats.ListRobotsResponse;
import admin_server.REST_response_formats.RobotRepresentation;
import admin_server.SmartCity;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

import static common.Printer.*;

@Path("query")
public class AdminClientServices {

    @Path("list_all_robots")
    @GET
    @Produces({"application/json", "application/xml"})
    public Response robotsList() {

        logln("Query1 : list_all_robots");

        List<RobotRepresentation> robots = SmartCity.getInstance().getRobotsList();
        successln("Query1 succeeded\n");
        return Response.ok(new ListRobotsResponse(robots)).build();
    }

    @Path("avg_last_n_of_id/{id}/{n}")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response avgLastNOfId(@PathParam("id") String id, @PathParam("n") int n) {

        logln("Query2 : avg_last_n_of_id/{id = " + id + "}/{n = " + n + "})");

        try {
            double result = SmartCity.getInstance().getAvgLastNOfId(n, id);
            successln("Query2 succeeded\n");
            return Response.ok(String.valueOf(result)).build();

        } catch (Exception e) {
            warnln(e.getMessage());
            return Response.status(404).build();
        }
    }

    @Path("avg_between_t1_and_t2/{t1}/{t2}")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response avgInTimeRange(@PathParam("t1") long t1, @PathParam("t2") long t2) {

        logln("Query3 : avg_between_t1_and_t2/{t1 = " + t1 + "}/{t2 = " + t2 + "}");

        try {
            double result = SmartCity.getInstance().getAvgInTimeRange(t1, t2);
            successln("Query3 succeeded\n");
            return Response.ok(String.valueOf(result)).build();

        } catch (Exception e) {
            warnln(e.getMessage());
            return Response.status(404).build();
        }
    }
}