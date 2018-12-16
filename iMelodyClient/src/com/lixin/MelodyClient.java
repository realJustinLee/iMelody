package com.lixin;

import java.awt.*;
import javax.swing.*;
import javax.sound.midi.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.io.*;
import java.net.Socket;
import java.util.*;
import java.awt.event.*;

/**
 * @author lixin
 */
class MelodyClient {

    private JList<String> incomingList;
    private JTextField userMessage;
    private ArrayList<JCheckBox> checkBoxArrayList;
    private int nextNum;
    private Vector<String> listVector = new Vector<>();
    private String username;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private HashMap<String, boolean[]> otherSeqMap = new HashMap<>();

    private Sequencer sequencer;
    private Sequence sequence;
    private Track track;
    private JFrame frame;
    private JLabel tempo;

    private String[] instrumentNames = {
            "Bass Drum",
            "Closed Hi-Hat",
            "Open Hi-Hat",
            "Acoustic Snare",
            "Crash Cymbal",
            "Hand Clap",
            "High Tom",
            "Hi Bongo",
            "Maracas",
            "Whistle",
            "Low Conga",
            "Cowbell",
            "Vibraslap",
            "Low-mid Tom",
            "High Agogo",
            "Open Hi Conga"
    };

    // private int[] instruments = {35, 42, 46, 38, 49, 39, 50, 60, 70, 72, 64, 56, 58, 47, 67, 63};

    private int[] instruments = {51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66};

//    public static void main(String[] args) {
//        new MelodyClient().startUp(args[0]);
//    }

    void startUp(String name) {
        username = name;
        try {
            Socket sock = new Socket("127.0.0.1", 5005);
            oos = new ObjectOutputStream(sock.getOutputStream());
            ois = new ObjectInputStream(sock.getInputStream());
            Thread remote = new Thread(new RemoteReader());
            remote.start();
        } catch (Exception ex) {
            System.out.println("Couldn't connect - you'll have to play alone.");
        }
        setUpMidi();
        buildGUI();
    }

    private void buildGUI() {
        frame = new JFrame("iMelody");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        BorderLayout layout = new BorderLayout();
        JPanel background = new JPanel(layout);
        background.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        checkBoxArrayList = new ArrayList<>();
        Box buttonBox = new Box(BoxLayout.Y_AXIS);

        JButton start = new JButton("Start");
        start.addActionListener(new MyStartListener());
        buttonBox.add(start);

        JButton stop = new JButton("Stop");
        stop.addActionListener(new MyStopListener());
        buttonBox.add(stop);

        JButton clearCheck = new JButton("Clear");
        clearCheck.addActionListener(new MyClearCheckListener());
        buttonBox.add(clearCheck);

        tempo = new JLabel("Tempo: " + 1);
        buttonBox.add(tempo);

        JButton upTempo = new JButton("Tempo Up");
        upTempo.addActionListener(new MyUpTempoListener());
        buttonBox.add(upTempo);

        JButton downTempo = new JButton("Tempo Down");
        downTempo.addActionListener(new MyDownTempoListener());
        buttonBox.add(downTempo);

        JButton save = new JButton("Save");
        save.addActionListener(new MySaveListener());
        buttonBox.add(save);

        JButton load = new JButton("Load");
        load.addActionListener(new MyLoadListener());
        buttonBox.add(load);

        JButton send = new JButton("Send");
        send.addActionListener(new MySendListener());
        buttonBox.add(send);

        userMessage = new JTextField();
        buttonBox.add(userMessage);

        incomingList = new JList<>();
        incomingList.addListSelectionListener(new MyListSelectionListener());
        incomingList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane theList = new JScrollPane(incomingList);
        buttonBox.add(theList);
        incomingList.setListData(listVector);

        Box nameBox = new Box(BoxLayout.Y_AXIS);
        for (String instrumentName : instrumentNames) {
            nameBox.add(new Label(instrumentName));
        }

        background.add(BorderLayout.EAST, buttonBox);
        background.add(BorderLayout.WEST, nameBox);

        frame.getContentPane().add(background);

        GridLayout grid = new GridLayout(16, 16);
        grid.setVgap(0);
        grid.setHgap(0);
        JPanel mainPanel = new JPanel(grid);
        background.add(BorderLayout.CENTER, mainPanel);

        for (int i = 0; i < 256; i++) {
            JCheckBox checkBox = new JCheckBox();
            checkBox.setSelected(false);
            checkBoxArrayList.add(checkBox);
            mainPanel.add(checkBox);
        }

        frame.setBounds(50, 50, 400, 300);
        frame.pack();
        frame.setVisible(true);
    }

