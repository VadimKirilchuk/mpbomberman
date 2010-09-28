package org.amse.bomberman.protocol;

import java.util.List;
import org.amse.bomberman.FakeAsynchroClient;
import org.amse.bomberman.server.net.tcpimpl.servers.TcpServer;
import org.amse.bomberman.util.Constants.Direction;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Kirilchuk V.E.
 */
public class RemoveBotTest {

    static TcpServer server;
    static FakeAsynchroClient client;
    static RequestCreator requestCreator = new RequestCreator();

    @BeforeClass
    public static void setUpClass() throws Exception {
        server = new TcpServer(65535);
        server.start();
        client = new FakeAsynchroClient(server.getPort());
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        Utilities.disconnectClientAndCloseAll(server, client);
    }

    @Test
    public void removeBotNotJoinedTest() throws Exception {
        ProtocolMessage<Integer, String> request = requestCreator.requestRemoveBotFromGame();
        client.sendRequest(request);

        ProtocolMessage<Integer, String> response = client.receiveResult();
        assertEquals(ProtocolConstants.INVALID_REQUEST_MESSAGE_ID, (int) response.getMessageId());

        List<String> data = response.getData();
        assertEquals(1, data.size());
        String message = response.getData().get(0);
        assertEquals("Can`t remove bot in 'Not Joined' state.", message);
    }
}
