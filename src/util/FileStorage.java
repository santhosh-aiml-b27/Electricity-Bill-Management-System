package util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles reading from and writing to plain-text data files stored in the 'data/' directory.
 */
public class FileStorage {
    private static final String DATA_DIR = "data";

    static {
        // Ensure data directory exists on startup
        File dir = new File(DATA_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * Reads all non-empty lines from the specified file inside the data directory.
     * If the file does not exist, an empty list is returned.
     */
    public static List<String> readAllLines(String fileName) {
        List<String> lines = new ArrayList<>();
        File file = new File(DATA_DIR, fileName);

        if (!file.exists()) {
            return lines;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading file " + fileName + ": " + e.getMessage());
        }

        return lines;
    }

    /**
     * Overwrites the specified file with the provided lines.
     */
    public static boolean writeAllLines(String fileName, List<String> lines) {
        File file = new File(DATA_DIR, fileName);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, false))) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
            return true;
        } catch (IOException e) {
            System.err.println("Error writing to file " + fileName + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Appends a single line to the specified file.
     */
    public static boolean appendLine(String fileName, String line) {
        File file = new File(DATA_DIR, fileName);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            writer.write(line);
            writer.newLine();
            return true;
        } catch (IOException e) {
            System.err.println("Error appending to file " + fileName + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Checks if a file exists in the data directory.
     */
    public static boolean fileExists(String fileName) {
        File file = new File(DATA_DIR, fileName);
        return file.exists() && file.isFile();
    }
}
