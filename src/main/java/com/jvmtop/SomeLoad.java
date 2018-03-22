package com.jvmtop;

public class SomeLoad {

    private static double load(int seconds) {
        long start = System.currentTimeMillis();
        double result = 0.0;
        while (System.currentTimeMillis() - start < seconds) {
            result = Math.sqrt(start);
        }
        return result;
    }

    private static void heavyLoad() {
        load(95000);
    }

    private static void smallLoad() {
        load(5000);
    }

    public static void main(String[] args) {
        while (true) {
            heavyLoad();
            smallLoad();
            System.out.println("Iteration : " + System.currentTimeMillis());
        }
    }
}
