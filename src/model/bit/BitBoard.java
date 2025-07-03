package model.bit;

import model.Board;
import model.move.Move;
import model.move.MoveType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BitBoard extends Board {
    private final long row_1 = 255L;
    private final long row_2 = 65280L;
    private final long row_3 = 16711680L;
    private final long row_4 = 4278190080L;
    private final long row_5 = 1095216660480L;
    private final long row_6 = 280375465082880L;
    private final long row_7 = 71776119061217280L;
    private final long col_A = -9187201950435737472L;
    private final long col_B = 18085043209519168L;
    private final long col_H = 72340172838076673L;
    private final long col_G = 565157600297474L;
    private final int[] redDirections = {0, 8, 7, -1, -9, -8, -7, 1, 9};
    private final int[] blueDirections = {0, -8, -7, 1, 9, 8, 7, -1, -9};

    private long bw;
    private long bt;
    private long rw;
    private long rt;
    private String FEN;

    public BitBoard(String FEN) {
        this.FEN = FEN;
        build(FEN);
    }

    @Override
    public void build(String FEN) {
        cleanBoard();
        int squareCol;
        int squareLoc;
        int bitLoc;
        long mask;
        String [] rows = FEN.split("[/\\s]+");
        for (int r = 0; r < 7; r++){
            squareCol = 0;
            for (int c = 0; c < rows[r].length(); c++){
                if (Character.isDigit(rows[r].charAt(c))){
                    squareCol += Character.getNumericValue(rows[r].charAt(c));
                }
                else {
                    squareCol++;
                    squareLoc = (r * 8) + (squareCol - 1);
                    bitLoc = 55 - squareLoc;
                    mask = 1L << bitLoc;
                    if (rows[r].charAt(c) == 'w') rw |= mask;
                    else if (rows[r].charAt(c) == 'W') bw |= mask;
                    else if (rows[r].charAt(c) == 't') rt |= mask;
                    else bt |= mask;
                }
            }
        }
    }

    @Override

    public String generateFEN() {
        char[] board = String.format("%64s", Long.toBinaryString(rw)).replace(' ', '0').replace('1','w').toCharArray();
        long blueWalls = bw;
        long redTowers = rt;
        long blueTowers = bt;
        int index;

        while (blueWalls != 0){
            index = Long.numberOfLeadingZeros(blueWalls);
            blueWalls ^= Long.highestOneBit(blueWalls);
            board[index] ='W';
        }
        while (redTowers != 0){
            index = Long.numberOfLeadingZeros(redTowers);
            redTowers ^= Long.highestOneBit(redTowers);
            board[index] = 't';
        }
        while (blueTowers != 0){
            index = Long.numberOfLeadingZeros(blueTowers);
            blueTowers ^= Long.highestOneBit(blueTowers);
            board[index] = 'T';
        }

        StringBuilder fen = new StringBuilder();
        for (int row = 1; row < 8; row++) {
            int emptyCount = 0;
            for (int col = 0; col < 8; col++) {
                char piece = board[row * 8 + col];
                if (piece == '0')emptyCount++;
                else {
                    if (emptyCount > 0) {
                        fen.append(emptyCount);
                        emptyCount = 0;
                    }
                    fen.append(piece);
                }
            }
            if (emptyCount > 0) fen.append(emptyCount);
            fen.append('/');
        }
        return fen.toString();
    }

        @Override
    public void makeMove(Move move, boolean isBlue) {
        long initial = 1L << move.getInitialLocation(!isBlue), firstTarget, secondTarget;
        if (isBlue){
            bt ^= initial;
            if (move.isTargetEmpty()) bw ^= normalMoveRouteFinder(initial, move.getDirection(), true);
            else if (move.isTargetBothFriendly()){
                firstTarget = normalMoveRouteFinder(initial, move.getDirection(), true);
                bw ^= firstTarget;
                bt ^= firstTarget;
            }
            else if (move.isTargetEnemy()){
                bw ^= initial;
                rw ^= sacrificingMoveRouteFinder(initial, move.getDirection(), true);
            }
            else {
                firstTarget = normalMoveRouteFinder(initial, move.getDirection(), true);
                secondTarget = firstTarget & bw;
                bw ^= firstTarget;
                bt ^= secondTarget;
            }
        }
        else {
            rt ^= initial;
            if (move.isTargetEmpty()) rw ^= normalMoveRouteFinder(initial, move.getDirection(), false);
            else if (move.isTargetBothFriendly()){
                firstTarget = normalMoveRouteFinder(initial, move.getDirection(), false);
                rw ^= firstTarget;
                rt ^= firstTarget;
            }
            else if (move.isTargetEnemy()){
                rw ^= initial;
                bw ^= sacrificingMoveRouteFinder(initial, move.getDirection(), false);
            }
            else {
                firstTarget = normalMoveRouteFinder(initial, move.getDirection(), false);
                secondTarget = firstTarget & rw;
                rw ^= firstTarget;
                rt ^= secondTarget;
            }


        }
    }

    @Override
    public void unmakeMove(Move move, boolean isBlue) {
        long initial = 1L << move.getInitialLocation(!isBlue), firstTarget, secondTarget;
        if (move.isTargetNearFriendly() || move.isTargetFarFriendly()){
            if (isBlue){
                bt ^= initial;
                firstTarget = normalMoveRouteFinder(initial, move.getDirection(), true);
                secondTarget = firstTarget & bt;
                bw ^= firstTarget;
                bt ^= secondTarget;
            }
            else{
                rt ^= initial;
                firstTarget = normalMoveRouteFinder(initial, move.getDirection(), false);
                secondTarget = firstTarget & rt;
                rw ^= firstTarget;
                rt ^= secondTarget;
            }
        }
        else makeMove(move,isBlue);
    }

    @Override
    public void cleanBoard() {
        rt = rw = bt = bw = 0L;
    }

    @Override
    public int towersDistances(boolean isBlue, int[] values) {
        long pieces = isBlue ? bt : rt;
        return distancesFinder(isBlue,pieces,values);
    }

    @Override
    public int wallsDistances(boolean isBlue, int[] values) {
        long pieces = isBlue ? bw : rw;
        return distancesFinder(isBlue,pieces,values);
    }


    @Override
    public int towersColumns(boolean isBlue, int[] values) {
        long pieces = isBlue ? bt : rt;
        return columnsFinder(pieces, values);
    }

    @Override
    public int wallsColumns(boolean isBlue, int[] values) {
        long pieces = isBlue ? bw : rw;
        return columnsFinder(pieces, values);
    }

    @Override
    public int wallsNumber(boolean isBlue) {
        return isBlue ? Long.bitCount(bw) :  Long.bitCount(rw);
    }

    @Override
    public int towersNumber(boolean isBlue) {
        return isBlue ? Long.bitCount(bt) :  Long.bitCount(rt);
    }

    @Override
    public boolean isInCheck(boolean isBlue) {
        if (generateMoves(isBlue).size() < 3) return true;
        return isBlue ? ((rt & (row_2|row_3)) | (rw & row_3)) != 0 : ((bt & (row_6|row_5)) | (bw & row_5)) != 0;
    }

    @Override
    public boolean isInLosingPos(boolean isBlue) {
        if (generateMoves(isBlue).size() < 3) return true;
        long normalMoveEnemyPieces = isBlue ? rt & row_3 : bt & row_5;
        long sacrificingMoveEnemyPieces = isBlue ? rt & row_2 : bt & row_6;
        int[] forwardDirections = new int[]{1,2,8};
        for (int direction : forwardDirections){
            if (normalMoveValidator(normalMoveEnemyPieces,direction,!isBlue) != 0) return true;
            if (sacrificingMoveValidator(sacrificingMoveEnemyPieces,direction,!isBlue) != 0) return true;
        }
        return false;
    }

    @Override
    public boolean lostGame(boolean isBlue) {
        if (generateMoves(isBlue).size() < 1) return true;
        return isBlue ? (rw & row_1) != 0 : (bw & row_7) != 0;
    }

    @Override
    public boolean isFriendlyTower(boolean isBlue, int location) {
        return isBlue ? ((1L << (55 - location)) & bt) != 0 : ((1L << (55 - location)) & rt) != 0;
    }

    @Override
    public boolean isFriendlyPiece(boolean isBlue, int location) {
        return isBlue ? ((1L << (55 - location)) & bw) != 0 : ((1L << (55 - location)) & rw) != 0;
    }

    @Override
    public List<Short> normalMovesLocations(boolean isBlue, int location, int startDirection) {
        List<Short> normalMovesLocations = new ArrayList<>();
        int currentDirection = startDirection;
        long bitLocation = 1L << (55 - location);
        if (this.isFriendlyTower(isBlue, location)){
            for (int i = 1; i < 9; i++){
                if (normalMoveValidator(bitLocation, currentDirection, isBlue) > 0){
                    short first = (short) (isBlue ? location + blueDirections[currentDirection] : location + redDirections[currentDirection]);
                    normalMovesLocations.add(first);
                    normalMovesLocations.add((short) (isBlue ? first + blueDirections[currentDirection] : first + redDirections[currentDirection]));
                }
                currentDirection = (currentDirection % 8) + 1;
            }
        }
        return normalMovesLocations;
    }

    @Override
    public List<Short> sacrificingMovesLocations(boolean isBlue, int location, int startDirection) {
        List<Short> sacrificingMovesLocations = new ArrayList<>();
        int currentDirection = startDirection;
        long bitLocation = 1L << (55 - location);
        if (this.isFriendlyTower(isBlue, location)){
            for (int i = 1; i < 9; i++){
                if (sacrificingMoveValidator(bitLocation, currentDirection, isBlue) > 0) {
                    sacrificingMovesLocations.add((short) (isBlue ? location + blueDirections[currentDirection] : location + redDirections[currentDirection]));
                }
                currentDirection = (currentDirection % 8) + 1;
            }
        }
        return sacrificingMovesLocations;
    }

    @Override
    public short sacrificingMovesLocation (boolean isBlue, int location, int direction) {
        return (short) (isBlue ? location + blueDirections[direction] : location + redDirections[direction]);
    }

    @Override
    public short[] normalMovesLocation (boolean isBlue, int location, int direction) {
        short[] normalMovesLocation = new short[2];
        short first = (short) (isBlue ? location + blueDirections[direction] : location + redDirections[direction]);
        normalMovesLocation[0] = first;
        normalMovesLocation[1] = (short) (isBlue ? first + blueDirections[direction] : first + redDirections[direction]);
        return normalMovesLocation;
    }

/////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public List<Move> allTypeMovesPieceByPiece(boolean isBlue, MoveType[] moveTypes, int[] directions, boolean frontToBack) {
        List<Move> allTypeMovesPieceByPiece = new ArrayList<>();
        long towers = isBlue ? bt : rt;
        List<Integer> locations = locationsProvider(towers);
        long locationBit;
        if ((!isBlue && frontToBack) || (isBlue && !frontToBack)) Collections.reverse(locations);
        for (int location : locations){
            locationBit = 1L << location;
            for (MoveType moveType : moveTypes){
                for (int direction : directions){
                    if (oneTypeMovesPiecesFinder(isBlue, moveType, locationBit, direction) > 0){
                        int loc = isBlue ? 55 - location : location;
                        allTypeMovesPieceByPiece.add(moveProvider(direction, loc, moveType));
                    }
                }
            }
        }

        return allTypeMovesPieceByPiece;
    }

    @Override
    public List<Move> typeByTypeMovesPieceByPiece(boolean isBlue, MoveType[] moveTypes, int[] directions, boolean frontToBack) {
        List<Move> typeByTypeMovesPieceByPiece = new ArrayList<>();
        long towers = isBlue ? bt : rt;
        List<Integer> locations = locationsProvider(towers);
        long locationBit;
        if ((!isBlue && frontToBack) || (isBlue && !frontToBack)) Collections.reverse(locations);
        for (MoveType moveType : moveTypes){
            for (int location : locations){
                locationBit = 1L << location;
                for (int direction : directions){
                    if (oneTypeMovesPiecesFinder(isBlue, moveType, locationBit, direction) > 0){
                        int loc = isBlue ? 55 - location : location;
                        typeByTypeMovesPieceByPiece.add(moveProvider(direction, loc, moveType));
                    }
                }
            }
        }

        return typeByTypeMovesPieceByPiece;
    }

    @Override
    public List<Move> directionByDirectionMovesPieceByPiece(boolean isBlue, MoveType[] moveTypes, int[] directions, boolean frontToBack) {
        List<Move> directionByDirectionMovesPieceByPiece = new ArrayList<>();
        long towers = isBlue ? bt : rt;
        List<Integer> locations = locationsProvider(towers);
        long locationBit;
        if ((!isBlue && frontToBack) || (isBlue && !frontToBack)) Collections.reverse(locations);
        for (int location : locations){
            locationBit = 1L << location;
            for (int direction : directions){
                for (MoveType moveType : moveTypes){
                    if (oneTypeMovesPiecesFinder(isBlue, moveType, locationBit, direction) > 0){
                        int loc = isBlue ? 55 - location : location;
                        directionByDirectionMovesPieceByPiece.add(moveProvider(direction, loc, moveType));
                    }
                }
            }
        }

        return directionByDirectionMovesPieceByPiece;
    }

    @Override
    public List<Move> allTypeMovesDirectionByDirection(boolean isBlue, MoveType[] moveTypes, int[] directions, boolean frontToBack) {
        List<Move> allTypeMovesDirectionByDirection = new ArrayList<>();
        long towers = isBlue ? bt : rt;
        List<Integer> locations = locationsProvider(towers);
        long locationBit;
        if ((!isBlue && frontToBack) || (isBlue && !frontToBack)) Collections.reverse(locations);
        for (int direction : directions){
            for (int location : locations){
                locationBit = 1L << location;
                for (MoveType moveType : moveTypes){
                    if (oneTypeMovesPiecesFinder(isBlue, moveType, locationBit, direction) > 0){
                        int loc = isBlue ? 55 - location : location;
                        allTypeMovesDirectionByDirection.add(moveProvider(direction, loc, moveType));
                    }
                }
            }
        }
        return allTypeMovesDirectionByDirection;
    }

    @Override
    public List<Move> typeByTypeMovesDirectionByDirection(boolean isBlue, MoveType[] moveTypes, int[] directions, boolean frontToBack) {
        List<Move> typeByTypeMovesDirectionByDirection = new ArrayList<>();
        long towers = isBlue ? bt : rt;
        long moveTypeLocations;
        List<Integer> locations;
        for (MoveType moveType : moveTypes){
            for (int direction : directions){
                moveTypeLocations = oneTypeMovesPiecesFinder(isBlue, moveType, towers, direction);
                if (moveTypeLocations > 0){
                    locations = locationsProvider(moveTypeLocations);
                    if ((!isBlue && frontToBack) || (isBlue && !frontToBack)) Collections.reverse(locations);
                    for (int location : locations){
                        int loc = isBlue ? 55 - location : location;
                        typeByTypeMovesDirectionByDirection.add(moveProvider(direction, loc, moveType));
                    }
                }
            }
        }
        return typeByTypeMovesDirectionByDirection;
    }

    @Override
    public List<Move> directionByDirectionMovesTypeByType(boolean isBlue, MoveType[] moveTypes, int[] directions, boolean frontToBack) {
        List<Move> directionByDirectionMovesTypeByType = new ArrayList<>();
        long towers = isBlue ? bt : rt;
        long moveTypeLocations;
        List<Integer> locations;
        for (int direction : directions){
            for (MoveType moveType : moveTypes){
                moveTypeLocations = oneTypeMovesPiecesFinder(isBlue, moveType, towers, direction);
                if (moveTypeLocations > 0){
                    locations = locationsProvider(moveTypeLocations);
                    if ((!isBlue && frontToBack) || (isBlue && !frontToBack)) Collections.reverse(locations);
                    for (int location : locations){
                        int loc = isBlue ? 55 - location : location;
                        directionByDirectionMovesTypeByType.add(moveProvider(direction, loc, moveType));
                    }
                }

            }
        }
        return directionByDirectionMovesTypeByType;
    }

    ///////////////////////////////////////////////////////////////////////

    @Override
    public int isolatedTowersNumber(boolean isBlue){
        long towers = isBlue ? bt : rt;
        long others, tower, accessibility, isolatedTowers, notIsolatedTowers = 0;
        for (int i = 1; i < 9; i++){
            notIsolatedTowers |= (friendOnNearPiecesFinder(towers,i,isBlue)|friendOnFarMovesPiecesFinder(towers,i,isBlue)|friendOnBothMovesPiecesFinder(towers,i,isBlue));
        }
        isolatedTowers = towers ^ notIsolatedTowers;
        while (isolatedTowers != 0){
            tower = Long.highestOneBit(isolatedTowers);
            isolatedTowers ^= tower;
            others = towers ^ tower;
            accessibility = 0;
            for (int i = 1; i < 9; i++){
                accessibility |= normalMoveValidator(others,i,isBlue);
            }
            for (int i = 1; i < 9; i++){
                if ((accessibility &  normalMoveValidator(tower,i,isBlue)) != 0){
                    notIsolatedTowers |= tower;
                    break;
                }
            }
        }

        return towersNumber(isBlue) -Long.bitCount(notIsolatedTowers);
    }

    @Override
    public int isolatedWallsNumber(boolean isBlue){
        long towers = isBlue ? bt : rt;
        long walls = isBlue ? bw : rw;
        long notIsolatedWalls = 0;
        for (int i = 1; i < 9; i++) notIsolatedWalls |= ((normalMoveValidator(towers,i,isBlue) & ~towers) & walls);
        return wallsNumber(isBlue) - Long.bitCount(notIsolatedWalls);
    }

    @Override
    public String printBoard(boolean isBlue){
        String color = isBlue ? "Blue" : "Red";
        long walls = isBlue ? bw : rw;
        long towers = isBlue ? bt : rt;
        int index;
        char[] board = String.format("%64s", Long.toBinaryString(walls)).replace(' ', '0').replace('1','W').toCharArray();
        while (towers != 0){
            index = Long.numberOfLeadingZeros(towers);
            towers ^= Long.highestOneBit(towers);
            board[index] = 'T';
        }
        StringBuilder boardString = new StringBuilder();
        for (int r = 1; r < 8; r++){
            for (int c = 0; c < 8; c++){
                boardString.append(board[(r * 8) + c]);
            }
            boardString.append("\n");
        }
        return "*************************\n"+
                "-------------------------\n"+
                color + " Pieces: \n"+
                boardString.toString() +
                "-------------------------\n";

    }

    @Override
    public int[] computeHashPositions(boolean isBlue) {
        int []values = isBlue ? new int []{0, 1, 2, 3} : new int []{2, 3, 0, 1};
        int[] positions = new int[56];
        long allPieces = bt|rt|rw|bw;
        int index;
        while (allPieces != 0){
            index = Long.numberOfLeadingZeros(allPieces) - 8;
            if (!isBlue) index = 55 - index;
            if ((bw & Long.highestOneBit(allPieces)) != 0) positions [index] = values[0];
            else if ((rw & Long.highestOneBit(allPieces)) != 0) positions [index] = values[2];
            else if ((bt & Long.highestOneBit(allPieces)) != 0) positions [index] = values[1];
            else positions [index] = values[3];
            allPieces ^= Long.highestOneBit(allPieces);
        }
        return positions;
    }

    ///////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////

    public int distancesFinder(boolean isBlue, long locations, int[] values) {
        long index = row_1;
        int row, result = 0;
        for (int i = 0; i < 7 ; i++){
            row = isBlue ? i : 6 - i;
            result += values[row] * (Long.bitCount(locations & index));
            index = index << 8;
        }
        return result;
    }

    public int columnsFinder(long locations, int[] values) {
        long indexLeft = 36170086419038336L;
        long indexRight = 282578800148737L;
        int result = 0;
        for (int i = 0; i < 4 ; i++){
            result += values[i] * (Long.bitCount(locations & (indexRight|indexLeft)));
            indexRight <<= 1;
            indexLeft >>= 1;
        }
        return result;
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////


    public long sacrificingMoveValidator(long initial, int direction, boolean isBlue){
        if (direction == 1) return isBlue ? (initial << 8) & rw : (initial >> 8) & bw;
        else if (direction == 2) return isBlue ? (initial << 7) & rw & ~col_A : (initial >> 7) & bw & ~col_H;
        else if (direction == 8) return isBlue ? (initial << 9) & rw & ~col_H  : (initial >> 9) & bw & ~col_A;
        else if (direction == 3) return isBlue ? (initial >> 1) & rw & ~col_A : (initial << 1) & bw & ~col_H;
        else if (direction == 7) return isBlue ? (initial << 1) & rw & ~col_H : (initial >> 1) & bw & ~col_A;
        else if (direction == 4) return isBlue ? (initial >> 9) & rw & ~col_A : (initial << 9) & bw & ~col_H;
        else if (direction == 5) return isBlue ? (initial >> 8) & rw : (initial << 8) & bw;
        else if (direction == 6) return isBlue ? (initial >> 7) & rw & ~col_H : (initial << 7) & bw & ~col_A;
        return 0L;
    }

    public long normalMoveValidator(long initial, int direction, boolean isBlue){
        long first = 0L, second = 0L;
        if (direction == 1) {
            first = isBlue ? (initial << 8) & ~(rw|rt|bt|row_7) : (initial >> 8) & ~(bw|bt|rt|row_1);
            second = isBlue ? (first << 8) & ~(rw|rt|bt) : (first >> 8) & ~(bw|bt|rt);
            first = isBlue ? second >> 8 : second << 8;
        }
        else if (direction == 2){
            first = isBlue ? (initial << 7) & ~(rw|rt|bt|col_A|col_H|row_7) : (initial >> 7) & ~(bw|bt|rt|col_A|col_H|row_1);
            second = isBlue ? (first << 7) & ~(rw|rt|bt) : (first >> 7) & ~(bw|bt|rt);
            first = isBlue ? second >> 7 : second << 7;
        }
        else if (direction == 8){
            first = isBlue ? (initial << 9) & ~(rw|rt|bt|col_A|col_H|row_7) : (initial >> 9) & ~(bw|bt|rt|col_A|col_H|row_1);
            second = isBlue ? (first << 9) & ~(rw|rt|bt) : (first >> 9) & ~(bw|bt|rt);
            first = isBlue ? second >> 9 : second << 9;
        }
        else if (direction == 3){
            first = isBlue ? (initial >> 1) & ~(rw|rt|bt|col_A|col_H) : (initial << 1) & ~(bw|bt|rt|col_A|col_H);
            second = isBlue ? (first >> 1) & ~(rw|rt|bt) : (first << 1) & ~(bw|bt|rt);
            first = isBlue ? second << 1 : second >> 1;
        }

        else if (direction == 7){
            first = isBlue ? (initial << 1) & ~(rw|rt|bt|col_A|col_H) : (initial >> 1) & ~(bw|bt|rt|col_A|col_H);
            second = isBlue ? (first << 1) & ~(rw|rt|bt) : (first >> 1) & ~(bw|bt|rt);
            first = isBlue ? second >> 1 : second << 1;
        }
        else if (direction == 4){
            first = isBlue ? ((initial & ~(row_1))>> 9) & ~(rw|rt|bt|col_A|col_H|row_1) : ((initial & ~(row_7)) << 9) & ~(bw|bt|rt|col_A|col_H|row_7);
            second = isBlue ? (first >> 9) & ~(rw|rt|bt) : (first << 9) & ~(bw|bt|rt);
            first = isBlue ? second << 9 : second >> 9;
        }
        else if (direction == 5){
            first = isBlue ? ((initial & ~(row_1)) >> 8) & ~(rw|rt|bt|row_1) : ((initial & ~(row_7)) << 8) & ~(bw|bt|rt|row_7);
            second = isBlue ? (first >> 8) & ~(rw|rt|bt) : (first << 8) & ~(bw|bt|rt);
            first = isBlue ? second << 8 : second >> 8;
        }
        else if (direction == 6){
            first = isBlue ? ((initial & ~(row_1)) >> 7) & ~(rw|rt|bt|col_A|col_H|row_1) : ((initial & ~(row_7)) << 7) & ~(bw|bt|rt|col_A|col_H|row_7);
            second = isBlue ? (first >> 7) & ~(rw|rt|bt) : (first << 7) & ~(bw|bt|rt);
            first = isBlue ? second << 7 : second >> 7;
        }
        return (first|second) ;
    }


//////////////////////////////////////////////////////////////////////////////////

    public long sacrificingMovesPiecesFinder(long initial, int direction, boolean isBlue){
        long target, pieces = 0L;
        if (direction == 1){
            target = isBlue ? (initial << 8) & rw : (initial >> 8) & bw;
            pieces = isBlue ? target >> 8 : target << 8;
        }
        else if (direction == 2){
            target = isBlue ? (initial << 7) & rw & ~col_A : (initial >> 7) & bw & ~col_H;
            pieces = isBlue ? target >> 7 : target << 7;
        }
        else if (direction == 8){
            target = isBlue ? (initial << 9) & rw & ~col_H  : (initial >> 9) & bw & ~col_A;
            pieces = isBlue ? target >> 9 : target << 9;
        }
        else if (direction == 3){
            target = isBlue ? (initial >> 1) & rw & ~col_A : (initial << 1) & bw & ~col_H;
            pieces = isBlue ? target << 1 : target >> 1;
        }
        else if (direction == 7){
            target = isBlue ? (initial << 1) & rw & ~col_H : (initial >> 1) & bw & ~col_A;
            pieces = isBlue ? target >> 1 : target << 1;
        }
        else if (direction == 4){
            target = isBlue ? (initial >> 9) & rw & ~col_A : (initial << 9) & bw & ~col_H;
            pieces = isBlue ? target << 9 : target >> 9;
        }
        else if (direction == 5){
            target = isBlue ? (initial >> 8) & rw : (initial << 8) & bw;
            pieces = isBlue ? target << 8 : target >> 8;
        }
        else if (direction == 6){
            target = isBlue ? (initial >> 7) & rw & ~col_H : (initial << 7) & bw & ~col_A;
            pieces = isBlue ? target << 7 : target >> 7;
        }
        return pieces;
    }

    public long quietMovesPiecesFinder(long initial, int direction, boolean isBlue){
        long first, second, empty = ~(rt|rw|bt|bw), pieces = 0L;
        if (direction == 1) {
            first = isBlue ? (initial << 8) & ~(row_7) & empty : (initial >> 8) & ~(row_1) & empty;
            second = isBlue ? (first << 8) & empty : (first >> 8) & empty;
            first = isBlue ? second >> 8 : second << 8;
            pieces = isBlue ? first >> 8 : first << 8;
        }
        else if (direction == 2){
            first = isBlue ? (initial << 7) & ~(col_A|col_H|row_7) & empty : (initial >> 7) & ~(col_A|col_H|row_1) & empty;
            second = isBlue ? (first << 7) & empty : (first >> 7) & empty;
            first = isBlue ? second >> 7 : second << 7;
            pieces = isBlue ? first >> 7 : first << 7;
        }
        else if (direction == 8){
            first = isBlue ? (initial << 9) & ~(col_A|col_H|row_7) & empty : (initial >> 9) & ~(col_A|col_H|row_1) & empty;
            second = isBlue ? (first << 9) & empty : (first >> 9) & empty;
            first = isBlue ? second >> 9 : second << 9;
            pieces = isBlue ? first >> 9 : first << 9;
        }
        else if (direction == 3){
            first = isBlue ? (initial >> 1) & ~(col_A|col_H) & empty : (initial << 1) & ~(col_A|col_H) & empty;
            second = isBlue ? (first >> 1) & empty : (first << 1) & empty;
            first = isBlue ? second << 1 : second >> 1;
            pieces = isBlue ? first << 1 : first >> 1;
        }
        else if (direction == 7){
            first = isBlue ? (initial << 1) & ~(col_A|col_H) & empty : (initial >> 1) & ~(col_A|col_H) & empty;
            second = isBlue ? (first << 1) & empty : (first >> 1) & empty;
            first = isBlue ? second >> 1 : second << 1;
            pieces = isBlue ? first >> 1 : first << 1;
        }
        else if (direction == 4){
            first = isBlue ? (initial >> 9) & ~(col_A|col_H|row_1) & empty : (initial << 9) & ~(col_A|col_H|row_7) & empty;
            second = isBlue ? (first >> 9) & empty : (first << 9) & empty;
            first = isBlue ? second << 9 : second >> 9;
            pieces = isBlue ? first << 9 : first >> 9;
        }
        else if (direction == 5){
            first = isBlue ? (initial >> 8) & ~(row_1) & empty : (initial << 8) & ~(row_7) & empty;
            second = isBlue ? (first >> 8) & empty : (first << 8) & empty;
            first = isBlue ? second << 8 : second >> 8;
            pieces = isBlue ? first << 8 : first >> 8;
        }
        else if (direction == 6){
            first = isBlue ? (initial >> 7) & ~(col_A|col_H|row_1) & empty : (initial << 7) & ~(col_A|col_H|row_7) & empty;
            second = isBlue ? (first >> 7) & empty : (first << 7) & empty;
            first = isBlue ? second << 7 : second >> 7;
            pieces = isBlue ? first << 7 : first >> 7;
        }
        return pieces;
    }

    public long friendOnBothMovesPiecesFinder(long initial, int direction, boolean isBlue){
        long first, second, friend = isBlue ? bw : rw, pieces = 0L;
        if (direction == 1) {
            first = isBlue ? (initial << 8) & ~(row_7) & friend : (initial >> 8) & ~(row_1) & friend;
            second = isBlue ? (first << 8) & friend : (first >> 8) & friend;
            first = isBlue ? second >> 8 : second << 8;
            pieces = isBlue ? first >> 8 : first << 8;
        }
        else if (direction == 2){
            first = isBlue ? (initial << 7) & ~(col_A|col_H|row_7) & friend : (initial >> 7) & ~(col_A|col_H|row_1) & friend;
            second = isBlue ? (first << 7) & friend : (first >> 7) & friend;
            first = isBlue ? second >> 7 : second << 7;
            pieces = isBlue ? first >> 7 : first << 7;
        }
        else if (direction == 8){
            first = isBlue ? (initial << 9) & ~(col_A|col_H|row_7) & friend : (initial >> 9) & ~(col_A|col_H|row_1) & friend;
            second = isBlue ? (first << 9) & friend : (first >> 9) & friend;
            first = isBlue ? second >> 9 : second << 9;
            pieces = isBlue ? first >> 9 : first << 9;
        }
        else if (direction == 3){
            first = isBlue ? (initial >> 1) & ~(col_A|col_H) & friend : (initial << 1) & ~(col_A|col_H) & friend;
            second = isBlue ? (first >> 1) & friend : (first << 1) & friend;
            first = isBlue ? second << 1 : second >> 1;
            pieces = isBlue ? first << 1 : first >> 1;
        }
        else if (direction == 7){
            first = isBlue ? (initial << 1) & ~(col_A|col_H) & friend : (initial >> 1) & ~(col_A|col_H) & friend;
            second = isBlue ? (first << 1) & friend : (first >> 1) & friend;
            first = isBlue ? second >> 1 : second << 1;
            pieces = isBlue ? first >> 1 : first << 1;
        }
        else if (direction == 4){
            first = isBlue ? (initial >> 9) & ~(col_A|col_H|row_1) & friend : (initial << 9) & ~(col_A|col_H|row_7) & friend;
            second = isBlue ? (first >> 9) & friend : (first << 9) & friend;
            first = isBlue ? second << 9 : second >> 9;
            pieces = isBlue ? first << 9 : first >> 9;
        }
        else if (direction == 5){
            first = isBlue ? (initial >> 8) & ~(row_1) & friend : (initial << 8) & ~(row_7) & friend;
            second = isBlue ? (first >> 8) & friend : (first << 8) & friend;
            first = isBlue ? second << 8 : second >> 8;
            pieces = isBlue ? first << 8 : first >> 8;
        }
        else if (direction == 6){
            first = isBlue ? (initial >> 7) & ~(col_A|col_H|row_1) & friend : (initial << 7) & ~(col_A|col_H|row_7) & friend;
            second = isBlue ? (first >> 7) & friend : (first << 7) & friend;
            first = isBlue ? second << 7 : second >> 7;
            pieces = isBlue ? first << 7 : first >> 7;
        }
        return pieces;
    }

    public long friendOnNearPiecesFinder(long initial, int direction, boolean isBlue){
        long first, second, friend = isBlue ? bw : rw, empty = ~(rt|rw|bt|bw), pieces = 0L;
        if (direction == 1) {
            first = isBlue ? (initial << 8) & ~(row_7) & friend : (initial >> 8) & ~(row_1) & friend;
            second = isBlue ? (first << 8) & empty : (first >> 8) & empty;
            first = isBlue ? second >> 8 : second << 8;
            pieces = isBlue ? first >> 8 : first << 8;
        }
        else if (direction == 2){
            first = isBlue ? (initial << 7) & ~(col_A|col_H|row_7) & friend : (initial >> 7) & ~(col_A|col_H|row_1) & friend;
            second = isBlue ? (first << 7) & empty : (first >> 7) & empty;
            first = isBlue ? second >> 7 : second << 7;
            pieces = isBlue ? first >> 7 : first << 7;
        }
        else if (direction == 8){
            first = isBlue ? (initial << 9) & ~(col_A|col_H|row_7) & friend : (initial >> 9) & ~(col_A|col_H|row_1) & friend;
            second = isBlue ? (first << 9) & empty : (first >> 9) & empty;
            first = isBlue ? second >> 9 : second << 9;
            pieces = isBlue ? first >> 9 : first << 9;
        }
        else if (direction == 3){
            first = isBlue ? (initial >> 1) & ~(col_A|col_H) & friend : (initial << 1) & ~(col_A|col_H) & friend;
            second = isBlue ? (first >> 1) & empty : (first << 1) & empty;
            first = isBlue ? second << 1 : second >> 1;
            pieces = isBlue ? first << 1 : first >> 1;
        }
        else if (direction == 7){
            first = isBlue ? (initial << 1) & ~(col_A|col_H) & friend : (initial >> 1) & ~(col_A|col_H) & friend;
            second = isBlue ? (first << 1) & empty : (first >> 1) & empty;
            first = isBlue ? second >> 1 : second << 1;
            pieces = isBlue ? first >> 1 : first << 1;
        }
        else if (direction == 4){
            first = isBlue ? (initial >> 9) & ~(col_A|col_H|row_1) & friend : (initial << 9) & ~(col_A|col_H|row_7) & friend;
            second = isBlue ? (first >> 9) & empty : (first << 9) & empty;
            first = isBlue ? second << 9 : second >> 9;
            pieces = isBlue ? first << 9 : first >> 9;
        }
        else if (direction == 5){
            first = isBlue ? (initial >> 8) & ~(row_1) & friend : (initial << 8) & ~(row_7) & friend;
            second = isBlue ? (first >> 8) & empty : (first << 8) & empty;
            first = isBlue ? second << 8 : second >> 8;
            pieces = isBlue ? first << 8 : first >> 8;
        }
        else if (direction == 6){
            first = isBlue ? (initial >> 7) & ~(col_A|col_H|row_1) & friend : (initial << 7) & ~(col_A|col_H|row_7) & friend;
            second = isBlue ? (first >> 7) & empty : (first << 7) & empty;
            first = isBlue ? second << 7 : second >> 7;
            pieces = isBlue ? first << 7 : first >> 7;
        }
        return pieces;
    }

    public long friendOnFarMovesPiecesFinder(long initial, int direction, boolean isBlue){
        long first, second, friend = isBlue ? bw : rw, empty = ~(rt|rw|bt|bw), pieces = 0L;
        if (direction == 1) {
            first = isBlue ? (initial << 8) & ~(row_7) & empty : (initial >> 8) & ~(row_1) & empty;
            second = isBlue ? (first << 8) & friend : (first >> 8) & friend;
            first = isBlue ? second >> 8 : second << 8;
            pieces = isBlue ? first >> 8 : first << 8;
        }
        else if (direction == 2){
            first = isBlue ? (initial << 7) & ~(col_A|col_H|row_7) & empty : (initial >> 7) & ~(col_A|col_H|row_1) & empty;
            second = isBlue ? (first << 7) & friend : (first >> 7) & friend;
            first = isBlue ? second >> 7 : second << 7;
            pieces = isBlue ? first >> 7 : first << 7;
        }
        else if (direction == 8){
            first = isBlue ? (initial << 9) & ~(col_A|col_H|row_7) & empty : (initial >> 9) & ~(col_A|col_H|row_1) & empty;
            second = isBlue ? (first << 9) & friend : (first >> 9) & friend;
            first = isBlue ? second >> 9 : second << 9;
            pieces = isBlue ? first >> 9 : first << 9;
        }
        else if (direction == 3){
            first = isBlue ? (initial >> 1) & ~(col_A|col_H) & empty : (initial << 1) & ~(col_A|col_H) & empty;
            second = isBlue ? (first >> 1) & friend : (first << 1) & friend;
            first = isBlue ? second << 1 : second >> 1;
            pieces = isBlue ? first << 1 : first >> 1;
        }
        else if (direction == 7){
            first = isBlue ? (initial << 1) & ~(col_A|col_H) & empty : (initial >> 1) & ~(col_A|col_H) & empty;
            second = isBlue ? (first << 1) & friend : (first >> 1) & friend;
            first = isBlue ? second >> 1 : second << 1;
            pieces = isBlue ? first >> 1 : first << 1;
        }
        else if (direction == 4){
            first = isBlue ? (initial >> 9) & ~(col_A|col_H|row_1) & empty : (initial << 9) & ~(col_A|col_H|row_7) & empty;
            second = isBlue ? (first >> 9) & friend : (first << 9) & friend;
            first = isBlue ? second << 9 : second >> 9;
            pieces = isBlue ? first << 9 : first >> 9;
        }
        else if (direction == 5){
            first = isBlue ? (initial >> 8) & ~(row_1) & empty : (initial << 8) & ~(row_7) & empty;
            second = isBlue ? (first >> 8) & friend : (first << 8) & friend;
            first = isBlue ? second << 8 : second >> 8;
            pieces = isBlue ? first << 8 : first >> 8;
        }
        else if (direction == 6){
            first = isBlue ? (initial >> 7) & ~(col_A|col_H|row_1) & empty : (initial << 7) & ~(col_A|col_H|row_7) & empty;
            second = isBlue ? (first >> 7) & friend : (first << 7) & friend;
            first = isBlue ? second << 7 : second >> 7;
            pieces = isBlue ? first << 7 : first >> 7;
        }
        return pieces;
    }

////////////////////////////////////////////////////////////////////////////////////

    public long sacrificingMoveRouteFinder(long initial, int direction, boolean isBlue){
        if (direction == 1) return isBlue ? (initial << 8) : (initial >> 8);
        else if (direction == 2) return isBlue ? (initial << 7) : (initial >> 7);
        else if (direction == 8) return isBlue ? (initial << 9) : (initial >> 9);
        else if (direction == 3) return isBlue ? (initial >> 1) : (initial << 1);
        else if (direction == 7) return isBlue ? (initial << 1) : (initial >> 1);
        else if (direction == 4) return isBlue ? (initial >> 9) : (initial << 9);
        else if (direction == 5) return isBlue ? (initial >> 8) : (initial << 8);
        else if (direction == 6) return isBlue ? (initial >> 7) : (initial << 7);
        return 0L;    }

    public long normalMoveRouteFinder(long initial, int direction, boolean isBlue){
        long first = 0L, second = 0L;
        if (direction == 1) {
            first = isBlue ? (initial << 8) : (initial >> 8);
            second = isBlue ? (first << 8) : (first >> 8);
        }
        else if (direction == 2){
            first = isBlue ? (initial << 7) : (initial >> 7);
            second = isBlue ? (first << 7) : (first >> 7);
        }
        else if (direction == 8){
            first = isBlue ? (initial << 9) : (initial >> 9);
            second = isBlue ? (first << 9) : (first >> 9);
        }
        else if (direction == 3){
            first = isBlue ? (initial >> 1) : (initial << 1);
            second = isBlue ? (first >> 1) : (first << 1);
        }
        else if (direction == 7){
            first = isBlue ? (initial << 1) : (initial >> 1);
            second = isBlue ? (first << 1) : (first >> 1);
        }
        else if (direction == 4){
            first = isBlue ? (initial >> 9) : (initial << 9);
            second = isBlue ? (first >> 9): (first << 9);
        }
        else if (direction == 5){
            first = isBlue ? (initial >> 8): (initial << 8);
            second = isBlue ? (first >> 8): (first << 8);
        }
        else if (direction == 6){
            first = isBlue ? (initial >> 7) : (initial << 7);
            second = isBlue ? (first >> 7) : (first << 7);
        }
        return first|second;
    }
    //////////////////////////////////////////////////////////////////////////////

    public Move moveProvider(int direction, int location, MoveType moveType){
        int type;
        if (moveType == MoveType.QUIET) type = 0;
        else if (moveType == MoveType.FRIEND_ON_NEAR) type = 1;
        else if (moveType == MoveType.FRIEND_ON_FAR) type = 2;
        else if (moveType == MoveType.FRIEND_ON_BOTH) type = 3;
        else type = 4;
        return new Move((short) (location << 7 | direction << 3 | type));
    }
    public long oneTypeMovesPiecesFinder(boolean isBlue, MoveType moveType, long initial, int direction){
        if (moveType == MoveType.QUIET) return quietMovesPiecesFinder(initial, direction, isBlue);
        else if (moveType == MoveType.FRIEND_ON_NEAR) return friendOnNearPiecesFinder(initial, direction, isBlue);
        else if (moveType == MoveType.FRIEND_ON_FAR) return friendOnFarMovesPiecesFinder(initial, direction, isBlue);
        else if (moveType == MoveType.FRIEND_ON_BOTH) return friendOnBothMovesPiecesFinder(initial, direction, isBlue);
        else return sacrificingMovesPiecesFinder(initial, direction, isBlue);
    }

    public List<Integer> locationsProvider(long pieces){
        List<Integer> locations = new ArrayList<>();
        int loc;
        while (pieces != 0){
            loc = Long.numberOfLeadingZeros(pieces);
            pieces ^= Long.highestOneBit(pieces);
            locations.add(63 - loc);
        }
        return locations;
    }

    public static void longBitsPrinter(long locations){
        String bits = String.format("%64s", Long.toBinaryString(locations)).replace(' ', '0');
        for (int i = 1; i < 8; i++)System.out.println(bits.substring(i * 8, (i * 8) + 8));
        System.out.println();
    }

}
