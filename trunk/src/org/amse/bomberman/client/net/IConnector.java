package org.amse.bomberman.client.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import org.amse.bomberman.client.model.BombMap;
import org.amse.bomberman.util.Constants.Direction;

/**
 *
 * @author michail korovkin
 */
public interface IConnector {

     public boolean joinBotIntoGame(int gameNumber) throws IOException;
     public void сonnect(InetAddress address, int port)
             throws UnknownHostException, IOException;
     public void leaveGame();
     public ArrayList<String> takeGamesList();
     public boolean createGame(String gameName, String mapName, int maxPl) throws IOException;
     public boolean joinGame(int gameID) throws IOException;
     public boolean doMove(Direction dir);
     public void startGame();
     public BombMap getMap();
     public void plantBomb();
     // must be here???
     public void beginUpdating();
     public InetAddress getInetAddress();
     public int getPort();
     public String[] getMaps();
     public boolean isStarted() throws IOException;
}