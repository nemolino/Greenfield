package admin_server.REST_services;

import common.District;
import admin_server.REST_response_formats.RegistrationResponse;
import common.Position;
import admin_server.REST_response_formats.RobotRepresentation;
import admin_server.SmartCity;

import static common.Position.generateRobotPosition;
import static common.Printer.*;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Objects;

@Path("robots")
public class RobotServices {

    @Path("register")
    @POST
    @Consumes({"application/json", "application/xml"})
    @Produces({"application/json", "application/xml"})
    public Response registerRobot(RobotRepresentation r) {

        logln("Registration request arrived for ID: " + r.getId());

        SmartCity city = SmartCity.getInstance();
        District districtAssignment;
        try {
            districtAssignment = city.add(r);
        } catch (Exception e) {
            warnln(e.getMessage() + "\n");
            return Response.status(404).build();
        }

        // building registration response
        Position position = generateRobotPosition(districtAssignment);
        List<RobotRepresentation> otherRobots = city.getRobotsList();
        for (RobotRepresentation x : otherRobots) {
            if (Objects.equals(x.getId(), r.getId())) {
                otherRobots.remove(x);
                break;
            }
        }

        successln("Registration succeeded\n");
        return Response.ok(new RegistrationResponse(position, otherRobots)).build();
    }

    @Path("remove")
    @DELETE
    @Consumes({"application/json", "application/xml"})
    public Response removeRobot(String id) {

        logln("Removal request arrived for ID: " + id);

        try {
            SmartCity.getInstance().remove(id);
        } catch (Exception e) {
            warnln(e.getMessage() + "\n");
            return Response.status(404).build();
        }

        successln("Removal succeeded\n");
        return Response.ok().build();
    }
}


