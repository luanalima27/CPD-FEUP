package com.chat.server;

import com.chat.server.model.Message;
import com.chat.server.model.Room;

import java.io.*;
import java.net.Socket;
import java.util.logging.Logger;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.ConcurrentMap;
import java.util.Map;
import java.util.HashMap;
import com.chat.server.model.Room; 
import com.chat.server.model.User;


public class ClientHandler {

    private final Socket socket;
    private final RoomManager roomManager;
    private final UserManager userManager;

    private BufferedReader in;
    private PrintWriter out;

    private String username;
    private Room currentRoom;
    private String userToken;

    public ClientHandler(Socket socket, RoomManager roomManager, UserManager userManager) {
        this.socket = socket;
        this.roomManager = roomManager;
        this.userManager = userManager;
    }

    public void handleClient() throws IOException {
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        out.println("Welcome to the Chat Server!");
        out.println("""
                    Please:
                        LOGIN <username> <password> 
                        REGISTER <username> <password> 
                        RECONNECT <token> <username>
                    """);

        String line;
        while ((line = in.readLine()) != null) {
            String[] tokens = line.trim().split(" ");
            if (tokens.length == 0) continue;

            String cmd = tokens[0].toUpperCase();

            switch (cmd) {
                case "REGISTER" -> {
                    if (tokens.length != 3) {
                        out.println("ERROR Usage: REGISTER <username> <password>");
                        continue;
                    }
                    if (userManager.registerUser(tokens[1], tokens[2])) {
                        out.println("REGISTERED");

                    } else {
                        out.println("ERROR Registration failed.");
                    }
                }
                case "LOGIN" -> {
                    if (tokens.length != 3) {
                        out.println("ERROR Usage: LOGIN <username> <password>");
                        continue;
                    }
                    String token = userManager.login(tokens[1], tokens[2]);
                    if (token != null) {
                        username = tokens[1];
                        userToken = token;
                        out.println("LOGIN SUCCESS " + token);
                        User user = userManager.getUserByToken(token);
                        mainLoop(user);
                        return;
                    } else {
                        out.println("ERROR Login failed.");
                    }
                }
                case "RECONNECT" -> {
                    if (tokens.length != 3) {
                        out.println("ERROR Usage: RECONNECT <token> <username>");
                        continue;
                    }
                    if (userManager.validateToken(tokens[1], tokens[2])) {
                        userToken = tokens[1];
                        username = tokens[2];
                        out.println("RECONNECTED");
                        User user = userManager.getUserByToken(userToken);
                        String lastRoom = userManager.getRoomByToken(userToken);

                        if (lastRoom != null) {
                            Room room = roomManager.getRoom(lastRoom);
                            if (room != null) {
                                currentRoom = room;
                                currentRoom.addUser(user);
                                currentRoom.subscribe(out);
                                out.println("You have been reconnected to room: " + lastRoom);
                            }
                        }
                        mainLoop(user); // resume session
                        return;
                    } else {
                        out.println("ERROR Reconnect failed.");
                    }
                }
                default -> out.println("ERROR Unknown command.");
            }
        }
    }

    private void mainLoop(User user) throws IOException {
        out.println("You are logged in as: " + username);
        out.println("""
                Available commands:
                    LIST                  - List all rooms and choose one
                    CREATE_Chat <name>    - Create a new chat room
                    CREATE_AI_Chat <name> - Create a new AI chat room
                    QUIT                  - Disconnect from the server
                """);


        String line;
        while ((line = in.readLine()) != null) {
            String[] tokens = line.trim().split(" ", 2);
            String cmd = tokens[0].toUpperCase();

            switch (cmd) {
                case "LIST" -> {
                    if(roomManager.getAvailableRooms().isEmpty()) {
                        out.println("No rooms available.");
                        mainLoop(user);
                        continue;
                    }
                    else {
                        out.println("Available rooms:");
                   
                        roomManager.getAvailableRooms().forEach((name, room) ->
                                out.println("- " + name + " (" + room.getRoomType() + ")"));
                        
                        out.println("Type JOIN <room> to enter a room:");
                    
                        chooseRoom(user);
                    }
                    continue;
                }
                case "CREATE_CHAT" -> {
                    if (tokens.length < 2) {
                        out.println("ERROR Usage: CREATE <room>");
                        continue;
                    }

                    Room room = roomManager.createRoom(tokens[1], false);

                    if (room != null) {
                        out.println("Chat room created: " + tokens[1]);
                    } else {
                        out.println("ERROR Room already exists.");
                    }

                    continue;
                }
                case "CREATE_AI_CHAT" -> {

                    String[] args = tokens[1].split(" ", 2);

                    if (args.length < 1) {
                        out.println("ERROR Usage: CREATE_AI_Chat <room>");
                        continue;
                    }


                    Room room = roomManager.createRoom(tokens[1], true);

                    if (room != null) {
                        out.println("AI Room created: " + args[0]);
                    } else {
                        out.println("ERROR Room already exists.");
                    }
                }
                case "QUIT" -> {

                    out.println("Bye!");

                    if (currentRoom != null){ 
                        currentRoom.unsubscribe(out);
                        currentRoom.removeUser(user);
                    }

                    socket.close();
                    return;
                }
                default -> out.println("ERROR Unknown command.");
            }
        }
    }

