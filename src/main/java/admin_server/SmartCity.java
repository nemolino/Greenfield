package admin_server;

import java.util.*;

import static utils.Printer.*;

public class SmartCity {

    private static SmartCity instance;

    private final Map<District, List<RobotRepresentation>> registeredRobots;
    private final Map<String, List<PollutionMessage>> statsData;

    private SmartCity() {
        /* District.Di -> [ ... list of robots in District.Di... ] */
        this.registeredRobots = new HashMap<>();
        this.registeredRobots.put(District.D1, new ArrayList<RobotRepresentation>());
        this.registeredRobots.put(District.D2, new ArrayList<RobotRepresentation>());
        this.registeredRobots.put(District.D3, new ArrayList<RobotRepresentation>());
        this.registeredRobots.put(District.D4, new ArrayList<RobotRepresentation>());

        // stats
        this.statsData = new HashMap<>();
    }

    //singleton
    public synchronized static SmartCity getInstance() {
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

    public synchronized District add(RobotRepresentation r) {

        // checking that doesn't already exist a robot with same ID as r
        if (statsData.containsKey(r.getId()))
            throw new RuntimeException("Registration failure, duplicated ID");

        /*
        // checking that doesn't already exist a robot with same ID as r
        for (Map.Entry<District, List<RobotRepresentation>> entry : registeredRobots.entrySet())
            for (RobotRepresentation x : entry.getValue())
                if (Objects.equals(x.getId(), r.getId()))
                    throw new RuntimeException("Registration failure, duplicated ID");
        */

        // --- stats
        statsData.put(r.getId(), new ArrayList<PollutionMessage>());
        // ---

        // assigning r to some district
        District newRobotDistrict = chooseNewRobotDistrict();
        registeredRobots.get(newRobotDistrict).add(r);
        return newRobotDistrict;
    }

    public synchronized void remove(String id) {

        // checking that exist a robot with the requested ID
        if (!statsData.containsKey(id))
            throw new RuntimeException("Removal failure, ID not present");

        // --- stats
        statsData.remove(id);
        // ---

        // removing robot
        for (Map.Entry<District, List<RobotRepresentation>> entry : registeredRobots.entrySet())
            for (RobotRepresentation x : entry.getValue())
                if (Objects.equals(x.getId(), id)){
                    entry.getValue().remove(x);
                    return;
                }
        throw new AssertionError("Unreachable code");
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
            throw new RuntimeException("addPollutionData - robot " + id + " is not in the city");
        statsData.get(id).add(msg);

        for (Map.Entry<String, List<PollutionMessage>> entry : statsData.entrySet()){
            logln(entry.getKey() + " --> ");
            for (PollutionMessage m : entry.getValue())
                logln(m.toString());
        }
    }

    public synchronized double getAvgLastNOfId(int n, String id) {

        if (n < 1)
            throw new RuntimeException("getAvgLastNOfId - n = " + n + " must be > 0");

        if (!statsData.containsKey(id))
            throw new RuntimeException("getAvgLastNOfId - robot " + id + " is not in the city");

        double result = 0;
        List<PollutionMessage> l = statsData.get(id);

        if (l.size() == 0)
            throw new RuntimeException("getAvgLastNOfId - robot " + id + " has not sent pollution levels yet");

        if (n > l.size() * 8){
            warnln("getAvgLastNOfId - last n = " + n + " is too big, we set n = " + l.size() * 8);
            n = l.size() * 8;
        }

        warn("Summed values : ");
        // summing over complete windows
        int nOfWindowsToTakeCompletely = n / 8;
        for (PollutionMessage x : l.subList(l.size() - nOfWindowsToTakeCompletely, l.size())){
            for (double value : x.getAverages()){
                warn(value + " ");
                result += value;
            }
        }

        // summing over the (optional) partial window
        int nElementsToTakeFromThePartialWindow = n % 8;
        if (nElementsToTakeFromThePartialWindow != 0){
            List<Double> partialWindow = l.get(l.size() - nOfWindowsToTakeCompletely - 1).getAverages();
            for (double value : partialWindow.subList(partialWindow.size() - nElementsToTakeFromThePartialWindow, partialWindow.size())) {
                warn(value + " ");
                result += value;
            }
        }
        warn("\n");

        return result / n;
    }

    public synchronized double getAvgInTimeRange(long t1, long t2) {

        if (t2 < t1)
            throw new RuntimeException("getAvgInTimeRange - t1 = " + t1 + " > t2 = " + t2);

        double result = 0;
        int n = 0;

        warn("Summed values : ");
        for (Map.Entry<String, List<PollutionMessage>> entry : statsData.entrySet()){
            for (PollutionMessage m : entry.getValue()){
                if (m.getTimestamp() < t1)
                    continue;
                if (m.getTimestamp() > t2)
                    break;
                for (double value : m.getAverages()){
                    warn(value + " ");
                    result += value;
                    n++;
                }
            }
        }
        warn("\n");

        if (n == 0)
            throw new RuntimeException("getAvgInTimeRange - no pollution levels in range [t1 = " + t1 + ", t2 = " + t2 + "]");

        return result / n;
    }

}

/*

    • The list of the cleaning robots currently located in Greenfield
    • The average of the last n air pollution levels sent to the server by a given (?CONNECTED?) robot
    • The average of the air pollution levels sent by all the (?CONNECTED?) robots to the server and occurred from timestamps t1 and t2

    How I represent statistics data :

    {
        id1 -> [(t1, [-]), (t2, [-]), (t3, [-]), ...] ,
        id2 -> [(t1, [-]), (t2, [-]), (t3, [-]), ...] ,
        ...
    }

*/



