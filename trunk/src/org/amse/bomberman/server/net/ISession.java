
/*
* To change this template, choose Tools | Templates
* and open the template in the editor.
 */
package org.amse.bomberman.server.net;

//~--- non-JDK imports --------------------------------------------------------

import org.amse.bomberman.server.gameinit.GameStorage;

//~--- JDK imports ------------------------------------------------------------

import java.util.List;

/**
 * Interface that represents session between client side and server side
 * of application. ISession is responsable for work with client request`s,
 * for answer`s on this requests and so on..
 *
 * @author Kirilchuk V.E
 */
public interface ISession {
    
    /**
     * Must somehow terminate session. After terminate session can`t be reused.
     */
    void terminateSession();

    /**
     * Tells session to start receiving requests and process them.
     */
    void start();

    /**
     * Sends list of strings to client.
     * @param messages lines of strings to send to client.
     */
    void sendAnswer(List<String> messages);

    /**
     * Sends simple one-string message to client.
     * @param message string to send to client.
     */
    void sendAnswer(String message);

    /**
     * Returns GameStorage where games are storing.
     * @see GameStorage
     * @return game storage.
     */
    GameStorage getGameStorage();

    /**
     * Returns the id of this session.
     *
     * <p> Note that if int will overflow, id would be not unique.
     * @return unique id of session.
     */
    int getID();

    /**
     * Returns if current session must terminate.
     * @return true if session must terminate, false - otherwise.
     */
    boolean isMustEnd();
}
