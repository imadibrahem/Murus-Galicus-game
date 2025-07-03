package view;

import javax.swing.*;
import java.awt.*;

public class GameOverWindow extends JFrame {
    public GameOverWindow(String winner, int totalRounds) {
        setTitle("Game Over");
        setSize(677, 716);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // Message
        JLabel message = new JLabel("<html><center>Game Over!<br/>Winner: " + winner +
                "<br/>Total Rounds: " + totalRounds + "</center></html>", SwingConstants.CENTER);
        message.setFont(new Font("Arial", Font.BOLD, 16));

        // Buttons
        JButton newGameButton = new JButton("New Game");
        newGameButton.addActionListener(e -> {
            new StartGameWindow();  // Your start window class
            dispose();              // Close the GameOverWindow
        });

        JButton exitButton = new JButton("Exit");
        exitButton.addActionListener(e -> System.exit(0));

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(newGameButton);
        buttonPanel.add(exitButton);

        // Layout
        setLayout(new BorderLayout());
        add(message, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        setVisible(true);
    }
}
