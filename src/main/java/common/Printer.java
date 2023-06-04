package common;

public final class Printer {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_PURPLE_BG = "\u001B[45m";

    public static void logln(String s) {
        System.out.println(ANSI_BLUE + s + ANSI_RESET);
    }

    public static void successln(String s) {
        System.out.println(ANSI_GREEN + s + ANSI_RESET);
    }

    public static void warnln(String s) {
        System.out.println(ANSI_YELLOW + s + ANSI_RESET);
    }

    public static void errorln(String s) {
        System.out.println(ANSI_RED + s + ANSI_RESET);
    }

    public static void cliln(String s) { System.out.println(ANSI_PURPLE_BG + s + ANSI_RESET); }

    public static void log(String s) {
        System.out.print(ANSI_BLUE + s + ANSI_RESET);
    }

    public static void success(String s) {
        System.out.print(ANSI_GREEN + s + ANSI_RESET);
    }

    public static void warn(String s) {
        System.out.print(ANSI_YELLOW + s + ANSI_RESET);
    }

    public static void error(String s) {
        System.out.print(ANSI_RED + s + ANSI_RESET);
    }

    public static void cli(String s) { System.out.print(ANSI_PURPLE_BG + s + ANSI_RESET); }

    public static void logln() {
        System.out.println();
    }

    public static void successln() {
        System.out.println();
    }

    public static void warnln() {
        System.out.println();
    }

    public static void errorln() {
        System.out.println();
    }

    public static void cli() { System.out.print(ANSI_PURPLE_BG + "" + ANSI_RESET); }
}
