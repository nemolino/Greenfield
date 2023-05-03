package utils;

import java.util.Random;

public class TestClass {
    public static void main(String[] args) {
        // ID generation
        for (int i = 0; i < 30; i++)
            System.out.println(String.format("%05d", new Random().nextInt(100000)));
    }
}
