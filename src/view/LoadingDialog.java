package view;

import javax.swing.*;
import java.awt.*;

public class LoadingDialog extends JDialog {

    public LoadingDialog(JFrame parent, String message) {
        super(parent, true); // true = modal

        setUndecorated(true); // Removes window borders including close button


        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        panel.setBackground(Color.WHITE);
        panel.setPreferredSize(new Dimension(450, 500));

        JLabel label = new JLabel(message, SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.PLAIN, 16));
        label.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.add(label, BorderLayout.CENTER);

        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));
        panel.add(progressBar, BorderLayout.SOUTH);

        setContentPane(panel);
        pack();
        setLocationRelativeTo(parent);
    }
}
