/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amse.bomberman.server.gameinit.imodel.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.amse.bomberman.server.gameinit.Bomb;
import org.amse.bomberman.server.gameinit.bot.Bot;
import org.amse.bomberman.server.gameinit.Game;
import org.amse.bomberman.server.gameinit.imodel.IModel;
import org.amse.bomberman.server.gameinit.GameMap;
import org.amse.bomberman.server.gameinit.Pair;
import org.amse.bomberman.server.gameinit.Player;
import org.amse.bomberman.server.gameinit.bot.RandomFullBotStrategy;
import org.amse.bomberman.server.gameinit.bot.RandomMoveBotStrategy;
import org.amse.bomberman.util.Constants;
import org.amse.bomberman.util.Constants.Direction;

/**
 * Model that is responsable for game rules and responsable for connection
 * between Map and Game.
 * @author Kirilchuk V.E.
 */
public class Model implements IModel {

    private final GameMap map;
    private final Game game;
    private final ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor();
    private final List<Thread> bots = new ArrayList<Thread>();
    //private final List<DetonateControl> detonateControls;

    /**
     * Constructor of Model.
     * @param map GameMap that correspond to game
     * @param game Game for which model was created
     */
    public Model(GameMap map, Game game) {
        this.map = map;
        this.game = game;        
    }

    /**
     * Return matrix of GameMap.
     * @return matrix of GameMap
     */
    public int[][] getMapArray() {
        return this.map.getMapArray();
    }

    /**
     * Return list of explosions.
     * @return List of explosions
     */
    public List<Pair> getExplosionSquares() {
        return this.map.getExplosionSquares();
    }

    /**
     * Give x coordinate for player. Search respawn point
     * of this player on map and return x coordinate of this respawn.
     * @param id ID of player
     * @return x coordinate of player
     */
    public int xCoordOf(int playerID) {
        int[][] mapArray = this.map.getMapArray();
        for (int i = 0; i < mapArray.length; i++) {
            for (int j = 0; j < mapArray.length; j++) {
                if (mapArray[i][j] == playerID) {
                    return i;
                }
            }
        }
        return 0;
    }

    /**
     * See xCoordOf
     * @param id
     * @return
     */
    public int yCoordOf(int playerID) {
        int[][] mapArray = this.map.getMapArray();
        for (int i = 0; i < mapArray.length; i++) {
            for (int j = 0; j < mapArray.length; j++) {
                if (mapArray[i][j] == playerID) {
                    return j;
                }
            }
        }
        return 0;
    }

    /**
     * Trying to move the player in defined direction.
     * @param player Player to move
     * @param direction Direction of move
     * @return true if player moved, false otherwise
     */
    public boolean doMove(Player player, Direction direction) {//CHECK THIS
        synchronized (map) {
            synchronized (player) {
                int arr[] = newCoords(player.getPosition(), direction);
                int newX = arr[0];
                int newY = arr[1];
                if (!isOutMove(newX, newY)) {
                    if (!isMoveToReserved(newX, newY)) {
                        makeMove(player, newX, newY);
                        return true;
                    }
                }
                return false;
            }
        }
    }

    private void makeMove(Player player, int newX, int newY) {
        int x = player.getPosition().getX();
        int y = player.getPosition().getY();

        if (this.map.isBomb(x, y)) { //if player setted mine but still in same square
            this.map.setSquare(x, y, Constants.MAP_BOMB);
        } else {
            this.map.setSquare(x, y, Constants.MAP_EMPTY);
        }
        this.map.setSquare(newX, newY, player.getID());

        //if player is making move to explosion zone.
        if (this.map.isExplosion(new Pair(newX, newY))) {
            player.bombed();
        }

        player.setPosition(new Pair(newX, newY));
    }

    private boolean isOutMove(int x, int y) {
        int dim = this.map.getDimension();
        if (x < 0 || x > dim - 1) {
            return true;
        }
        if (y < 0 || y > dim - 1) {
            return true;
        }
        return false;
    }

    private boolean isMoveToReserved(int x, int y) {//note that on explosions isEmpty = true!!!
        return (this.map.isEmpty(x, y)) ? false : true;
    }

    private int[] newCoords(Pair currentPosition, Direction direction) { //whats about catch illegalArgumentException???
        int[] arr = new int[2];

        switch (direction) {
            case DOWN: {
                arr[0] = currentPosition.getX() + 1;
                arr[1] = currentPosition.getY();
                break;
            }
            case LEFT: {
                arr[0] = currentPosition.getX();
                arr[1] = currentPosition.getY() - 1;
                break;
            }
            case UP: {
                arr[0] = currentPosition.getX() - 1;
                arr[1] = currentPosition.getY();
                break;
            }
            case RIGHT: {
                arr[0] = currentPosition.getX();
                arr[1] = currentPosition.getY() + 1;
                break;
            }
            default: {
                throw new IllegalArgumentException("Default block " +
                        "in switch(ENUM). Error in code.");
            }
        }

        return arr;
    }

    /**
     * Printing matrix of GameMap to console. Maybe would be deleted soon.
     */
    public void printToConsole() { //useless?
        int dim = this.map.getDimension();
        for (int i = 0; i < dim; i++) {
            System.out.println();
            for (int j = 0; j < dim; j++) {
                System.out.print(this.map.getSquare(i, j) + " ");
            }
        }
        System.out.println();
    }

    /**
     * Change GameMap for defined in argument number of players by
     * removing unused players from GameMap.
     * @param maxPlayers number of players to use
     */
    public void changeMapForCurMaxPlayers(int maxPlayers) {
        this.map.changeMapForCurMaxPlayers(maxPlayers);
    }

    /**
     * Remove one player from mapArray
     * @param id ID of player we need to remove
     */
    public void removePlayer(int playerID) {
        this.map.removePlayer(playerID);
    }

    /**
     * Trying to place bomb of defined player.
     * @param player Player which trying to place bomb.
     */
    public void placeBomb(Player player) {// whats about synchronization?? //Maybe return value must be boolean type???
        synchronized (map) {
            synchronized (player) {           // whats about syncronize(map)???
                if (player.canPlaceBomb()) { //player is alive and have bombs to set up
                    int x = player.getPosition().getX();
                    int y = player.getPosition().getY();
                    if (this.map.isBomb(x, y)) {
                        return; //if player staying under the bomb
                    }
                    Bomb bomb = new Bomb(this, player, map, new Pair(x, y) , timer);
                }
            }
        }
    }

    public void playerBombed(int id){
        game.getPlayer(id).bombed();
    }

    /**
     * Return name of GameMap of this Model.
     * @return Name of GameMap in String
     */
    public String getMapName() {
        return this.map.getName();
    }

    public Bot addBot(String name, int id) {
        Bot bot = new Bot(name, id, this, new RandomFullBotStrategy());
        Thread t = new Thread(bot);
        t.setDaemon(true);
        this.bots.add(t);
        return bot;
    }

    public void removeBot(Bot bot){
        this.game.disconnectFromGame(bot);
        //this.bots.remove(bot); //TO DO //bot is PLAYER and bots store THREADS!!!! remove always false!
    }

    public GameMap getMap() {
        return map;
    }

    public void startBots() {
        for (Thread thread : bots) {
            thread.start();
        }
    }
    

}