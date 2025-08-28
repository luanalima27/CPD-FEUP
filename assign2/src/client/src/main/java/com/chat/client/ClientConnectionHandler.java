package com.chat.client;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.FileWriter;



class ClientConnectionHandler {

    private final Socket socket;
    private final BufferedReader in;
    private final PrintWriter out;
    private final ChatClient chatClient;


    public ClientConnectionHandler(Socket socket, ChatClient chatClient) throws IOException {
        this.socket = socket;
        this.chatClient = chatClient;

        try {
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize streams", e);
        }

    }


    public void start() {
        // Start listening for server responses
        Thread.startVirtualThread(this::listenToServer);

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                String input = scanner.nextLine();
                
                out.println(input);
            }

        }
    }

    
    private void listenToServer() {
        try {
            String response;
            while ((response = in.readLine()) != null) {
                System.out.println(response);
                if (response.startsWith("LOGIN SUCCESS")) {
                    // Exemplo: LOGIN SUCCESS a1b2c3d4
                    String[] parts = response.split(" ");
                    if (parts.length == 3) {
                        String token = parts[2];
                        String username = chatClient.getUsername(); // Supondo que tens este método
                        saveTokenToFile(token, username);
                        chatClient.setAuthToken(token); // opcional
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Disconnected from server. Attempting reconnection...");
            attemptReconnection();
        }
    }

    private void attemptReconnection() {
        for (int i = 0; i < 5; i++) { // tenta 5 vezes
            try {
                Thread.sleep(3000);
                Socket newSocket = new Socket(chatClient.getHost(), chatClient.getPort());
                // reiniciar handler com novo socket
                ClientConnectionHandler newHandler = new ClientConnectionHandler(newSocket, chatClient);
                newHandler.start();
                return;
            } catch (Exception ex) {
                System.err.println("Reconnect attempt failed (" + (i+1) + ")");
            }
        }
        System.err.println("Failed to reconnect after multiple attempts.");
    }

    private void saveTokenToFile(String token, String username) {
        try (FileWriter writer = new FileWriter("token.txt")) {
            writer.write(username + ":" + token);
            System.out.println("Token saved to token.txt");
        } catch (IOException e) {
            System.err.println("Failed to save token: " + e.getMessage());
        }
    }
}