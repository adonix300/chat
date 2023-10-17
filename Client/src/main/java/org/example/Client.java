package org.example;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class Client {
    private static int port;
    private static String ip;
    private static String nickName;

    public static void main(String[] args) {
        setting("Client\\config\\settings.txt");

        try (Socket socket = new Socket(ip, port);
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             Scanner scanner = new Scanner(System.in)) {

            System.out.println("Connected");

            nickName = enterNickname(scanner);
            sendMessage(writer, nickName);

            new Thread(() -> {
                String response;
                try {
                    while ((response = reader.readLine()) != null) {
                        if (response.startsWith("Server: ") && response.contains(nickName + " has been kicked from the chat.")) {
                            System.out.println("You have been kicked from the chat.");
                            socket.close();
                            break;
                        } else if (response.startsWith("Server: ") && response.contains("Server has been stopped.")) {
                            socket.close();
                            break;
                        } else {
                            String formattedMessage = formatString(response);
                            System.out.println(formattedMessage);
                        }
                    }
                } catch (SocketException e) {
                    System.out.println("Connection to the server was lost.");
                } catch (IOException e) {
                    System.out.println("An error occurred while reading from the server.");
                    e.printStackTrace();
                }
            }).start();

            while (true) {
                String request = scanner.nextLine();
                try {
                    sendMessage(writer, request);
                } catch (IOException e) {
                    System.out.println("An error occurred while sending a message to the server.");
                    e.printStackTrace();
                }

                if (request.equals("/exit")) {
                    socket.close();
                    break;
                }
            }

        } catch (IOException e) {
            System.out.println("An error occurred while connecting to the server.");
            e.printStackTrace();
        }
    }

    private static void sendMessage(BufferedWriter writer, String text) throws IOException {
        try {
            writer.write(text);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            throw new IOException("An error occurred while sending a message.", e);
        }
    }

    private static String enterNickname(Scanner scanner) {
        System.out.print("Put your nickname: ");
        return scanner.nextLine();
    }

    private static void setting(String filename) {
        try (BufferedReader fileReader = new BufferedReader(new FileReader(filename))) {
            while (fileReader.ready()) {
                String line = fileReader.readLine();
                String[] settings = line.split(":");
                if (settings[0].equals("ip")) {
                    ip = settings[1].trim();
                } else if (settings[0].equals("port")) {
                    port = Integer.parseInt(settings[1].trim());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("An error occurred while reading the settings file.", e);
        }
    }

    private static String formatString(String message) {
        return "[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "] " + message;
    }
}