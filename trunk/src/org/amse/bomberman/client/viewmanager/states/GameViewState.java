package org.amse.bomberman.client.viewmanager.states;

import java.util.List;
import javax.swing.JOptionPane;
import org.amse.bomberman.client.models.gamemodel.Player;
import org.amse.bomberman.client.models.gamemodel.impl.GameMapModel;
import org.amse.bomberman.client.models.gamemodel.impl.GameStateModel;
import org.amse.bomberman.client.models.gamemodel.impl.PlayerModel;
import org.amse.bomberman.client.models.impl.ChatModel;
import org.amse.bomberman.client.models.impl.ResultsModel;
import org.amse.bomberman.client.models.listeners.ChatModelListener;
import org.amse.bomberman.client.models.listeners.GameMapModelListener;
import org.amse.bomberman.client.models.listeners.GameStateListener;
import org.amse.bomberman.client.models.listeners.PlayerModelListener;
import org.amse.bomberman.client.models.listeners.ResultModelListener;
import org.amse.bomberman.client.view.gamejframe.GameFrame;
import org.amse.bomberman.client.view.gamejframe.GameKeyListener;
import org.amse.bomberman.client.view.gamejframe.GameMenuBar;
import org.amse.bomberman.client.viewmanager.ViewManager;

/**
 *
 * @author Kirilchuk V.E.
 */
public class GameViewState extends AbstractState
                           implements GameMapModelListener,
                                      ChatModelListener,
                                      ResultModelListener,
                                      PlayerModelListener,
                                      GameStateListener {
    private GameFrame gameFrame = new GameFrame();
    private GameKeyListener keyListener = new GameKeyListener(getController());
    private GameMenuBar menu = new GameMenuBar(getController());

    private boolean dead = false;
    private boolean isFirstInit = true;

    public GameViewState(ViewManager machine) {
        super(machine);
        
        gameFrame.setJMenuBar(menu);
        gameFrame.addKeyListener(keyListener);
    }

    public void init() {
        getController().getContext().getGameMapModel().addListener(this);
        getController().getContext().getChatModel().addListener(this);
        getController().getContext().getResultsModel().addListener(this);
        getController().getContext().getPlayerModel().addListener(this);
        getWizard().setVisible(false);
        gameFrame.setVisible(true);
    }

    @Override
    public void release() {
        getController().getContext().getGameMapModel().removeListener(this);
        getController().getContext().getChatModel().removeListener(this);
        getController().getContext().getResultsModel().removeListener(this);
        getController().getContext().getPlayerModel().removeListener(this);
    }

    public void previous() {
        gameFrame.setVisible(false);
        machine.setState(previous);
        getWizard().setVisible(true);
    }

    public void next() {
        //TODO log
        //do nothing
    }

    public void gameMapChanged() {
        GameMapModel model = getController().getContext().getGameMapModel();

//        if (isFirstInit) {//TODO CLIENT bad code
//            int mapSize = model.getMap().getSize();
//            if (mapSize < GamePanel.DEFAULT_RANGE) {
//                width = mapSize * GamePanel.CELL_SIZE + 50 + infoTextWidth;
//                height = mapSize * GamePanel.CELL_SIZE + 160;
//            }
//            isFirstInit = false;//TODO CLIENT bad code
//        }
        gameFrame.setGameMap(model);               
    }

    public void updateResults() {
        ResultsModel model = getController().getContext().getResultsModel();
        gameFrame.setResults(model.getResults());
    }

    public void updateChat(List<String> newMessages) {
        ChatModel model = getController().getContext().getChatModel();
        gameFrame.setHistory(model.getHistory());
    }

    public void updatePlayer() {
        PlayerModel model = getController().getContext().getPlayerModel();
        Player player = model.getPlayer();
        gameFrame.setBonuses(player.getLifes(),
                             player.getBombAmount(),
                             player.getBombRadius());

        int lives = player.getLifes();
        if (lives <= 0) {
            if (!dead) {//TODO CLIENT bad code
                dead = true;
                gameFrame.stopGame();
                JOptionPane.showMessageDialog(gameFrame, "You are dead!!!",
                        "Death", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    public void updateGameState() {
        GameStateModel model = getController().getContext().getGameStateModel();
        if (model.isEnded()) {
            if (!dead) { //TODO bad code
                dead = true;
                gameFrame.stopGame();
                JOptionPane.showMessageDialog(gameFrame, "You win!!!",
                        "Congratulations!", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    public void gameTerminated(String cause) {
        gameFrame.stopGame();
        JOptionPane.showMessageDialog(gameFrame, "Game terminated.",
                        cause, JOptionPane.ERROR_MESSAGE);
    }
}
