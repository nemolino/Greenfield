package admin_server;

import java.util.Random;

public class RobotPosition {
    private int x;
    private int y;
    private RobotPosition(int x, int y){
        this.x = x;
        this.y = y;
    }

    public static RobotPosition generateRobotPosition(District d){
        Random r = new Random();
        int x = r.nextInt(5);
        if (d == District.D2 || d == District.D3)
            x += 5;
        int y = r.nextInt(5);
        if (d == District.D3 || d == District.D4)
            y += 5;
        return new RobotPosition(x,y);
    }

    @Override
    public String toString() {
        return "[" + this.x + "," + this.y + "]";
    }
}

