package model.move;
import model.Board;
import model.move.Move;
import model.move.MoveGeneratingStyle;
import model.move.MoveGenerator;
import model.move.MoveType;

import java.util.ArrayList;
import java.util.List;

public class MoveGeneratorEvolutionTheory extends MoveGenerator {
    private Board board;
    private MoveGeneratingStyle style;
    private MoveType[] moveTypes;
    private int[] directions;
    private boolean frontToBack;
    private final MoveType[] loudMoveTypes;

    public MoveGeneratorEvolutionTheory(Board board, MoveGeneratingStyle style, MoveType[] moveTypes, int[] directions, boolean frontToBack) {
        this.board = board;
        this.style = style;
        this.moveTypes = moveTypes;
        this.directions = directions;
        this.frontToBack = frontToBack;
        this.loudMoveTypes = new MoveType[moveTypes.length - 1];
        int j = 0;
        for (int i = 0; i < moveTypes.length; i++){
            if (moveTypes[i] == MoveType.QUIET) continue;
            loudMoveTypes[j] = moveTypes[i];
            j++;
        }
    }

    public Board getBoard() {
        return board;
    }

    public void setBoard(Board board) {
        this.board = board;
    }

    public MoveGeneratingStyle getStyle() {
        return style;
    }

    public void setStyle(MoveGeneratingStyle style) {
        this.style = style;
    }

    public MoveType[] getMoveTypes() {
        return moveTypes;
    }

    public void setMoveTypes(MoveType[] moveTypes) {
        this.moveTypes = moveTypes;
    }

    public int[] getDirections() {
        return directions;
    }

    public void setDirections(int[] directions) {
        this.directions = directions;
    }

    public boolean isFrontToBack() {
        return frontToBack;
    }

    public void setFrontToBack(boolean frontToBack) {
        this.frontToBack = frontToBack;
    }

    public void switchFrontToBack() {
        frontToBack = !frontToBack;

    }

    @Override
    public List<Move> generateMoves (boolean isBlue){
        if (this.style == MoveGeneratingStyle.ALL_TYPE_MOVES_PIECE_BY_PIECE) return allTypeMovesPieceByPiece(isBlue);
        if (this.style == MoveGeneratingStyle.TYPE_BY_TYPE_MOVES_PIECE_BY_PIECE) return typeByTypeMovesPieceByPiece(isBlue);
        if (this.style == MoveGeneratingStyle.DIRECTION_BY_DIRECTION_MOVES_PIECE_BY_PIECE) return directionByDirectionMovesPieceByPiece(isBlue);
        if (this.style == MoveGeneratingStyle.ALL_TYPE_MOVES_DIRECTION_BY_DIRECTION) return allTypeMovesDirectionByDirection(isBlue);
        if (this.style == MoveGeneratingStyle.TYPE_BY_TYPE_MOVES_DIRECTION_BY_DIRECTION) return typeByTypeMovesDirectionByDirection(isBlue);
        else return directionByDirectionMovesTypeByType(isBlue);
    }

    @Override
    public List<Move> generateLoudMoves(boolean isBlue) {
        List<Move> loudMoves = generateInteractiveMoves(isBlue);
        List<Move> threateningAndWinningMoves = generateThreateningMoves(isBlue);
        loudMoves.removeAll(threateningAndWinningMoves);
        loudMoves.addAll(threateningAndWinningMoves);
        return loudMoves;
    }

    @Override
    public List<Move> generateThreateningMoves(boolean isBlue) {
        List<Move> threateningWinningMoves = new ArrayList<>();
        List<Move> allMoves = generateMoves(isBlue);
        for (Move move : allMoves){
            if (move.isThreateningMove() || move.isWinnerMove() || move.isTargetEnemy()) threateningWinningMoves.add(move);
            else {
                board.makeMove(move, isBlue);
                if (generateMoves(isBlue).size() < 3 || generateMoves(!isBlue).size() < 3) threateningWinningMoves.add(move);
                board.unmakeMove(move, isBlue);
            }
        }
        return threateningWinningMoves;
    }

    @Override
    public List<Move> generateWinningMoves(boolean isBlue) {
        List<Move> winningMoves = new ArrayList<>();
        List<Move> allMoves = generateMoves(isBlue);
        for (Move move : allMoves){
            if (move.isWinnerMove()) winningMoves.add(move);
        }
        return winningMoves;
    }

    public List<Move> generateInteractiveMoves(boolean isBlue) {
        if (this.style == MoveGeneratingStyle.ALL_TYPE_MOVES_PIECE_BY_PIECE) return board.allTypeMovesPieceByPiece(isBlue, loudMoveTypes, directions, frontToBack);
        if (this.style == MoveGeneratingStyle.TYPE_BY_TYPE_MOVES_PIECE_BY_PIECE) return board.typeByTypeMovesPieceByPiece(isBlue, loudMoveTypes, directions, frontToBack);
        if (this.style == MoveGeneratingStyle.DIRECTION_BY_DIRECTION_MOVES_PIECE_BY_PIECE) return board.directionByDirectionMovesPieceByPiece(isBlue, loudMoveTypes, directions, frontToBack);
        if (this.style == MoveGeneratingStyle.ALL_TYPE_MOVES_DIRECTION_BY_DIRECTION) return board.allTypeMovesDirectionByDirection(isBlue, loudMoveTypes, directions, frontToBack);
        if (this.style == MoveGeneratingStyle.TYPE_BY_TYPE_MOVES_DIRECTION_BY_DIRECTION) return board.typeByTypeMovesDirectionByDirection(isBlue, loudMoveTypes, directions, frontToBack);
        else return board.directionByDirectionMovesTypeByType(isBlue, loudMoveTypes, directions, frontToBack);
    }

