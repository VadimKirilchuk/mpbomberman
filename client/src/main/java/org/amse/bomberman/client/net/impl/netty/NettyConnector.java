package org.amse.bomberman.client.net.impl.netty;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import org.amse.bomberman.client.net.ConnectorListener;
import org.amse.bomberman.client.net.GenericConnector;
import org.amse.bomberman.protocol.ProtocolMessage;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.socket.ClientSocketChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Kirilchuk V.E.
 */
public class NettyConnector implements GenericConnector<ProtocolMessage>, ClientHandlerListener {
    private static Logger LOG = LoggerFactory.getLogger(NettyConnector.class);

    private final ClientSocketChannelFactory factory;

    private ClientBootstrap bootstrap;
    private Channel connection;
    private ConnectorListener listener;

    public NettyConnector(ClientSocketChannelFactory factory) {
        this.factory = factory;
    }

    /**
     *
     * @param host
     * @param port
     * @throws UnknownHostException
     * @throws IOException
     */
    @Override
    public void сonnect(InetAddress host, int port) throws ConnectException {
        if(connection != null && connection.isConnected()) {
            throw new IllegalStateException("Already connected. Disconnect first.");
        }

        initBootstrap(factory);

        // Trying to connect
        ChannelFuture connect = bootstrap.connect(new InetSocketAddress(host, port));
        connect.awaitUninterruptibly();

        if(!connect.isSuccess()) {
            LOG.info("Client failed to connect to " + host + ":" + port);            
            throw new ConnectException(connect.getCause().getMessage());
        } else {
            connection = connect.getChannel();            
        }
        LOG.info("Connection established with ." + host + ":" + port);
    }

    @Override
    public void send(ProtocolMessage message) {
        if(connection == null || !connection.isConnected()) {
            throw new IllegalStateException("You are not connected");
        }
        connection.write(message);
        LOG.info("Sended message to channel asynchronously.");
    }

    @Override
    public void closeConnection() {
        if(connection == null) {
            return;
        }
        
        if(connection.isOpen()) {
            connection.close().awaitUninterruptibly();
        }
        LOG.info("Connection closed.");
    }
    
    private void initBootstrap(ClientSocketChannelFactory chanelFactory) {
        //creating bootstrap
        bootstrap = new ClientBootstrap();
        bootstrap.setFactory(chanelFactory);
        bootstrap.setPipelineFactory(new ClientPipelineFactory(this));
    }

    @Override
    public void setListener(ConnectorListener listener) {
        this.listener = listener;
    }

    @Override
    public void received(ProtocolMessage message) {
        if(listener != null) {
            listener.received(message);
        }
    }
}