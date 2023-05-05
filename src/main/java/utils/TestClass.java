package utils;

import admin_server.RobotRepresentation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static utils.Printer.*;

public class TestClass {
    public static void main(String[] args) {

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
    }
}
