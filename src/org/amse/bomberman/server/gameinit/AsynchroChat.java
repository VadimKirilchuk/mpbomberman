/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amse.bomberman.server.gameinit;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.amse.bomberman.util.ProtocolConstants;
import org.amse.bomberman.util.interpretator.*;

/**
 * Class that represents lobby chat. And ingame kill info.
 * @author Kirilchuk V.E.
 */
public class AsynchroChat {

    private final Game game;
    private final Map<Variable, Constant> vars = new HashMap<Variable, Constant>();

    public AsynchroChat(Game game) {
        this.game = game;
    }

    /**
     * Adding message to chat in next notation:
     * <p>
     * playerName: message.
     * @param playerName nickName of player that added message.
     * @param message message to add to chat.
     */
    public void addMessage(String playerName, String message) {
        addMessage(playerName + ": " + message);
        /////////////
        superMindAnswer(message);
        /////////////
    }

    public void addMessage(String message) {
        List<String> forClients = new ArrayList<String>();
        forClients.add(ProtocolConstants.CAPTION_GET_CHAT_MSGS);
        forClients.add(message);
        this.game.notifyGameSessions(forClients);
    }

    private void superMindAnswer(String text) {
        String idName = null;

        StringBuilder res = new StringBuilder();

        //Trying to find Identificator = Expression
        int eqPos = text.indexOf('=');
        String buff;
        try {

            if (eqPos != -1) {
                String in = text.substring(0, eqPos);

                //Trying to get left side identificator name.
                idName = getIdName(in);

                //In the right side must be Expression.
                text = text.substring(eqPos + 1, text.length());
            }

            //Generating expression from text.
            Expression expression = ExprBuilder.generate(text);

            //Evaluating expression. Can throw Arithmetic exception or ParseException
            Double value = expression.evaluate(vars);
            buff = value.toString();

            if (idName != null) {
                //If it was Identificator = Expression then remembering value of identificator.
                vars.put(new Variable(idName), new Constant(value));

                //Adding identificator name in the beggining of result
                res.append(idName + " := ");
            } else {//if it was just Expression
                //Adding text then equals and then value of expression.
                res.append(text + " = ");
            }

            res.append(buff);

            // adding to chat
            addMessage("SuperMind: " + res.toString());
        } catch (Exception ex) {
            //TODO
        }
    }

    /**
     * Always returns "No new messages."
     * @param chatID id of player that wants to get new messages.
     * @return list of messages or list with only item - "No new messages."
     */
    public List<String> getNewMessages(int chatID) {
        List<String> result = new ArrayList<String>();

        result.add("No new messages.");

        return result;
    }

    private static String getIdName(String in) throws ParseException {
        LexAnalyzer la = new LexAnalyzer(in);
        //Должно быть две лексемы
        //1)Identificator 2)EOTEXT
        Lexema lex = la.nextLex();
        if (lex.type != Lexema.Type.IDENT) {
            throw new ParseException("Wrong Identificator in left side", 0);
        } else {
            String name = ((IdLexema) lex).name;
            lex = la.nextLex();
            if (lex != Lexema.EOTEXT) {
                throw new ParseException("Wrong Identificator in left side", 0);
            } else {
                return name;
            }
        }

    }
}
