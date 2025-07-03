package model.move;

import model.Board;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class MoveComparator implements Comparator<Move> {
    private final boolean isBlue;
    private final int[][] historyTable;
    private final Move[][][] killerMoves;
    private Board board;
    //private Move best;
    
    public MoveComparator(boolean isBlue, Board board, int[][] historyTable, Move[][][] killerMoves) {
        this.isBlue = isBlue;
        this.historyTable = historyTable;
        this.killerMoves = killerMoves;
        this.board = board;
    }

    private boolean isWinningMove(Move move){
        return move.isWinnerMove();
    }

    private boolean wouldGetOutOfCheck(Move move) {
        board.makeMove(move, isBlue);
        boolean isOutOfCheck = !board.isInCheck(isBlue);
        board.unmakeMove(move, isBlue);
        return isOutOfCheck;
    }

    private boolean wouldGetOutOfLosing(Move move) {
        board.makeMove(move, isBlue);
        boolean isOutOfLosing = !board.isInLosingPos(isBlue);
        board.unmakeMove(move, isBlue);
        return isOutOfLosing;
    }

    private boolean isKillerMove(Move move, int depth, int type) {
        Move killer1 = killerMoves[depth][type][0];
        Move killer2 = killerMoves[depth][type][1];
        return (killer1 != null && killer1.equals(move)) || (killer2 != null && killer2.equals(move));
    }

    private int compareHistory(Move m1, Move m2) {
        return Integer.compare(historyTable[m2.getInitialLocation(isBlue)][m2.getDirection() - 1], historyTable[m1.getInitialLocation(isBlue)][m1.getDirection() - 1]);
    }

    private int compareTargetRow(Move m1, Move m2) {
        return Integer.compare(m2.getTargetRowSorting(), m1.getTargetRowSorting());
    }

    private int compareMoveType(Move m1, Move m2) {
        return Integer.compare(getMoveTypePriority(m2), getMoveTypePriority(m1));
    }


    private int getMoveTypePriority(Move m) {
        if (m.isTargetEmpty()) return 1;
        if (m.isTargetEnemy()) return 2;
        if (m.isTargetNearFriendly()) return  3;
        if (m.isTargetFarFriendly()) return 4;
        return 5;
    }

    @Override
    public int compare(Move m1, Move m2) {
        int history = compareHistory(m1, m2);
        if (history != 0) return history;
        int targetRow = compareTargetRow(m1, m2);
        if (targetRow != 0) return targetRow;
        return compareMoveType(m1,m2);
    }
    public List<Move> filterAndSortMoves(List<Move> allMoves, Move best, int depth, boolean isBestRelevant, boolean isLosing, boolean isInCheck) {
        List<Move> winningMoves = allMoves.stream().filter(this::isWinningMove).collect(Collectors.toList());
        if (!winningMoves.isEmpty()) return winningMoves;
        if (isLosing){
            List<Move> outOfChek = allMoves.stream().filter(this::wouldGetOutOfCheck).collect(Collectors.toList());
            List<Move> outOfLosing = allMoves.stream().filter(this::wouldGetOutOfLosing).collect(Collectors.toList());
            outOfChek.addAll(outOfLosing);
            if (!outOfChek.isEmpty()) return outOfChek;
        }
        List<Move> sorted = new ArrayList<>();
        if (isBestRelevant && best != null){
            allMoves.remove(best);
            sorted.add(best);
        }
        if (isInCheck) {
            List<Move> outOfChek = allMoves.stream().filter(this::wouldGetOutOfCheck).collect(Collectors.toList());
            allMoves.removeAll(outOfChek);
            sorted.addAll(outOfChek);
        }
        List<Move> friendlyKiller = allMoves.stream().filter(move -> isKillerMove(move, depth, 1)).collect(Collectors.toList());
        List<Move> enemyKiller = allMoves.stream().filter(move -> isKillerMove(move, depth, 2)).collect(Collectors.toList());
        List<Move> emptyKiller = allMoves.stream().filter(move -> isKillerMove(move, depth, 0)).collect(Collectors.toList());
        allMoves.removeAll(friendlyKiller);
        allMoves.removeAll(emptyKiller);
        allMoves.removeAll(enemyKiller);
        sorted.addAll(friendlyKiller);
        sorted.addAll(enemyKiller);
        sorted.addAll(emptyKiller);
        allMoves.sort(this);
        sorted.addAll(allMoves);
        return sorted;
    }

    public List<Move> quiescenceFilterAndSortMoves(List<Move> allMoves, boolean isLosing, boolean isInCheck) {
        List<Move> winningMoves = allMoves.stream().filter(this::isWinningMove).collect(Collectors.toList());
        if (!winningMoves.isEmpty()) return winningMoves;
        if (isLosing){
            List<Move> outOfChek = allMoves.stream().filter(this::wouldGetOutOfCheck).collect(Collectors.toList());
            List<Move> outOfLosing = allMoves.stream().filter(this::wouldGetOutOfLosing).collect(Collectors.toList());
            outOfChek.addAll(outOfLosing);
            if (!outOfChek.isEmpty()) return outOfChek;
        }
        List<Move> sorted = new ArrayList<>();

        if (isInCheck) {
            List<Move> outOfChek = allMoves.stream().filter(this::wouldGetOutOfCheck).collect(Collectors.toList());
            allMoves.removeAll(outOfChek);
            sorted.addAll(outOfChek);
        }

        allMoves.sort(this);
        sorted.addAll(allMoves);
        return sorted;
    }


}
