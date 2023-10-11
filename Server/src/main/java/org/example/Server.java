package org.example;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Server {
    private static int port;
    private static List<Socket> clientSockets = new ArrayList<>();

    public static void main(String[] args) {
        setting("Server\\config\\settings.txt");

        try (ServerSocket serverSocket = new ServerSocket(port);
        Scanner scanner = new Scanner(System.in)) {
            System.out.println("Server started");

            new Thread(() -> {
                sender(scanner);
            });

            while (true) {
                Socket socket = serverSocket.accept();
                clientSockets.add(socket);
                new Thread(() -> {
                    handleClient(socket);
                }).start();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void sender(Scanner scanner) {
        while (true) {
            String serverCommand = scanner.nextLine();
            if (serverCommand.startsWith("/broadcast")) {
                String message = serverCommand.substring("/broadcast".length()).trim();
                broadcastMessage("Server: " + message);
            }
        }
    }

    private static void handleClient(Socket socket) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            String nickname = reader.readLine();
            System.out.println(nickname + " has joined the chat.");
            broadcastMessage(nickname + " has joined the chat.");

            while (true) {
                String response = reader.readLine();
                System.out.println(nickname + ": " + response);
                broadcastMessage(nickname + ": " + response);
                if (response.equals("/exit")) {
                    break;
                }
            }

            clientSockets.remove(socket);

            System.out.println(nickname + " has left the chat.");
            broadcastMessage(nickname + " has left the chat.");
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
        synchronized (clientSockets) {
            for (Socket socket : clientSockets) {
                try {
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
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
}
