package model.player;

import model.Board;
import model.TimeManager;
import model.evaluationFunction.EvaluationFunction;
import model.move.*;
import model.transpositionTable.ReportTranspositionTable;
import model.transpositionTable.TranspositionTable;
import model.transpositionTable.TranspositionTableManager;

import java.util.ArrayList;
import java.util.List;

public class Caesar extends Player{
    private int currentSearchDepth = 1;
    private Move best;
    private Move globalBest;
    private final int window = 13;
    private final int windowMultiplier = 6;
    private final Move[][][] killerMoves = new Move[MAX_DEPTH][3][2];
    private final int[][] maxHistoryTable = new int[56][8];
    private final int[][] minHistoryTable = new int[56][8];
    private final int interactiveDepthRatio = 2;
    private final int fullDepthMoveNumber = 10;
    private final float roundsFactor = 0.6f;
    private final float towersFactor = -0.4f;
    private final float distancesFactor = -0.4f;
    int[] killerSort = new int[]{0, 1, 2, 4, 5, 3};
    int[] moveComparatorMoveTypes = new int[] {3, 4, 2, 1, 0};
    int[] comp = new int[] {1, 0, 2};
    boolean moveComparatorFrontToBack = true;
    int peakMove = 8;
    int midGameMoves = 9;
    double earlyFactor = 0.58;
    double midFactor = 0.20;
    double endFactor = 0.17;
    private final MoveComparatorEvolutionTheory maxComparator;
    private final MoveComparatorEvolutionTheory minComparator;
    private final TranspositionTable transpositionTable;
    private final TranspositionTable memoryTable;
    private final TimeManager timeManager;
    private final List<TranspositionTable.TranspositionEntry> newEntries = new ArrayList<>();
    private String result;
    String fen = "";
    List<Move> moves;
    boolean timeOut = false;
    double startTime;
    double totalTime;

    public Caesar(boolean isBlue, Board board, EvaluationFunction evaluationFunction, double totalTime) {
        super(isBlue, board,new MoveGeneratorEvolutionTheory(board, MoveGeneratingStyle.ALL_TYPE_MOVES_PIECE_BY_PIECE, new MoveType[]{MoveType.FRIEND_ON_NEAR, MoveType.SACRIFICE, MoveType.FRIEND_ON_FAR, MoveType.FRIEND_ON_BOTH, MoveType.QUIET},new int[]{2, 4, 7, 1, 8, 6, 5, 3},true),evaluationFunction);
        this.totalTime = totalTime;
        this.maxComparator = new MoveComparatorEvolutionTheory(isEvaluationBlue(),board, maxHistoryTable, killerMoves, killerSort, moveComparatorMoveTypes, comp, moveComparatorFrontToBack);
        this.minComparator = new MoveComparatorEvolutionTheory(!isEvaluationBlue(),board, minHistoryTable, killerMoves, killerSort, moveComparatorMoveTypes, comp, moveComparatorFrontToBack);
        this.timeManager = new TimeManager(totalTime,peakMove, midGameMoves, earlyFactor, midFactor, endFactor);
        this.transpositionTable= new ReportTranspositionTable();
        System.out.println("First Table created..");
        memoryTable = TranspositionTableManager.loadTranspositionTable(true);
        System.out.println("Second Table loaded..");
        this.remainingTime = totalTime / 1000;
    }

