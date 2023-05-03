package admin_server;

import java.util.*;

public class SmartCity {
    private Map<District, List<RobotRepresentation>> registeredRobots;
    private static SmartCity instance;

    private SmartCity() {
        /* District.Di -> [ ... list of robots in District.Di... ] */
        this.registeredRobots = new HashMap<>();
        this.registeredRobots.put(District.D1, new ArrayList<RobotRepresentation>());
        this.registeredRobots.put(District.D2, new ArrayList<RobotRepresentation>());
        this.registeredRobots.put(District.D3, new ArrayList<RobotRepresentation>());
        this.registeredRobots.put(District.D4, new ArrayList<RobotRepresentation>());
    }

    //singleton
    public synchronized static SmartCity getInstance(){
        if (instance == null)
            instance = new SmartCity();
        return instance;
    }

    public synchronized List<RobotRepresentation> getRobotsList() {
        List<RobotRepresentation> registeredRobotsList = new ArrayList<>();
        for (District d : District.values())
            registeredRobotsList.addAll(registeredRobots.get(d));
        return registeredRobotsList;
    }

    /*
    public synchronized void setRobotsList(List<RobotRepresentation> robotsList) {
        this.robotsList = robotsList;
    }*/

    public synchronized District add(RobotRepresentation r){

        // checking that doesn't already exist a robot same ID as r
        for (Map.Entry<District, List<RobotRepresentation>> entry : registeredRobots.entrySet())
            for (RobotRepresentation x : entry.getValue())
                if (Objects.equals(x.getId(), r.getId()))
                    throw new RuntimeException("duplicated ID");

        // assigning r to some district
        District newRobotDistrict = chooseNewRobotDistrict();
        registeredRobots.get(newRobotDistrict).add(r);
        return newRobotDistrict;
    }

    private District chooseNewRobotDistrict(){
        District lessPopulatedDistrict = District.D1;
        for (District d : new District[]{District.D2, District.D3, District.D4})
            if (registeredRobots.get(d).size() < registeredRobots.get(lessPopulatedDistrict).size())
                lessPopulatedDistrict = d;
        return lessPopulatedDistrict;
    }

}

