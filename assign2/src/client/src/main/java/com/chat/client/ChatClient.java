package com.chat.client;

import java.io.IOException;
import java.net.Socket;

public class ChatClient {
    private final String host;
    private final int port;
    private String authToken;
    private String username;

    
    public ChatClient(String host, int port) {
        this.host = host;
        this.port = port;
    }
    
    public void start() {
        try {
            Socket socket = new Socket(host, port);
            System.out.println("Connected to chat server at " + host + ":" + port);

            ClientConnectionHandler handler = new ClientConnectionHandler(socket,this );
            
            handler.start();
            
        } catch (IOException e) {
            System.err.println("Client error: " + e.getMessage());
        }
    }
    
    public void setAuthToken(String token) {
        this.authToken = token;
    }
    
    public String getAuthToken() {
        return authToken;
    }

    public void setUsername(String username) { 
        this.username = username; 
    }
    
    public String getUsername() { 
        return username;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }
}