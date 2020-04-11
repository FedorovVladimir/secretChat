package server;

import network.TCPConnection;
import network.TCPConnectionListener;

import javax.swing.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

public class Server implements TCPConnectionListener {

    private long a;
    private long g;
    private long p;
    private long A;
    private long K;
    private boolean isConnect;

    private final List<TCPConnection> connections = new ArrayList<>();

    public static void main(String[] args) {
        new Server();
    }

    private Server() {
        System.out.println("Server running...");
        try (ServerSocket serverSocket = new ServerSocket(8189)) {
            while (true) {
                try {
                    new TCPConnection(this, serverSocket.accept());
                } catch (IOException e) {
                    System.out.println("TCPConnection exception: " + e);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized void onConnectionReady(TCPConnection tcpConnection) {
        a = getRandom();
        g = getRandom();
        p = getRandom();
        A = pow(g, a) % p;
        tcpConnection.sendString(g + " " + p + " " + A);
    }

    @Override
    public synchronized void onReceiveString(TCPConnection tcpConnection, String value) {
        if (!isConnect) {
            isConnect = true;
            long B = Long.parseLong(value);
            K = pow(B, a) % p;
            connections.add(tcpConnection);
        } else {
            System.out.println(value);
        }
    }

    private long pow(long b, long a) {
        long ret = 1;
        for (int i = 0; i < a; i++) {
            ret *= b;
        }
        return ret;
    }

    @Override
    public synchronized void onDisconnect(TCPConnection tcpConnection) {
        connections.remove(tcpConnection);
        sendToAllConnections("Client disconnected: " + tcpConnection.toString());
    }

    @Override
    public synchronized void onException(TCPConnection tcpConnection, Exception e) {
        System.out.println("TCPConnection exception: " + e);
    }

    private void sendToAllConnections(String value) {
        System.out.println(value);
        for (TCPConnection connection : connections) {
            connection.sendString(value);
        }
    }

    long getRandom() {
        return (long) (Math.random() * 10) + 1;
    }
}
