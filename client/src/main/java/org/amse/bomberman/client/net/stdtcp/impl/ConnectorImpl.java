package org.amse.bomberman.client.net.stdtcp.impl;

import java.io.*;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import org.amse.bomberman.client.net.ConnectorListener;
import org.amse.bomberman.client.net.GenericConnector;
import org.amse.bomberman.client.net.NetException;
import org.amse.bomberman.protocol.ProtocolConstants;
import org.amse.bomberman.protocol.ProtocolMessage;
import org.amse.bomberman.util.IOUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Realization of {@link GenericConnector} interface that uses
 * standart java tcp sockets. This connector is asynchronous, it means that
 * it can send and receive data separately. However send and receive use blocking io.
 *
 * @author Mikhail Korovkin
 * @author Kirilchuk V.E.
 */
public class ConnectorImpl implements GenericConnector<ProtocolMessage> {

    private final static Logger LOG = LoggerFactory.getLogger(ConnectorImpl.class);
    
    private ConnectorListener listener;
    private Socket socket;
    private Thread inputThread;
    private DataOutputStream out = null;
    private DataInputStream in = null;

    @Override
    public void setListener(ConnectorListener listener) {
        this.listener = listener;
    }

    @Override
    public synchronized void сonnect(InetAddress address, int port) throws ConnectException {
        try {
            socket = new Socket(address, port);
            out = initOut();
            in  = initIn();

            inputThread = new Thread(new ServerListen());
            inputThread.setDaemon(true);
            inputThread.start();
        } catch (IOException ex) {
            //rollback if for example socket connected, but streams init failed.
            if (socket != null && !socket.isClosed()) {
                try {
                    socket.close();
                } catch (IOException e) {
                    //TODO CLIENT log this
                    ; //ignore cause can do nothing.
                }
            }
            out    = null;
            in     = null;
            throw new ConnectException(ex.getMessage());
        }
    }

    private DataOutputStream initOut() throws IOException {
        OutputStream os = socket.getOutputStream();
        return new DataOutputStream(new BufferedOutputStream(os));
    }

    private DataInputStream initIn() throws IOException {
        InputStream is = socket.getInputStream();
        return new DataInputStream(new BufferedInputStream(is));
    }

    public synchronized boolean isClosed() {
        if (socket == null || socket.isClosed() || out == null || in == null ) {
            return true;
        }

        return false;
    }

    @Override
    public synchronized void closeConnection() {
        if (isClosed()) {
            return;
        }
        try {
            if (inputThread != null) {
                inputThread.interrupt();
                if (socket != null && !socket.isInputShutdown()) {
                    socket.shutdownInput();
                }
            }
            IOUtilities.close(out);
            IOUtilities.close(in);
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException ex) {
            LOG.error("Session: terminating error. IOException "
                    + "while closing resourses.", ex);
        }
    }

    @Override
    public synchronized void send(ProtocolMessage request) throws NetException {
        if(isClosed()) {
            throw new NetException("Can`t send any data. Connection is closed");
        }
        try {
            List<String> data = request.getData();
            if (data == null) {
                throw new IllegalArgumentException("Data can`t be null.");
            }
            int size = data.size();

            out.writeInt(request.getMessageId());
            out.writeInt(size);
            for (String string : data) {
                if (string == null) {
                    throw new IllegalArgumentException("Strings in data can`t be null.");
                }
                out.writeUTF(string);
            }
            //
            out.flush();
        } catch (IOException ex) {
            LOG.error("AsynchroConnector: sendRequest error.", ex);
            closeConnection();
            throw new NetException();
        }
    }

    private class ServerListen implements Runnable {

        @Override
        public void run() {
            try {
                while (!Thread.interrupted()) {
                    try {
                        ProtocolMessage message = new ProtocolMessage();
                        int messageId = in.readInt();
                        if (messageId == ProtocolConstants.DISCONNECT_MESSAGE_ID) {
                            break;
                        }

                        message.setMessageId(messageId);

                        int size = in.readInt();
                        List<String> data = new ArrayList<String>(size);
                        for (int i = 0; i < size; i++) {
                            data.add(in.readUTF());
                        }
                        message.setData(data);

                        listener.received(message);
                    } catch (EOFException ex) {
                        /* Ignore cause server always send DISCONNECT_MESSAGE before
                        end of stream. (Except the collapse)
                        But this catch used to not fall into IOException
                        when application is closing and we are closing connection. */
                    }
                }
            } catch (IOException ex) {                
                LOG.error("ServerListen: run error.", ex);
            } 
            
            closeConnection();
            LOG.info("ServerListen: run ended.");
        }
    }
}