    private void chooseRoom(User user) throws IOException {
        out.println("You are logged in as: " + username);

        out.println("""
                Available commands:
                    JOIN <room>        - Join a room
                    LEAVE <room>       - Leave the current room
                    BACK               - Go back to the main menu
                    QUIT               - Disconnect from the server
                """);

        String line;
        while ((line = in.readLine()) != null) {
            String[] tokens = line.trim().split(" ", 2);
            String cmd = tokens[0].toUpperCase();

            switch (cmd) {
                case "JOIN" -> {

                    if (tokens.length < 2) {
                        out.println("ERROR Usage: JOIN <room>");
                        continue;
                    }
                    Room room = roomManager.getRoom(tokens[1]);

                    if (room == null) {
                        out.println("ERROR Room not found.");
                        continue;
                    }

                    currentRoom = room;
                    currentRoom.addUser(user);
                    userManager.updateUserRoom(userToken, room.getRoomName());
                    currentRoom.subscribe(out);
                    out.println("You have joined the "+ room.getRoomName() + " room."+"\n");
                    displayRoom(room, user);

                }
                case "LEAVE" -> {
                    if (currentRoom != null) {
                        currentRoom.unsubscribe(out);
                        currentRoom.removeUser(user);
                        userManager.updateUserRoom(userToken, null); // limpar sala
                        out.println("You left the room.");
                        currentRoom = null;

                    } else {
                        out.println("You're not in a room.");
                        mainLoop(user);
                        
                    }
                }
                case "QUIT" -> {
                    out.println("Bye!");
                    if (currentRoom != null){
                        currentRoom.unsubscribe(out);
                        currentRoom.removeUser(user);
                        userManager.updateUserRoom(userToken, null); // limpar sala ao sair
                    } 

                    socket.close();
                    return;
                }
                case "BACK" -> {
                    mainLoop(user);
                }
            }
        }
    }

    private void displayRoom(Room room,User user) throws IOException {

        out.println("You are logged in as: " + username);
        out.println("""
                Available commands:
                    MESSAGES          - See the messages in the room
                    SEND <message>    - Send a message to the room
                    MEMBERS           - See the members of the room
                    BACK              - Go back to the main menu
                    QUIT              - Disconnect from the server
                """);

        String line;
        while ((line = in.readLine()) != null) {
            String[] tokens = line.trim().split(" ", 2);
            String cmd = tokens[0].toUpperCase();

            switch (cmd) {
                case "MESSAGES" -> {
                    out.println("Messages in the room:");
                    for (Message message : room.getMessages()) {
                        out.println(message.getText());
                    }
                    continue;
                }
                case "SEND" -> {
                    if (tokens.length < 2) {
                        out.println("ERROR Usage: SEND <message>");
                        continue;
                    }

                    Message message1 = Message.createMessage(tokens[1], username, room.getRoomId());
                    room.addMessage(message1);
                    continue;
                }
                case "QUIT" -> {
                    out.println("Bye!");

                    if (currentRoom != null) {
                        currentRoom.unsubscribe(out);
                        currentRoom.removeUser(user);
                    }
                    socket.close();
                    return;
                }
                case "MEMBERS" -> {
                    out.println("Members in the room:");
                    for (User member : room.getUsers()) {
                        out.println(member.getUsername());
                    }
                    out.println("Type BACK to go back to the main menu.");
                    out.println("Type QUIT to disconnect from the server.");
            
                }
                case "BACK" -> {
                    if (currentRoom != null) {
                        currentRoom.unsubscribe(out);
                        currentRoom.removeUser(user);
                        userManager.updateUserRoom(userToken, null); // limpar sala
                        out.println("You left the room.");
                        currentRoom = null;
                    }
                    chooseRoom(user);
                    return;
                }
                default -> out.println("ERROR Unknown command.");

            }
        }
    }

        
    public void close() throws IOException {
        socket.close();
    }
}