package view;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

public class DisplayPieces extends JPanel {
    String FEN;
    int tileSize = 85;
    BufferedImage blueWallSheet;
    BufferedImage blueTowerSheet;
    BufferedImage redWallSheet;
    BufferedImage redTowerSheet;
    Image blueWall;
    Image blueTower;
    Image redWall;
    Image redTower;
    {
        try {
            blueTowerSheet = ImageIO.read(Objects.requireNonNull(ClassLoader.getSystemResourceAsStream("blueTower.png")));
            blueTower = blueTowerSheet.getScaledInstance(tileSize, tileSize, BufferedImage.SCALE_SMOOTH);
            blueWallSheet = ImageIO.read(Objects.requireNonNull(ClassLoader.getSystemResourceAsStream("blueWall.png")));
            blueWall = blueWallSheet.getScaledInstance(tileSize, tileSize, BufferedImage.SCALE_SMOOTH);
            redTowerSheet = ImageIO.read(Objects.requireNonNull(ClassLoader.getSystemResourceAsStream("redTower.png")));
            redTower = redTowerSheet.getScaledInstance(tileSize, tileSize, BufferedImage.SCALE_SMOOTH);
            redWallSheet = ImageIO.read(Objects.requireNonNull(ClassLoader.getSystemResourceAsStream("redWall.png")));
            redWall = redWallSheet.getScaledInstance(tileSize, tileSize, BufferedImage.SCALE_SMOOTH);

        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public DisplayPieces(String FEN) {
        this.setPreferredSize(new Dimension(8 * tileSize, 7*tileSize));
        this.FEN = FEN;
        //setOpaque(true);
    }

    public void paintComponent(Graphics g){
        Graphics2D g2d = (Graphics2D) g;
        paintPieces(g2d);

    }
    public void paintPieces(Graphics2D g2d){
        String [] rows = FEN.split("[/\\s]+");
        int squareCol;
        for (int r = 0; r < 7; r++){
            squareCol = 0;
            for (int c = 0; c < rows[r].length(); c++){
                if (Character.isDigit(rows[r].charAt(c))){
                    squareCol += Character.getNumericValue(rows[r].charAt(c));
                }

                else {
                    if (rows[r].charAt(c) == 'w') g2d.drawImage(redWall, (squareCol * tileSize) + 1, (r * tileSize) + 1, null);
                    else if (rows[r].charAt(c) == 'W') g2d.drawImage(blueWall, (squareCol * tileSize) + 1, (r * tileSize) + 1, null);
                    else if (rows[r].charAt(c) == 't') g2d.drawImage(redTower, (squareCol * tileSize) + 1, (r * tileSize) + 1, null);
                    else g2d.drawImage(blueTower, (squareCol * tileSize) + 1, (r * tileSize) + 1, null);
                    squareCol++;

                }

            }
        }
    }
    public void updateBoard(String FEN){
        this.FEN = FEN;
        //repaint();
    }
}
