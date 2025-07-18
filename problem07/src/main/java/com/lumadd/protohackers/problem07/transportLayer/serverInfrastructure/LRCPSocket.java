package com.lumadd.protohackers.problem07.transportLayer.serverInfrastructure;

import com.lumadd.protohackers.problem07.transportLayer.messages.Ack;
import com.lumadd.protohackers.problem07.transportLayer.messages.Close;
import com.lumadd.protohackers.problem07.transportLayer.messages.Data;
import com.lumadd.protohackers.problem07.transportLayer.messages.Message;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class LRCPSocket {
    private static final Logger logger = LogManager.getLogger();

    private static final int RETRANSMISSION_TIMEOUT_MS = 3_000;
    private static final int SESSION_EXPIRY_TIMEOUT_MS = 60_000;

    @Getter
    private boolean Alive;

    private final int SessionId;
    private final InetAddress RemoteIP;
    private final int RemotePort;

    private final LRCPServer ParentServer;

    private long LastClientActionTimestampMillis;

    private final Map<Integer, Data> DataSent = new ConcurrentHashMap<>();
    private int LastByteServerAcknowledged = 0;
    private int LastByteClientAcknowledged = 0;
    private int LastByteSent = 0;

    private final BlockingQueue<String> ClientLinesQueue = new LinkedBlockingQueue<>();
    private String IncompleteLine = null;

    LRCPSocket(int sessionId, InetAddress remoteIP, int remotePort, LRCPServer parentServer) {
        SessionId = sessionId;
        RemoteIP = remoteIP;
        RemotePort = remotePort;
        ParentServer = parentServer;
        LastClientActionTimestampMillis = System.currentTimeMillis();

        Alive = true;

        this.sendAck(0);

        new Thread(this::connectionTimeoutChecker).start();
    }

    private void connectionTimeoutChecker() {
        while (Alive) {
            long now = System.currentTimeMillis();
            long timeDiff = now - LastClientActionTimestampMillis;

            if (timeDiff > SESSION_EXPIRY_TIMEOUT_MS) {
                this.closeConnection();
            } else {
                try {
                    //noinspection BusyWait
                    Thread.sleep(SESSION_EXPIRY_TIMEOUT_MS - timeDiff + 10);
                } catch (InterruptedException e) {
                    // If this happens I have bigger issues than closing things gracefully
                    throw new RuntimeException(e);
                }
            }
        }
    }

    void incomingMessage(Message clientMessage) {
        LastClientActionTimestampMillis = System.currentTimeMillis();

        switch (clientMessage.getMessageType()) {
            case CONNECT -> this.sendAck(0);
            case DATA -> this.processData((Data) clientMessage);
            case ACK -> this.processAck((Ack) clientMessage);
            case CLOSE -> this.closeConnection();
        }
    }

    private void processData(Data clientMessage) {
        if (clientMessage.getPosition() != LastByteServerAcknowledged) {
            this.sendAck(LastByteServerAcknowledged);
            return;
        }

        int length = clientMessage.getPayload().length();
        this.sendAck(LastByteServerAcknowledged + length);
        LastByteServerAcknowledged += length;

        String[] lines = clientMessage.getPayload().split("(?<=\n)");
        for (String line : lines) {
            if (IncompleteLine != null) {
                line = IncompleteLine + line;
                IncompleteLine = null;
            }

            if (line.isEmpty()) {
                continue;
            }

            if (line.charAt(line.length() - 1) != '\n') {
                IncompleteLine = line;
            } else {
                ClientLinesQueue.add(line.substring(0, line.length() - 1));
            }
        }
    }

    private void processAck(Ack ack) {
        int position = ack.getPosition();

        if (position > LastByteSent) {
            this.closeConnection();
            return;
        }

        if (position > LastByteClientAcknowledged) {
            LastByteClientAcknowledged = position;
            for (int positionToRemove : DataSent.keySet().stream()
                    .filter(p -> p < LastByteClientAcknowledged)
                    .toList()) {
                DataSent.remove(positionToRemove);
            }
        }

        Data data = DataSent.get(LastByteClientAcknowledged);
        if (data == null) {
            return;
        }

        ParentServer.send(data, RemoteIP, RemotePort);
    }

    /**
     * Returns null when it times out
     */
    public String getLine(int timeoutMs) {
        try {
            return ClientLinesQueue.poll(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            logger.fatal("The ClientLinesQueue threw InterruptException while polling. Error message: {}", e.getMessage());
            ParentServer.close();
            return null;
        }
    }

    public void sendLine(String line) {
        Data data = new Data(SessionId, LastByteSent, line + "\n");

        List<Data> splitDataMessages = data.split(LRCPServer.MAX_LENGTH);

        for (Data splitDataMessage : splitDataMessages) {
            LastByteSent += splitDataMessage.getPayload().length();
            DataSent.put(splitDataMessage.getPosition(), splitDataMessage);
            ParentServer.send(splitDataMessage, RemoteIP, RemotePort);
            new Thread(() -> this.retransmissionCheck(splitDataMessage)).start();
        }
    }

    private void sendAck(int position) {
        Ack ack = new Ack(SessionId, position);

        ParentServer.send(ack, RemoteIP, RemotePort);
    }

    private void retransmissionCheck(Data data) {
        while (Alive) {
            try {
                //noinspection BusyWait
                Thread.sleep(RETRANSMISSION_TIMEOUT_MS);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            if (LastByteClientAcknowledged == data.getPosition()) {
                ParentServer.send(data, RemoteIP, RemotePort, true);
            } else if (LastByteClientAcknowledged >= data.getPosition() + data.getPayload().length()) {
                return;
            }
        }
    }

    private void sendClose() {
        Close close = new Close(SessionId);

        ParentServer.send(close, RemoteIP, RemotePort);
    }

    /**
     * Remove socket from server.
     */
    void close() {
        Alive = false;
        ParentServer.removeSession(SessionId);
    }

    /**
     * Send the CLOSE message and remove socket from server.
     */
    public void closeConnection() {
        this.sendClose();
        this.close();
    }
}
