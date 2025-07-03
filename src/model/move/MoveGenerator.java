package model.move;

import model.move.Move;

import java.util.List;

public abstract class MoveGenerator {

    public MoveGenerator() {

    }

    public abstract List<Move> generateMoves (boolean isBlue);

    public abstract List<Move> generateLoudMoves (boolean isBlue);

    public abstract List<Move> generateWinningMoves(boolean isBlue);

    public abstract List<Move> generateThreateningMoves(boolean isBlue);

    // TODO: 11/15/2024 delete it as an abstract method later..
    public abstract List<List<Move>> generateAllStyles (boolean isBlue);

}
