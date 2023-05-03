package admin_server;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class RobotRepresentation {

    private String id;
    private String address;
    private int port;

    public RobotRepresentation(){}

    public RobotRepresentation(String id, String address, int port) {
        this.id = id;
        this.address = address;
        this.port = port;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public String toString() {
        return "R_" + getId();
    }
}