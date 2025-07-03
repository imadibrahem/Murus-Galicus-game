package model.transpositionTable;

import model.Board;
import model.move.Move;

import java.io.*;
import java.security.SecureRandom;

public class ZobristHashing {
    private static final int BOARD_SIZE = 56;
    private static final int PIECE_TYPES = 4;
    private static final String KEY_FILE = "zobrist_keys.dat";;
    private final SecureRandom random;
    private final Board board;
    private final boolean isBoardBlue;
    private long[][] keys;
    private long hash;

    public ZobristHashing(Board board, boolean isBoardBlue) {
        this.keys = new long[BOARD_SIZE][PIECE_TYPES];
        this.board = board;
        this.isBoardBlue = isBoardBlue;
        this.random = new SecureRandom();
        loadKeys();
    }

    private void loadKeys() {
        File file = new File(KEY_FILE);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                keys = (long[][]) ois.readObject();
                System.out.println("Zobrist keys loaded from file.");
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Failed to load Zobrist keys from file, loading keys again..");
                loadKeys();
            }
        } else {
            generateKeys();
        }
    }

    private void generateKeys() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < PIECE_TYPES; j++) {
                keys[i][j] = random.nextLong();
            }
        }
        saveKeys();
    }

    private void saveKeys() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(KEY_FILE))) {
            oos.writeObject(keys);
            System.out.println("Zobrist keys saved to file.");
        } catch (IOException e) {
            System.err.println("Failed to save Zobrist keys to file, saving again..");
            saveKeys();
        }
    }

    public void computeHash() {
        long hash = 0L;
        int[] computeHashPositions = board.computeHashPositions(isBoardBlue);
        for (int i = 0; i < computeHashPositions.length; i++) hash ^= keys[i][computeHashPositions[i]];
        this.hash = hash;
    }

    public void updateHashForMoves(Move move, boolean isBlue) {
        int []values = isBlue == isBoardBlue ? new int []{0, 1, 2, 3} : new int []{2, 3, 0, 1};
        int initialLocation = move.getInitialLocation(isBlue);
        short nearTarget;
        short farTarget;
        hash ^= keys[initialLocation][values[1]];
        if (move.isTargetEnemy()){
            hash ^= keys[initialLocation][values[0]];
            nearTarget = board.sacrificingMovesLocation(isBlue,initialLocation, move.getDirection());
            hash ^= keys[nearTarget][values[2]];
        }
        else {
            short[] normalMovesLocation = board.normalMovesLocation(isBlue,initialLocation, move.getDirection());
            nearTarget = normalMovesLocation[0];
            farTarget = normalMovesLocation[1];
            hash ^= keys[nearTarget][values[0]];
            hash ^= keys[farTarget][values[0]];
            if (move.isTargetNearFriendly() || move.isTargetBothFriendly()) hash ^= keys[nearTarget][values[1]];
            if (move.getTargetType() > 1) hash ^= keys[farTarget][values[1]];
        }
    }

    public long getHash() {
        return hash;
    }
}
