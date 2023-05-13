package utils;

import admin_server.PollutionMessage;
import robot.MQTT_pollution.BufferImpl;
import simulator.PM10Simulator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestClass {
    public static void main(String[] args) {

        List<PollutionMessage> l = new ArrayList<PollutionMessage>();
        l.add(new PollutionMessage(1, new ArrayList<>(Arrays.asList(1.0,2.0,3.0,4.0,5.0,6.0,7.0,8.0))));
        l.add(new PollutionMessage(2, new ArrayList<>(Arrays.asList(9.0,10.0,11.0,12.0,13.0,14.0,15.0,16.0))));
        l.add(new PollutionMessage(3, new ArrayList<>(Arrays.asList(17.0,18.0,19.0,20.0,21.0,22.0,23.0,24.0))));

        System.out.println(l);
        System.out.println();

        int n = 24;
        if (n > l.size() * 8){
            System.out.println("grande");
            n = l.size() * 8;
        }


        int completeWindows = n / 8;
        System.out.println(l.subList(l.size() - completeWindows,l.size()));
        int partialWindow = n % 8;
        if (partialWindow != 0){
            List<Double> partialList = l.get(l.size() - completeWindows - 1).getAverages();
            System.out.println(partialList);
            System.out.println(partialList.subList(partialList.size()-partialWindow,partialList.size()));
            //System.out.println(l.subList(l.size() - completeWindows - 1,l.size() - completeWindows));
        }

        System.out.println("" + n/8 + " " + n % 8);

        /*
        // ID generation
        for (int i = 0; i < 30; i++)
            System.out.println(String.format("%05d", new Random().nextInt(100000)));

        // Printer
        Printer p = new Printer();
        log("prova");
        success("prova");
        warn("doppia prova");
        error("doppia prova");

        List<String> s = null;
        List<String> s2 = new ArrayList<>(s);
        log(s.toString());
        log(s2.toString());

        BufferImpl b = new BufferImpl();
        PM10Simulator s = new PM10Simulator(b);
        s.start();
        int i = 0;
        while (i < 4) {
            System.out.println(b.readAllAndClean());
            i++;
        }

        s.stopMeGently();*/
    }
}
