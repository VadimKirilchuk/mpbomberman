
/*
* To change this template, choose Tools | Templates
* and open the template in the editor.
 */
package org.amse.bomberman.server.gameservice.bots;

//~--- non-JDK imports --------------------------------------------------------

import org.amse.bomberman.server.gameservice.GameMap;
import org.amse.bomberman.server.gameservice.models.Model;
import org.amse.bomberman.util.Constants.Direction;
import org.amse.bomberman.util.Pair;

//~--- JDK imports ------------------------------------------------------------

import java.util.Random;

/**
 * Class that represents bot strategy where bot
 * randomly choose where to go. Only MoveAction and EmptyAction can be
 * generated by this strategy.
 * @see EmptyAction
 * @see MoveAction
 * @author Kirilchuk V.E.
 */
public class RandomMoveBotStrategy extends BotStrategy {
    private Pair target;

    /**
     * Method from BotStrategy. Returns new move action.
     * @see BotStrategy
     * @param bot bot that thinking about action.
     * @param model model that owns this bot.
     * @return new move action.
     */
    @Override
    public Action thinkAction(Bot bot, Model model) {
        if (bot.getPosition().equals(this.target) || (target == null)) {
            target = findNewTarget(model);
        }

        Direction direction = null;

        do {
            try {
                Thread.sleep(75);
                direction = findWay(bot.getPosition(), target,
                                    model.getGameMap().getField(), model);

                // System.out.println("Direction" + direction.toString());
            } catch (IllegalArgumentException ex) {
                target = findNewTarget(model);

                // System.out.println("NEW TARGET");
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        } while (direction == null);

        return new MoveAction(direction, bot);
    }

    private Pair findNewTarget(Model model) {
        GameMap map = model.getGameMap();
        Random  random = new Random();
        int     x = 0;
        int     y = 0;

        do {
            x = random.nextInt(map.getDimension() - 1);
            y = random.nextInt(map.getDimension() - 1);
        } while (!map.isEmpty(x, y));

        Pair dir = new Pair(x, y);

        // System.out.println("Choosed " + dir.toString());
        return dir;
    }
}
