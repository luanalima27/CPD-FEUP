package com.chat.server;

import com.chat.server.model.Room;
import com.chat.server.model.ChatRoom;
import com.chat.server.model.AIRoom;
import com.chat.server.model.Message;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Logger;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class RoomManager {
    
    private final Map<String, Room> rooms = new HashMap<>();
    private final ReentrantLock lock = new ReentrantLock();
    private static final Logger logger = Logger.getLogger(RoomManager.class.getName());
    
    
    public Room createRoom(String name, Boolean isAI) {
        lock.lock();

        try {
            

            Room room;
            if (name == null || name.isEmpty()) {
                return null; // Room name is null or empty
            }
            else if(rooms.containsKey(name)){
                logger.info("User logged out successfully");
                return null; // Room already exists
            }
            
        
            if(isAI ) { 
                room = new AIRoom(UUID.randomUUID().toString(), name);
            }
            else{
                room= new ChatRoom(UUID.randomUUID().toString(), name);
                if(room!= null) {
                    logger.info("Room created successfully: " + name);
                } else {
                    logger.warning("Failed to create room: " + name);
                    return null; // Room creation failed
                }
            }
            rooms.put(name, room);
            return room;
        } finally {
            lock.unlock();
        }
    }
    
    public Room getRoom(String name) {
        lock.lock();
        try{
            return rooms.get(name);
        } finally {
            lock.unlock();
        }
    }

    public Map<String, Room> getAvailableRooms() {
        lock.lock();
        try {
            return Map.copyOf(rooms);
        } finally {
            lock.unlock();
        }

    }


    public void shutdown() {
        lock.lock();
        try {
            for (Room room : rooms.values()) {
               removeRoom(room.getRoomId());         
            }
            rooms.clear();
        } finally {
            lock.unlock();
        }
    }

    public void removeRoom(String id) {
        lock.lock();
        try {
            rooms.remove(id);
        } finally {
            lock.unlock();
        }
    }
    
    
}