    private void setUpMidi() {
        try {
            sequencer = MidiSystem.getSequencer();
            sequencer.open();
            sequence = new Sequence(Sequence.PPQ, 4);
            track = sequence.createTrack();
            sequencer.setTempoInBPM(120);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void buildTrackAndStart() {
        ArrayList<Integer> trackList;
        sequence.deleteTrack(track);
        track = sequence.createTrack();

        for (int i = 0; i < 16; i++) {
            trackList = new ArrayList<>();
            int key = instruments[i];
            for (int j = 0; j < 16; j++) {
                JCheckBox checkBox = checkBoxArrayList.get((16 * i) + j);
                if (checkBox.isSelected()) {
                    trackList.add(key);
                } else {
                    trackList.add(null);
                }
            }
            makeTracks(trackList);
            track.add(makeEvent(176, 1, 127, 0, 16));
        }
        track.add(makeEvent(192, 9, 1, 0, 15));
        try {
            sequencer.setSequence(sequence);
            sequencer.setLoopCount(Sequencer.LOOP_CONTINUOUSLY);
            sequencer.start();
            sequencer.setTempoInBPM(120);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public class MyStartListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            buildTrackAndStart();
        }
    }

    public class MyStopListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            sequencer.stop();
        }
    }

    public class MyUpTempoListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            float tempoFactor = sequencer.getTempoFactor();
            sequencer.setTempoFactor((float) (tempoFactor * 1.024));
            tempo.setText("Tempo: " + tempoFactor);
        }
    }

    public class MyDownTempoListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            float tempoFactor = sequencer.getTempoFactor();
            sequencer.setTempoFactor((float) (tempoFactor * 0.9765625));
            tempo.setText("Tempo: " + tempoFactor);
        }
    }

    public class MyClearCheckListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            for (JCheckBox checkBox : checkBoxArrayList) {
                checkBox.setSelected(false);
            }
        }
    }

    public class MySaveListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            boolean[] checkBoxState = new boolean[256];
            for (int i = 0; i < 256; i++) {
                JCheckBox checkBox = checkBoxArrayList.get(i);
                if (checkBox.isSelected()) {
                    checkBoxState[i] = true;
                }
            }
            try {
                JFileChooser fileSave = new JFileChooser();
                fileSave.showSaveDialog(frame);
                FileOutputStream fileOutputStream = new FileOutputStream(fileSave.getSelectedFile());
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
                objectOutputStream.writeObject(checkBoxState);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }
    }

    public class MyLoadListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            boolean[] readInState = null;
            try {
                JFileChooser fileOpen = new JFileChooser();
                fileOpen.showOpenDialog(frame);
                FileInputStream iFIS = new FileInputStream(fileOpen.getSelectedFile());
                ObjectInputStream iOIS = new ObjectInputStream(iFIS);
                readInState = (boolean[]) iOIS.readObject();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            changeSequence(readInState);
            sequencer.stop();
            buildTrackAndStart();
        }
    }

    public class MySendListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            boolean[] checkBoxState = new boolean[256];
            for (int i = 0; i < 256; i++) {
                JCheckBox checkBox = checkBoxArrayList.get(i);
                if (checkBox.isSelected()) {
                    checkBoxState[i] = true;
                }
            }
            try {
                oos.writeObject(username + nextNum++ + ": " + userMessage.getText());
                oos.writeObject(checkBoxState);
            } catch (Exception ex) {
                System.out.println("Sorry dude. Could not send it to server.");
            }
            userMessage.setText("");
        }
    }

    public class MyListSelectionListener implements ListSelectionListener {
        @Override
        public void valueChanged(ListSelectionEvent e) {
            if (!e.getValueIsAdjusting()) {
                String selected = incomingList.getSelectedValue();
                if (selected != null) {
                    boolean[] selectedState = otherSeqMap.get(selected);
                    changeSequence(selectedState);
                    sequencer.stop();
                    buildTrackAndStart();
                }
            }
        }
    }

    public class RemoteReader implements Runnable {
        boolean[] checkBoxState = null;
        String nameToShow = null;
        Object obj = null;

        @Override
        public void run() {
            try {
                while ((obj = ois.readObject()) != null) {
                    System.out.println("Got an object from server.");
                    System.out.println(obj.getClass());
                    nameToShow = (String) obj;
                    checkBoxState = (boolean[]) ois.readObject();
                    // checkBoxState = (boolean[]) obj;
                    otherSeqMap.put(nameToShow, checkBoxState);
                    listVector.add(nameToShow);
                    incomingList.setListData(listVector);
                }
            } catch (Exception ex) {
                System.out.println("Server connection lost");
            }
        }
    }

    private void changeSequence(boolean[] checkBoxState) {
        for (int i = 0; i < 256; i++) {
            JCheckBox checkBox = checkBoxArrayList.get(i);
            if (checkBoxState[i]) {
                checkBox.setSelected(true);
            } else {
                checkBox.setSelected(false);
            }
        }
    }

    private void makeTracks(ArrayList<Integer> list) {
        Iterator<Integer> it = list.iterator();
        for (int i = 0; i < 16; i++) {
            Integer num = it.next();
            if (num != null) {
                int numKey = num;
                // Instrument Changes
                // channel         9         2
                // instrument   beatBox    piano
                track.add(makeEvent(144, 9, numKey, 100, i));
                track.add(makeEvent(128, 9, numKey, 100, i + 4));
            }
        }
    }

    private MidiEvent makeEvent(int command, int channel, int data1, int data2, int tick) {
        MidiEvent event = null;
        try {
            ShortMessage a = new ShortMessage();
            a.setMessage(command, channel, data1, data2);
            event = new MidiEvent(a, tick);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return event;
    }
}
