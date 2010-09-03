package org.amse.bomberman.protocol;

/**
 * Utility class for storing some constants for protocol.
 * @author Kirilchuk V.E.
 * @author Mikhail Korovkin
 */
public class ProtocolConstants {

    /** Caption for message with list of availiable games. */
    public static final String CAPTION_GAMES_LIST = "Games list.";

    /** Caption for message with result of "create game" request. */
    public static final String CAPTION_CREATE_GAME_RESULT = "Create game info.";

    /** Caption for message with result of "join game" request. */
    public static final String CAPTION_JOIN_GAME_RESULT = "Join game info.";

    /** Caption for message with result of "do move" request. */
    public static final String CAPTION_DO_MOVE_RESULT = "Do move info.";

    /**
     * Caption for message with game field, exlosions and other
     * info about position. This is info to draw field on client.
     */
    public static final String CAPTION_GAME_MAP_INFO = "Game map info.";

    /** Caption for message with result of "start game" request. */
    public static final String CAPTION_START_GAME_RESULT = "Start game info.";

    /** Caption for message with result of "leave game" request. */
    public static final String CAPTION_LEAVE_GAME_RESULT = "Leave game info.";

    /** Caption for message with result of "place bomb" request. */
    public static final String CAPTION_PLACE_BOMB_RESULT = "Plant bomb info.";

    /** Caption for message with game map which client is requested to download. */
    public static final String CAPTION_DOWNLOAD_GAME_MAP = "Game map download.";

    /** Caption for message with status of game: is it started or not. */
    public static final String CAPTION_GAME_STATUS = "Game status info.";

    /** Caption for message with list of availiable game maps. */
    public static final String CAPTION_GAME_MAPS_LIST = "Game maps list.";

    /**
     * Caption for message with main info game. Usually it is info about
     * number of max and current players in game, their names and so on.
     */
    public static final String CAPTION_GAME_INFO = "Game info.";

    /** Caption for message with result of "join bot" request. */
    public static final String CAPTION_JOIN_BOT_RESULT = "Join bot.";

    /** Caption for message with result of "remove bomb" request. */
    public static final String CAPTION_REMOVE_BOT_RESULT = "Remove bot info.";

    /** Caption for message with result of "add message to chat" request. */
    public static final String CAPTION_ADD_CHAT_MSG_RESULT = "Send chat messages.";

    /** Caption for message with new chat messages. */
    public static final String CAPTION_NEW_CHAT_MSGS = "Get chat messages.";

    /** Caption for message with players stats. Usually it is deaths-kills and so on. */
    public static final String CAPTION_GAME_PLAYERS_STATS = "Game players stats.";

    /** Caption for message with players result. It is sended when game ends. */
    public static final String CAPTION_GAME_END_RESULTS = "Game ended players stats.";

    /** Caption for message with result of "set client name" request. */
    public static final String CAPTION_SET_CLIENT_NAME = "Set client name info.";

    /** Message for client about kick from game. */
    public static final String MESSAGE_GAME_KICK = "You were kicked from the game.";

    /** Message for client about start of game. */
    public static final String MESSAGE_GAME_START = "Game started.";

    /** Notification for client to update game field. */
    public static final String UPDATE_GAME_MAP = "Update game map.";

    /** Notification for client to update list of games. */
    public static final String UPDATE_GAMES_LIST = "Update games list.";

    /** Notification for client to update game info(about number of players and so on). */
    public static final String UPDATE_GAME_INFO = "Update game info.";

    /** Notification for client to update chat messages. */
    public static final String UPDATE_CHAT_MSGS = "Update chat messages.";

    /** Split symbol that must be used in protocol between args. */
    public static final String SPLIT_SYMBOL = "/";

    private ProtocolConstants() {}

}