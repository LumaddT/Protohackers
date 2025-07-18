package com.lumadd.protohackers.problem04;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;

class UnusualDatabaseProgram {
    private static final Logger logger = LogManager.getLogger();

    private static final int TIMEOUT = 1_000;

    private static volatile boolean Running = false;

    private static final String VERSION = "My Key-Value Store 1.0";
    private static final int MAX_LENGTH = 1_000;

    private static final Map<String, String> DATABASE = new HashMap<>();

    public static void run(int port) {
        if (Running) {
            logger.warn("Attempted to run, but this is already running.");
            return;
        }

        Running = true;

        try (DatagramSocket serverSocket = new DatagramSocket(port)) {
            logger.info("Started on port {}.", port);

            serverSocket.setSoTimeout(TIMEOUT);

            while (Running) {
                try {
                    checkSocket(serverSocket);
                } catch (SocketTimeoutException e) {
                    logger.trace("Socket timed out (timeout: {}) in thread {}.", TIMEOUT, Thread.currentThread().toString());
                }
            }
        } catch (IOException e) {
            logger.fatal("An IO exception was thrown by the DatagramSocket. No attempt will be made to reopen the socket.\n{}\n{}", e.getMessage(), e.getStackTrace());
        }
    }

    private static void checkSocket(DatagramSocket socket) throws IOException {
        byte[] clientBytes = new byte[MAX_LENGTH];

        DatagramPacket clientPacket = new DatagramPacket(clientBytes, clientBytes.length);
        socket.receive(clientPacket);

        String clientMessage = new String(clientPacket.getData(), 0, clientPacket.getLength());

        logger.debug("Received \"{}\".", clientMessage);

        if (clientMessage.contains("=")) {
            String[] splitClientMessage = clientMessage.split("=", 2);
            logger.debug("Putting \"{}\" in the database.", clientMessage);
            DATABASE.put(splitClientMessage[0], splitClientMessage[1]);
        } else {
            String value;
            if (clientMessage.equals("version")) {
                value = VERSION;
            } else {
                value = DATABASE.getOrDefault(clientMessage, "");
            }

            String serverMessageString = clientMessage + '=' + value;

            logger.debug("Retrieving \"{}\" from the database.", serverMessageString);

            byte[] serverMessage = serverMessageString.getBytes();
            InetAddress address = clientPacket.getAddress();
            int port = clientPacket.getPort();

            DatagramPacket serverPacket = new DatagramPacket(serverMessage, serverMessage.length, address, port);

            socket.send(serverPacket);
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
