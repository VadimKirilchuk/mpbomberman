
/*
* To change this template, choose Tools | Templates
* and open the template in the editor.
 */
package org.amse.bomberman.server.gameservice.models.impl;

//~--- non-JDK imports --------------------------------------------------------

import org.amse.bomberman.server.gameservice.models.DieListener;
import org.amse.bomberman.server.gameservice.Game;
import org.amse.bomberman.server.gameservice.GameMap;
import org.amse.bomberman.server.gameservice.models.MoveableObject;
import org.amse.bomberman.util.Pair;
import org.amse.bomberman.server.gameservice.models.Model;
import org.amse.bomberman.util.Constants;
import org.amse.bomberman.util.Constants.Direction;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Model that is responsable for game rules and responsable for connection
 * between GameMap and Game.
 * @author Kirilchuk V.E.
 */
public class DefaultModel implements Model, DieListener {
    private static final ScheduledExecutorService timer =
                                   Executors.newSingleThreadScheduledExecutor();

    private final List<Bomb>                  bombs;
    private final List<Pair>                  explosionSquares;
    private final List<Integer>               freeIDs;
    private final Game                        game;
    private final GameMap                     gameMap;
    private final List<ModelPlayer>           players;
    private boolean                           ended;

    /**
     * Constructor of Model.
     * @param map GameMap that correspond to game
     * @param game Game for which model was created
     */
    public DefaultModel(GameMap gameMap, Game game) {
        this.gameMap = gameMap;
        this.game = game;
        this.bombs = new CopyOnWriteArrayList<Bomb>();
        this.players = new CopyOnWriteArrayList<ModelPlayer>();
        this.ended = false;

        Integer[] freeIDArray = new Integer[this.game.getMaxPlayers()];

        for (int i = 0; i < freeIDArray.length; ++i) {
            freeIDArray[i] = i + 1;//cause players indexes are from 1 to ..
        }

        this.freeIDs = new CopyOnWriteArrayList<Integer>(freeIDArray);
        this.explosionSquares = new CopyOnWriteArrayList<Pair>();
    }

    @Override
    public void addExplosions(List<Pair> explSq) {
        if(this.ended){
            return;
        }
        this.explosionSquares.addAll(explSq);
        this.game.fieldChanged();
    }

    @Override
    public int addPlayer(String name) {
        ModelPlayer playerToAdd = new ModelPlayer(name, DefaultModel.timer);

        playerToAdd.setID(getFreeID());
        playerToAdd.setDieListener(this);
        this.players.add(playerToAdd);

        return playerToAdd.getID();
    }

    @Override
    public void bombDetonated(Bomb bomb) {
        this.bombs.remove(bomb);

        if(this.ended){
            return;
        }
        
        ModelPlayer owner = bomb.getOwner();

        if (owner.getPosition().equals(bomb.getPosition())) {
            this.gameMap.setSquare(owner.getPosition(), owner.getID());
        } else {
            this.gameMap.setSquare(bomb.getPosition(), Constants.MAP_EMPTY);
        }

        this.game.fieldChanged();
    }

    @Override
    public void detonateBombAt(Pair position) {
        if(this.ended){
            return;
        }
        Bomb bombToDetonate = null;

        for (Bomb bomb : bombs) {
            if (bomb.getPosition().equals(position)) {
                bombToDetonate = bomb;

                break;
            }
        }

        bombToDetonate.detonate(true);
    }

    public void tryEnd() {
        int aliveCount = 0;
        for (ModelPlayer pl : players) {
            if (pl.isAlive()) {
                aliveCount++;
            }
        }
        if (aliveCount <= 1 && !this.ended) {
            this.end();
        }
    }

    @Override
    public int getCurrentPlayersNum() {
        return this.players.size();
    }

    /**
     * Return list of explosions.
     * @return List of explosions
     */
    @Override
    public List<Pair> getExplosionSquares() {
        return this.explosionSquares;
    }

    private int getFreeID() {
        return this.freeIDs.remove(0);
    }

    @Override
    public GameMap getGameMap() {
        return gameMap;
    }

