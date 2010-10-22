package org.amse.bomberman.client.net.impl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;
import org.amse.bomberman.client.net.Connector;
import org.amse.bomberman.client.net.NetException;
import org.amse.bomberman.client.control.impl.ControllerImpl;
import org.amse.bomberman.protocol.ProtocolConstants;
import org.amse.bomberman.protocol.ProtocolMessage;
import org.amse.bomberman.util.IOUtilities;

/**
 *
 * @author Mikhail Korovkin
 * @author Kirilchuk V.E.
 */
public class AsynchroConnector implements Connector {

    private static Connector connector = null;
    private Socket socket;
    private Thread inputThread;
    private DataOutputStream out = null;
    private DataInputStream in = null;

    private AsynchroConnector() {
    }

    public static Connector getInstance() {
        if (connector == null) {
            connector = new AsynchroConnector();
        }
        return connector;
    }

    public synchronized void сonnect(InetAddress address, int port) throws
            UnknownHostException, IOException, IllegalArgumentException {

        this.socket = new Socket(address, port);
        out = initOut();
        in = initIn();

        inputThread = new Thread(new ServerListen());
        inputThread.setDaemon(true);
        inputThread.start();
    }

    private DataOutputStream initOut() throws IOException {
        OutputStream os = this.socket.getOutputStream();
        return new DataOutputStream(new BufferedOutputStream(os));
    }

    private DataInputStream initIn() throws IOException {
        InputStream is = socket.getInputStream();
        return new DataInputStream(new BufferedInputStream(is));
    }

    public synchronized void closeConnection() {
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
            System.err.println("Session: terminating error. IOException "
                    + "while closing resourses. " + ex.getMessage());
        }
    }

    public synchronized void sendRequest(ProtocolMessage<Integer, String> request) throws NetException {
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
            System.err.println("AsynchroConnector: sendRequest error." + ex.getMessage());
            throw new NetException();
        }
    }

    private class ServerListen implements Runnable {

        public void run() {
            try {
                while (!Thread.interrupted()) {
                    try {
                        ProtocolMessage<Integer, String> message
                                = new ProtocolMessage<Integer, String>();
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

                        SwingUtilities.invokeLater(new InvokationCommand(message));
                    } catch (EOFException ex) {
                        /* Ignore cause server always send DISCONNECT_MESSAGE before
                        end of stream. (Except the collapse)
                        But this catch used to not fall into IOException
                        when application is closing and we are closing connection. */
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                System.err.println("ServerListen: run error. " + ex.getMessage());
            }
            
            System.out.println("ServerListen: run ended.");
        }

        private class InvokationCommand implements Runnable {

            ProtocolMessage<Integer, String> message;

            public InvokationCommand(ProtocolMessage<Integer, String> message) {
                this.message = message;
            }

            public void run() {
                ControllerImpl.getInstance().receivedResponse(message);
            }
        }
    }
}