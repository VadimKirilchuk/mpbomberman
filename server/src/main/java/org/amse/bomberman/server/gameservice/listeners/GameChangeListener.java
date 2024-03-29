package org.amse.bomberman.server.gameservice.listeners;

import org.amse.bomberman.server.gameservice.impl.Game;

/**
 *
 * @author Kirilchuk V.E
 */
public interface GameChangeListener {

    void parametersChanged(Game game);

    /**
     * Tells that game was started.
     */
    void gameStarted(Game game);

    /**
     * Tells that game was terminated.
     */
    void gameTerminated(Game game);

    void newChatMessage(String message);

    void fieldChanged();

    void gameEnded(Game game);

    void statsChanged(Game game);
}
