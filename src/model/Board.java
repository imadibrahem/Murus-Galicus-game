package model;

import model.move.MoveGeneratorEvolutionTheory;
import model.move.Move;
import model.move.MoveGeneratingStyle;
import model.move.MoveGenerator;
import model.move.MoveType;

import java.util.List;

public abstract class Board {
    MoveGenerator moveGenerator = new MoveGeneratorEvolutionTheory(this, MoveGeneratingStyle.ALL_TYPE_MOVES_PIECE_BY_PIECE,
            new MoveType[]{MoveType.FRIEND_ON_BOTH, MoveType.FRIEND_ON_NEAR, MoveType.FRIEND_ON_FAR, MoveType.QUIET, MoveType.SACRIFICE},
            new int[]{1, 8, 2, 3, 7, 6, 4, 5}, true);

    public Board() {}

    public abstract void build(String FEN);

    public abstract String generateFEN();

    public abstract void makeMove(Move move, boolean isBlue);

    public abstract void unmakeMove(Move move, boolean isBlue);

    public abstract void cleanBoard();

    public abstract int towersDistances(boolean isBlue, int[] values);

    public abstract int wallsDistances(boolean isBlue, int[] values);

    public abstract int towersColumns(boolean isBlue, int[] values);

    public abstract int wallsColumns(boolean isBlue, int[] values);

    public abstract int wallsNumber(boolean isBlue);

    public abstract int towersNumber(boolean isBlue);

    public abstract boolean isInCheck(boolean isBlue);

    public abstract boolean isInLosingPos(boolean isBlue);

    public abstract boolean lostGame(boolean isBlue);

    public abstract boolean isFriendlyTower(boolean isBlue, int location);

    public abstract boolean isFriendlyPiece(boolean isBlue, int location);

    public abstract List<Short> normalMovesLocations (boolean isBlue, int location, int startDirection);

    public abstract List<Short> sacrificingMovesLocations (boolean isBlue, int location, int startDirection);

    public abstract short sacrificingMovesLocation (boolean isBlue, int location, int direction);

    public abstract short[] normalMovesLocation (boolean isBlue, int location, int direction);

    public abstract List<Move>  allTypeMovesPieceByPiece(boolean isBlue, MoveType[] moveTypes, int[] directions, boolean frontToBack);

    public abstract List<Move> typeByTypeMovesPieceByPiece(boolean isBlue, MoveType[] moveTypes, int[] directions, boolean frontToBack);

    public abstract List<Move> directionByDirectionMovesPieceByPiece(boolean isBlue,MoveType[] moveTypes, int[] directions, boolean frontToBack);

    public abstract List<Move> allTypeMovesDirectionByDirection(boolean isBlue,MoveType[] moveTypes, int[] directions, boolean frontToBack);

    public abstract List<Move> typeByTypeMovesDirectionByDirection(boolean isBlue, MoveType[] moveTypes, int[] directions, boolean frontToBack);

    public abstract List<Move> directionByDirectionMovesTypeByType(boolean isBlue, MoveType[] moveTypes, int[] directions, boolean frontToBack);

    public abstract int isolatedTowersNumber(boolean isBlue);

    public abstract int isolatedWallsNumber(boolean isBlue);

    public int gameState(boolean isBlue) {
        int state = 0;

        if (isInCheck(isBlue)) {
            if (lostGame(isBlue)) return -5;
            state = isInLosingPos(isBlue) ? -4 : -1;
        }

        if (isInCheck(!isBlue)) {
            if (lostGame(!isBlue)) return 5;

            int opponentState = isInLosingPos(!isBlue) ? 4 : 2;

            if (state == 0) return opponentState;
            if (state == -1)  return opponentState - 1;
            return (opponentState == 2 ? -3 : -2);
         }

        return state;
    }

    public List<Move> generateMoves(boolean isBlue){
        return moveGenerator.generateMoves(isBlue);
    }

    public abstract String printBoard(boolean isBlue);

    public abstract int[] computeHashPositions(boolean isBlue);
}
