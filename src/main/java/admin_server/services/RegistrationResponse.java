package admin_server.services;

import admin_server.RobotPosition;
import admin_server.RobotRepresentation;
import admin_server.SmartCity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static utils.Printer.*;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

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