    public List<Move> generateEmptyMoves(boolean isBlue) {
        MoveType[] quietType = new MoveType[]{MoveType.QUIET};
        if (this.style == MoveGeneratingStyle.ALL_TYPE_MOVES_PIECE_BY_PIECE) return board.allTypeMovesPieceByPiece(isBlue, quietType, directions, frontToBack);
        if (this.style == MoveGeneratingStyle.TYPE_BY_TYPE_MOVES_PIECE_BY_PIECE) return board.typeByTypeMovesPieceByPiece(isBlue, quietType, directions, frontToBack);
        if (this.style == MoveGeneratingStyle.DIRECTION_BY_DIRECTION_MOVES_PIECE_BY_PIECE) return board.directionByDirectionMovesPieceByPiece(isBlue, quietType, directions, frontToBack);
        if (this.style == MoveGeneratingStyle.ALL_TYPE_MOVES_DIRECTION_BY_DIRECTION) return board.allTypeMovesDirectionByDirection(isBlue, quietType, directions, frontToBack);
        if (this.style == MoveGeneratingStyle.TYPE_BY_TYPE_MOVES_DIRECTION_BY_DIRECTION) return board.typeByTypeMovesDirectionByDirection(isBlue, quietType, directions, frontToBack);
        else return board.directionByDirectionMovesTypeByType(isBlue, quietType, directions, frontToBack);
    }

    public List<Move> generateSacrificingMoves(boolean isBlue) {
        MoveType[] sacrificeType = new MoveType[]{MoveType.SACRIFICE};
        if (this.style == MoveGeneratingStyle.ALL_TYPE_MOVES_PIECE_BY_PIECE) return board.allTypeMovesPieceByPiece(isBlue, sacrificeType, directions, frontToBack);
        if (this.style == MoveGeneratingStyle.TYPE_BY_TYPE_MOVES_PIECE_BY_PIECE) return board.typeByTypeMovesPieceByPiece(isBlue, sacrificeType, directions, frontToBack);
        if (this.style == MoveGeneratingStyle.DIRECTION_BY_DIRECTION_MOVES_PIECE_BY_PIECE) return board.directionByDirectionMovesPieceByPiece(isBlue, sacrificeType, directions, frontToBack);
        if (this.style == MoveGeneratingStyle.ALL_TYPE_MOVES_DIRECTION_BY_DIRECTION) return board.allTypeMovesDirectionByDirection(isBlue, sacrificeType, directions, frontToBack);
        if (this.style == MoveGeneratingStyle.TYPE_BY_TYPE_MOVES_DIRECTION_BY_DIRECTION) return board.typeByTypeMovesDirectionByDirection(isBlue, sacrificeType, directions, frontToBack);
        else return board.directionByDirectionMovesTypeByType(isBlue, sacrificeType, directions, frontToBack);
    }

    public List<Move> generateEmptyWinningMoves(boolean isBlue) {
        List<Move> winningEmptyMoves = new ArrayList<>();
        List<Move> emptyMoves = generateEmptyMoves(isBlue);
        for (Move move : emptyMoves){
            if (move.isWinnerMove()) winningEmptyMoves.add(move);
        }
        return winningEmptyMoves;
    }

    public List<Move> allTypeMovesPieceByPiece(boolean isBlue){
        return board.allTypeMovesPieceByPiece(isBlue, moveTypes, directions, frontToBack);
    }

    public List<Move> typeByTypeMovesPieceByPiece(boolean isBlue){
        return board.typeByTypeMovesPieceByPiece(isBlue, moveTypes, directions, frontToBack);
    }

    public List<Move> directionByDirectionMovesPieceByPiece(boolean isBlue){
        return board.directionByDirectionMovesPieceByPiece(isBlue, moveTypes, directions, frontToBack);
    }

    public List<Move> allTypeMovesDirectionByDirection (boolean isBlue){
        return board.allTypeMovesDirectionByDirection(isBlue, moveTypes, directions, frontToBack);
    }

    public List<Move> typeByTypeMovesDirectionByDirection(boolean isBlue){
        return board.typeByTypeMovesDirectionByDirection(isBlue, moveTypes, directions, frontToBack);
    }

    public List<Move> directionByDirectionMovesTypeByType(boolean isBlue ){
        return board.directionByDirectionMovesTypeByType(isBlue, moveTypes, directions, frontToBack);
    }

    @Override
    public List<List<Move>> generateAllStyles (boolean isBlue){
        List<List<Move>> allStyles = new ArrayList<>();

        allStyles.add(allTypeMovesPieceByPiece(isBlue));
        switchFrontToBack();
        allStyles.add(allTypeMovesPieceByPiece(isBlue));

        allStyles.add(typeByTypeMovesPieceByPiece(isBlue));
        switchFrontToBack();
        allStyles.add(typeByTypeMovesPieceByPiece(isBlue));

        allStyles.add(directionByDirectionMovesPieceByPiece(isBlue));
        switchFrontToBack();
        allStyles.add(directionByDirectionMovesPieceByPiece(isBlue));

        allStyles.add(allTypeMovesDirectionByDirection(isBlue));
        switchFrontToBack();
        allStyles.add(allTypeMovesDirectionByDirection(isBlue));

        allStyles.add(typeByTypeMovesDirectionByDirection(isBlue));
        switchFrontToBack();
        allStyles.add(typeByTypeMovesDirectionByDirection(isBlue));

        allStyles.add(directionByDirectionMovesTypeByType(isBlue));
        switchFrontToBack();
        allStyles.add(directionByDirectionMovesTypeByType(isBlue));

        return allStyles;
    }

}
