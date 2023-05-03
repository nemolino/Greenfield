package admin_server.services;

import admin_server.District;
import admin_server.RobotRepresentation;
import admin_server.SmartCity;
import static admin_server.RobotPosition.generateRobotPosition;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

@Path("robots")
public class RobotService {

    @Path("register")
    @POST
    @Consumes({"application/json", "application/xml"})
    public Response registerRobot(RobotRepresentation r){

        System.out.println("Registration request arrived ... ID: " + r.getId() +
                ", address: " + r.getAddress() +
                ", listeningPort: " + r.getPort());

        District districtAssignment;

        try{
            districtAssignment = SmartCity.getInstance().add(r);
        } catch (Exception e) {
            System.out.println("ID duplicato, non posso aggiungere");
            return Response.status(404).build();
        }
        System.out.println("Robot assegnato a " + districtAssignment +
                " in posizione " + generateRobotPosition(districtAssignment));

        System.out.print("All robots: [ ");
        for (RobotRepresentation x : SmartCity.getInstance().getRobotsList())
            System.out.print(x + " ");
        System.out.println("]");

        return Response.ok().build();
    }

}