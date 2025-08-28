package com.chat.server.model;

import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReadWriteLock;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URI;
import com.chat.server.model.Message;
import com.chat.server.model.Room;

public class AIRoom extends Room {
    //private final ReadWriteLock usersLock = new ReentrantReadWriteLock(); 
    //creates a thread-safe lock specifically designed for scenarios with frequent reads and rare writes.


    public AIRoom(String roomId, String name) {
        super(roomId, name, RType.AI);
    }

    @Override
    protected String handleMessage(Message message) {
        if(!message.isSystemMessage()&& !message.isBotMessage()){

            String formattedMessage= message.formattedMessage(message);

            StringBuilder context = new StringBuilder("Conversation:\n");
            

            for (Message msg : getMessages()) {
                if (!msg.isBotMessage() && !msg.isSystemMessage()) {
                    context.append(msg.getUser()).append(": ").append(msg.getText()).append("\n");
                }
            }
            try {
                String aiReply = OllamaClient.generateResponse(context.toString());
                Message aiMessage = Message.createBotMessage(aiReply, message.getRoomId());
                addMessage(aiMessage);

                return formattedMessage;
            }
            catch (IOException e) {

                return "Error generating AI response: " + e.getMessage();
            }
        } else if (message.isSystemMessage() || message.isBotMessage()) {
            return message.formattedMessage(message);
        }
        return "Invalid message type for AI room.";
    }

    

    public static class OllamaClient {

        public static String generateResponse(String prompt) throws IOException {
            
            URL url = URI.create("http://localhost:11434/api/generate").toURL();

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");

            String jsonBody = String.format(
                "{ \"model\": \"llama3\", \"prompt\": \"%s\", \"stream\": false }",
                escape(prompt));

            try (OutputStream os = connection.getOutputStream()) {
                os.write(jsonBody.getBytes());
            }

            StringBuilder result = new StringBuilder();
            try (BufferedReader bufferReader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String line;
                while ((line = bufferReader.readLine()) != null) {
                    result.append(line);
                }
            }

            String json = result.toString();
            int start = json.indexOf("\"response\":\"") + 12;
            int end = json.indexOf("\",", start);
            if (start >= 12 && end > start) {
                return json.substring(start, end).replace("\\n", "\n").replace("\\\"", "\"");
            } else {
                return "[Error parsing AI response]";
            }
        }

        private static String escape(String input) {
            return input
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "");
        }
    }


}