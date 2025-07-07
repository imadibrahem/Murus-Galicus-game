package model;

import model.bit.BitBoard;
import model.evaluationFunction.EvaluationFunction;
import model.evaluationFunction.InitialEvaluationFunction;
import model.evaluationFunction.SuperEvaluationFunction;
import model.move.MoveGeneratorEvolutionTheory;
import model.move.Move;
import model.move.MoveGeneratingStyle;
import model.move.MoveGenerator;
import model.move.MoveType;
import model.player.*;
import view.DisplayBoard;
import view.DisplayFrame;
import view.GameOverWindow;
import view.UserInput;

import java.awt.*;
import java.util.*;
import java.util.List;

public class Game {
    String initialFEN;
    String FEN;
    DisplayFrame displayFrame;
    DisplayBoard displayBoard;
    Board blueBoard;
    Board redBoard;
    Board generalBoard;
    Player red;
    Player blue;
    Player playerOn;
    UserInput userInput;
    double playTime;
    List<Short> history;
    Deque<Move> moves;
    List<Move> blueMoves;
    List<Move> redMoves;
    List<Long> hashes;
    List<Long> blueHashes;
    List<Long> redHashes;
    int rounds = 0;
    Player winner;
    String winnerColor= "";
    short oldMoveInitial = 0;
    short oldMoveFirst = 0;
    short oldMoveSecond = 0;
    double blueDurationMean = 0;
    double redDurationMean = 0;
    double blueNodesMean = 0;
    double redNodesMean = 0;
    double gameDuration = 0;
    int [] winnerReport = new int[3];
    Scanner scanner = new Scanner(System.in);

    public Game(UserInput userInput, Player red, Player blue, String initialFEN, double playTime) {
        this.red = red;
        this.blue = blue;
        this.initialFEN =initialFEN;
        this.playTime = playTime;
        generalBoard = new BitBoard(FenTrimmer(initialFEN));
        history = new ArrayList<>();
        moves = new ArrayDeque<>();
        blueMoves = new ArrayList<>();
        redMoves = new ArrayList<>();
        hashes = new ArrayList<>();
        blueHashes = new ArrayList<>();
        redHashes = new ArrayList<>();
        setFenAndPlayerOn();
        displayFrame = new DisplayFrame(FEN, this);
        displayBoard = displayFrame.getDisplayBoard();
        this.userInput = userInput;
        userInput.setDisplayBoard(displayBoard);
        setPlayer(blue);
        setPlayer(red);
        winner = null;
    }

    public static String FenTrimmer (String initialFEN){
        String [] parts = initialFEN.split(",");
        return parts[0];
    }

    public void setFenAndPlayerOn(){
        String [] parts = initialFEN.split(",");
        FEN = parts[0];
        playerOn = parts[1].equals("b") ? blue : red;
        playerOn.setOn(true);
        short startingPlayer = (short) (playerOn.isEvaluationBlue() ? 1 : 0);
        history.add(startingPlayer);
    }

    public void setPlayer(Player player){
        if (player.isEvaluationBlue()) {
            this.blue = player;
            this.blueBoard = blue.getBoard();
            if (player instanceof User && playerOn.isEvaluationBlue())userInput.setPlayer(blue);

        }
        else{
            this.red = player;
            this.redBoard = red.getBoard();
            if (player instanceof User && !(playerOn.isEvaluationBlue()))userInput.setPlayer(red);

        }
    }

    public void switchPlayer() {
        playerOn = playerOn.isEvaluationBlue()? red : blue;
        playerOn.getBoard().build(FEN);
        blue.switchTurn();
        red.switchTurn();
        if (playerOn instanceof User)userInput.setPlayer(playerOn);
    }

    public void playRound() {
        rounds++;
        displayFrame.startMoveTimer(playerOn.isBlue());
        Move move = playerOn.findMove();
        if (!generalBoard.generateMoves(playerOn.isEvaluationBlue()).contains(move)) {
            System.out.println("Invalid Move!! Player lost");
            winner = playerOn.isEvaluationBlue()? red : blue;
        }
        else{
            //addMove(move);
            //history.add(move.getValue());
            playerOn.makeMove(move);
            //addHashes();
            generalBoard.makeMove(move,playerOn.isEvaluationBlue());
            FEN = generalBoard.generateFEN();
            colorOldMove(playerOn.isEvaluationBlue(), move);
            displayBoard.updateBoard(FEN);
        }
        displayFrame.stopMoveTimer();

    }

    public void withdrawRound() {
        rounds--;
        Move move = moves.pollLast();
        moves.remove(move);
        history.remove(history.get(history.size()-1));
        playerOn.unmakeMove(move);
        FEN = playerOn.getBoard().generateFEN();
        displayBoard.updateBoard(FEN);
    }

