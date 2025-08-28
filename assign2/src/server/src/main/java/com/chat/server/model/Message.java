package com.chat.server.model;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Logger;


/*
    * texto da mensagem
    * autor
    * data
    * hora
    * Room id
    * id da mensagem
    * SystemMessage
    * BotMessage
    
*/
public class Message {
    private final String text; // Texto da mensagem
    private final String user; // Autor da mensagem
    private final LocalDateTime timestamp; // Data e hora da mensagem
    private final String roomId; // ID da room
    private final String messageId; // ID da mensagem
    private final boolean systemMessage; // Se é uma mensagem do sistema
    private final boolean botMessage; // Se é uma mensagem do bot

    // Construtor privado
    private Message(String text, String user, LocalDateTime timestamp, String roomId, String messageId, boolean systemMessage, boolean botMessage) {
        this.text = text;
        this.user = user;
        this.timestamp = timestamp;
        this.roomId = roomId;
        this.messageId = messageId;
        this.systemMessage = systemMessage;
        this.botMessage = botMessage;
    }

    //[12:00] Maria: Hello world!
    // Imprime a mensagem no formato [hora] autor: texto
   String formattedMessage(Message message) {
        String formatted;
        if(message.isSystemMessage()) {
           formatted="[System] " + message.getText();
        } else if (botMessage) {
            formatted="[Bot] " + message.getText();
        } else {
             formatted= user + ": " + message.getText();
        }
        System.out.println(formatted);
        return formatted;
    }


    public String getText() {
        return text;
    }
    public String getUser() {
        return user;
    }
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    public String getRoomId() {
        return roomId;
    }
    public String getMessageId() {
        return messageId;
    }
    public boolean isSystemMessage() {
        return systemMessage;
    }
    public boolean isBotMessage() {
        return botMessage;
    }

    
    public static Message createMessage(String text, String user, String roomId) {
        String messageId = UUID.randomUUID().toString();
        LocalDateTime timestamp = LocalDateTime.now();
        return new Message(text, user, timestamp, roomId, messageId, false, false);
    }
    public static Message createSystemMessage(String text, String roomId) {
        String messageId = UUID.randomUUID().toString();
        LocalDateTime timestamp = LocalDateTime.now();
        return new Message(text, null, timestamp, roomId, messageId, true, false);
    }
    public static Message createBotMessage(String text, String roomId) {
        String messageId = UUID.randomUUID().toString();
        LocalDateTime timestamp = LocalDateTime.now();
        return new Message(text, null, timestamp, roomId, messageId, false, true);
    }
    

}