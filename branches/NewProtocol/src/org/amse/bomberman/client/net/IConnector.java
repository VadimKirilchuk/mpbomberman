/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amse.bomberman.client.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.amse.bomberman.protocol.ProtocolMessage;

/**
 * @author Mikhail Korovkin
 * @author Kirilchuk V.E
 */
public interface IConnector {

    void сonnect(InetAddress address, int port) throws UnknownHostException,
                                                       IOException;

    void disconnect();

    void sendRequest(ProtocolMessage<Integer, String> message) throws
            NetException;

}
