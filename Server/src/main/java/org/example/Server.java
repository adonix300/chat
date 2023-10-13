package org.example;

import org.example.Logger.ServerLogger;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Server {
    private static int port;
    private static Map<String, Socket> nicknameAndSocketMap = new HashMap<>();
    private static List<Socket> clientSockets = new ArrayList<>();

    public static void main(String[] args) {
        setting("Server\\config\\settings.txt");

        try (ServerSocket serverSocket = new ServerSocket(port);
             Scanner scanner = new Scanner(System.in)) {

            System.out.println("Server started");

            new Thread(() -> {
                serverCommands(scanner);
            }).start();

            while (true) {
                Socket socket = serverSocket.accept();

                new Thread(() -> {
                    handleClient(socket);
                }).start();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void serverCommands(Scanner scanner) {
        while (true) {
            String serverCommand = scanner.nextLine();
            if (serverCommand.startsWith("/broadcast")) {
                String message = serverCommand.substring("/broadcast".length()).trim();
                printWriteAndLogInfo("Server: " + message);
            } else if (serverCommand.startsWith("/kick")) {
//                String nameToKick = serverCommand.substring("/kick".length()).trim();
//                try {
//                    nicknameAndSocketMap.get(nameToKick).close();
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//                nicknameAndSocketMap.remove(nameToKick);

            }
        }
    }

    private static void handleClient(Socket socket) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            String nickname = reader.readLine();
            nicknameAndSocketMap.put(nickname, socket);
            printWriteAndLogInfo(nickname + " has joined the chat.");

            while (true) {
                String response = reader.readLine();
                printWriteAndLogInfo(nickname + ": " + response);
                if (response.equals("/exit")) {
                    break;
                }
            }

            nicknameAndSocketMap.remove(nickname);
            printWriteAndLogInfo(nickname + " has left the chat.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void broadcastMessage(String message) {
        synchronized (nicknameAndSocketMap) {
            for (Map.Entry<String, Socket> entry : nicknameAndSocketMap.entrySet()) {
                try {
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(entry.getValue().getOutputStream()));
                    writer.write("[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "] " + message);
                    writer.newLine();
                    writer.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void setting(String filename) {
        try (BufferedReader fileReader = new BufferedReader(new FileReader(filename))) {
            String line = fileReader.readLine();
            String[] settings = line.split(":");
            port = Integer.parseInt(settings[1].trim());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void printWriteAndLogInfo(String line) {
        System.out.println(line);
        broadcastMessage(line);
        ServerLogger.logInfo(line);
    }
}
