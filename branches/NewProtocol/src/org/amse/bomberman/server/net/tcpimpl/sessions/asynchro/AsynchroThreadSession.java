
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amse.bomberman.server.net.tcpimpl.sessions.asynchro; 

//~--- non-JDK imports --------------------------------------------------------
import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import org.amse.bomberman.protocol.RequestCommand;
import org.amse.bomberman.protocol.RequestExecutor;

import org.amse.bomberman.server.gameservice.GameStorage;
import org.amse.bomberman.server.net.SessionEndListener;

//~--- JDK imports ------------------------------------------------------------

import java.net.Socket;
import java.util.ArrayList;

import java.util.List;
import org.amse.bomberman.protocol.InvalidDataException;
import org.amse.bomberman.protocol.ProtocolConstants;
import org.amse.bomberman.protocol.ProtocolMessage;
import org.amse.bomberman.protocol.ResponseCreator;
import org.amse.bomberman.server.net.tcpimpl.sessions.AbstractSession;
import org.amse.bomberman.server.net.tcpimpl.sessions.asynchro.controllers.Controller;
import org.amse.bomberman.util.Constants;

/**
 * This class represents asynchronous session between client and server
 * which process client requests.
 * 
 * @author Kirilchuk V.E.
 */
public class AsynchroThreadSession extends AbstractSession {

    private final ResponseCreator protocol = new ResponseCreator();
    private final Controller controller;
    /** GameStorage for this session. */
    private final GameStorage gameStorage;
    private final SessionEndListener endListener;
    //
    private Thread sessionThread;
    private AsynchroSender sender;
    //
    private DataInputStream in = null;

    /**
     * Constructs asynchro session. This session can send messages to client
     * asynchronously and even send some messages without any requests
     * from client side. However, client must support asynchronous
     * receivers to work with this session.
     *
     * @param endListener listener of session end.
     * @param clientSocket socket of client of this session.
     * @param gameStorage game storage for this session.
     * @param sessionID unique id of this session.
     */
    public AsynchroThreadSession(SessionEndListener endListener,
                                 Socket clientSocket,
                                 GameStorage gameStorage, long sessionID) {
        super(clientSocket, sessionID);

        this.endListener = endListener; //TODO move listeners support to AbstractSession
        this.gameStorage = gameStorage;
        this.controller = new Controller(this);

    }

    public void start() throws IOException {
        this.sender = new AsynchroSender(this, clientSocket);
        this.sessionThread = new SessionThread();
        this.sessionThread.setDaemon(true);
        in = initReader();
        this.sessionThread.start();
    }

    private DataInputStream initReader() throws IOException {
        InputStream is = clientSocket.getInputStream();
        return new DataInputStream(new BufferedInputStream(is));
    }

    /**
     * @return object that will execute protocol commands.
     */
    private RequestExecutor getRequestExecutor() {
        return controller;
    }

    public void send(ProtocolMessage<Integer, String> message) {
        this.sender.addToQueue(message);
    }

    /**
     * Method that returns current session game storage.
     *
     * @see GameStorage
     * @return game storage of this session.
     */
    public GameStorage getGameStorage() {
        return this.gameStorage;
    }

    /**
     * Freeing additional resources of this
     * session: if client was in game, then leave from game plus notifying
     * endListener about end.
     *
     * @see SessionEndListener
     */
    private void freeResources() {
        if(this.controller != null) { //Do all these checks realy need?
            //if(this.controller.getMyGame() != null) {    // without this check controller will print error.
                this.controller.tryLeave();
            //}
        }

        if(this.endListener != null) {
            this.endListener.sessionTerminated(this);
        }
    }

    /**
     * Processing query. Maybe sending response - <b>it depends</b> on realization
     * of RequestExecutor and actual realization of <i>sendAnswer(...)</i> method.
     *
     * @see RequestExecutor
     * @param query request to process.
     */   
    protected void process(ProtocolMessage<Integer, String> message) {
        System.out.println("Session: message received.");

        RequestCommand cmd = null;
        try {
            int commandId = message.getMessageId();                                                                       
            cmd = RequestCommand.valueOf(commandId); // throws IllegalArgumentException
            cmd.execute(this.getRequestExecutor(), message.getData()); 
        } catch (IllegalArgumentException ex) {
            send(protocol.notOk(ProtocolConstants.INVALID_REQUEST_MESSAGE_ID,
                    "Not supported command."));
            System.out.println("Session: answerOnCommand error. "
                    + "Non supported command int from client. "
                    + ex.getMessage());
        } catch (InvalidDataException ex) {
            send(protocol.notOk(ProtocolConstants.INVALID_REQUEST_MESSAGE_ID,
                    ex.getMessage()));
        }
    }

    private class SessionThread extends Thread {

        /**
         * Method in which session gets requests from client and process them.
         */
        @Override
        public void run() {
            setClientSocketTimeout(Constants.DEFAULT_CLIENT_TIMEOUT);
            try {
                sender.start();
                
                System.out.println("Session: waiting queries from client...");
                cyclicReadAndProcess(in);
            } catch (SocketTimeoutException ex) {
                System.out.println("Session: terminated by socket timeout. "
                        + ex.getMessage());
            } catch (IOException ex) {
                System.err.println("Session: run error. " + ex.getMessage());
            } finally {
                this.closeConnection(in);
            }
        }

        private void closeConnection(Closeable in) {
            System.out.println("Session: freeing resources.");
            try {
                if(in != null) {
                    in.close(); // this will close socket too...
                } else {
                    clientSocket.close();
                }
            } catch (IOException ex) {
                System.err.println("Session: terminating error. IOException "
                        + "while closing resourses. " + ex.getMessage());
            }
            freeResources();

            System.out.println("Session: terminated.");
        }

        private void cyclicReadAndProcess(DataInputStream in) throws EOFException, IOException {            
            while(!mustEnd) {

                ProtocolMessage<Integer, String> message = new ProtocolMessage<Integer, String>();
                int messageId = in.readInt();
                if (messageId == ProtocolConstants.DISCONNECT_MESSAGE_ID) {
                    break;
                }

                int dataCount = in.readInt();
                List<String> data = new ArrayList<String>(dataCount); //TODO what if dataCount < 0  !?!?
                for(int i = 0; i < dataCount; ++i) {
                    data.add(in.readUTF());
                }
                message.setMessageId(messageId);
                message.setData(data);

                process(message);
            }
        }

        private void setClientSocketTimeout(int timeout) {
            try {
                clientSocket.setSoTimeout(timeout); // throws SocketException
            } catch (SocketException ex) {
                System.err.println("Session: run error. " + ex.getMessage()); // Error in the underlaying TCP protocol.
            }
        }                
    }
}
