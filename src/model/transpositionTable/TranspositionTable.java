package model.transpositionTable;

import model.move.Move;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

public class TranspositionTable implements Serializable {
    @Serial
    protected static final long serialVersionUID = 1L;
    protected final int tableSize = 1 << 22;
    protected final TranspositionEntry[] table = new TranspositionEntry[tableSize];
    public int used = 0;
    public int conflicts = 0;
    public int misses = 0;
    public int hits = 0;
    public int calls = 0;
    public int emptyCalls = 0;

    public void put(long hash, int depth, int score,int priority ,byte flag, Move move) {
        TranspositionEntry newEntry = new TranspositionEntry(hash, move.getValue(), (byte) ((priority << 4) | depth), (short) (((score + 8191) << 2) | flag));
        int index = Math.abs((int) (hash % tableSize));
        TranspositionEntry existingEntry = table[index];
        if ((existingEntry == null) || (!existingEntry.equals(newEntry) && (priority > existingEntry.getPriority() || (priority == existingEntry.getPriority() && depth > existingEntry.getDepth()))))table[index] = newEntry;
    }

    public void putForMemory(TranspositionEntry newEntry){
        int index = Math.abs((int) (newEntry.getHash() % tableSize));
        TranspositionEntry existingEntry = table[index];
        if ((existingEntry == null) || (!existingEntry.equals(newEntry) && (newEntry.getPriority() > existingEntry.getPriority() || (newEntry.getPriority() == existingEntry.getPriority() && newEntry.getDepth() > existingEntry.getDepth()))))table[index] = newEntry;
    }

    public TranspositionEntry get(long hash) {
        int index = Math.abs((int) (hash % tableSize));
        TranspositionEntry entry = table[index];
        return (entry != null && entry.getHash() == hash) ? entry : null;
    }

    public TranspositionEntry buildEntry (long hash, int depth, int score,int priority ,byte flag, Move move) {
        return new TranspositionEntry(hash, move.getValue(), (byte) ((priority << 4) | depth), (short) (((score + 8191) << 2) | flag));
    }

    public void resetCounters(){
        this.conflicts = 0;
        this.calls = 0;
        this.misses = 0;
        this.hits = 0;
        this.emptyCalls = 0;
    }

    public int getTableSize() {
        return tableSize;
    }

    public static class TranspositionEntry implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        public static final byte EXACT = 0;
        public static final byte LOWERBOUND = 1;
        public static final byte UPPERBOUND = 2;
        private final long hash;
        private final short bestMoveValue;
        private final byte priority_depth;
        private final short flag_score;

        public TranspositionEntry(long hash, short bestMoveValue, byte priority_depth, short flag_score) {
            this.hash = hash;
            this.bestMoveValue = bestMoveValue;
            this.priority_depth = priority_depth;
            this.flag_score = flag_score;
        }

        public long getHash() {
            return hash;
        }

        public short getBestMoveValue() {
            return bestMoveValue;
        }

        public int getScore(){
            return ((flag_score & 65532) >>> 2) - 8191;
        }

        public byte getFlag(){
            return (byte) (flag_score & 3);
        }

        public int getDepth(){
            return priority_depth & 15;
        }

        public int getPriority(){
            return  ((priority_depth) & 240) >>> 4;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TranspositionEntry that = (TranspositionEntry) o;
            return hash == that.hash && bestMoveValue == that.bestMoveValue && priority_depth == that.priority_depth && flag_score == that.flag_score;
        }

        @Override
        public int hashCode() {
            return Objects.hash(hash, bestMoveValue, priority_depth, flag_score);
        }
    }
}
