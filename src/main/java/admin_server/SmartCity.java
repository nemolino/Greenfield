package admin_server;

import admin_server.rest_response_formats.RobotRepresentation;
import com.google.common.collect.Lists;
import common.District;
import common.printer.Type;

import java.util.*;

import static common.printer.Printer.*;

public class SmartCity {

    private static SmartCity instance;

    private final Map<District, List<RobotRepresentation>> registeredRobots;
    private final Map<String, List<PollutionMessage>> statsData;

    private SmartCity() {
        /*
            registeredRobots =
            { District.D1 : [ ... list of robots in District.D1 ... ],
              District.D2 : [ ... list of robots in District.D2 ... ],
              District.D3 : [ ... list of robots in District.D3 ... ],
              District.D4 : [ ... list of robots in District.D4 ... ]  }
        */
        this.registeredRobots = new HashMap<>();
        this.registeredRobots.put(District.D1, new ArrayList<RobotRepresentation>());
        this.registeredRobots.put(District.D2, new ArrayList<RobotRepresentation>());
        this.registeredRobots.put(District.D3, new ArrayList<RobotRepresentation>());
        this.registeredRobots.put(District.D4, new ArrayList<RobotRepresentation>());

        /*
            statsData =
            { ... ,
              robotID : [ (t1, [...averages...]), (t2, [...averages...]), ... ],
              ...  }
        */
        this.statsData = new HashMap<>();
    }

    //singleton
    public synchronized static SmartCity getInstance() {
        if (instance == null)
            instance = new SmartCity();
        return instance;
    }

    // returns the list of the cleaning robots currently located in Greenfield
    public synchronized List<RobotRepresentation> getRegisteredRobotsList() {

        List<RobotRepresentation> registeredRobotsList = new ArrayList<>();
        for (District d : District.values())
            registeredRobotsList.addAll(registeredRobots.get(d));
        return registeredRobotsList;
    }

    public synchronized District add(RobotRepresentation r) {

        // checking that doesn't already exist a robot with same ID as r
        if (statsData.containsKey(r.getId()))
            throw new RuntimeException("Duplicated ID");

        statsData.put(r.getId(), new ArrayList<PollutionMessage>());

        // assigning r to some district
        District newRobotDistrict = chooseNewRobotDistrict();
        registeredRobots.get(newRobotDistrict).add(r);
        return newRobotDistrict;
    }

    public synchronized void remove(String id) {

        // checking that exist a robot with the requested ID
        if (!statsData.containsKey(id))
            throw new RuntimeException("Missing ID");

        statsData.remove(id);

        // removing robot
        for (Map.Entry<District, List<RobotRepresentation>> entry : registeredRobots.entrySet())
            for (RobotRepresentation x : entry.getValue())
                if (Objects.equals(x.getId(), id)) {
                    entry.getValue().remove(x);
                    return;
                }
    }

    private District chooseNewRobotDistrict() {

        District lessPopulatedDistrict = District.D1;
        for (District d : District.values())
            if (registeredRobots.get(d).size() < registeredRobots.get(lessPopulatedDistrict).size())
                lessPopulatedDistrict = d;
        return lessPopulatedDistrict;
    }

    public synchronized void addPollutionData(String id, PollutionMessage msg) {

        if (!statsData.containsKey(id))
            throw new RuntimeException("pollution data received from R_" + id + ", that is not currently in Greenfield");
        statsData.get(id).add(msg);
        /* code used for debugging purpose:
        for (Map.Entry<String, List<PollutionMessage>> entry : statsData.entrySet()){
            logln(entry.getKey() + " --> ");
            for (PollutionMessage m : entry.getValue())
                logln(m.toString());
        }
        */
    }

    // returns the average of the last n air pollution levels
    // sent to the server by a given (registered) robot
    public synchronized double getAvgLastNOfId(int n, String id) {

        if (n < 1)
            throw new RuntimeException("n = " + n + " must be > 0");

        if (!statsData.containsKey(id))
            throw new RuntimeException("robot " + id + " is not in the city");

        List<PollutionMessage> l = statsData.get(id);
        if (l.size() == 0)
            throw new RuntimeException("robot " + id + " has not sent pollution levels yet");

        double result = 0;
        int remainingToTake = n;
        int taken = 0;

        logInline(Type.Q_LOW, "... summed values for query2 : ");
        for (PollutionMessage x : Lists.reverse(l)) {
            List<Double> averages = x.getAverages();
            if (averages.size() <= remainingToTake) {
                for (double value : averages) {
                    logInline(Type.Q_LOW, value + " ");
                    result += value;
                    taken++;
                }
                remainingToTake -= averages.size();
            } else {
                for (double value : averages.subList(averages.size() - remainingToTake, averages.size())) {
                    logInline(Type.Q_LOW, value + " ");
                    result += value;
                    taken++;
                    remainingToTake--;
                }
                break;
            }
        }
        log(Type.Q_LOW, "");

        if (remainingToTake > 0)
            warn(Type.Q, "... query2 info - last n = " + n + " is too big, we take all the levels as having n = " + taken);

        return result / taken;
    }

    // returns the average of the air pollution levels
    // sent by all the (registered) robots to the server and occurred from timestamps t1 and t2
    public synchronized double getAvgInTimeRange(long t1, long t2) {

        if (t1 < 0)
            throw new RuntimeException("t1 = " + t1 + " must be >= 0");
        if (t2 < 0)
            throw new RuntimeException("t2 = " + t2 + " must be >= 0");
        if (t2 < t1)
            throw new RuntimeException("t1 = " + t1 + " > t2 = " + t2);

        double result = 0;
        int n = 0;

        logInline(Type.Q_LOW, "... summed values for query3 : ");
        for (Map.Entry<String, List<PollutionMessage>> entry : statsData.entrySet()) {
            for (PollutionMessage m : entry.getValue()) {
                if (m.getTimestamp() < t1)
                    continue;
                if (m.getTimestamp() > t2)
                    break;
                for (double value : m.getAverages()) {
                    logInline(Type.Q_LOW, value + " ");
                    result += value;
                    n++;
                }
            }
        }
        log(Type.Q_LOW, "");

        if (n == 0)
            throw new RuntimeException("no pollution levels in range [t1 = " + t1 + ", t2 = " + t2 + "]");

        return result / n;
    }

}



