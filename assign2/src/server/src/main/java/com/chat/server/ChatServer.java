package com.chat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.chat.server.*;



public class ChatServer {
    /*
    Logger
        Part of Java's built-in logging API (java.util.logging)
        Used to log messages (errors, warnings, info, etc.)
        Replaces System.out
    */
    private static final Logger logger = Logger.getLogger(ChatServer.class.getName());

    private final int port;
    private final RoomManager roomManager;
    private final UserManager userManager;
    private ServerSocket serverSocket;
    private ExecutorService executorService;//Thread pool executor para gerir os clientes
    private final ReentrantLock lock = new ReentrantLock();// para garantir a exclusividade de acesso ao servidor
    private volatile boolean running = false;


    public ChatServer(int port, RoomManager roomManager, UserManager userManager) {
        this.port = port;
        this.roomManager = roomManager;
        this.userManager = userManager;

    }
    
    public void start() {
        lock.lock();
        try{
            if (running) {
                logger.warning("Server is already running.");
                return; 
            }

            //criar o socket do servidor
            serverSocket = new ServerSocket(port);
            executorService = Executors.newVirtualThreadPerTaskExecutor();
            running = true;

            logger.info("Server started on port " + port);
            //talvez colocar aqui o username do user que criou este server

                while (running) {
                    try {
                        Socket cltSocket = serverSocket.accept();
                        logger.info("Client connected: " + cltSocket.getInetAddress().getHostAddress());
                        executorService.execute(()-> {
                            try{
                                new ClientHandler(cltSocket, roomManager, userManager).handleClient();
                            }
                            catch (IOException e){
                                logger.severe("Error handling client");
                            }
                        });
                        } catch (IOException e) {
                            if (running) {
                                logger.severe( "Error accepting client connection"+ e);
                            }
                        }
                }
            }
        
        catch (Exception e) {
            logger.log(Level.SEVERE, "Error while starting the server", e);
        } finally {
            lock.unlock();
        }
    }
    
    public void stop() {
        lock.lock();
            try{
                if (!running) {
                    logger.warning("Server is already shutdown.");
                    return; 
                }
                running = false;
                logger.info("Server is shutting down...");

                //matar a socket do servidor
                if (serverSocket != null && !serverSocket.isClosed()) {
                
                    try{
                        serverSocket.close();
                        logger.info("Server socket closed.");
                    }
                    catch (IOException e){
                        logger.severe("Error closing server socket "+ e);
                    }
                    //matar a thread pool executor
                    try{
                        executorService.shutdown();
                        logger.info("Executor service shutdown.");
                    }
                    catch (Exception e){
                        logger.severe("Error shutting down executor service "+ e);
                    }
                
                    roomManager.shutdown();
                    userManager.clean();

                    logger.info("Server shutdown complete!");
                }

            }
            catch (Exception e) {
                logger.log(Level.SEVERE, "Error while stopping the server", e);
            } finally {
                lock.unlock();
            }
    }
    

    public boolean isRunning() {
        return running;
    }
}