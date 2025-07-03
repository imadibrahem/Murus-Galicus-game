package model.transpositionTable;

import model.move.Move;

public class ReportTranspositionTable extends TranspositionTable {

    public void put(long hash, int depth, int score,int priority ,byte flag, Move move) {
        TranspositionEntry newEntry = new TranspositionEntry(hash, move.getValue(), (byte) ((priority << 4) | depth), (short) (((score + 8191) << 2) | flag));
        int index = Math.abs((int) (hash % tableSize));
        TranspositionEntry existingEntry = table[index];
        if (existingEntry == null) used++;
        else if (existingEntry.getHash() != newEntry.getHash()) conflicts++;
        if ((existingEntry == null) || (!existingEntry.equals(newEntry) && (priority > existingEntry.getPriority() || (priority == existingEntry.getPriority() && depth > existingEntry.getDepth()))))table[index] = newEntry;
    }

    public void putForMemory(TranspositionEntry newEntry){
        int index = Math.abs((int) (newEntry.getHash() % tableSize));
        TranspositionEntry existingEntry = table[index];
        if (existingEntry == null) used++;
        else if (newEntry.getHash() != existingEntry.getHash()) conflicts++;
        if ((existingEntry == null) || (!existingEntry.equals(newEntry) && (newEntry.getPriority() > existingEntry.getPriority() || (newEntry.getPriority() == existingEntry.getPriority() && newEntry.getDepth() > existingEntry.getDepth()))))table[index] = newEntry;
    }

    public TranspositionEntry get(long hash) {
        calls++;
        int index = Math.abs((int) (hash % tableSize));
        TranspositionEntry entry = table[index];
        if (entry == null) emptyCalls++;
        else if (entry.getHash() != hash) misses++;
        else if (entry.getHash() == hash) hits++;
        return (entry != null && entry.getHash() == hash) ? entry : null;
    }
}
