/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.yuana.mojo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.yuana.ircbot.PircBot;

/**
 *
 * @author Kester Everts
 */
public class ConsoleThread extends Thread {
    private final Umbrella umbrella;

    public ConsoleThread(Umbrella umbrella) {
        this.setDaemon(true);
        this.umbrella = umbrella;
    }

    @Override
    public void run() {
         BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        while(true) {
            try {
                String command = in.readLine();
                StringTokenizer tokenizer = new StringTokenizer(command);
                if(tokenizer.countTokens() < 2) {
                    continue;
                }
                String ident = tokenizer.nextToken();
                MojoBot bot = null;
                for(String i : umbrella.getIdentifiers()) {
                    if(ident.equals(i)) {
                        bot = umbrella.getBot(ident);
                        break;
                    }
                }
                if(bot == null) {
                    System.out.println("Error: identifier not found.");
                    continue;
                }
                bot.sendRawLine(tokenizer.nextToken("\n"));
                System.out.println("Command sent");
            } catch (IOException ex) {
                Logger.getLogger(ConsoleThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
