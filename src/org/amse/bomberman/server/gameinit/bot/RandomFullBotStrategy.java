/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.amse.bomberman.server.gameinit.bot;

import java.util.Random;
import org.amse.bomberman.server.gameinit.GameMap;
import org.amse.bomberman.util.Pair;
import org.amse.bomberman.server.gameinit.imodel.IModel;
import org.amse.bomberman.util.Constants.Direction;

/**
 *
 * @author Kirilchuck V.E.
 */
public class RandomFullBotStrategy extends BotStrategy{

    Pair target;

    private Pair findNewTarget(IModel model) {
        GameMap map = model.getGameMap();
        Random random = new Random();
        int x = 0;
        int y = 0;
        do {
            x = random.nextInt(map.getDimension() - 1);
            y = random.nextInt(map.getDimension() - 1);
        } while (!(map.isEmpty(x, y) || map.isBonus(x, y)));

        Pair dir = new Pair(x, y);
        //System.out.println("Choosed " + dir.toString());
        return dir;
    }

    @Override
    public IAction thinkAction(Bot bot, IModel model) {
        if (bot.getPosition().equals(this.target) || target == null) {
            target = findNewTarget(model);
        }

        Direction direction = null;
        do {
            try {
                Thread.sleep(75);
                direction = findWay(bot.getPosition(), target, model.getGameMapArray());
                //System.out.println("Direction" + direction.toString());
            } catch (IllegalArgumentException ex) {
                target = findNewTarget(model);
                //System.out.println("NEW TARGET");
            } catch (InterruptedException ex){
                ; //TO DO
            }
        } while (direction == null);

        Random rnd = new Random();
        int n = rnd.nextInt(100);

        if(n<10){
            return new PlaceAndMoveAction(direction, bot);
        }
        
        return new MoveAction(direction, bot);
    }
}
