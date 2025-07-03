package view;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class DisplaySquare extends JLabel {
    final int x, y, width, height;
    final Color lightColor = new Color(208, 207, 147);
    final Color darkColor = new Color(86, 77, 41);
    Color color;
    Border border = BorderFactory.createLineBorder(Color.black,1);
    //Border border = BorderFactory.createBevelBorder(1);

    DisplaySquare(int x, int y, int width, int height){
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.color = ((x + y) / 85) % 2 == 0 ? lightColor : darkColor;
        this.setBounds(x, y, width, height);
        this.setBorder(border);
        //setOpaque(true);

    }
    public void paintComponent(Graphics g){
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(color);
        g2d.fillRect(x, y, width, height);
        border.paintBorder(this, g2d, this.x, this.y, this.width, this.height);
    }

    public void changeColor(Color color){
        this.color = color;
        repaint();
    }

    public void returnOldColor(){
        this.color = ((x + y) / 85) % 2 == 0 ? lightColor : darkColor;
    }
}
