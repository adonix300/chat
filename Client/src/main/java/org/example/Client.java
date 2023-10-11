package org.example;

import java.io.*;
import java.net.Socket;
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
                        System.out.println(response);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }).start();

            while (true) {
                String request = scanner.nextLine();
                sendMessage(writer, request);

                if (request.equals("/exit")) {
                    break;
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void sendMessage(BufferedWriter writer, String text) throws IOException {
        writer.write(text);
        writer.newLine();
        writer.flush();
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
            throw new RuntimeException(e);
        }
    }
}