package common.printer;

import java.util.Arrays;
import java.util.List;

import static common.printer.Type.*;

public final class Printer {

    private static final List<Type> toPrint = Arrays.asList(
            B,          // basic
            N,          // network
            M,          // maintenance
            P,          // pollution
            //M_LOW,    // maintenance, low priority
            //P_LOW     // pollution, low priority
            Q,          // query
            Q_LOW       // query, low priority
    );

    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_RED = "\u001B[31m";
    // private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_PURPLE = "\u001B[35m";
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_YELLOW_BG = "\u001B[103m";

    private static final String ANSI_BRIGHT_BLACK  = "\u001B[90m";
    // private static final String ANSI_BRIGHT_RED    = "\u001B[91m";
    // private static final String ANSI_BRIGHT_GREEN  = "\u001B[92m";
    // private static final String ANSI_BRIGHT_YELLOW = "\u001B[93m";
    // private static final String ANSI_BRIGHT_BLUE   = "\u001B[94m";
    // private static final String ANSI_BRIGHT_PURPLE = "\u001B[95m";
    // private static final String ANSI_BRIGHT_CYAN   = "\u001B[96m";
    // private static final String ANSI_BRIGHT_WHITE  = "\u001B[97m";

    public static void log(Type t, String s) {
        if (!toPrint.contains(t)) return;
        if (t == Type.N || t == Type.B) System.out.println(s);
        else if (t == Type.M)           System.out.println(ANSI_PURPLE + s + ANSI_RESET);
        else if (t == Type.P)           System.out.println(ANSI_BLUE + s + ANSI_RESET);
        else if (t == Type.Q)           System.out.println(ANSI_CYAN + s + ANSI_RESET);
        else                            System.out.println(ANSI_BRIGHT_BLACK + s + ANSI_RESET);

    }

    public static void logInline(Type t, String s) {
        if (!toPrint.contains(t)) return;
        if (t == Type.N || t == Type.B) System.out.print(s);
        else if (t == Type.M)           System.out.print(ANSI_PURPLE + s + ANSI_RESET);
        else if (t == Type.P)           System.out.print(ANSI_BLUE + s + ANSI_RESET);
        else if (t == Type.Q)           System.out.print(ANSI_CYAN + s + ANSI_RESET);
        else                            System.out.print(ANSI_BRIGHT_BLACK + s + ANSI_RESET);
    }

    public static void warn(Type t, String s) {
        if (!toPrint.contains(t)) return;
        System.out.println(ANSI_YELLOW + s + ANSI_RESET);
    }

    public static void error(Type t, String s) {
        if (!toPrint.contains(t)) return;
        System.out.println(ANSI_RED + s + ANSI_RESET);
    }

    public static void cli(String s) {
        System.out.println(ANSI_YELLOW_BG + s + ANSI_RESET);
    }
}