    @Override
    public Move decideMove() {
        timeOut = false;
        startTime = System.currentTimeMillis();
        zobristHashing.computeHash();
        globalBest = null;
        best = null;
        fen = this.board.generateFEN();
        moves = maxComparator.filterAndSortMoves( moveGenerator.generateMoves(isEvaluationBlue), globalBest, currentSearchDepth, true, board.isInLosingPos(isEvaluationBlue), board.isInCheck(isEvaluationBlue));
        int aspirationScore = evaluationFunction.evaluate(isEvaluationBlue, 0);
        while ((!timeOut) && currentSearchDepth < (MAX_DEPTH + 1)){
            iterateDepth(currentSearchDepth, aspirationScore);
            currentSearchDepth++;
        }
        movesNodes.add(moveNodes);
        nodes += moveNodes;
        moveNodes = 0;
        System.out.println("Depth: " + currentSearchDepth);
        currentSearchDepth = 1;
        if (globalBest == null || globalBest.getValue() == 0 || !moves.contains(globalBest)){
            if (best != null && moves.contains(best)) globalBest = best;
            else globalBest = moves.get(0);
        }
        timeManager.updateRemainingTime(System.currentTimeMillis() - startTime);
        return globalBest;
    }
    private int iterateDepth(int depth, int score) {
        int aspirationScore = aspirationWindowsSearch(depth, score, window, windowMultiplier);
        if (best == null){
            if (globalBest == null) globalBest = moves.get(0);
        }
        else globalBest = best;
        best = null;
        return aspirationScore;
    }

    public int aspirationWindowsSearch(int depth, int score, int window, int windowMultiplier){
        int alpha = score - window;
        int beta = score + window;
        while (true) {
            score = maximizer(depth, alpha, beta);
            if (timeOut) return score;
            if (score <= alpha) alpha -= (windowMultiplier * window);
            else if (score >= beta) beta += (windowMultiplier * window);
            else return score;
        }
    }

    private int maximizer(int depth, int alpha, int beta) {
        if (System.currentTimeMillis() - startTime > timeManager.getTimeForMove(rounds - 1) || currentSearchDepth >= MAX_DEPTH) {
            timeOut = true;
            return alpha;
        }
        TranspositionTable.TranspositionEntry entry = memoryTable.get(zobristHashing.getHash());
        TranspositionTable.TranspositionEntry tableEntry = transpositionTable.get(zobristHashing.getHash());
        if (tableEntry != null){
            if (entry == null || entry.getDepth() < tableEntry.getDepth()) entry = tableEntry;
        }
        Move pvs = null;
        if (entry != null && entry.getDepth() >= depth) {
            if (entry.getFlag() == TranspositionTable.TranspositionEntry.EXACT){
                if (depth == currentSearchDepth && entry.getBestMoveValue() != 0)  best = new Move(entry.getBestMoveValue());
                return entry.getScore();
            }
            else if (entry.getFlag() == TranspositionTable.TranspositionEntry.LOWERBOUND) {
                if (entry.getScore() > alpha && entry.getBestMoveValue() != 0) pvs = new Move(entry.getBestMoveValue());
                alpha = Math.max(alpha, entry.getScore());
            }
            else if (entry.getFlag() == TranspositionTable.TranspositionEntry.UPPERBOUND) {
                if (entry.getFlag() < beta && entry.getBestMoveValue() != 0) pvs = new Move(entry.getBestMoveValue());
                beta = Math.min(beta, entry.getScore());
            }
            if (alpha >= beta){
                if (depth == currentSearchDepth && entry.getBestMoveValue() != 0)  best = new Move(entry.getBestMoveValue());
                return entry.getScore();
            }
        }
        if (board.lostGame(true) || board.lostGame(false)) return evaluationFunction.evaluate(isEvaluationBlue, currentSearchDepth - depth);
        if (depth == 0) return quiescenceSearch(currentSearchDepth, alpha, beta);
        moveNodes++;
        List<Move> allMoves = maxComparator.filterAndSortMoves( moveGenerator.generateMoves(isEvaluationBlue), globalBest, depth, depth == currentSearchDepth, board.isInLosingPos(isEvaluationBlue), board.isInCheck(isEvaluationBlue));
        if (entry != null && pvs != null ){
            allMoves.remove(pvs);
            allMoves.add(0, pvs);
        }
        boolean firstMove = true;
        int moveIndex = 0;
        for (Move move : allMoves) {
            moveIndex++;
            zobristHashing.updateHashForMoves(move,isEvaluationBlue);
            makeMove(move);
            switchColor();
            int rating;
            if (firstMove) {
                rating = minimizer(depth - 1, alpha, beta);
                firstMove = false;
            }
            else {
                int reducedDepth = depth - 1;
                if (depth > 3 && moveIndex > fullDepthMoveNumber && board.towersNumber(isEvaluationBlue) > 2 && board.towersNumber(isEvaluationBlue) > 2
                        && (!board.isInCheck(isEvaluationBlue))  && (!board.isInCheck(!isEvaluationBlue))) reducedDepth = depth - 2;
                rating = minimizer( reducedDepth, alpha, alpha + 1);
                if (rating > alpha && rating < beta) rating = minimizer(depth - 1, alpha, beta);
            }
            zobristHashing.updateHashForMoves(move,isEvaluationBlue);
            switchColor();
            unmakeMove(move);
            if (rating > alpha){
                alpha = rating;
                pvs = move;
                if (depth == currentSearchDepth) best = move;
                updateHistoryTable(move, depth, true, isBlue());
                if (alpha >= beta){
                    int type = 1;
                    if (move.isTargetEmpty()) type = 0;
                    else if (move.isTargetEnemy()) type = 2;
                    storeKillerMove(move, depth, type);
                    transpositionTable.put(zobristHashing.getHash(),depth, alpha, entryPriority(depth), TranspositionTable.TranspositionEntry.LOWERBOUND, move);
                    newEntries.add(memoryTable.buildEntry(zobristHashing.getHash(),depth, alpha, memoryEntryPriority(depth), TranspositionTable.TranspositionEntry.LOWERBOUND, move));
                    return alpha;
                }
                transpositionTable.put(zobristHashing.getHash(),depth, alpha, entryPriority(depth), TranspositionTable.TranspositionEntry.EXACT, move);
                newEntries.add(memoryTable.buildEntry(zobristHashing.getHash(),depth, alpha, memoryEntryPriority(depth), TranspositionTable.TranspositionEntry.EXACT, move));
            }
        }
        Move move;
        if (pvs != null) move = pvs;
        else if (allMoves.isEmpty()) move = new Move((short) 0);
        else move = allMoves.get(0);
        transpositionTable.put(zobristHashing.getHash(), depth, alpha, entryPriority(depth), TranspositionTable.TranspositionEntry.UPPERBOUND, move);
        newEntries.add(memoryTable.buildEntry(zobristHashing.getHash(), depth, alpha, memoryEntryPriority(depth), TranspositionTable.TranspositionEntry.UPPERBOUND, move));
        return alpha;

    }


