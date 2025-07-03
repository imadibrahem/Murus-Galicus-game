package view;


import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class DisplayBoard extends JPanel {
    int tileSize = 85;
    int rows = 7;
    int cols = 8;
    public DisplaySquare[] displaySquare;
    String FEN;
    DisplayPieces displayPieces;
    Border border = BorderFactory.createLineBorder(Color.black,2);

    public DisplayBoard(String FEN) {
        this.setPreferredSize(new Dimension(cols * tileSize, rows*tileSize));
        this.FEN = FEN;
        //this.setLayout(null);
        setLayout(new GridBagLayout());
        this.setBorder(border);
        displayPieces = new DisplayPieces(FEN);
        displaySquare = new DisplaySquare[56];

        for (int r = 0; r < rows; r++){
            for (int c = 0; c < cols; c++){
                displaySquare[(8 * r) + c] = new DisplaySquare((c * tileSize) , (r * tileSize) , tileSize, tileSize);
                this.add(displaySquare[(8 * r) + c]);
               // displaySquare[(8 * r) + c].setText(""+(8 * r) + c);
            }
        }
        this.add(displayPieces);
        //setVisible(true);
    }

    public void paintComponent(Graphics g){
        Graphics2D g2d = (Graphics2D) g;
        for (int s = 0; s < displaySquare.length; s++) this.displaySquare[s].paintComponent(g2d);
        this.displayPieces.paintPieces(g2d);
    }

    public void updateBoard(String FEN){
        this.displayPieces.updateBoard(FEN);
        repaint();
    }
}