    @Override
    public ModelPlayer getPlayer(int playerID) {
        for (ModelPlayer player : players) {
            if (player.getID() == playerID) {
                return player;
            }
        }

        return null;
    }

    @Override
    public List<ModelPlayer> getPlayersList() {
        return Collections.unmodifiableList(players);
    }

    @Override
    public boolean isExplosion(Pair coords) {
        return this.explosionSquares.contains(coords);
    }

    //TODO rewrite this casuistic logic
    private boolean isMoveToReserved(Pair pair) {    // note that on explosions isEmpty = true!!!
        int x = pair.getX();
        int y = pair.getY();

        boolean isFree = this.gameMap.isEmpty(x, y)
                         || this.gameMap.isBonus(x, y);

        return !isFree;
    }

    private boolean isOutMove(Pair pair) {
        int x = pair.getX();
        int y = pair.getY();

        int dim = this.gameMap.getDimension();

        if ((x < 0) || (x > dim - 1)) {
            return true;
        }

        if ((y < 0) || (y > dim - 1)) {
            return true;
        }

        return false;
    }

    //TODO use polymorphism instead of instanceof construction
    private void makeMove(MoveableObject objectToMove, Pair destination) {
        int x = objectToMove.getPosition().getX();
        int y = objectToMove.getPosition().getY();
        int newX = destination.getX();
        int newY = destination.getY();

        if (objectToMove instanceof ModelPlayer) {
            if (this.gameMap.isBomb(x, y)) {    // if player setted mine but still in same square
                this.gameMap.setSquare(x, y, Constants.MAP_BOMB);
            } else {
                this.gameMap.setSquare(x, y, Constants.MAP_EMPTY);
            }

            if (this.gameMap.isBonus(newX, newY)) {
                int bonus = this.gameMap.getSquare(newX, newY);

                ((ModelPlayer) objectToMove).takeBonus(bonus);
            }
        } else if (objectToMove instanceof Bomb) {
            Bomb bomb = (Bomb) objectToMove;

            if (bomb.getOwner().getPosition().equals(bomb.getPosition())) {
                this.gameMap.setSquare(x, y, bomb.getOwner().getID());
            } else {
                this.gameMap.setSquare(x, y, Constants.MAP_EMPTY);
            }
        }

        this.gameMap.setSquare(newX, newY, objectToMove.getID());

        Pair newPosition = new Pair(newX, newY);

        objectToMove.setPosition(newPosition);

        // if object is making move to explosion zone.
        if (isExplosion(newPosition)) {
            if(objectToMove instanceof ModelPlayer) {
                String name = ((ModelPlayer)objectToMove).getNickName();
                this.game.addMessageToChat("Ouch, " + name +
                                           " just rushed into the fire.");
            }
            objectToMove.bombed();
        }
    }

    private Pair newPosition(Pair curPos, Direction direction) {    // whats about catch illegalArgumentException???

        switch (direction) {
            case DOWN : {
                return new Pair(curPos.getX() + 1,curPos.getY());
            }

            case LEFT : {
                return new Pair(curPos.getX(),curPos.getY() - 1);
            }

            case UP : {
                return new Pair( curPos.getX() - 1,curPos.getY());
            }

            case RIGHT : {
                return new Pair( curPos.getX(),curPos.getY() + 1);
            }

            default : {
                throw new IllegalArgumentException("Default block " +
                        "in switch(ENUM). Error in code.");
            }
        }
    }

    @Override
    public void playerBombed(ModelPlayer atacker, int victimID) {
        if(this.ended){
            return;
        }
        ModelPlayer victim = this.getPlayer(victimID);
        this.playerBombed(atacker, victim);
    }

    @Override
    public void playerBombed(ModelPlayer atacker, ModelPlayer victim) {
        this.game.addMessageToChat(/*"Bomb of " + */atacker.getNickName() +
                                   " damaged " + victim.getNickName());
        if (atacker != victim) {
            atacker.damagedSomeone();     //TODO clear such damn hardcoded code
            atacker.changePoints(+1);
        }
        victim.bombed();
        victim.changePoints(-1);
    }

