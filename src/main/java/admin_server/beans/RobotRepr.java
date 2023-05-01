package admin_server.beans;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class RobotRepr {

    private int id;
    private String address;
    private int port;

    public RobotRepr(){}

    public RobotRepr(int id, String address, int port) {
        this.id = id;
        this.address = address;
        this.port = port;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
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
}