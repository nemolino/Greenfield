package admin_server.REST_response_formats;

import common.Position;

import java.util.List;

import javax.xml.bind.annotation.*;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class RegistrationResponse {

    @XmlElement(name="other_robots")
    private List<RobotRepresentation> otherRobots;

    private Position position;

    private RegistrationResponse() {
    }

    public RegistrationResponse(Position position, List<RobotRepresentation> otherRobots) {
        this.position = position;
        this.otherRobots = otherRobots;
    }

    public List<RobotRepresentation> getOtherRobots() {
        return otherRobots;
    }

    public void setOtherRobots(List<RobotRepresentation> otherRobots) {
        this.otherRobots = otherRobots;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

}