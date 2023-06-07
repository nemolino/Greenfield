package admin_server.rest_services;

import common.District;
import admin_server.rest_response_formats.RegistrationResponse;
import common.Position;
import admin_server.rest_response_formats.RobotRepresentation;
import admin_server.SmartCity;
import common.printer.Type;

import static common.Position.generateRobotPosition;
import static common.printer.Printer.*;

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

        log(Type.N, "... registration request arrived for ID: " + r.getId());

        SmartCity city = SmartCity.getInstance();
        District districtAssignment;
        try {
            districtAssignment = city.add(r);
        } catch (Exception e) {
            warn(Type.N, "... registration of R_" + r.getId() + " failed - " + e.getMessage());
            return Response.status(404).build();
        }

        // building registration response
        Position position = generateRobotPosition(districtAssignment);
        List<RobotRepresentation> otherRobots = city.getRegisteredRobotsList();
        for (RobotRepresentation x : otherRobots) {
            if (Objects.equals(x.getId(), r.getId())) {
                otherRobots.remove(x);
                break;
            }
        }

        log(Type.N, "... registration of R_" + r.getId() + " succeeded");
        return Response.ok(new RegistrationResponse(position, otherRobots)).build();
    }

    @Path("remove")
    @DELETE
    @Consumes({"application/json", "application/xml"})
    public Response removeRobot(String id) {

        log(Type.N, "... removal request arrived for ID: " + id);

        try {
            SmartCity.getInstance().remove(id);
        } catch (Exception e) {
            warn(Type.N, "... removal of R_" + id + " failed - " + e.getMessage());
            return Response.status(404).build();
        }

        log(Type.N, "... removal of R_" + id + " succeeded");
        return Response.ok().build();
    }
}


