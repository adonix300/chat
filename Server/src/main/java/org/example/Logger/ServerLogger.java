package org.example.Logger;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ServerLogger {
    static PrintWriter writer;

    static {
        try {
            writer = new PrintWriter(new FileWriter("Server\\logs\\server.log", true));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void log(String message) {
        writer.println(message);
        writer.flush();
    }

    public static String formatLogMessage(String level, String message) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return String.format("[%s] %s: %s", timestamp, level, message);
    }

    public static void logInfo(String message) {
        String logMessage = formatLogMessage("INFO", message);
        log(logMessage);
    }

    public static void logWarning(String message) {
        String logMessage = formatLogMessage("WARNING", message);
        log(logMessage);
    }

    public static void logError(String message) {
        String logMessage = formatLogMessage("ERROR", message);
        log(logMessage);
    }

    public static void close () {
        writer.close();
    }
}
