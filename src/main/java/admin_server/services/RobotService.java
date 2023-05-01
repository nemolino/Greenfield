package admin_server.services;

import admin_server.beans.RobotRepr;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

@Path("robots")
public class RobotService {

    @Path("register")
    @POST
    @Consumes({"application/json", "application/xml"})
    public Response registerRobot(RobotRepr r){

        System.out.println("Registration request arrived ... ID: " + r.getId() +
                ", address: " + r.getAddress() +
                ", listeningPort: " + r.getPort());
        
        return Response.ok().build();
    }

}