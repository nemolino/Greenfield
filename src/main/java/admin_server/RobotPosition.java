package admin_server;

import java.util.Random;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class RobotPosition {
    private int x;
    private int y;

    private RobotPosition() {
    }

    private RobotPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public static RobotPosition generateRobotPosition(District d) {
        Random r = new Random();
        int x = r.nextInt(5);
        if (d == District.D2 || d == District.D3)
            x += 5;
        int y = r.nextInt(5);
        if (d == District.D3 || d == District.D4)
            y += 5;
        return new RobotPosition(x, y);
    }

    public District getDistrict() {
        return (x < 5) ? ((y < 5) ? District.D1 : District.D4) : ((y < 5) ? District.D2 : District.D3);
    }

    @Override
    public String toString() {
        return "(" + this.x + "," + this.y + ")";
    }
}

