package com.bodera.section01;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

public class Task {

    private static final Logger LOGGER = LoggerFactory.getLogger(Task.class);

    public static void ioIntensiveOp(int i) {

        try {
            LOGGER.info("starting I/O task: {}", i);

            Thread.sleep(Duration.ofSeconds(60));

            LOGGER.info("ending I/O task: {}", i);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
