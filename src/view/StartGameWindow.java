package view;

import model.GameMaker;
import model.PlayerType;
import model.Starter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

@SuppressWarnings("serial")
public class StartGameWindow extends JFrame {

    private JComboBox<String> bluePlayerBox;
    private JComboBox<String> redPlayerBox;
    private JComboBox<String> timeBox;

    public StartGameWindow() {
        setTitle("Start Game");
        setSize(677, 716);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        String[] playerTypes = {"User", "Easy", "Medium", "Vercingetorix", "Caesar"};
        String[] timeOptions = {
                "2:00", "2:30", "3:00", "3:30", "4:00", "4:30", "5:00"
        };

        JLabel blueLabel = new JLabel("Blue Player:");
        JLabel redLabel = new JLabel("Red Player:");
        JLabel timeLabel = new JLabel("Play Time (min:sec):");

        bluePlayerBox = new JComboBox<>(playerTypes);
        redPlayerBox = new JComboBox<>(playerTypes);
        timeBox = new JComboBox<>(timeOptions);

        JButton startButton = new JButton("Start Game");
        startButton.addActionListener(new StartButtonListener());

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(40, 40, 40, 40);
        gbc.gridx = 0; gbc.gridy = 0; add(blueLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 0; add(bluePlayerBox, gbc);
        gbc.gridx = 0; gbc.gridy = 1; add(redLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 1; add(redPlayerBox, gbc);
        gbc.gridx = 0; gbc.gridy = 2; add(timeLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 2; add(timeBox, gbc);
        gbc.gridwidth = 2; gbc.gridx = 0; gbc.gridy = 3; add(startButton, gbc);

        setVisible(true);
    }

    private class StartButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            PlayerType blueType = PlayerType.valueOf(((String) bluePlayerBox.getSelectedItem()).toUpperCase());
            PlayerType redType = PlayerType.valueOf(((String) redPlayerBox.getSelectedItem()).toUpperCase());

            String selectedTime = (String) timeBox.getSelectedItem();
            double totalSeconds = parseTimeToSeconds(selectedTime);

            System.out.println("Starting game:");
            System.out.println("Blue Player: " + blueType);
            System.out.println("Red Player: " + redType);
            System.out.println("Time per Player: " + totalSeconds + " seconds");
            dispose();
            //StartGameWindow.this.setVisible(false);
            //new Thread(() -> new GameMaker(blueType, redType, totalSeconds)).start();
            Thread t = new Thread(() -> new GameMaker(blueType, redType, totalSeconds));
            //t.setDaemon(true);
            t.start();

        }
    }

    private double parseTimeToSeconds(String timeStr) {
        String[] parts = timeStr.split(":");
        return Double.parseDouble(parts[0]) * 60 + Double.parseDouble(parts[1]);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(StartGameWindow::new);
    }
}
