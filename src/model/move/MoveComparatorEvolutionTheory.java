package model.move;

import model.Board;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class MoveComparatorEvolutionTheory implements Comparator<Move> {
    private final boolean isBlue;
    private final int[][] historyTable;
    private final Move[][][] killerMoves;
    private Board board;
    private int[] moveTypes;
    private int[] killerSort;
    private int[] comp;
    private boolean frontToBack;

    public MoveComparatorEvolutionTheory(boolean isBlue, Board board, int[][] historyTable, Move[][][] killerMoves,int[] killerSort, int[] moveTypes, int[] comp ,boolean frontToBack) {
        this.isBlue = isBlue;
        this.historyTable = historyTable;
        this.killerMoves = killerMoves;
        this.board = board;
        this.moveTypes = moveTypes;
        this.killerSort = killerSort;
        this.comp = comp;
        this.frontToBack = frontToBack;
    }

    private boolean isWinningMove(Move move){
        return move.isWinnerMove();
    }

    private boolean isKillerMove(Move move, int depth, int type, boolean isFirstMove) {
        Move killer;
        if (isFirstMove) killer = killerMoves[depth][type][0];
        else killer = killerMoves[depth][type][1];
        return (killer != null && killer.equals(move));
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

    private int compareHistory(Move m1, Move m2) {
        return Integer.compare(historyTable[m2.getInitialLocation(isBlue)][m2.getDirection() - 1], historyTable[m1.getInitialLocation(isBlue)][m1.getDirection() - 1]);
    }

    private int compareTargetRow(Move m1, Move m2) {
        if (frontToBack )return Integer.compare(m2.getTargetRowSorting(), m1.getTargetRowSorting());
        return Integer.compare(m1.getTargetRowSorting(), m2.getTargetRowSorting());
    }

    private int compareMoveType(Move m1, Move m2) {
        return Integer.compare(getMoveTypePriority(m2), getMoveTypePriority(m1));
    }

    private int getMoveTypePriority(Move m) {
        if (m.isTargetEmpty()) return moveTypes[0];
        if (m.isTargetEnemy()) return moveTypes[1];
        if (m.isTargetNearFriendly()) return moveTypes[2];
        if (m.isTargetFarFriendly()) return moveTypes[3];
        return moveTypes[4];
    }
    @Override
    public int compare(Move m1, Move m2) {
        int [] comparing = new int[3];
        int history = compareHistory(m1, m2);
        int targetRow = compareTargetRow(m1, m2);
        int moveType = compareMoveType(m1,m2);
        comparing[comp[0]] = history;
        comparing[comp[1]] = targetRow;
        comparing[comp[2]] = moveType;
        if (comparing[0] != 0) return comparing[0];
        if (comparing[1] != 0) return comparing[1];
        return comparing[2];
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
        List<Move> friendlyKiller_1 = allMoves.stream().filter(move -> isKillerMove(move, depth, 1,true)).collect(Collectors.toList());
        List<Move> friendlyKiller_2 = allMoves.stream().filter(move -> isKillerMove(move, depth, 1,false)).collect(Collectors.toList());
        List<Move> enemyKiller_1 = allMoves.stream().filter(move -> isKillerMove(move, depth, 2,true)).collect(Collectors.toList());
        List<Move> enemyKiller_2 = allMoves.stream().filter(move -> isKillerMove(move, depth, 2,false)).collect(Collectors.toList());
        List<Move> emptyKiller_1 = allMoves.stream().filter(move -> isKillerMove(move, depth, 0,true)).collect(Collectors.toList());
        List<Move> emptyKiller_2 = allMoves.stream().filter(move -> isKillerMove(move, depth, 0,false)).collect(Collectors.toList());
        allMoves.removeAll(friendlyKiller_1);
        allMoves.removeAll(friendlyKiller_2);
        allMoves.removeAll(emptyKiller_1);
        allMoves.removeAll(emptyKiller_2);
        allMoves.removeAll(enemyKiller_1);
        allMoves.removeAll(enemyKiller_2);
        Move[] killers = new Move[6];
        if (!friendlyKiller_1.isEmpty())killers[killerSort[0]] = friendlyKiller_1.get(0);
        if (!friendlyKiller_2.isEmpty())killers[killerSort[1]] = friendlyKiller_2.get(0);
        if (!enemyKiller_1.isEmpty())killers[killerSort[2]] = enemyKiller_1.get(0);
        if (!enemyKiller_2.isEmpty())killers[killerSort[3]] = enemyKiller_2.get(0);
        if (!emptyKiller_1.isEmpty())killers[killerSort[4]] = emptyKiller_1.get(0);
        if (!emptyKiller_2.isEmpty())killers[killerSort[5]] = emptyKiller_2.get(0);
        for (int i = 0; i < killers.length; i++) {
            if (killers[i] != null) {sorted.add(killers[i]);}
        }
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
