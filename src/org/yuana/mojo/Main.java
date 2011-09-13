/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.yuana.mojo;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.yuana.ircbot.IrcException;

/**
 *
 * @author Kester Everts
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, IrcException, IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException {
        if(args.length == 0) {
            System.out.println("Please specify the configuration folder as an argument.");
            System.exit(1);
        }
        Umbrella umbrella = new Umbrella(args[0]);
        umbrella.start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }

        new ConsoleThread(umbrella).start();
    }
}
