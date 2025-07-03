package view;

import model.Game;
import model.player.Player;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;

public class DisplayFrame extends JFrame{
    DisplayBoard displayBoard;
    JLabel blueTimeLabel;
    JLabel redTimeLabel;
    JLabel moveTimeLabel;

    Player blue;
    Player red;
    double blueRemaining;
    double redRemaining;

    private Timer moveTimer;
    private Timer timer;
    private long moveStartTime;
    private boolean isBlueTurn;
    private final DecimalFormat timeFormat = new DecimalFormat("0.0");

    JLabel blueRoundsLabel;
    JLabel totalRoundsLabel;
    JLabel redRoundsLabel;
    JPanel turnColorPanel;

    public DisplayFrame(String FEN, Game game) {
        this.blue = game.getBlue();
        this.red = game.getRed();
        isBlueTurn = game.getPlayerOn().equals(blue);

        getContentPane().setBackground(Color.darkGray);
        setLayout(new GridBagLayout());
        setMinimumSize(new Dimension(677 ,716));
        setTitle("Murus Gallicus");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        displayBoard = new DisplayBoard(FEN);

        blueTimeLabel = new JLabel("Blue Time: " + blue.getRemainingTime() + "s");
        redTimeLabel = new JLabel("Red Time: " + red.getRemainingTime() + "s");
        moveTimeLabel = new JLabel("Move Time: "  + 0 + "s");

        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 15));
        centerPanel.setOpaque(false);
        centerPanel.add(moveTimeLabel);

        JPanel timePanel = new JPanel(new BorderLayout());
        timePanel.add(blueTimeLabel, BorderLayout.WEST);
        timePanel.add(centerPanel, BorderLayout.CENTER);
        timePanel.add(redTimeLabel, BorderLayout.EAST);


        /*JPanel timePanel = new JPanel(new GridLayout(1, 2));
        timePanel.add(blueTimeLabel);
        timePanel.add(redTimeLabel);
         */

        timePanel.setPreferredSize(new Dimension(677, 40));

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        add(timePanel, gbc);

        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        add(displayBoard, gbc);

        turnColorPanel = new JPanel();
        JLabel turnLabel = new JLabel("Turn: ");

        turnColorPanel.setPreferredSize(new Dimension(20, 20));
        turnColorPanel.setBackground(isBlueTurn ? Color.BLUE : Color.RED);
        turnColorPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        JPanel turnPanel = new JPanel();
        turnPanel.add(turnLabel);
        turnPanel.add(turnColorPanel);

        blueRoundsLabel = new JLabel("Blue Rounds: " + blue.getRounds());
        totalRoundsLabel = new JLabel("Total Rounds: " + (blue.getRounds() + red.getRounds()));
        redRoundsLabel = new JLabel("Red Rounds: " + red.getRounds());

        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 68, 5)){
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(677, 40); // fixed width & height
            }

            @Override
            public Dimension getMinimumSize() {
                return getPreferredSize();
            }

            @Override
            public Dimension getMaximumSize() {
                return getPreferredSize();
            }
        };
        //statusPanel.setOpaque(false);
        statusPanel.add(turnPanel);
        statusPanel.add(blueRoundsLabel);
        statusPanel.add(totalRoundsLabel);
        statusPanel.add(redRoundsLabel);

        gbc.gridy = 1;
        gbc.weighty = 0.0;
        add(displayBoard, gbc);

        gbc.fill = GridBagConstraints.NONE;
        gbc.gridy = 2;
        gbc.weighty = 0.0;
        add(statusPanel, gbc);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        timer = new Timer(200, e -> updateTimeLabels());
        timer.start();

        setupTimer();
    }

    public DisplayBoard getDisplayBoard() {
        return displayBoard;
    }

    private void updateTimeLabels() {
        blueRemaining = blue.getRemainingTime();
        redRemaining = red.getRemainingTime();

        blueTimeLabel.setText("Blue Time: " + String.format("%.1f", blueRemaining) + "s");
        redTimeLabel.setText("Red Time: " + String.format("%.1f", redRemaining) + "s");
    }

    private void setupTimer() {
        moveTimer = new Timer(100, e -> updateTimerLabel());
    }

    private void updateTimerLabel() {
        double elapsed = (System.currentTimeMillis() - moveStartTime) / 1000.0;
        String formatted = "Move Time: " +  timeFormat.format(elapsed) + "s";
        moveTimeLabel.setText(formatted);
    }

    public void startMoveTimer(boolean isBlue) {
        isBlueTurn = isBlue;
        moveStartTime = System.currentTimeMillis();
        moveTimer.start();
    }

    public void stopMoveTimer() {
        moveTimer.stop();
    }

    public void stopTimer(){ timer.stop();}

    public void updateRoundInfo(boolean isBlueTurnNow) {
        isBlueTurn = isBlueTurnNow;
        turnColorPanel.setBackground(isBlueTurn ? Color.BLUE : Color.RED);
        totalRoundsLabel.setText("Total Rounds: " + (blue.getRounds() + red.getRounds()));
        blueRoundsLabel.setText("Blue Rounds: " + blue.getRounds());
        redRoundsLabel.setText("Red Rounds: " + red.getRounds());
    }

}
