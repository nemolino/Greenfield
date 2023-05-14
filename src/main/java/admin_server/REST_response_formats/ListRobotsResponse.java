package admin_server.REST_response_formats;

import admin_server.RobotRepresentation;

import java.util.List;

import javax.xml.bind.annotation.*;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ListRobotsResponse {

    @XmlElement(name="robots")
    private List<RobotRepresentation> robots;

    private ListRobotsResponse() {
    }

    public ListRobotsResponse(List<RobotRepresentation> robots) {
        this.robots = robots;
    }

    public List<RobotRepresentation> getRobots() {
        return robots;
    }

    public void setRobots(List<RobotRepresentation> robots) {
        this.robots = robots;
    }
}