    @Override
    public void playerDied(ModelPlayer player) {
        this.gameMap.removePlayer(player.getID());
        this.game.fieldChanged();
        this.game.addMessageToChat("Oh, no. " + player.getNickName() +
                                   " was cruelly killed.");
    }

    /**
     * Printing matrix of GameMap to console. Maybe would be deleted soon.
     */
    @Override
    public void printToConsole() {    // useless?
        int dim = this.gameMap.getDimension();

        for (int i = 0; i < dim; i++) {
            System.out.println();

            for (int j = 0; j < dim; j++) {
                System.out.print(this.gameMap.getSquare(i, j) + " ");
            }
        }

        System.out.println();
    }

    @Override
    public void removeExplosions(List<Pair> explosions) {

        for (Pair pair : explosions) {
            this.explosionSquares.remove(pair);
        }

        this.game.fieldChanged();
    }

    @Override
    public boolean removePlayer(int playerID) {
        for (ModelPlayer player : players) {
            if (player.getID() == playerID) {
                this.players.remove(player);
                this.freeIDs.add(playerID);
                if(this.game.isStarted()){
                    this.gameMap.removePlayer(playerID);
                }
                return true;
            }
        }

        return false;
    }

    @Override
    public void startup() {
        this.gameMap.changeMapForCurMaxPlayers(this.players.size());

        int playerX;
        int playerY;

        for (ModelPlayer player : players) {
            playerX = this.gameMap.xCoordOf(player.getID());
            playerY = this.gameMap.yCoordOf(player.getID());

            Pair playerCoords = new Pair(playerX, playerY);

            player.setPosition(playerCoords);
        }
    }

    public void end() {
        this.ended = true;
        for (ModelPlayer player : players) {
            if(player.isAlive()){
                player.changePoints(+3);
            }
        }
//        List<String> stats = ConverterToString.convertPlayersStats(this.players); //TODO BIG
//        stats.add(0, ProtocolConstants.CAPTION_GAME_END_RESULTS);
//        this.game.notifyGameSessions(stats);
    }

    /**
     * Trying to move the player in defined direction.
     * @param player Player to move
     * @param direction Direction of move
     * @return true if player moved, false otherwise
     */
    @Override
    public boolean tryDoMove(MoveableObject objToMove, Direction direction) {    // TODO synchronization?
        if(this.ended){
            return false;
        }
        synchronized (gameMap) {
            synchronized (objToMove) {
                Pair destination = newPosition(objToMove.getPosition(), direction);

                if (!isOutMove(destination)) {
                    if (this.gameMap.isBomb(destination)
                            && (objToMove instanceof ModelPlayer)) {
                        Bomb bombToMove = null;

                        for (Bomb bomb : bombs) {
                            if (bomb.getPosition().equals(destination)) {
                                bombToMove = bomb;
                                tryDoMove(bombToMove, direction);

                                break;
                            }
                        }
                    }

                    if (!isMoveToReserved(destination)) {
                        makeMove(objToMove, destination);

                        return true;
                    }
                }

                return false;
            }
        }
    }

    /**
     * Trying to place bomb of defined player.
     * @param player Player which trying to place bomb.
     */
    @Override
    public boolean tryPlaceBomb(ModelPlayer player) {    // whats about synchronization??
        if(this.ended){
            return false;
        }
        synchronized (gameMap) {
            synchronized (player) {    // whats about syncronize(map)???
                if (player.canPlaceBomb()) {    // player is alive and have bombs to set up
                    int x = player.getPosition().getX();
                    int y = player.getPosition().getY();

                    if (this.gameMap.isBomb(x, y)
                            || this.isExplosion(new Pair(x, y))) {
                        return false;    // if player staying under the bomb or explosion
                    }

                    Bomb bomb = new Bomb(this, player, new Pair(x, y), DefaultModel.timer);

                    this.bombs.add(bomb);

                    Pair bombPosition = bomb.getPosition();

                    this.gameMap.setSquare(bombPosition.getX(),
                                           bombPosition.getY(),
                                           Constants.MAP_BOMB);                    

                    return true;
                }
            }
        }

        return false;
    }
}
