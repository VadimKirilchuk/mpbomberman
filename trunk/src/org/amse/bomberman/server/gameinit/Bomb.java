/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amse.bomberman.server.gameinit;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.amse.bomberman.server.gameinit.imodel.IModel;
import org.amse.bomberman.util.Constants;

/**
 *
 * @author Kirilchuk V.E.
 */
public class Bomb {

    private final IModel model;
    private final Player player;
    private final GameMap map;
    private final ScheduledExecutorService timer;
    private final int bombX;
    private final int bombY;
    private final int radius;
    private boolean wasDetonated = false;

    public Bomb(IModel model, Player player, GameMap map, int bombX, int bombY, ScheduledExecutorService timer) {
        this.model = model;
        this.player = player;
        this.map = map;
        this.timer = timer;

        this.bombX = bombX;
        this.bombY = bombY;
        this.radius = this.player.getRadius();

        this.player.placedBomb();
        this.map.addBomb(this);
        timer.schedule(new DetonateTask(),
                            Constants.BOMB_TIMER_VALUE, TimeUnit.MILLISECONDS);
    }

    public int getX() {
        return bombX;
    }

    public int getY() {
        return bombY;
    }

    public void detonate() {

        if (this.wasDetonated) {
            return;
        }

        this.wasDetonated = true;
        this.map.bombStartDetonating(this);
        map.setSquare(bombX, bombY, Constants.MAP_DETONATED_BOMB);

        ArrayList<Pair> explosions = new ArrayList<Pair>();

        //if player still staying in bomb square.
        if (this.player.getX() == this.bombX && this.player.getY() == this.bombY) {
            this.player.bombed();
        }

        
        //explosion lines
        int i; // common iterator
        int k; // common radius counter
        boolean contin; //common continue boolean

        //uplines
        k = radius;
        for (i = bombX - 1; (i >= 0 && k > 0); --i, --k) {
            contin = explodeSquare(i, bombY);
            explosions.add(new Pair(i, bombY));
            if (!contin) {
                break;
            }
        }

        //downlines
        k = radius;
        for (i = bombX + 1; (i < map.getDimension() && k > 0); ++i, --k) {
            contin = explodeSquare(i, bombY);
            explosions.add(new Pair(i, bombY));
            if (!contin) {
                break;
            }
        }

        //leftlines
        k = radius;
        for (i = bombY - 1; (i >= 0 && k > 0); --i, --k) {
            contin = explodeSquare(bombX, i);
            explosions.add(new Pair(bombX, i));
            if (!contin) {
                break;
            }
        }

        //rightlines
        k = radius;
        for (i = bombY + 1; (i < map.getDimension() && k > 0); ++i, --k) {
            contin = explodeSquare(bombX, i);
            explosions.add(new Pair(bombX, i));
            if (!contin) {
                break;
            }
        }

        this.map.addExplosions(explosions); //add explosions to map
        this.player.detonatedBomb();
        timer.schedule(new ClearExplosionTask(explosions, new Pair(bombX, bombY), this.player), Constants.BOMB_DETONATION_TIME, TimeUnit.MILLISECONDS);
    }

    //true if we must continue cycle
    //false if we must break cycle;
    private boolean explodeSquare(int x, int y) {
        if (map.isEmpty(x, y)) {
            if (map.isExplosion(new Pair(x, y))) {     //explosion
                return false;
            }
            return true;                                         //emptySquare
        } else if (map.blockAt(x, y) != -1) {                    //blockSquare
            if (map.blockAt(x, y) == 1) {                        //undestroyableBlock
                //undestroyable so do nothing
                //going to return false;
            } else {                                         //destroyable block
                map.setSquare(x, y, map.blockAt(x, y) + 1 - 9);
            }
            return false;
        } else if (map.playerIdAt(x, y) != -1) {                 //playerSquare
            int id = map.playerIdAt(x, y);
            this.model.playerBombed(id);
            return false;
        } else if (map.isBomb(x, y)) {                           //another bomb
            map.detonateBomb(x,y);
            return false;
        }

        return true;
    }

    private class DetonateTask implements Runnable {

        public DetonateTask() {//whats about syncronization(player)
        }

        @Override
        public void run() {
            detonate();
        }
    }

        private class ClearExplosionTask implements Runnable {

        private final int bombX;
        private final int bombY;
        private final List<Pair> explSqToClear;
        private final Player player;

        public ClearExplosionTask(List<Pair> toClear, Pair bombToClear, Player player) {
            this.bombX = bombToClear.getX();
            this.bombY = bombToClear.getY();
            this.explSqToClear = toClear;
            this.player = player;
        }

        @Override
        public void run() {//whats about syncronization(player,map)
            if (player.getX() == bombX && player.getY() == bombY && player.isAlive()) {
                map.setSquare(bombX, bombY, this.player.getID());
            } else {
                map.setSquare(bombX, bombY, Constants.MAP_EMPTY); //clear from map
            }
            for (Pair pair : explSqToClear) { // clear from explosions list
                map.removeExplosion(pair);
            }
        }
    }

}
