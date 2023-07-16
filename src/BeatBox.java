import javax.sound.midi.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class BeatBox {
    private JFrame frame;
    private JPanel mainPanel;
    private ArrayList<JCheckBox> checkBoxes;
    private Sequence sequence;
    private Sequencer sequencer;
    private Track track;
    private String[] instrumentNames = {"Bass Drum", "Closed Hi-Hat", "Open Hi-Hat", "Acoustic Snare", "Crash Cymbal", "Hand Clap", "High Tom", "Hi Bongo", "Maracas", "Whistle", "Low Conga", "Cowbell", "Vibraslap", "Low-mid Tom", "High Agogo", "Open High Conga"};
    private int[] instrumentsInts = {35, 42, 46, 38, 49, 39, 50, 60, 70, 72, 64, 56, 58, 47, 67, 63};

    public static void main(String[] args) {
        BeatBox beatBox = new BeatBox();
        beatBox.buildGui();
    }
    public void buildGui () {
        // SET UP MAIN
        frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        BorderLayout layout = new BorderLayout();
        JPanel panel = new JPanel(layout);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        checkBoxes = new ArrayList<JCheckBox>();
        ArrayList<JButton> buttons = new ArrayList<JButton>();

        // ADDING BUTTONS
        JButton startButton = new JButton("Start");
        startButton.addActionListener(new startButtonListener());
        buttons.add(startButton);

        JButton stopButton = new JButton("Stop");
        stopButton.addActionListener(new stopButtonListener());
        buttons.add(stopButton);

        JButton tempoUp = new JButton("Temp Up");
        tempoUp.addActionListener(new tempoUpListener());
        buttons.add(tempoUp);

        JButton tempoDown = new JButton("Temp Down");
        tempoDown.addActionListener(new tempoDownListener());
        buttons.add(tempoDown);

        GridLayout buttonsGrid = new GridLayout(4, 1);
        buttonsGrid.setVgap(4);
        JPanel buttonsPanel = new JPanel(buttonsGrid);
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(6, 10, 200, 10));

        for (JButton button : buttons) {
            button.setBackground(new Color(40, 37, 37));
            button.setForeground(new Color(194, 189, 189));
            button.setFont(new Font("Quicksand Medium", Font.BOLD, 20));
            buttonsPanel.add(button);
        }

        // ADDING INSTUMENT NAMES TO THE BOX
        GridLayout instrumentsGrid = new GridLayout(16, 1);
        instrumentsGrid.setVgap(2);
        JPanel instruments = new JPanel(instrumentsGrid);
        instruments.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
        for (String instrument : instrumentNames) {
            JLabel label = new JLabel(instrument);
            label.setFont(new Font("Quicksand Medium", Font.BOLD, 15));
            label.setHorizontalAlignment(JLabel.RIGHT);
            instruments.add(label);
        }

        // ADDING ELEMS TO THE PANEL
        panel.add(BorderLayout.WEST, instruments);
        panel.add(BorderLayout.EAST, buttonsPanel);

        // ADDING PANEL TO THE FRAME
        frame.getContentPane().add(panel);

        // SET UP GRID PANEL AND CHECK BOXES
        GridLayout grid = new GridLayout(16, 16);
        grid.setVgap(1);
        grid.setHgap(2);
        mainPanel = new JPanel(grid);
        panel.add(BorderLayout.CENTER, mainPanel);

        // CREATING CHECKBOXES
        for (int i = 0; i < 256; i++) {
            JCheckBox checkBox = new JCheckBox();
            checkBox.setSelected(false);
            checkBoxes.add(checkBox);
            mainPanel.add(checkBox);
        }

        setUpMidi();

        // SETTING THE FRAME'S APPEREANCE
        frame.setTitle("BeatBox Maker");
        frame.setBounds(100, 100, 1480, 800);
        frame.pack();
        frame.setVisible(true);
    }
    public class startButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            buildTrackAndStart();
        }
    }
    public class stopButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            sequencer.stop();
        }
    }
    public class tempoUpListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            float tempoFactor = sequencer.getTempoFactor();
            sequencer.setTempoFactor((float) (tempoFactor * 1.03));
        }
    }
    public class tempoDownListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            float tempoFactor = sequencer.getTempoFactor();
            sequencer.setTempoFactor((float) (tempoFactor * 0.97));
        }
    }
    public void setUpMidi () {
        try {
            sequencer = MidiSystem.getSequencer();
            sequencer.open();
            sequence = new Sequence(Sequence.PPQ, 4);
            track = sequence.createTrack();
            sequencer.setTempoInBPM(120);
        } catch (MidiUnavailableException e) {
            throw new RuntimeException(e);
        } catch (InvalidMidiDataException e) {
            throw new RuntimeException(e);
        }
    }
    public void buildTrackAndStart () {
        int[] trackList = null;

        sequence.deleteTrack(track);
        track = sequence.createTrack();

        for (int i = 0; i < 16; i++) {
            trackList = new int[16];
            int key = instrumentsInts[i];
            for (int j = 0; j < 16; j++) {
                JCheckBox jc = (JCheckBox) checkBoxes.get(j + (16 * i));
                if (jc.isSelected()) {
                    trackList[j] = key;
                } else {
                    trackList[j] = 0;
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
        } catch (InvalidMidiDataException e) {
            throw new RuntimeException(e);
        }
    }

    public void makeTracks (int[] trackList) {
        for (int i = 0; i < 16; i++) {
            int key = trackList[i];
            if (key != 0) {
                track.add(makeEvent(144, 9, key, 100, i));
                track.add(makeEvent(128, 9, key, 100, i+1));
            }
        }
    }
    public MidiEvent makeEvent (int command, int channel, int num1, int num2, int tick) {
        MidiEvent event = null;
        try {
            ShortMessage message = new ShortMessage();
            message.setMessage(command, channel, num1, num2);
            event = new MidiEvent(message, tick);
        } catch (InvalidMidiDataException e) {
            throw new RuntimeException(e);
        }
        return event;
    }
}
