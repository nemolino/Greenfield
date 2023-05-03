package oldies;

import admin_server.RobotRepresentation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class OldSmartCity {

    private List<RobotRepresentation> registeredRobotsList;
    private int[] districtRobotsCount;

    private static OldSmartCity instance;

    private OldSmartCity() {
        this.registeredRobotsList = new ArrayList<RobotRepresentation>();
        /*
            districtRobotsCount[0] = #robots in D1
            districtRobotsCount[1] = #robots in D2
            districtRobotsCount[2] = #robots in D3
            districtRobotsCount[3] = #robots in D4
         */
        this.districtRobotsCount = new int[4];
    }

    //singleton
    public synchronized static OldSmartCity getInstance(){
        if (instance == null)
            instance = new OldSmartCity();
        return instance;
    }

    public synchronized List<RobotRepresentation> getRobotsList(int id) {
        return new ArrayList<>(registeredRobotsList);
    }

    /*public synchronized void setRobotsList(List<RobotRepresentation> robotsList) {
        this.robotsList = robotsList;
    }*/

    public synchronized int add(RobotRepresentation r){
        for (RobotRepresentation x : registeredRobotsList){
            if (Objects.equals(x.getId(), r.getId()))
                throw new RuntimeException("duplicated ID");
        }
        registeredRobotsList.add(r);
        int newRobotDistrict = assignDistrictToRobot();
        return newRobotDistrict;
    }

    private int assignDistrictToRobot(){
        // district of the new robots = argmin{ districtRobotsCount[] } + 1
        int min = 0;
        for (int i = 1; i < 4; i++){
            if (districtRobotsCount[i] < districtRobotsCount[min])
                min = i;
        }
        districtRobotsCount[min]++;
        min++;
        return min;
    }

}