    private int minimizer(int depth, int alpha, int beta) {
        TranspositionTable.TranspositionEntry entry = memoryTable.get(zobristHashing.getHash());
        TranspositionTable.TranspositionEntry tableEntry = transpositionTable.get(zobristHashing.getHash());
        if (tableEntry != null){
            if (entry == null || entry.getDepth() < tableEntry.getDepth()) entry = tableEntry;
        }
        Move pvs = null;
        if (entry != null && entry.getDepth() >= depth) {
            if (entry.getFlag() == TranspositionTable.TranspositionEntry.EXACT) return entry.getScore();

            else if (entry.getFlag() == TranspositionTable.TranspositionEntry.LOWERBOUND) {
                if (entry.getScore() > alpha) pvs = new Move(entry.getBestMoveValue());
                alpha = Math.max(alpha, entry.getScore());
            }
            else if (entry.getFlag() == TranspositionTable.TranspositionEntry.UPPERBOUND) {
                if (entry.getScore() < beta) pvs = new Move(entry.getBestMoveValue());
                beta = Math.min(beta, entry.getScore());
            }
            if (alpha >= beta) return entry.getScore();
        }
        if (board.lostGame(true) || board.lostGame(false)) return evaluationFunction.evaluate(isEvaluationBlue, currentSearchDepth - depth);
        if (depth == 0) return quiescenceSearch(currentSearchDepth, alpha, beta);
        moveNodes++;
        List<Move> allMoves = minComparator.filterAndSortMoves( moveGenerator.generateMoves(!isEvaluationBlue), globalBest, depth,false ,board.isInLosingPos(!isEvaluationBlue), board.isInCheck(!isEvaluationBlue));
        if (entry != null && pvs != null ){
            allMoves.remove(pvs);
            allMoves.add(0, pvs);
        }
        boolean firstMove = true;
        int moveIndex = 0;
        for (Move move : allMoves) {
            moveIndex++;
            zobristHashing.updateHashForMoves(move,!isEvaluationBlue);
            makeMove(move);
            switchColor();
            int rating;
            if (firstMove) {
                rating = maximizer(depth - 1, alpha, beta);
                firstMove = false;
            } else {
                int reducedDepth = depth - 1;
                if (depth > 3 && moveIndex > fullDepthMoveNumber && board.towersNumber(isEvaluationBlue) > 2 && board.towersNumber(isEvaluationBlue) > 2
                        && (!board.isInCheck(isEvaluationBlue))  && (!board.isInCheck(!isEvaluationBlue))) reducedDepth = depth - 2;
                rating = maximizer(reducedDepth, beta - 1, beta);
                if (rating > alpha && rating < beta) {
                    rating = maximizer(depth - 1, alpha, beta);
                }
            }
            zobristHashing.updateHashForMoves(move,!isEvaluationBlue);
            switchColor();
            unmakeMove(move);
            if (rating < beta) {
                beta = rating;
                pvs = move;
                updateHistoryTable(move, depth, false, isBlue());
                if (alpha >= beta) {
                    int type = 1;
                    if (move.isTargetEmpty()) type = 0;
                    else if (move.isTargetEnemy()) type = 2;
                    storeKillerMove(move, depth, type);
                    transpositionTable.put(zobristHashing.getHash(),depth, beta, entryPriority(depth), TranspositionTable.TranspositionEntry.UPPERBOUND, move);
                    newEntries.add(memoryTable.buildEntry(zobristHashing.getHash(),depth, beta, memoryEntryPriority(depth), TranspositionTable.TranspositionEntry.UPPERBOUND, move));
                    return beta;
                }
                transpositionTable.put(zobristHashing.getHash(),depth, beta, entryPriority(depth), TranspositionTable.TranspositionEntry.EXACT, move);
                newEntries.add(memoryTable.buildEntry(zobristHashing.getHash(),depth, beta, memoryEntryPriority(depth), TranspositionTable.TranspositionEntry.EXACT, move));
            }
        }
        Move move;
        if (pvs != null) move = pvs;
        else if (allMoves.isEmpty()) move = new Move((short) 0);
        else move = allMoves.get(0);
        transpositionTable.put(zobristHashing.getHash(), depth, beta, entryPriority(depth), TranspositionTable.TranspositionEntry.LOWERBOUND, move);
        newEntries.add(memoryTable.buildEntry(zobristHashing.getHash(), depth, beta, memoryEntryPriority(depth), TranspositionTable.TranspositionEntry.LOWERBOUND, move));
        return beta;
    }

