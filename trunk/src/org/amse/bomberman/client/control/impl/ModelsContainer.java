package org.amse.bomberman.client.control.impl;

import org.amse.bomberman.client.models.impl.ChatModel;
import org.amse.bomberman.client.models.impl.ClientStateModel;
import org.amse.bomberman.client.models.impl.ConnectionStateModel;
import org.amse.bomberman.client.models.impl.GameMapsModel;
import org.amse.bomberman.client.models.impl.GamesModel;
import org.amse.bomberman.client.models.impl.ResultsModel;
import org.amse.bomberman.client.models.gamemodel.impl.GameMapModel;
import org.amse.bomberman.client.models.gamemodel.impl.GameStateModel;
import org.amse.bomberman.client.models.gamemodel.impl.PlayerModel;
import org.amse.bomberman.client.models.impl.GameInfoModel;

/**
 * Container of models(From MVC). Also can be called 'context'.
 * All models can be treated as singletons. cause getter methods
 * of this container always return the same object.
 *
 * @author Kirilchuk V.E.
 */
public class ModelsContainer {
    private final ConnectionStateModel  connectionStateModel
            = new ConnectionStateModel();

    private final GameMapModel  gameMapModel  = new GameMapModel();
    private final PlayerModel   playerModel   = new PlayerModel();
    private final GameInfoModel gameInfoModel = new GameInfoModel();
    private final ChatModel     chatModel     = new ChatModel();
    private final ResultsModel  resultsModel  = new ResultsModel();
    private final GameMapsModel gameMapsModel = new GameMapsModel();
    private final GamesModel    gamesModel    = new GamesModel();

    private final ClientStateModel clientStateModel = new ClientStateModel();
    private final GameStateModel   gameStateModel   = new GameStateModel();

    /**
     * @return gameMaps model.
     */
    public GameMapsModel getGameMapsModel() {
        return gameMapsModel;
    }

    /**
     * @return games model.
     */
    public GamesModel getGamesModel() {
        return gamesModel;
    }

    /**
     * @return client state model.
     */
    public ClientStateModel getClientStateModel() {
        return clientStateModel;
    }

    /**
     * @return chat model.
     */
    public ChatModel getChatModel() {
        return chatModel;
    }

    /**
     * @return connection state model.
     */
    public ConnectionStateModel getConnectionStateModel() {
        return connectionStateModel;
    }

    /**
     * @return gameMap model.
     */
    public GameMapModel getGameMapModel() {
        return gameMapModel;
    }

    /**
     * @return results model.
     */
    public ResultsModel getResultsModel() {
        return resultsModel;
    }

    /**
     * @return game info model.
     */
    public GameInfoModel getGameInfoModel() {
        return gameInfoModel;
    }

    /**
     * @return player model.
     */
    public PlayerModel getPlayerModel() {
        return playerModel;
    }

    /**
     * @return game state model.
     */
    public GameStateModel getGameStateModel() {
        return gameStateModel;
    }
}
