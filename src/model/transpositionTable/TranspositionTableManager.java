package model.transpositionTable;

import java.io.*;

public class TranspositionTableManager {
    private static final String FILE_NAME = "transpositionTable.ser";
    private static final String BACKUP_FILE_NAME = "transpositionTable.bak";
    private static final String TEMP_FILE_NAME = "transpositionTable.tmp";

    public static void saveTranspositionTable(TranspositionTable transpositionTable) {
        File tempFile = new File(TEMP_FILE_NAME);
        File originalFile = new File(FILE_NAME);
        File backupFile = new File(BACKUP_FILE_NAME);
        System.out.println("saving..");

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(tempFile))) {
            oos.writeObject(transpositionTable);
            oos.flush();
            oos.close();

            if (originalFile.exists()) {
                if (backupFile.exists()) {
                    backupFile.delete();
                }
                originalFile.renameTo(backupFile);
            }

            tempFile.renameTo(originalFile);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            tempFile.delete();
            System.out.println("done..");
            System.out.println("======================================");
        }
    }

    public static TranspositionTable loadTranspositionTable(boolean isReportTable) {
        File originalFile = new File(FILE_NAME);
        File backupFile = new File(BACKUP_FILE_NAME);
        System.out.println("loading..");

        TranspositionTable table = loadTableFromFile(originalFile);
        if (table == null) {
            table = loadTableFromFile(backupFile);
            if (table == null) {
                return isReportTable? new ReportTranspositionTable() : new TranspositionTable();
            }
        }
        return table;
    }

    private static TranspositionTable loadTableFromFile(File file) {
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                return (TranspositionTable) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
