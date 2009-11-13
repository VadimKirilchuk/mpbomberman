/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amse.bomberman.server.gameInit;

import java.util.ArrayList;
import java.util.List;
import org.amse.bomberman.server.gameInit.IModel.IModel;
import org.amse.bomberman.server.gameInit.IModel.impl.Model;

/**
 *
 * @author Kirilchuk V.E
 */
public class Game {

    private boolean started = false;
    private String gameName = "game";
    private int maxPlayers;
    private List<Player> players;
    private IModel model;

    public Game(Map map, String gameName) {
        this.started = false;
        this.gameName = gameName;
        this.maxPlayers = map.getMaxPlayers();
        this.players = new ArrayList<Player>();
        this.model = new Model(map);
    }

    public Game(Map map, String gameName, int maxPlayers) {
        this(map, gameName);
        if (maxPlayers > 0 && maxPlayers <= this.maxPlayers) {
            this.maxPlayers = maxPlayers;
        }
    }

    public Player join(String name) {
        if (players.size() == this.maxPlayers) {
            return null;
        } else {
            Player player = new Player(name, players.size() + 1);
            //coordinates of players will be set when game would start!!!!
            players.add(player);
            return player;
        }
    }

    public void disconnect(Player player) {
        this.players.remove(player);
        this.model.removePlayer(player.getID());
    }

    public void startGame() {
        this.started = true;
        //Here model must change map to support currrent num of players
        //and then give coordinates.
        this.model.changeMapForCurMaxPlayers(this.players.size());
        for (Player player : players) {
            player.setX(model.xCoordOf(player.getID()));
            player.setY(model.yCoordOf(player.getID()));
        }
    }

    public boolean doMove(Player player, int direction) {
        if (this.started != false) {
            return model.doMove(player, direction);
        } else {
            return false;
        }
    }

    public int[][] getMapArray() {
        return this.model.getMapArray();
    }

    public boolean isStarted() {
        return this.started;
    }

    public String getName() {
        return this.gameName;
    }
}