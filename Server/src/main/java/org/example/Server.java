package org.example;

import org.example.Logger.ServerLogger;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Server {
    private static int port;
    private static Map<String, Socket> clientSockets = new HashMap<>();
    private static final String COMMAND_TEXT_BROADCAST = "/broadcast";
    private static final String COMMAND_TEXT_KICK = "/kick";
    private static final String COMMAND_TEXT_PM = "/pm";

    public static void main(String[] args) {
        setting("Server\\config\\settings.txt");

        try (ServerSocket serverSocket = new ServerSocket(port); Scanner scanner = new Scanner(System.in)) {

            System.out.println("Server started");

            new Thread(() -> {
                serverCommands(scanner, serverSocket);
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
            printWriteLogInfo(nickname + " has joined the chat.");

            while (true) {
                String response = reader.readLine();
                printWriteLogInfo(nickname + ": " + response);
                if (response.equals("/exit")) {
                    break;
                }
            }

            clientSockets.remove(nickname);
            printWriteLogInfo(nickname + " has left the chat.");
        } catch (SocketException e) {
            ServerLogger.logWarning("void handleClient - SocketException - socket closed");
            System.out.println("void handleClient - SocketException - socket closed");
        } catch (IOException e) {
            ServerLogger.logError("void handleClient - IOException");
            System.out.println("void handleClient - IOException");
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void serverCommands(Scanner scanner, ServerSocket serverSocket) {
        while (true) {
            String serverCommand = scanner.nextLine();
            if (serverCommand.startsWith(COMMAND_TEXT_BROADCAST)) {
                String message = serverCommand.substring(COMMAND_TEXT_BROADCAST.length()).trim();
                printWriteLogInfo("Server: " + message);
            } else if (serverCommand.startsWith(COMMAND_TEXT_KICK)) {
                String nameToKick = serverCommand.substring(COMMAND_TEXT_KICK.length()).trim();
                kickClient(nameToKick);
            } else if (serverCommand.startsWith(COMMAND_TEXT_PM)) {
                String message = serverCommand.substring(COMMAND_TEXT_PM.length()).trim();
                String[] nicknameFinder = message.split(" ");
                String recipient = nicknameFinder[0];
                String privateMessage = message.substring(recipient.length()).trim();
                privateMessage(recipient, privateMessage);
            } else if (serverCommand.startsWith("/stop")) {
                stopServer(serverSocket);
                break;
            }
        }
    }

    private static void kickClient(String nameToKick) {
        Socket socketToKick = clientSockets.get(nameToKick);
        if (clientSockets.get(nameToKick) != null) {
            try {
                printWriteLogInfo("Server: " + nameToKick + " has been kicked from the chat.");
                socketToKick.close();
                clientSockets.remove(nameToKick);
            } catch (IOException e) {
                ServerLogger.logError("void kickClient - IOException");
            }
        } else {
            printWriteLogInfo("No user with the name " + nameToKick + " is currently connected.");
            ServerLogger.logWarning("No user with the name " + nameToKick + " is currently connected.");
        }
    }

    private static void privateMessage(String nickname, String message) {
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(clientSockets.get(nickname).getOutputStream()));

            String formattedMessage = formatString("Server PM to " + nickname + ": " + message);
            System.out.println(formattedMessage);
            writeAndFlush(writer, "Server PM: " + message);
            ServerLogger.logInfo("Server PM to " + nickname + ": " + message);
        } catch (IOException e) {
            ServerLogger.logError("void privateMessage - IOException");
            throw new RuntimeException(e);
        }
    }

    private static void broadcastMessage(String message) {
        synchronized (clientSockets) {
            for (Map.Entry<String, Socket> entry : clientSockets.entrySet()) {
                try {
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(entry.getValue().getOutputStream()));
                    writeAndFlush(writer, message);
                } catch (IOException e) {
                    ServerLogger.logError("void broadcastMessage - IOException");
                    e.printStackTrace();
                }
            }
        }
    }

    private static void writeAndFlush(BufferedWriter writer, String message) throws IOException {
        writer.write(message);
        writer.newLine();
        writer.flush();
    }

    private static void printWriteLogInfo(String message) {
        String formattedMessage = formatString(message);
        System.out.println(formattedMessage);
        broadcastMessage(message);
        ServerLogger.logInfo(message);
    }

    private static void setting(String filename) {
        try (BufferedReader fileReader = new BufferedReader(new FileReader(filename))) {
            String line = fileReader.readLine();
            String[] settings = line.split(":");
            port = Integer.parseInt(settings[1].trim());
        } catch (IOException e) {
            ServerLogger.logError("settings.txt not founded");
            throw new RuntimeException(e);
        }
    }

    private static void stopServer(ServerSocket serverSocket) {
        try {
            printWriteLogInfo("Server has been stopped.");
            for (Socket socket : clientSockets.values()) {
                socket.close();
            }
            clientSockets.clear();
            serverSocket.close();
        } catch (IOException e) {
            System.out.println("Error occurred while stopping the server.");
            ServerLogger.logError("Error occurred while stopping the server.");
            e.printStackTrace();
        }
    }

    private static String formatString(String message) {
        return "[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "] " + message;
    }
}
