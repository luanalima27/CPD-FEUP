package com.chat.server;

import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Logger;
import com.chat.server.model.User;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/*
Track connected users and their tokens

Handle user sessions and reconnection logic

Manage user state (current room, etc.)

*/

public class UserManager {

    private static final Logger logger = Logger.getLogger(UserManager.class.getName());
    
    // User credentials storage (username -> passwordHash)
    private final Map<String,User> usersStorage = new HashMap<>(); // (username, User)

    private final Map<String, TokenINFO> sessionTokens = new HashMap<>(); // (token, username)

    private final ReentrantLock lock = new ReentrantLock();


    //o user pode ter mais de um token
    //Tempo de duração dos tokens pe 30 minutos
    private static final Duration TOKEN_DURATION = Duration.ofMinutes(30);

    private static class TokenINFO {

        private final String username;
        private final LocalDateTime expiryTime;
        private String lastRoom; 

        public TokenINFO(String username, LocalDateTime expiryTime, String lastRoom) {
            this.username = username;
            this.expiryTime = expiryTime;
            this.lastRoom = lastRoom;

        }

        public String getUsername() {
            return username;
        }

        public LocalDateTime getExpiryTime() {
            return expiryTime;
        }
        
        public String getLastRoom() {
            return lastRoom;
        }
    }

    public synchronized boolean registerUser(String username, String password) {
        lock.lock();
        try{
            if(username==null ||username.isEmpty()||password==null || password.isEmpty()){
                logger.warning("Username is empty");
                return false; // Username is null or empty
            }
            
            if (usersStorage.containsKey(username)) {
                logger.warning("User already exists");
                return false; // User already exists
            }
            else{
                User user = new User(username,hashPassword(password) );
                usersStorage.put(username, user);
                logger.info("User registered successfully: "+ username);
                return true; // User registered successfully;
            }
        } finally {
            lock.unlock();
        }
    }
    

    public boolean validateToken(String token, String username) {
        lock.lock();
        try{
            TokenINFO tokenInfo = sessionTokens.get(token);

            if (tokenInfo == null) {
                logger.warning("Token not found");
                return false;
            }
            LocalDateTime expiryTime =tokenInfo.getExpiryTime();

            if (expiryTime.isBefore(LocalDateTime.now())) {
                sessionTokens.remove(token); // Remove expired token
                System.out.println("Token expired");
                return false; // Token expired
            }

            System.out.println("Token is valid");
            return true; // Token is valid

        } finally {
            lock.unlock();
        }
        
    }

    public String login(String username, String password) {
        lock.lock();
        try{
            User user = usersStorage.get(username);

            if (!usersStorage.containsKey(username)) {
                logger.warning("User not found");
                return null; // User not found
            }
            if (!user.validatePassword(password)) {
                logger.warning("Invalid password");
                return null; // Invalid password
            }

            String token = UUID.randomUUID().toString();
            sessionTokens.put(token, new TokenINFO(username, LocalDateTime.now().plus(TOKEN_DURATION),null));
            logger.info("User logged in successfully: "+username);

            return token; // Login successful, return token
        }
        finally {
            lock.unlock();
        }
    }

    public void updateUserRoom(String token, String roomName) {
        lock.lock();
        try{
            TokenINFO old = sessionTokens.get(token);
            if (old != null) {
                sessionTokens.put(token, new TokenINFO(old.getUsername(), old.getExpiryTime(), roomName));
            }
        } finally {
            lock.unlock();
        }
    }
    
    public void logout(String token) {
        lock.lock();
        try{
            if(token==null){
                logger.warning("Token is null");
                return; // Token is null
            }
            if (!sessionTokens.containsKey(token)) {
                logger.warning("Token not found");
                return; // Token not found
            }
            sessionTokens.remove(token); // Remove token from storage
            logger.info("User logged out successfully");
        } finally {
            lock.unlock();
        }
    }
    
    public User getUserByToken(String token) {
        lock.lock();
        try{
            TokenINFO tokenInfo = sessionTokens.get(token);

            if (tokenInfo == null) return null;

            String username = tokenInfo.getUsername();
            User user = usersStorage.get(username);
            return user;
        } finally {
            lock.unlock();
        }
    }

    private String hashPassword(String password) {
        return Integer.toString(password.hashCode()); // simple (not secure) hash
    }

    public void clean() {
        lock.lock();
        try{
            usersStorage.clear();
            sessionTokens.clear();
        }
        finally {
            lock.unlock();
        }
    }
    
    public String getRoomByToken(String token) {
        lock.lock();
        try{
            TokenINFO tokenInfo = sessionTokens.get(token);
            if (tokenInfo == null) return null;
            return tokenInfo.lastRoom; // Return the last room associated with the token
        } finally {
            lock.unlock();
        }
    }
    
    
}