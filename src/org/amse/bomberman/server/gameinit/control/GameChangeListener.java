/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.amse.bomberman.server.gameinit.control;

import org.amse.bomberman.server.gameinit.Game;

/**
 *
 * @author Kirilchuk V.E
 */
public interface GameChangeListener extends GameStartedListener,
                                            GameEndedListener {
    void parametersChanged(Game game);
}
