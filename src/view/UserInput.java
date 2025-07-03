package view;

import model.move.Move;
import model.player.Player;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;

public class UserInput implements MouseListener, MouseMotionListener {
    DisplayBoard displayBoard;
    Player player;
    boolean isInitialChosen = false;
    boolean isChoosing = false;
    List<Short> sacrificingMovesLocations;
    List<Short> normalMovesLocations;
    List<Integer> nearDistances = new ArrayList<>();
    int initial = -1;
    int targetNear = - 1;
    int targetFar = -1;


    public UserInput() {
        //this.displayBoard = displayBoard;
        //displayBoard.addMouseListener(this);
        nearDistances.add(-9);
        nearDistances.add(-8);
        nearDistances.add(-7);
        nearDistances.add(-1);
        nearDistances.add(1);
        nearDistances.add(7);
        nearDistances.add(8);
        nearDistances.add(9);

    }

    public void setDisplayBoard(DisplayBoard displayBoard) {
        this.displayBoard = displayBoard;
        displayBoard.addMouseListener(this);

    }

    public void setPlayer(Player player) {
        this.player = player;
        if (player != null) isChoosing = true;
    }

    public boolean isChoosing() {
        return isChoosing;
    }

    public void setChoosing(boolean choosing) {
        isChoosing = choosing;
    }

    public int getInitial() {
        return initial;
    }

    public int getTargetNear() {
        return targetNear;
    }

    public int getTargetFar() {
        return targetFar;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (player.isOn()){
            int squareLocation = (e.getX() / displayBoard.tileSize) + ((e.getY() / displayBoard.tileSize) * 8);
            if (isInitialChosen && isChoosing){
                for (short sacrificing : sacrificingMovesLocations)displayBoard.displaySquare[sacrificing].returnOldColor();
                for (short normal : normalMovesLocations)displayBoard.displaySquare[normal].returnOldColor();
                displayBoard.displaySquare[initial].returnOldColor();
                isInitialChosen = false;
                displayBoard.repaint();
                if (normalMovesLocations.contains((short)squareLocation)){
                    if (nearDistances.contains(squareLocation - initial)) {
                        targetNear = squareLocation;
                        targetFar = targetNear + squareLocation - initial;
                    }
                    else{
                        targetFar = squareLocation;
                        targetNear = initial + ((squareLocation - initial) / 2);
                    }
                    isChoosing = false;
                    //System.out.println("Input :: initial :" + initial + "");
                }
                else if (sacrificingMovesLocations.contains((short)squareLocation)){
                    targetNear = squareLocation;
                    targetFar = -1;
                    isChoosing = false;
                }
                sacrificingMovesLocations.clear();
                normalMovesLocations.clear();

            }
            if (isChoosing && player.getBoard().isFriendlyTower(player.isEvaluationBlue(), squareLocation)){
                displayBoard.displaySquare[squareLocation].changeColor(Color.CYAN);
                sacrificingMovesLocations = player.getBoard().sacrificingMovesLocations(player.isEvaluationBlue(), squareLocation, 1);
                normalMovesLocations = player.getBoard().normalMovesLocations(player.isEvaluationBlue(), squareLocation, 1);
                for (short sacrificing : sacrificingMovesLocations)displayBoard.displaySquare[sacrificing].changeColor(Color.RED);
                for (short normal : normalMovesLocations)displayBoard.displaySquare[normal].changeColor(Color.GREEN);
                initial = squareLocation;
                targetNear = -1;
                targetFar = -1;
                isInitialChosen = true;
            }

        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (player.isOn()){
            int squareLocation = (e.getX() / displayBoard.tileSize) + ((e.getY() / displayBoard.tileSize) * 8);
            if (isInitialChosen && isChoosing){
                for (short sacrificing : sacrificingMovesLocations)displayBoard.displaySquare[sacrificing].returnOldColor();
                for (short normal : normalMovesLocations)displayBoard.displaySquare[normal].returnOldColor();
                displayBoard.displaySquare[initial].returnOldColor();
                isInitialChosen = false;
                displayBoard.repaint();
                if (normalMovesLocations.contains((short)squareLocation)){
                    if (nearDistances.contains(squareLocation - initial)) {
                        targetNear = squareLocation;
                        targetFar = targetNear + squareLocation - initial;
                    }
                    else{
                        targetFar = squareLocation;
                        targetNear = initial + ((squareLocation - initial) / 2);
                    }
                    isChoosing = false;
                    //System.out.println("Input :: initial :" + initial + "");
                }
                else if (sacrificingMovesLocations.contains((short)squareLocation)){
                    targetNear = squareLocation;
                    targetFar = -1;
                    isChoosing = false;
                }

                sacrificingMovesLocations.clear();
                normalMovesLocations.clear();

            }
            if (isChoosing && player.getBoard().isFriendlyTower(player.isEvaluationBlue(), squareLocation)){
                displayBoard.displaySquare[squareLocation].changeColor(Color.CYAN);
                sacrificingMovesLocations = player.getBoard().sacrificingMovesLocations(player.isEvaluationBlue(), squareLocation, 1);
                normalMovesLocations = player.getBoard().normalMovesLocations(player.isEvaluationBlue(), squareLocation, 1);
                for (short sacrificing : sacrificingMovesLocations)displayBoard.displaySquare[sacrificing].changeColor(Color.RED);
                for (short normal : normalMovesLocations)displayBoard.displaySquare[normal].changeColor(Color.GREEN);
                initial = squareLocation;
                targetNear = -1;
                targetFar = -1;
                isInitialChosen = true;
            }
        }

    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (player.isOn()) {
            int squareLocation = (e.getX() / displayBoard.tileSize) + ((e.getY() / displayBoard.tileSize) * 8);
            if (isInitialChosen && isChoosing){
                for (short sacrificing : sacrificingMovesLocations)displayBoard.displaySquare[sacrificing].returnOldColor();
                for (short normal : normalMovesLocations)displayBoard.displaySquare[normal].returnOldColor();
                displayBoard.displaySquare[initial].returnOldColor();
                isInitialChosen = false;
                displayBoard.repaint();
                if (normalMovesLocations.contains((short)squareLocation)){
                    if (nearDistances.contains(squareLocation - initial)) {
                        targetNear = squareLocation;
                        targetFar = targetNear + squareLocation - initial;
                    }
                    else{
                        targetFar = squareLocation;
                        targetNear = initial + ((squareLocation - initial) / 2);
                    }
                    isChoosing = false;
                    //System.out.println("Input :: initial :" + initial + "");
                }
                else if (sacrificingMovesLocations.contains((short)squareLocation)){
                    targetNear = squareLocation;
                    targetFar = -1;
                    isChoosing = false;
                }

                sacrificingMovesLocations.clear();
                normalMovesLocations.clear();

            }
        }
    }

    public Move decideMove() {
        while (isChoosing) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

       // System.out.println("DONE!!!!!" + getInitial() + " " + getTargetNear() + " " + getTargetFar());
        return player.recieveCords(initial, targetNear, targetFar);
    }


    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }
}