    public void makeRound(Move move) {
        rounds++;
        //System.out.println(move);
        //addMove(move);
        //history.add(move.getValue());
        playerOn.makeMove(move);
        FEN = playerOn.getBoard().generateFEN();
        colorOldMove(playerOn.isEvaluationBlue(), move);
        displayBoard.updateBoard(FEN);
    }

    public void unmakeRound(Move move) {
        rounds--;
        moves.remove(move);
        history.remove(history.get(history.size()-1));
        playerOn.unmakeMove(move);
        FEN = playerOn.getBoard().generateFEN();
        displayBoard.updateBoard(FEN);
    }


    public void playGame() {
        while (winner == null){
            playRound();
            switchPlayer();
            displayFrame.updateRoundInfo(playerOn.isBlue());
            checkForWinner();
        }
    }

    public void rewindGame() {
        playerOn = playerOn.isEvaluationBlue()? red : blue;
        playerOn.getBoard().build(FEN);
        blue.switchTurn();
        red.switchTurn();
        while (rounds > 0){
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Move move = moves.pollLast();
            System.out.println(move);
            playerOn.unmakeMove(move);
            colorOldMove(playerOn.isEvaluationBlue(), move);
            FEN = playerOn.getBoard().generateFEN();
            displayBoard.updateBoard(FEN);
            playerOn = playerOn.isEvaluationBlue()? red : blue;
            playerOn.getBoard().build(FEN);
            blue.switchTurn();
            red.switchTurn();
            rounds--;

        }
    }

    public void replayGame() {
        String [] parts = initialFEN.split(",");
        FEN = parts[0];
        playerOn = parts[1].equals("b") ? blue : red;
        Player playerOff = parts[1].equals("b") ? red : blue;
        playerOff.setOn(false);
        playerOn.setOn(true);
        red.getBoard().build(FEN);
        blue.getBoard().build(FEN);
        displayBoard.updateBoard(FEN);
        while (!moves.isEmpty()){
            /*
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

             */
            Move move = moves.pollFirst();
            // System.out.println(move);
            scanner.nextLine();
            playerOn.makeMove(move);
            FEN = playerOn.getBoard().generateFEN();
            displayBoard.updateBoard(FEN);
            playerOn = playerOn.isEvaluationBlue()? red : blue;
            playerOn.getBoard().build(FEN);
            blue.switchTurn();
            red.switchTurn();
        }
    }

    public void checkForWinner(){
        if (playerOn.isEvaluationBlue()){
            if (playerOn.getDuration() > playTime || blueBoard.lostGame(true)) winner = red;
            else if (redBoard.lostGame(false)) winner = blue;
        }
        else {
            if (playerOn.getDuration() > playTime || redBoard.lostGame(false)) winner = blue;
            else if (blueBoard.lostGame(true)) winner = red;
        }
        if (winner != null){
            displayFrame.stopMoveTimer();
            displayFrame.stopTimer();
            System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++");
            System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++");
            if (winner == blue){
                System.out.println("+++++++++++ GAME OVER BLUE PLAYER WON!! ++++++++++++++");
                winnerColor = "Blue";
            }
            else{
                System.out.println("+++++++++++ GAME OVER RED PLAYER WON!! +++++++++++++++");
                winnerColor = "Red";
            }
            System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++");
            System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++");
            displayFrame.dispose();
            blue.updateTables();
            red.updateTables();
            new GameOverWindow(winnerColor, (blue.getRounds() + red.getRounds()));
            printGameResults();
        }
    }

