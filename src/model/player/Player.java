package model.player;

import model.Board;
import model.transpositionTable.ZobristHashing;
import model.evaluationFunction.EvaluationFunction;
import model.move.Move;
import model.move.MoveGenerator;

import java.util.*;

public abstract class Player {
    protected boolean isBlue;
    protected final boolean isEvaluationBlue;
    protected final Board board;
    protected boolean isOn = false;
    protected final EvaluationFunction evaluationFunction;
    protected int rounds = 0;
    protected int nodes = 0;
    protected int moveNodes = 0;
    protected List<Integer> movesNodes = new ArrayList<>();
    protected double moveStartTime;
    protected double duration = 0;
    protected double moveDuration;
    protected double remainingTime;
    protected List<Double> moveDurations = new ArrayList<>();
    protected Map<Integer, Integer> directionMap = new HashMap<>();
    protected MoveGenerator moveGenerator;
    protected final ZobristHashing zobristHashing;
    protected static final int MAX_DEPTH = 15;
    protected String name;

    public Player(boolean isBlue,Board board, EvaluationFunction evaluationFunction) {
        this.isBlue = isBlue;
        this.board = board;
        this.isEvaluationBlue = isBlue;
        this.evaluationFunction = evaluationFunction;
        zobristHashing = new ZobristHashing(board, isEvaluationBlue);
        directionMap.put(-9, 8);
        directionMap.put(-8, 1);
        directionMap.put(-7, 2);
        directionMap.put(-1, 7);
        directionMap.put(1, 3);
        directionMap.put(7, 6);
        directionMap.put(8, 5);
        directionMap.put(9, 4);
    }

    public Player(boolean isBlue,Board board,MoveGenerator moveGenerator, EvaluationFunction evaluationFunction) {
        this.isBlue = isBlue;
        this.board = board;
        this.isEvaluationBlue = isBlue;
        this.moveGenerator = moveGenerator;
        this.evaluationFunction = evaluationFunction;
        zobristHashing = new ZobristHashing(board, isEvaluationBlue);
        directionMap.put(-9, 8);
        directionMap.put(-8, 1);
        directionMap.put(-7, 2);
        directionMap.put(-1, 7);
        directionMap.put(1, 3);
        directionMap.put(7, 6);
        directionMap.put(8, 5);
        directionMap.put(9, 4);
    }

    public int getRounds() {
        return rounds;
    }

    public double getDuration() {
        return duration;
    }

    public List<Double> getMoveDurations() {
        return moveDurations;
    }

    public int getNodes() {
        return nodes;
    }

    public List<Integer> getMovesNodes() {
        return movesNodes;
    }

    public boolean isBlue() {
        return isBlue;
    }

    public boolean isEvaluationBlue() {
        return isEvaluationBlue;
    }

    public boolean isOn() {
        return isOn;
    }

    public Board getBoard() {
        return board;
    }

    public MoveGenerator getMoveGenerator() {
        return moveGenerator;
    }

    public EvaluationFunction getEvaluationFunction() {
        return evaluationFunction;
    }

    public ZobristHashing getZobristHashing() {
        return zobristHashing;
    }

    public String getName() {
        return name;
    }

    public void switchColor() {
        isBlue = !isBlue;
    }

    public void setOn(boolean on) {
        isOn = on;
    }

    public void switchTurn(){
        isOn = !isOn;
    }

    public void makeMove(Move move){
        board.makeMove(move, isBlue);
    }

    public void unmakeMove(Move move){
        board.unmakeMove(move, isBlue);
    }

    public double getRemainingTime() {
        return remainingTime;
    }

    public int priority(int depth, float roundsFactor, float towersFactor, float distancesFactor){
        int roundsPlayed = rounds + depth;
        int towersNumber = board.towersNumber(isEvaluationBlue) + board.towersNumber(!isEvaluationBlue);
        int[] values = new int[]{0, 1, 2, 3, 4, 5, 6};
        int piecesNumber = towersNumber + board.wallsNumber(isEvaluationBlue) + board.wallsNumber(!isEvaluationBlue);
        int distances = board.towersDistances(isEvaluationBlue, values) + board.wallsDistances(isEvaluationBlue, values)
                + board.towersDistances(!isEvaluationBlue, values) + board.wallsDistances(!isEvaluationBlue, values);
        int distancesMean = distances / piecesNumber;
        int result = (int) ((roundsPlayed * roundsFactor) + (towersNumber * towersFactor) + (distancesMean * distancesFactor));
        if (result > 15) result = 15;
        else if (result < 0) result = 0;
        return result;
    }

    public int memoryPriority(int depth, float roundsFactor, float towersFactor, float distancesFactor){
        float roundResult = depth - (roundsFactor * rounds);
        int towersNumber = board.towersNumber(isEvaluationBlue) + board.towersNumber(!isEvaluationBlue);
        int[] values = new int[]{0, 1, 2, 3, 4, 5, 6};
        int piecesNumber = towersNumber + board.wallsNumber(isEvaluationBlue) + board.wallsNumber(!isEvaluationBlue);
        int distances = board.towersDistances(isEvaluationBlue, values) + board.wallsDistances(isEvaluationBlue, values)
                + board.towersDistances(!isEvaluationBlue, values) + board.wallsDistances(!isEvaluationBlue, values);
        int distancesMean = distances / piecesNumber;
        int result = (int) (roundResult + (towersNumber * towersFactor) + (distancesMean * distancesFactor));
        if (result > 15) result = 15;
        else if (result < 0) result = 0;
        return result;
    }

    public String tableUsageReport(){
        return "No table found for this player..";
    }

    public void updateTables(){

    }

    public Move recieveCords(int initial , int targetNear, int targetFar){
        int location = this.isEvaluationBlue() ? initial : 55 - initial;
        int distance = isEvaluationBlue() ? targetNear - initial :initial - targetNear;
        int direction = directionMap.get(distance);
        int targetType;
        if (targetFar < 0) targetType = 4;
        else {
            boolean friendlyNear = getBoard().isFriendlyPiece(isEvaluationBlue(), targetNear);
            boolean friendlyFar = getBoard().isFriendlyPiece(isEvaluationBlue(), targetFar);
            if (!friendlyNear && !friendlyFar) targetType = 0;
            else if (friendlyNear && friendlyFar) targetType = 3;
            else if (friendlyNear) targetType = 1;
            else targetType = 2;
        }
        return new Move((short) (location << 7 | direction << 3 | targetType));
    }

    public Move findMove(){
        rounds++;
        moveStartTime = System.currentTimeMillis();
        Move move = decideMove();
        moveDuration = (System.currentTimeMillis() - moveStartTime) / 1000;
        moveDurations.add(moveDuration);
        duration += moveDuration;
        remainingTime -= moveDuration;
        //System.out.println(remainingTime);
        return move;
    }
    public abstract Move decideMove();

}
