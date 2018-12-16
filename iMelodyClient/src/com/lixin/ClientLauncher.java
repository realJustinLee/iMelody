package com.lixin;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author lixin
 */
public class ClientLauncher {
    private MelodyClient client;
    private JFrame frame;
    private JTextField username;


    public static void main(String[] args) {
        new ClientLauncher().go();
    }

    private void go() {
        buildGUI();
        client = new MelodyClient();
    }

    private void buildGUI() {
        frame = new JFrame("iMelody Login");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        BorderLayout layout = new BorderLayout();
        JPanel mainPanel = new JPanel(layout);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        Box usernameBox = new Box(BoxLayout.X_AXIS);

        JLabel labelUsername = new JLabel("Username: ");
        usernameBox.add(labelUsername);

        username = new JTextField();
        usernameBox.add(username);

        Box buttonBox = new Box(BoxLayout.Y_AXIS);

        JButton quit = new JButton("Quit");
        quit.addActionListener(new MyQuitListener());
        buttonBox.add(quit);

        JButton login = new JButton("Login");
        login.addActionListener(new MyLoginListener());
        login.requestFocus();
        buttonBox.add(login);

        mainPanel.add(BorderLayout.CENTER, usernameBox);
        mainPanel.add(BorderLayout.EAST, buttonBox);
        frame.getContentPane().add(mainPanel);

        frame.setBounds(50, 50, 400, 300);
        frame.setMinimumSize(new Dimension(400, 300));
        frame.pack();
        frame.setVisible(true);
        frame.validate();
    }


    public class MyLoginListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            frame.dispose();
            String name = username.getText();
            if (name.equals("")) {
                client.startUp("Anonymous");
            } else {
                client.startUp(username.getText());
            }
        }
    }

    public class MyQuitListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            System.exit(0);
        }
    }
}
