package org.amse.bomberman.client.view.mywizard;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import org.amse.bomberman.client.model.IModel;
import org.amse.bomberman.client.model.impl.Model;
import org.amse.bomberman.client.net.NetException;
import org.amse.bomberman.client.control.impl.Controller;
import org.amse.bomberman.client.view.gamejframe.GameJFrame;
import org.amse.bomberman.util.Constants;

/**
 *
 * @author Michael Korovkin
 */
public class Panel3old extends JPanel implements Updating{
    private int playersNum = 1;
    private MyWizard parent;
    private JLabel[] players = new JLabel[Constants.MAX_PLAYERS];
    private JButton botJButton = new JButton();
    private JButton chatJButton = new JButton();
    private JTextArea chatTA = new JTextArea();
    private JTextField messageTF = new JTextField();
    private final int width = 640;
    private final int height = 480;
    private final Dimension defaultButton = new Dimension(100,20);
    private final String emptyName = "EMPTY";
    private final String closedName = "Closed";

    public Panel3old(MyWizard jframe) {
        parent = jframe;
        this.setSize(width, height);
        initComponents();
        setVisible(true);
    }
    public void setPlayersNum(int number) {
        playersNum = number;
    }
    public void doBeforeShow() {
        try {
            chatTA.setText("");
            Controller.getInstance().requestGameInfo();
        } catch (NetException ex) {
            JOptionPane.showMessageDialog(this, "Connection was lost.\n"
                    + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            parent.setCurrentJPanel(0);
        }
    }
    public void startGame() throws NetException {
        parent.dispose();
        IModel model = Model.getInstance();
        Controller.getInstance().setReceiveInfoListener((RequestResultListener) Model.getInstance());
        GameJFrame jframe = new GameJFrame();
        Model.getInstance().addListener(jframe);
        Controller.getInstance().requestGameMap();
    }
    public void setGameInfo(List<String> info) {
        if (info.get(0).equals("false")) {
            botJButton.setEnabled(false);
            botJButton.setVisible(false);
            parent.setNextButtonEnable(false);
        } else {
            botJButton.setAction(new AddBotAction());
        }
        playersNum = Integer.parseInt(info.get(1));
        if (Integer.parseInt(info.get(2)) > 0) {
            for (int i = 0; i < Integer.parseInt(info.get(2)); i++) {
                this.setPlayer(i, info.get(i + 3));
            }
        }
        for (int i = Integer.parseInt(info.get(2)); i < playersNum; i++) {
            this.setPlayer(i, emptyName);
        }
    }
    public void setNewMessages(List<String> messages) {
        if (!messages.get(0).equals("No new messages.")) {
            for (String message: messages) {
                chatTA.append(message + "\n");
            }
        }
    }
    private void setPlayer(int id, String name) {
        players[id].setText(name);
        LineBorder border = null;
        Color color = null;
        if (name.equals(emptyName)) {
            border = new LineBorder(Color.GRAY);
            color = Color.GRAY;
            players[id].setVisible(true);
        } else {
            if (name.equals(closedName)) {
                border = new LineBorder(Color.LIGHT_GRAY);
                color = Color.LIGHT_GRAY;
                players[id].setVisible(false);
            } else {
                border = new LineBorder(Color.BLACK);
                color = Color.BLACK;
                players[id].setVisible(true);
            }
        }
        players[id].setBorder(border);
        players[id].setForeground(color);
    }
    private String getMessage() {
        String message = messageTF.getText();
        messageTF.setText("");
        return message;
    }
    private void initComponents() {
        for (int i = 0; i < Constants.MAX_PLAYERS; i++) {
            players[i] = new JLabel();
            players[i].setHorizontalAlignment(SwingConstants.CENTER);
            this.setPlayer(i, closedName);
        }
        messageTF.addKeyListener(new KeyListener() {
            public void keyTyped(KeyEvent e) {
            }
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_ENTER: {
                        try {
                            String message = getMessage();
                            if (message.length() > 0) {
                                Controller.getInstance().requestSendChatMessage(message);
                            }
                        } catch (NetException ex) {
                            // is it good?
                            ex.printStackTrace();
                            parent.setCurrentJPanel(0);
                        }
                    }
                }
            }
            public void keyReleased(KeyEvent e) {
            }
        });
        botJButton.setPreferredSize(defaultButton);

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new GridLayout(Constants.MAX_PLAYERS, 1, 10, 10));
        leftPanel.setPreferredSize(new Dimension(100, height - 100));
        for (int i = 0; i < Constants.MAX_PLAYERS; i++) {
            leftPanel.add(players[i]);
        }
        JPanel rightPanel = new JPanel(new FlowLayout());
        rightPanel.setPreferredSize(new Dimension(250, height - 100));
        JPanel chatPanel = new JPanel(new GridLayout());
        chatPanel.setPreferredSize(new Dimension(240, height - 150));
        chatPanel.add(new JScrollPane(chatTA));
        chatTA.setEditable(false);
        chatTA.setLineWrap(true);
        messageTF.setPreferredSize(new Dimension(150, 25));
        chatJButton.setPreferredSize(new Dimension(85, 25));
        chatJButton.setAction(new ChatAction());
        rightPanel.add(chatPanel);
        rightPanel.add(messageTF);
        rightPanel.add(chatJButton);

        Box bottomBox = Box.createHorizontalBox();
        // how calculate sizes???
        bottomBox.setPreferredSize(new Dimension(width, 25));
        bottomBox.add(Box.createHorizontalGlue());
        bottomBox.add(botJButton);
        bottomBox.add(Box.createHorizontalGlue());

        this.setLayout(new FlowLayout());
        this.add(leftPanel);
        this.add(rightPanel);
        this.add(bottomBox);
    }
    private class AddBotAction extends AbstractAction {
        public AddBotAction() {
            putValue(NAME, "Add Bot");
            putValue(SHORT_DESCRIPTION, "Add one bot to this game");
            putValue(SMALL_ICON, null);
        }

        public void actionPerformed(ActionEvent e) {
            try {
                Controller.getInstance().requestJoinBotIntoGame();
            } catch (NetException ex) {
                JOptionPane.showMessageDialog(parent, "Connection was lost.\n"
                        + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                parent.setCurrentJPanel(0);
            } 
        }
    }
    private class ChatAction extends AbstractAction{
        public ChatAction(){
            putValue(NAME, "Send");
            putValue(SMALL_ICON, null);
        }
        public void actionPerformed(ActionEvent e) {
            try {
                String message = getMessage();
                if (message.length() > 0) {
                    Controller.getInstance().requestSendChatMessage(message);
                }
            } catch (NetException ex) {
                JOptionPane.showMessageDialog(parent, "Connection was lost.\n"
                        + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                parent.setCurrentJPanel(0);
            }
        }
    }
    
}