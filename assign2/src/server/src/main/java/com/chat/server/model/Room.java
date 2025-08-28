package com.chat.server.model;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Logger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.io.*;



//class que representa uma sala de chat
public abstract class Room {

    protected final String roomId; // ID da room
    protected final String roomName; // Nome da room
    protected final Lock messageLock= new ReentrantLock(); // Lock para mensagens
    protected final List<Message> messages; // Lista de mensagens
    private final Map<String, User> users = new HashMap<>(); // (username, User)
    protected final RType roomType; // Tipo da room
    protected final List<PrintWriter> subscribers = new CopyOnWriteArrayList<>(); // Lista de assinantes (PrintWriter)

    public enum RType {
        REGULAR,
        AI
    }

    protected Room(String roomId, String roomName, RType roomType) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.roomType = roomType;
        this.messages = new CopyOnWriteArrayList<>(); // Thread-safe for reads
    }

    public String getRoomId() {
        return roomId;
    }
    public String getRoomName() {
        return roomName;
    }
    public List<Message> getMessages() {
        return messages;
    }
    public List<User> getUsers() {
        List<User> usersList = new ArrayList<>();
        for (Map.Entry<String, User> entry : this.users.entrySet()) {
            usersList.add(entry.getValue());
        }
        return usersList;
    }
    public RType getRoomType() {
        return roomType;
    }

    //muda para cada tipo de room e por isso é abstract
    protected abstract String handleMessage(Message message);


    public void addUser(User user){
        messageLock.lock();
        try {
            if (users.containsKey(user.getUsername())) {
                addMessage(Message.createSystemMessage(
                "You are already in the room",
                roomName
                )); // Send message to user
                return; // User already exists
            }

            users.put(user.getUsername(), user);
            addMessage(Message.createSystemMessage(
               user.getUsername() + " joined the room", 
                this.roomName
            ));
        } finally {
            messageLock.unlock();
        }
    }


    public void removeUser(User user){
        messageLock.lock();
        try {
            users.remove(user.getUsername());
            addMessage(Message.createSystemMessage(
                user.getUsername() + " left the room", 
                roomId
            ));
        } finally {
            messageLock.unlock();
        }
    }

    public void addMessage(Message message) {
        messageLock.lock();
        try {
            messages.add(message);
            String formattedMessage=handleMessage(message); 
            // Chama o método handleMessage para processar a mensagem
            // de acordo com o tipo da room

            broadcast(formattedMessage); // Envia a mensagem para todos os assinantes

        } finally {
            messageLock.unlock();
        }
    }

    public void subscribe(PrintWriter out) {
        messageLock.lock();
        try {
            subscribers.add(out);
        } finally {
            messageLock.unlock();
        }
    }

    public void unsubscribe(PrintWriter out) {
        messageLock.lock();
        try {
            subscribers.remove(out);
        } finally {
            messageLock.unlock();
        }
    }

    private void broadcast(String messageText) {
        for (PrintWriter writer : subscribers) {
            writer.println(messageText);
        }
    }

}