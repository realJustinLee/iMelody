package com.lixin;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author lixin
 */
public class ServerLauncher {
    private MelodyServer server;
    private JButton startServer;
    private JButton stopServer;
    private JLabel serverStatus;

    public static void main(String[] args) {
        new ServerLauncher().go();
    }

    private void go() {
        buildGUI();
        server = new MelodyServer();
    }

    private void buildGUI() {
        JFrame frame = new JFrame("iMelody Server");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        BorderLayout layout = new BorderLayout();
        JPanel mainPanel = new JPanel(layout);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        Box labelBox = new Box(BoxLayout.X_AXIS);

        JLabel statusLabel = new JLabel("Server status: ");
        labelBox.add(statusLabel);

        serverStatus = new JLabel("Stopped");
        labelBox.add(serverStatus);

        Box buttonBox = new Box(BoxLayout.Y_AXIS);

        startServer = new JButton("Start");
        startServer.addActionListener(new MyStartListener());
        startServer.requestFocus();
        buttonBox.add(startServer);

        stopServer = new JButton("Stop");
        stopServer.addActionListener(new MyStopListener());
        stopServer.setEnabled(false);
        buttonBox.add(stopServer);

        Box clientBox = new Box(BoxLayout.Y_AXIS);

        //TODO: 动态显示连接的客户端，实现管理员踢人功能

        mainPanel.add(BorderLayout.NORTH, labelBox);
        mainPanel.add(BorderLayout.EAST, buttonBox);
        mainPanel.add(BorderLayout.CENTER, clientBox);
        frame.getContentPane().add(mainPanel);

        frame.setBounds(50, 50, 400, 300);
        frame.setMinimumSize(new Dimension(400, 300));
        frame.pack();
        frame.setVisible(true);
        frame.validate();
    }

    public class MyStartListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            server.go();
            serverStatus.setText("Running");
            startServer.setEnabled(false);
            stopServer.setEnabled(true);
            stopServer.requestFocus();
        }
    }

    public class MyStopListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            server.stop();
            serverStatus.setText("Stopped");
            startServer.setEnabled(true);
            startServer.requestFocus();
            stopServer.setEnabled(false);
        }
    }
}

