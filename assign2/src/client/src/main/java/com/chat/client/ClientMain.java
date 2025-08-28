
package com.chat.client;

public class ClientMain {
    public static void main(String[] args) {

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Session preserved for auto-reconnection.");
        }));

        String host = "10.227.145.154";
        int port = 8080;
        
        if (args.length > 1) {
            host = args[0];
            port = Integer.parseInt(args[1]);
        }
        
        ChatClient client = new ChatClient(host, port);
        client.start();
    }
}