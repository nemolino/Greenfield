package utils;

import robot.MQTT_pollution.BufferImpl;
import simulator.PM10Simulator;

public class TestClass {
    public static void main(String[] args) {
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
        log(s2.toString());*/

        BufferImpl b = new BufferImpl();
        PM10Simulator s = new PM10Simulator(b);
        s.start();
        int i = 0;
        while (i < 4) {
            System.out.println(b.readAllAndClean());
            i++;
        }

        s.stopMeGently();
    }
}
