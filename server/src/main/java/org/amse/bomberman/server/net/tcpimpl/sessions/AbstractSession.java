package org.amse.bomberman.server.net.tcpimpl.sessions;

import java.io.IOException;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.amse.bomberman.server.net.Session;
import org.amse.bomberman.server.net.SessionEndListener;
import org.amse.bomberman.server.net.tcpimpl.sessions.control.RequestExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class that represents basic functionality of session.
 *
 * <p>Actual realization can choose how and when to send answers on requests
 * and who would execute this requests.
 *
 * @see RequestExecutor
 * @author Kirilchuk V.E.
 */
public abstract class AbstractSession implements Session {
    
    private static final Logger LOG = LoggerFactory.getLogger(AbstractSession.class);

    /** Client socket of this session. It can`t be null. */
    protected final Socket clientSocket;

    /** Session id. In fact it can be not unique. */
    protected final long sessionId;

    protected final Set<SessionEndListener> listeners
            = new CopyOnWriteArraySet<SessionEndListener>();

    /** The mustEnd boolean. Tells if this session must terminate. */
    protected volatile boolean mustEnd;

    /**
     * Constructs AbstractSession with defined clientSocket, gameStorage,
     * sessionID and log.
     *
     * @param clientSocket client socket.
     * @param gameStorage game storage for this session.
     * @param sessionID session id. In fact it can be not unique.
     * @param log currently not used.
     */
    public AbstractSession(Socket clientSocket, long sessionID) {
        if(clientSocket == null) {
            throw new IllegalArgumentException("Client socket can`t be null.");
        }

        this.clientSocket = clientSocket;
        this.sessionId    = sessionID;
        this.mustEnd      = false;
    }

    @Override
    public void addEndListener(SessionEndListener listener) {
        this.listeners.add(listener);
    }

    @Override
    public void removeEndListener(SessionEndListener listener) {
        this.listeners.remove(listener);
    }

    public void clearEndListeners() {
        this.listeners.clear();
    }

//    /**
//     * Checks if session must terminate.
//     *
//     * @return true if session must terminate, false - otherwise.
//     */
//    @Override
//    public boolean isMustEnd() {
//        return this.mustEnd;
//    }

    /**
     * This method must provide terminating of session.
     * Call on already terminated session can cause error or exception,
     * it depends on actual realization. After terminate session can`t be reused
     * and it`s thread must be in TERMINATED_STATE.
     * <p>By default, the second call on this method
     * will lead to RuntimeException.
     */
    @Override
    public void terminateSession() {
        if (this.mustEnd) {
            throw new IllegalStateException("Already terminating.");
        }

        this.mustEnd = true;

        try {
            //force blocking io to unblock
            this.clientSocket.shutdownInput();
        } catch (IOException ex) {
            LOG.warn("Session: terminateSession error.", ex);
        }
    }

    /**
     * Method that returns pseudo-unique id for this session.
     *
     * @return pseudo-unique id for this session.
     */
    @Override
    public long getId() {
        return sessionId;
    }
}

