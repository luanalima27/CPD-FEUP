package com.chat.server;

import java.util.logging.*;


public class ServerMain {
    public static void main(String[] args) {

        Logger rootLogger = Logger.getLogger("");
        rootLogger.setLevel(Level.INFO);

        int port = 8080; // Default port
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }
        else {
            System.out.println("No port specified, using default port: " + port);
        }
        RoomManager roomManager = new RoomManager();
        UserManager userManager = new UserManager();
        ChatServer server = new ChatServer(port, roomManager, userManager);
        
        server.start();
    }
}