    private int quiescenceSearch(int depth, int alpha, int beta) {
        if (board.lostGame(true) || board.lostGame(false))return evaluationFunction.evaluate(isEvaluationBlue,depth);
        moveNodes++;
        int standPat = evaluationFunction.evaluate(isEvaluationBlue, depth);
        boolean isMaximizingPlayer = (isBlue() == isEvaluationBlue);
        if (isMaximizingPlayer) {
            if (standPat >= beta) return beta;
            if (alpha < standPat) alpha = standPat;
        } else {
            if (standPat <= alpha) return alpha;
            if (beta > standPat) beta = standPat;
        }
        List<Move> loudMoves;
        if ((depth - currentSearchDepth) < (currentSearchDepth / interactiveDepthRatio)) loudMoves = moveGenerator.generateThreateningMoves(isBlue());
        else loudMoves = moveGenerator.generateLoudMoves(isBlue());
        if (isMaximizingPlayer) loudMoves = maxComparator.quiescenceFilterAndSortMoves( loudMoves, board.isInLosingPos(isEvaluationBlue), board.isInCheck(isEvaluationBlue));
        else loudMoves = minComparator.quiescenceFilterAndSortMoves( loudMoves,  board.isInLosingPos(!isEvaluationBlue), board.isInCheck(!isEvaluationBlue));
        for (Move move : loudMoves) {
            makeMove(move);
            switchColor();
            int score = quiescenceSearch(depth + 1 ,alpha, beta);
            switchColor();
            unmakeMove(move);
            if (isMaximizingPlayer) {
                if (score >= beta) return beta;
                if (score > alpha) alpha = score;
            } else {
                if (score <= alpha) return alpha;
                if (score < beta) beta = score;
            }
        }
        return isMaximizingPlayer ? alpha : beta;
    }

