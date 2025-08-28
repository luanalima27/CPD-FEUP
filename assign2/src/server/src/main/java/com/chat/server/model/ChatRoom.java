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

public class ChatRoom extends Room {
    //private final ReadWriteLock usersLock = new ReentrantReadWriteLock(); 
    //creates a thread-safe lock specifically designed for scenarios with frequent reads and rare writes.
    private final ReadWriteLock usersLock = new ReentrantReadWriteLock();


    public ChatRoom(String roomId, String name) {
        super(roomId, name, RType.REGULAR);
    }

    @Override
    protected String handleMessage(Message message) {
        return message.formattedMessage(message);
    }

}