package admin_server.REST_response_formats;

import admin_server.RobotPosition;
import admin_server.RobotRepresentation;

import java.util.List;

import javax.xml.bind.annotation.*;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class RegistrationResponse {

    @XmlElement(name="other_robots")
    private List<RobotRepresentation> otherRobots;

    private RobotPosition position;

    private RegistrationResponse() {
    }

    public RegistrationResponse(RobotPosition position, List<RobotRepresentation> otherRobots) {
        this.position = position;
        this.otherRobots = otherRobots;
    }

    public List<RobotRepresentation> getOtherRobots() {
        return otherRobots;
    }

    public void setOtherRobots(List<RobotRepresentation> otherRobots) {
        this.otherRobots = otherRobots;
    }

    public RobotPosition getPosition() {
        return position;
    }

    public void setPosition(RobotPosition position) {
        this.position = position;
    }

}