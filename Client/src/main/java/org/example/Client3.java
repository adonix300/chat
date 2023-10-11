package org.example;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client3 {
    private static int port;
    private static String ip;
    private static String nickName;


    public static void main(String[] args) {
        try (BufferedReader fileReader = new BufferedReader(new FileReader("Client\\config\\settings.txt"))) {
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

        try (Socket socket = new Socket(ip, port);
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             Scanner scanner = new Scanner(System.in);) {

            System.out.println("Connected");
            System.out.print("Put your nickname: ");
            nickName = scanner.nextLine();
            writer.write(nickName);
            writer.newLine();
            writer.flush();


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

            String request;
            while (!(request = scanner.nextLine()).equals("/exit")) {
                writer.write(request);
                writer.newLine();
                writer.flush();
            }


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}