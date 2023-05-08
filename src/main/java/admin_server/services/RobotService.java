package admin_server.services;

import admin_server.District;
import admin_server.RobotPosition;
import admin_server.RobotRepresentation;
import admin_server.SmartCity;

import static admin_server.RobotPosition.generateRobotPosition;
import static utils.Printer.*;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Objects;

@Path("robots")
public class RobotService {

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
            warn(e.getMessage());
            return Response.status(404).build();
        }

        // building registration response
        RobotPosition position = generateRobotPosition(districtAssignment);
        List<RobotRepresentation> otherRobots = city.getRobotsList();
        for (RobotRepresentation x : otherRobots) {
            if (Objects.equals(x.getId(), r.getId())) {
                otherRobots.remove(x);
                break;
            }
        }

        successln("Registration succeded\n");
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
            warn(e.getMessage());
            return Response.status(404).build();
        }

        successln("Removal succeded\n");
        return Response.ok().build();
    }
}


