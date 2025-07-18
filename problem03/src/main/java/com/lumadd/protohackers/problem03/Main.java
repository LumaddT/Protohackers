package com.lumadd.protohackers.problem03;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {
    private static final Logger logger = LogManager.getLogger();

    public static void main(String[] args) {
        logger.info("Starting up...");

        Runtime.getRuntime().addShutdownHook(new Thread(Main::shutdownRoutine));

        new Thread(() -> BudgetChat.run(10_003)).start();
    }

    private static void shutdownRoutine() {
        logger.info("Shutting down...");

        BudgetChat.stop();

        logger.info("Have a nice day!.");
    }
}
