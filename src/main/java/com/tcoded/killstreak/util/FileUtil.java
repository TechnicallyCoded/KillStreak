package com.tcoded.killstreak.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Utility methods for reading and writing files.
 */
public final class FileUtil {

    private FileUtil() {
    }

    /**
     * Reads the given file into a string.
     *
     * @param file file to read
     * @return file contents or empty string if file does not exist
     * @throws IOException if an error occurs while reading
     */
    public static String readFile(File file) throws IOException {
        if (!file.exists()) {
            return "";
        }
        return Files.readString(file.toPath(), StandardCharsets.UTF_8);
    }

    /**
     * Writes the given string to the file, creating parent directories if needed.
     *
     * @param file    target file
     * @param content content to write
     * @throws IOException if an error occurs while writing
     */
    public static void writeFile(File file, String content) throws IOException {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        Files.writeString(file.toPath(), content, StandardCharsets.UTF_8);
    }

    /**
     * Atomically writes the given string to a file using a temporary file.
     * This prevents data corruption if the write fails partway through.
     *
     * @param file    target file
     * @param content content to write
     * @throws IOException if an error occurs while writing
     */
    public static void writeFileAtomic(File file, String content) throws IOException {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        // Create temporary file in the same directory
        File tempFile = new File(parent, file.getName() + ".tmp");

        try {
            // Write to temporary file
            Files.writeString(tempFile.toPath(), content, StandardCharsets.UTF_8);

            // Atomically move temp file to target location
            Files.move(tempFile.toPath(), file.toPath(),
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            // Clean up temp file on failure
            if (tempFile.exists()) {
                tempFile.delete();
            }
            throw e;
        }
    }
}
