package com.lumadd.protohackers.problem07;

import com.lumadd.protohackers.problem07.transportLayer.serverInfrastructure.LRCPServer;
import com.lumadd.protohackers.problem07.transportLayer.serverInfrastructure.LRCPSocket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.SocketTimeoutException;

public class LineReversal {
    private static final Logger logger = LogManager.getLogger();

    private static final int TIMEOUT = 1_000;

    private static volatile boolean Running = false;

    public static void run(int port) {
        if (Running) {
            logger.warn("Attempted to run, but this is already running.");
            return;
        }

        Running = true;

        try (LRCPServer serverSocket = new LRCPServer(port)) {
            logger.info("Started on port {}.", port);

            serverSocket.setSoTimeout(TIMEOUT);

            while (Running) {
                try {
                    LRCPSocket socket = serverSocket.accept();

                    new Thread(() -> manageSocket(socket)).start();
                } catch (SocketTimeoutException e) {
                    logger.trace("Socket timed out (timeout: {}) in thread {}.", TIMEOUT, Thread.currentThread().toString());
                }
            }
        } catch (IOException e) {
            logger.fatal("An IO exception was thrown by the DatagramSocket. No attempt will be made to reopen the socket.\n{}\n{}", e.getMessage(), e.getStackTrace());
        }
    }

    private static void manageSocket(LRCPSocket socket) {
        while (socket.isAlive()) {
            String line = socket.getLine(1_000);
            if (line == null) {
                continue;
            }

            String reversed = new StringBuilder(line).reverse().toString();

            socket.sendLine(reversed);
        }
    }

    public static void stop() {
        if (Running) {
            logger.info("Stopped.");
        } else {
            logger.warn("Attempted to stop, but this is already stopped.");
        }
        Running = false;
    }
}
