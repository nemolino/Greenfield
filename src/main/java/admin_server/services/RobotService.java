package admin_server.services;

import admin_server.District;
import admin_server.RobotPosition;
import admin_server.RobotRepresentation;
import admin_server.SmartCity;

import static admin_server.RobotPosition.generateRobotPosition;
import static utils.Printer.*;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Path("robots")
public class RobotService {

    @Path("register")
    @POST
    @Consumes({"application/json", "application/xml"})
    public Response registerRobot(RobotRepresentation r) {

        logln("Registration request arrived ... ID: " + r.getId() +
                ", address: " + r.getAddress() + ", listeningPort: " + r.getPort());

        SmartCity city = SmartCity.getInstance();
        District districtAssignment;
        try {
            districtAssignment = city.add(r);
        } catch (Exception e) {
            warn("Duplicated ID, registration failed");
            return Response.status(404).build();
        }

        // building registration response
        RobotPosition position = generateRobotPosition(districtAssignment);

        /*logln("All robots: " + city.getRobotsList());*/

        List<RobotRepresentation> otherRobots = city.getRobotsList();
        for (RobotRepresentation x : otherRobots){
            if (Objects.equals(x.getId(), r.getId())) {
                otherRobots.remove(x);
                break;
            }
        }
        /*
        logln("Other robots: " + otherRobots);
        RegistrationResponse response = new RegistrationResponse(position, otherRobots);
        logln("Robot assigned to " + districtAssignment + " at position " + response.getPosition());
        logln("Other robots from response: " + response.getOtherRobots());
        logln();
        */
        logln("Registration succeded\n");
        return Response.ok(new RegistrationResponse(position, otherRobots)).build();
    }

}