package model.evaluationFunction;

import model.Board;
import view.DisplayBoard;
import view.DisplayFrame;

public class EvaluationFunction {

    protected final Board board;
    private final int[] wallsDistancesFactor;
    private final int[] wallsColumnsFactor;
    private final int[] towersDistancesFactor;
    private final int[] towersColumnsFactor;
    private final int[] towersRatioFactor;
    private final int[] gameStateFactor;
    private final int mobilityFactor;
    private final int isolatedTowersFactor;
    private final int isolatedWallsFactor;
    private static final int winScore = 8191;
    private static final int loseScore = -8191;


    public EvaluationFunction(Board board, int[] wallsDistancesFactor, int[] wallsColumnsFactor, int[] towersDistancesFactor, int[] towersColumnsFactor, int[] towersRatioFactor, int[] gameStateFactor, int mobilityFactor, int isolatedTowersFactor, int isolatedWallsFactor) {
        this.board = board;
        this.wallsDistancesFactor = wallsDistancesFactor;
        this.wallsColumnsFactor = wallsColumnsFactor;
        this.towersDistancesFactor = towersDistancesFactor;
        this.towersColumnsFactor = towersColumnsFactor;
        this.towersRatioFactor = towersRatioFactor;
        this.gameStateFactor = gameStateFactor;
        this.mobilityFactor = mobilityFactor;
        this.isolatedTowersFactor = isolatedTowersFactor;
        this.isolatedWallsFactor = isolatedWallsFactor;
    }


    public int evaluate(boolean isBlue, int depth){
        if (board.gameState(isBlue) == 5) return (winScore - depth);
        if (board.gameState(isBlue) == -5) return (loseScore + depth);
        int gameState = gameState(isBlue, gameStateFactor);
        int towersRatio = towersRatio(isBlue, towersRatioFactor);
        int wallsDistances = board.wallsDistances(isBlue, wallsDistancesFactor) - board.wallsDistances(!isBlue, wallsDistancesFactor);
        int wallsColumns = board.wallsColumns(isBlue, wallsColumnsFactor) - board.wallsColumns(isBlue, wallsColumnsFactor);
        int towersDistances = board.towersDistances(isBlue, towersDistancesFactor) - board.towersDistances(!isBlue, towersDistancesFactor);
        int towersColumns = board.towersColumns(isBlue, towersColumnsFactor) - board.towersColumns(isBlue, towersColumnsFactor);
        int mobility = mobilityFactor * (board.generateMoves(isBlue).size() - board.generateMoves(!isBlue).size());
        int isolatedTowers = isolatedTowersFactor * (board.isolatedTowersNumber(isBlue) - board.isolatedTowersNumber(!isBlue));
        int isolatedWalls = isolatedWallsFactor * (board.isolatedWallsNumber(isBlue) - board.isolatedWallsNumber(!isBlue));
        int score = gameState + towersRatio + wallsDistances + wallsColumns + towersDistances + towersColumns + mobility + isolatedTowers + isolatedWalls;
        if (score > winScore) score = winScore;
        else if (score < loseScore) score = loseScore;
        return score;
    }

    protected int gameState(boolean isBlue, int[] gameStateFactor){
        int gameState = board.gameState(isBlue);
        if (gameState == 0) return 0;
        return gameState > 0 ? gameStateFactor[gameState + 3] : gameStateFactor[gameState + 4];
    }

    protected int towersRatio(boolean isBlue, int[] towersRatioFactor){
        int towerDifference = board.towersNumber(isBlue) - board.towersNumber(!isBlue);
        if (towerDifference == 0) return 0;
        if (towerDifference > 8 || towerDifference < -8){
            System.out.println("towerDifference: " + towerDifference);
            //String Fen = board.generateFEN();
            //DisplayFrame displayFrame = new DisplayFrame(Fen);
        }

        return towerDifference > 0 ? towersRatioFactor[towerDifference - 1] : - (towersRatioFactor[(-towerDifference) - 1]);
    }

    public Board getBoard() {
        return board;
    }

    public int[] getWallsDistancesFactor() {
        return wallsDistancesFactor;
    }

    public int[] getWallsColumnsFactor() {
        return wallsColumnsFactor;
    }

    public int[] getTowersDistancesFactor() {
        return towersDistancesFactor;
    }

    public int[] getTowersColumnsFactor() {
        return towersColumnsFactor;
    }

    public int[] getTowersRatioFactor() {
        return towersRatioFactor;
    }

    public int[] getGameStateFactor() {
        return gameStateFactor;
    }

    public int getMobilityFactor() {
        return mobilityFactor;
    }

    public int getIsolatedTowersFactor() {
        return isolatedTowersFactor;
    }

    public int getIsolatedWallsFactor() {
        return isolatedWallsFactor;
    }
}
