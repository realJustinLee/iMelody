package com.lixin;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * @author lixin
 */
class MelodyServer {
    private ArrayList<ObjectOutputStream> clientOutputStreams;
    private Thread server;

    void go() {
        clientOutputStreams = new ArrayList<>();
        server = new Thread(this::serverLauncher);
        server.start();
    }

    private void serverLauncher() {
        try {
            ServerSocket serverSocket = new ServerSocket(5005);
            System.out.println("Server established");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
                clientOutputStreams.add(oos);

                //TODO: 使用线程池
                Thread client = new Thread(new ClientHandler(clientSocket));
                client.start();

                System.out.println("Got a connection");
            }
        } catch (Exception ex) {
            System.out.println("Server Stopped");
        }
    }

    public class ClientHandler implements Runnable {
        ObjectInputStream ois;
        Socket clientSocket;

        ClientHandler(Socket socket) {
            try {
                clientSocket = socket;
                ois = new ObjectInputStream(clientSocket.getInputStream());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        @Override
        public void run() {
            Object user;
            Object melody;
            try {
                while ((user = ois.readObject()) != null) {
                    melody = ois.readObject();
                    System.out.println("Read user and melody");
                    tellEveryOne(user, melody);
                }
            } catch (Exception ex) {
                System.out.println("Connection lost");
            }
        }
    }

    void stop() {
        server.interrupt();
        server = null;
        //FIXME: 断开所有与客户端的链接
        //TODO: 关闭所有 Thread client
        System.out.println("Server Stopped");
    }

    private void tellEveryOne(Object user, Object melody) {
        for (ObjectOutputStream clientOutputStream : clientOutputStreams) {
            try {
                clientOutputStream.writeObject(user);
                clientOutputStream.writeObject(melody);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
