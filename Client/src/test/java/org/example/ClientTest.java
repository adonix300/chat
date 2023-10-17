package org.example;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class ClientTest {
    @Test
    public void testEnterNickname() {
        Scanner scanner = new Scanner("testNickname");
        assertEquals("testNickname", Client.enterNickname(scanner));
    }

    @Test
    public void testSendMessage() throws IOException {
        BufferedWriter writer = mock(BufferedWriter.class);
        Client.sendMessage(writer, "testMessage");
        verify(writer, times(1)).write("testMessage");
        verify(writer, times(1)).newLine();
        verify(writer, times(1)).flush();
    }

    @Test
    public void testMain() throws IOException {
        Socket socket = mock(Socket.class);
        BufferedWriter writer = mock(BufferedWriter.class);
        BufferedReader reader = mock(BufferedReader.class);
        Scanner scanner = new Scanner("testNickname\n/exit");

        when(socket.getOutputStream()).thenReturn(mock(OutputStream.class));
        when(socket.getInputStream()).thenReturn(mock(InputStream.class));
        whenNew(Socket.class).withArguments(anyString(), anyInt()).thenReturn(socket);
        whenNew(BufferedWriter.class).withArguments(any(OutputStreamWriter.class)).thenReturn(writer);
        whenNew(BufferedReader.class).withArguments(any(InputStreamReader.class)).thenReturn(reader);

        Client.main(new String[]{});

        verify(writer, times(2)).write(anyString());
    }
}