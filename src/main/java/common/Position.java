package common;

import java.util.Random;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Position {
    private int x;
    private int y;

    private Position() {}

    private Position(int x, int y) {
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

    // generates a valid Position for a robot in District d
    public static Position generateRobotPosition(District d) {
        Random r = new Random();
        int x = r.nextInt(5);
        if (d == District.D2 || d == District.D3)
            x += 5;
        int y = r.nextInt(5);
        if (d == District.D3 || d == District.D4)
            y += 5;
        return new Position(x, y);
    }

    // returns the District associated to this Position
    public District getDistrict() {
        return (x < 5) ? ((y < 5) ? District.D1 : District.D4) : ((y < 5) ? District.D2 : District.D3);
    }

    @Override
    public String toString() {
        return "(" + x + "," + y + ")";
    }
}