    public void printGameResults(){
        System.out.println();
        System.out.println("++++++++++++ Blue Player played : "+ blue.getRounds() +" rounds ++++++++++++");
        System.out.println("++++++++++++ Blue Player Nodes: "+ blue.getNodes() +" ++++++++++++++++");
        System.out.println("Blue Player Moves Nodes: "+ blue.getMovesNodes());
        System.out.println();
        System.out.println("++++++++++ Blue Player Duration: "+ blue.getDuration() +" ++++++++++++++");
        System.out.println("Blue Player Moves Durations: "+ blue.getMoveDurations());
        System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        System.out.println();
        System.out.println("++++++++++++ Red Player played : "+ red.getRounds() +" rounds ++++++++++++");
        System.out.println("++++++++++++ Red Player Nodes: "+ red.getNodes() +" ++++++++++++++++");
        System.out.println("Red Player Moves Nodes: "+ red.getMovesNodes());
        System.out.println();
        System.out.println("++++++++++ Red Player Duration: "+ red.getDuration() +" ++++++++++++++");
        System.out.println("Red Player Moves Durations: "+ red.getMoveDurations());
        System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        System.out.println();
        blueDurationMean = blue.getDuration()/blue.getRounds();
        redDurationMean = red.getDuration()/red.getRounds();
        blueNodesMean =  blue.getNodes()/(double)blue.getRounds();
        redNodesMean = red.getNodes()/(double)red.getRounds();
        gameDuration = blue.getDuration() + red.getDuration();
        int gameDurationHours = (int) (gameDuration / 3600);
        int gameDurationMinutes = (int) ((gameDuration % 3600) / 60);
        double gameDurationSeconds = gameDuration % 60;
        System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        System.out.println("Blue Duration Mean: " + blueDurationMean + " ||| Red Duration Mean: " + redDurationMean);
        System.out.println("Blue Nodes Mean: " + blueNodesMean + " ||| Red Nodes Mean: " + redNodesMean);
        System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        System.out.println();
        System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        System.out.println("gameDuration: " + gameDurationHours + " hours, " + gameDurationMinutes + " minutes, " + String.format("%.2f", gameDurationSeconds) + " seconds");
        System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        System.out.println();
        System.out.println("++++++++++++++++++ Tables report +++++++++++++++++++++ ");
        System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        System.out.println("Blue Table: " + blue.tableUsageReport());
        System.out.println("Red Table: " + red.tableUsageReport());
        System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        System.out.println();
        if (winner == blue){
            winnerReport[0] = blue.getRounds();
            winnerReport[1] = (int) blueNodesMean;
            winnerReport[2] = (int) (blueDurationMean * 1000);
        }
        else {
            winnerReport[0] = red.getRounds();
            winnerReport[1] = (int) redNodesMean;
            winnerReport[2] = (int) (redDurationMean * 1000);
        }
    }

    public void makeAndUnmakeAllMoves(){
        Scanner scanner = new Scanner(System.in);
        List<Move> allMoves = playerOn.getMoveGenerator().generateMoves(playerOn.isEvaluationBlue());
        for (Move move : allMoves){
            String before = playerOn.getBoard().printBoard(playerOn.isEvaluationBlue());
            System.out.println("Next Move: " + move + " Press to make");
            scanner.nextLine();
            makeRound(move);
            System.out.println("Move was: " + move + " Press to unmake");
            scanner.nextLine();
            unmakeRound(move);
            String after = playerOn.getBoard().printBoard(playerOn.isEvaluationBlue());
            if (!before.equals(after)){
                System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                System.out.println("!!!!!!!!!!!!!!!!!!!! BEFORE !!!!!!!!!!!!!!!!!!!!!!!!!");
                System.out.println(before);
                System.out.println("!!!!!!!!!!!!!!!!!!!! AFTER !!!!!!!!!!!!!!!!!!!!!!!!!!");
                System.out.println(after);
                System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            }
            System.out.println();
        }
        System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        System.out.println();
    }

    public void addMove(Move move){
        if (playerOn.isEvaluationBlue()) {
            blueMoves.add(move);
        } else {
            redMoves.add(move);
        }
        //System.out.println(move);
        moves.add(move);
    }

    public void addHashes(){
        if (playerOn.isEvaluationBlue()) {
            blueHashes.add(playerOn.getZobristHashing().getHash());
        } else {
            redHashes.add(playerOn.getZobristHashing().getHash());
        }
        hashes.add(playerOn.getZobristHashing().getHash());
    }


    public void colorOldMove(boolean isBlue, Move move){
        displayBoard.displaySquare[oldMoveInitial].returnOldColor();
        displayBoard.displaySquare[oldMoveFirst].returnOldColor();
        displayBoard.displaySquare[oldMoveSecond].returnOldColor();
        int initial = move.getInitialLocation(isBlue);
        oldMoveInitial = (short) initial;
        if (move.isTargetEnemy()){
            oldMoveFirst = playerOn.getBoard().sacrificingMovesLocation(isBlue, initial,move.getDirection());
            displayBoard.displaySquare[oldMoveFirst].changeColor(Color.orange);
        }
        else {
            short[] targets = playerOn.getBoard().normalMovesLocation(isBlue, initial,move.getDirection());
            oldMoveFirst = targets[0];
            oldMoveSecond = targets[1];
            displayBoard.displaySquare[oldMoveSecond].changeColor(Color.orange);
        }
        displayBoard.displaySquare[oldMoveFirst].changeColor(Color.orange);
        displayBoard.displaySquare[oldMoveInitial].changeColor(Color.BLUE);

    }

    public Player getWinner() {
        return winner;
    }

    public int[] getWinnerReport() {
        return winnerReport;
    }

    public Player getRed() {
        return red;
    }

    public Player getBlue() {
        return blue;
    }

    public Player getPlayerOn() {
        return playerOn;
    }
}
