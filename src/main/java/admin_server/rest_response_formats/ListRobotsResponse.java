package admin_server.rest_response_formats;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ListRobotsResponse {

    @XmlElement(name = "robots")
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