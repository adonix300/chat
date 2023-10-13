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
    private static Map<String, Socket> clientSockets = new HashMap<>();

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

    private static void handleClient(Socket socket) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            String nickname = reader.readLine();
            clientSockets.put(nickname, socket);
            printWriteAndLogInfo(nickname + " has joined the chat.");

            while (true) {
                String response = reader.readLine();
                printWriteAndLogInfo(nickname + ": " + response);
                if (response.equals("/exit")) {
                    break;
                }
            }

            clientSockets.remove(nickname);
            printWriteAndLogInfo(nickname + " has left the chat.");
        } catch (IOException e) {
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void serverCommands(Scanner scanner) {
        while (true) {
            String serverCommand = scanner.nextLine();
            if (serverCommand.startsWith("/broadcast")) {
                String message = serverCommand.substring("/broadcast".length()).trim();
                printWriteAndLogInfo("Server: " + message);

            } else if (serverCommand.startsWith("/kick")) {
                String nameToKick = serverCommand.substring("/kick".length()).trim();
                kickClient(nameToKick);
            } else if (serverCommand.startsWith("/pm")) {
                String message = serverCommand.substring("/pm".length()).trim();
                String[] nicknameFinder = message.split(" ");
                String nickname = nicknameFinder[0];
                String normalMessage = message.substring(nickname.length()).trim();
                privateMessage(nickname, normalMessage);
            }
        }
    }

    private static void kickClient(String nameToKick) {
        Socket socketToKick = clientSockets.get(nameToKick);
        if (clientSockets.get(nameToKick) != null) {
            try {
                printWriteAndLogInfo(nameToKick + " has been kicked from the chat.");
                socketToKick.close();
                clientSockets.remove(nameToKick);
            } catch (IOException e) {
            }
        } else {
            printWriteAndLogInfo("No user with the name " + nameToKick + " is currently connected.");
        }
    }

    private static void privateMessage(String nickname, String message) {
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(clientSockets.get(nickname).getOutputStream()));

            String formattedMessage = "[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "] " + "Server PM to " + nickname + ": " + message;

            System.out.println(formattedMessage);

            writer.write("Server PM: " + message);
            writer.newLine();
            writer.flush();

            ServerLogger.logInfo("Server PM to " + nickname + ": " + message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void broadcastMessage(String message) {
        synchronized (clientSockets) {
            for (Map.Entry<String, Socket> entry : clientSockets.entrySet()) {
                try {
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(entry.getValue().getOutputStream()));
                    writer.write(message);
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

    private static void printWriteAndLogInfo(String message) {
        String formattedMessage = "[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "] " + message;
        System.out.println(formattedMessage);
        broadcastMessage(message);
        ServerLogger.logInfo(message);
    }
}
