package org.amse.bomberman.server.net.netty;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.amse.bomberman.protocol.InvalidDataException;
import org.amse.bomberman.protocol.ProtocolConstants;
import org.amse.bomberman.protocol.ProtocolMessage;
import org.amse.bomberman.server.ServiceContext;
import org.amse.bomberman.server.net.Server;
import org.amse.bomberman.server.net.Session;
import org.amse.bomberman.server.net.SessionEndListener;
import org.amse.bomberman.server.net.tcpimpl.sessions.asynchro.controllers.Controller;
import org.amse.bomberman.server.net.tcpimpl.sessions.control.RequestCommand;
import org.amse.bomberman.server.protocol.ResponseCreator;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Kirilchuk V.E.
 */
class SessionHandler extends SimpleChannelUpstreamHandler implements Session {
    private static final Logger LOG = LoggerFactory.getLogger(SessionHandler.class.getName());

    private final Server server;
    private final Set<Session> sessions;

    private final ResponseCreator protocol = new ResponseCreator();

    private Channel connection;
    
    private final List<SessionEndListener> listeners
            = new CopyOnWriteArrayList<SessionEndListener>();

    private Controller controller;

    public SessionHandler(Server server, Set<Session> sessions) {
        this.server = server;
        this.sessions = sessions;
    }

    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        sessions.add(this);
        addEndListener(server);
        connection = e.getChannel();
        start();
        super.channelConnected(ctx, e);
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        if (!(e.getMessage() instanceof ProtocolMessage)) {//TODO server must disconnect such client not throw exception!
            throw new RuntimeException("Wrong messagew type.");
        }
        ProtocolMessage message = (ProtocolMessage) e.getMessage();
        RequestCommand cmd = null;
        try {
            int commandId = message.getMessageId();
            cmd = RequestCommand.valueOf(commandId); // throws IllegalArgumentException
            cmd.execute(controller, message.getData());
        } catch (IllegalArgumentException ex) {
            send(protocol.notOk(ProtocolConstants.INVALID_REQUEST_MESSAGE_ID,
                    "Not supported command."));
            LOG.warn("Session: answerOnCommand error. "
                    + "Non supported command int from client.", ex);
        } catch (InvalidDataException ex) {
            send(protocol.notOk(ProtocolConstants.INVALID_REQUEST_MESSAGE_ID,
                    ex.getMessage()));
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        LOG.error("Unexpected exception from downStream", e.getCause());
        e.getChannel().close().awaitUninterruptibly();
        for (SessionEndListener listener : listeners) {
            listener.sessionTerminated(this);
        }
        super.exceptionCaught(ctx, e);
    }

    @Override
    public void terminateSession() {
        connection.close();
    }

    @Override
    public void start() throws IOException {
        controller = new Controller(this, protocol);
        listeners.add(controller);
        server.getServiceContext().getGameStorage().addListener(controller);
    }

    @Override
    public void send(ProtocolMessage message) {
        connection.write(message);
    }

    @Override
    public ServiceContext getServiceContext() {
        return server.getServiceContext();
    }

    @Override
    public long getId() {//TODO is it need?
        return 100500;
    }

//    @Override
//    public boolean isMustEnd() {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }

    @Override
    public void addEndListener(SessionEndListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeEndListener(SessionEndListener listener) {
        listeners.remove(listener);
    }
}