    private void updateHistoryTable(Move move, int depth, boolean isMax, boolean isBlue) {
        if (isMax) maxHistoryTable [move.getInitialLocation(isBlue)][move.getDirection() - 1] += depth * depth;
        else minHistoryTable [move.getInitialLocation(isBlue)][move.getDirection() - 1] += depth * depth;
    }

    private void storeKillerMove(Move move, int depth, int type) {
        if (killerMoves[depth][type][0] == null) killerMoves[depth][type][0] = move;
        else if (!killerMoves[depth][type][0].equals(move) && killerMoves[depth][type][1] == null) killerMoves[depth][type][1] = move;
        else if (!killerMoves[depth][type][0].equals(move) && !killerMoves[depth][type][1].equals(move)){
            killerMoves[depth][type][0] = killerMoves[depth][type][1];
            killerMoves[depth][type][1] = move;
        }
    }

    private int entryPriority(int depth){
        return priority(depth, roundsFactor, towersFactor, distancesFactor);
    }

    private int memoryEntryPriority(int depth){
        return memoryPriority(depth, roundsFactor, towersFactor, distancesFactor);
    }

    private void updateResult(){
        double percentage = (transpositionTable.used /(double)transpositionTable.getTableSize()) * 100;
        double conflictsPercentage = (transpositionTable.conflicts /(double)transpositionTable.getTableSize()) * 100;

        double memoryPercentage = (memoryTable.used /(double)memoryTable.getTableSize()) * 100;
        double memoryConflictsPercentage = (memoryTable.conflicts /(double)memoryTable.getTableSize()) * 100;

        this.result = "Entries used = " + transpositionTable.used + " | Total Entries Number = " + transpositionTable.getTableSize() + " | Percentage = " + percentage + " %\n" +
                "Entries conflicts = " + transpositionTable.conflicts + " | Total Entries Number = " + transpositionTable.getTableSize() + " | Percentage = " + conflictsPercentage + " %\n" +
                "Number of Entry calls = " + transpositionTable.calls + " | Number of empty Entry calls = " + transpositionTable.emptyCalls + "| Number of misses = " + transpositionTable.misses + "| Number of hits = " + transpositionTable.hits + "\n" + "\n" +
                "Memory Entries used = " + memoryTable.used + " | Total Entries Number = " + memoryTable.getTableSize() + " | Percentage = " + memoryPercentage + " %\n" +
                "Memory Entries conflicts = " + memoryTable.conflicts + " | Total Entries Number = " + transpositionTable.getTableSize() + " | Percentage = " + memoryConflictsPercentage + " %\n" +
                "Number of Entry calls = " + memoryTable.calls + " | Number of empty Entry calls = " + memoryTable.emptyCalls + " | Number of misses = " + memoryTable.misses + "| Number of hits = " + memoryTable.hits+ "\n";
    }

    @Override
    public String tableUsageReport(){
        return result;
    }

    public void updateTables(){
        for (TranspositionTable.TranspositionEntry entry : newEntries){
            memoryTable.putForMemory(entry);
        }
        updateResult();
        memoryTable.resetCounters();
        TranspositionTableManager.saveTranspositionTable(memoryTable);
    }

}
