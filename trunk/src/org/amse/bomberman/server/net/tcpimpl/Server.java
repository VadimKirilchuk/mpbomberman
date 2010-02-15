/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amse.bomberman.server.net.tcpimpl;

import org.amse.bomberman.server.net.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.channels.IllegalBlockingModeException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.amse.bomberman.server.gameinit.Game;
import org.amse.bomberman.util.Constants;
import org.amse.bomberman.util.ILog;
import org.amse.bomberman.util.impl.ConsoleLog;

/**
 *
 * @author Kirilchuk V.E.
 */
public class Server implements IServer {

    private ILog log; // could be never initialized. Use writeToLog(...) instead of log.println(...)
    private int port;
    private List<Game> games;
    private ServerSocket serverSocket;
    private boolean shutdowned = true; //true until we start accepting clients.
    private Thread listeningThread;
    private int sessionCounter = 0; //need to generate name of log files.   

    /**
     * Constructor with default port.
     */
    public Server() {
        this(Constants.DEFAULT_PORT);
    }

    /**
     * Main constructor that creating Server object with port param.
     * @param port Free port number. Port must be between 0 and 65535, inclusive.
     * '0' for random free port. 0 is not reccomended cause clients must know
     * actual port number to connect.
     */
    public Server(int port) {
        this.port = port;
        this.games = Collections.synchronizedList(new LinkedList<Game>());
        this.log = new ConsoleLog();
    }

    /**
     * Starts listening thread where ServerSocket.accept() method is using.
     * 
     */
    public synchronized void start() throws IOException, IllegalStateException {
        try {

            if (shutdowned) {
                this.sessionCounter = 0;
                this.serverSocket = new ServerSocket(port, 0); // throws IOExeption,SecurityException
                this.listeningThread = new Thread(new SocketListen(this));
                this.listeningThread.start();
                this.games = Collections.synchronizedList(new LinkedList<Game>());
                this.shutdowned = false;
            } else {
                throw new IllegalStateException("Already accepting. Can`t raise server.");
            }

        } catch (IOException ex) {
            writeToLog("Raise server error. " + ex.getMessage());
//            this.shutdown();
            throw ex;
        } catch (SecurityException ex) {
            writeToLog("Raise server error. " + ex.getMessage());
//            this.shutdown();
            throw ex;
        }

        writeToLog("Server: started.");
    }

    /**
     * Closing server socket and ending listening thread and clients threads
     * by calling interrupt()
     * @throws java.io.IOException if an error occurs when closing socket.
     * @throws IllegalStateException if we are trying to shutdown not raised server.
     */
    public synchronized void shutdown() throws IOException, IllegalStateException {
        try {

            if (!this.shutdowned) {            
                if (this.listeningThread != null) {
                    this.listeningThread.interrupt(); //throws SecurityException
                    this.listeningThread = null;
                }
                if (this.serverSocket != null) {
                    this.serverSocket.close();
                    this.serverSocket = null;
                }
                this.games = null;
                this.shutdowned = true;
                if (this.log != null) {
                    try{
                        this.log.close();
                    } catch (IOException ex){
                        System.out.println("Shutdown server warning. Can`t close log." +
                                ex.getMessage());
                    }
                }
            } else {
                throw new IllegalStateException("Server is not raised. Can`t shutdown it.");
            }

        } catch (IOException ex) {
            writeToLog("Shutdown server error. " + ex.getMessage());
            throw ex;
        } catch (SecurityException ex) {
            writeToLog("Shutdown server error.  " +
                    ex.getMessage());
            throw ex;
        }

        writeToLog("Server: shutdowned.");
    }

    public void addGame(Game game) {
        if (!this.shutdowned) { // is it redundant?
            this.games.add(game);
            writeToLog("Game added.");
        } else {
            writeToLog("Tryed to add game to shutdowned server.");            
        }
    }

    public void removeGame(Game gameToRemove) {
        if (!this.shutdowned) { // is it redundant?
            if(this.games.remove(gameToRemove)){
                writeToLog("Game removed");
            }else{
                writeToLog("Server removeGame warning. No specified game found.");
            }
        } else {
            writeToLog("Tryed to remove game from shutdowned server.");            
        }
    }

    /**
     * Return game from list at the specified index
     * @param n
     * @return
     */
    public Game getGame(int n) {
        Game game = null;
        if (!this.shutdowned) { // is it redundant?
            try {
                game = this.games.get(n);
            } catch (IndexOutOfBoundsException ex) {
                writeToLog("Server getGame warning. Tryed to get game with illegal ID. Canceled.");
            }
        } else {
            writeToLog("Tryed to get game from shutdowned server.");
        }
        return game;
    }

    public List<Game> getGamesList() {
        if (!this.shutdowned) { // is it redundant?
            return this.games;
        } else { //if server was stopped.
            writeToLog("Server getGamesList warning. Tryed to get games list from shutdowned server.");
            return null;
        }
    }

    public synchronized boolean isShutdowned() {
        return this.shutdowned;
    }

    public synchronized int getPort() {
        return this.port;
    }

    private class SocketListen implements Runnable {

        private Server net;
        private List<ISession> sessions;

        public SocketListen(Server net) {
            this.net = net;
            this.sessions = new ArrayList<ISession>();
        }

        public void run() {
            
            writeToLog("Server: Waiting for a new client...");

            try {

                while (!Thread.interrupted()) {
                    //throws IO, Security, SocketTimeout, IllegalBlockingMode
                    //exceptions
                    Socket clientSocket = serverSocket.accept();
                    writeToLog("Server: Client connected. Starting new session thread...");
                    sessionCounter++;
                    //CHECK V THIS// Is it throwing any exceptions?
                    ISession newSession = new Session(this.net, clientSocket,
                            sessionCounter, this.net.log);
                    this.sessions.add(newSession);
                    newSession.start();
                }

            } catch (SocketTimeoutException ex) { //never happen in current realization
                writeToLog("Server error." + ex.getMessage());
            } catch (IOException ex) { //if an I/O error occurs when waiting for a connection.
                writeToLog("Server error." + ex.getMessage()); //or socket closed
            } catch (SecurityException ex) { //accept wasn`t allowed
                writeToLog("Server error." + ex.getMessage());
            } catch (IllegalBlockingModeException ex) { //CHECK < THIS// what comments should i write?
                writeToLog("Server error." + ex.getMessage());
            }

            /*must free resources and stop our thread.*/
            for (ISession session : sessions) {
                session.interrupt();
            }

            writeToLog("Server: listening thread come to end.");
        }
    }

    public void writeToLog(String message) {
        if (log == null) {
            System.out.println(message);
        } else {
            log.println(message);
        }
    }
}