package com.bodera.utils;

import java.time.Duration;

public class ThreadUtils {

    public static void sleep(Duration duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static String measure(Runnable runnable) {
        long start = System.nanoTime();
        runnable.run();
        long end = System.nanoTime();

        double miliseconds = (end - start) / 1_000_000D;
        return String.format("%.2f", miliseconds / 1000D);
    }
}
