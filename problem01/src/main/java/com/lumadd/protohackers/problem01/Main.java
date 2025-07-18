package com.lumadd.protohackers.problem01;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {
    private static final Logger logger = LogManager.getLogger();

    public static void main(String[] args) {
        logger.info("Starting up...");

        Runtime.getRuntime().addShutdownHook(new Thread(Main::shutdownRoutine));

        new Thread(() -> PrimeTime.run(10_001)).start();
    }

    private static void shutdownRoutine() {
        logger.info("Shutting down...");

        PrimeTime.stop();

        logger.info("Have a nice day!.");
    }
}
