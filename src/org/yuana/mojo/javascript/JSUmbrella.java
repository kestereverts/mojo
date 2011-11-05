/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.yuana.mojo.javascript;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mozilla.javascript.ClassShutter;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;
import org.yuana.mojo.HTTPClient;
import org.yuana.mojo.MojoBot;
import org.yuana.mojo.Umbrella;

/**
 *
 * @author Kester Everts
 */
public class JSUmbrella extends ScriptableObject {

    private Umbrella umbrella;
    private final HashMap<String, ArrayList<Function>> listeners = new HashMap<String, ArrayList<Function>>();

    @Override
    public String getClassName() {
        return "Umbrella";
    }

    public void setUmbrella(Umbrella umbrella) {
        this.umbrella = umbrella;
    }

    public Umbrella getUmbrella() {
        return this.umbrella;
    }

    public JSBot jsFunction_createBot(String identifier) {
        if (identifier == null || identifier.isEmpty()) {
            throw Context.reportRuntimeError("identifier is invalid");
        }
        MojoBot bot = umbrella.createBot(identifier);
        return umbrella.createJSBotInstance(bot);
    }

    public boolean jsFunction_removeBot(String identifier) {
        return umbrella.removeBot(identifier);
    }

    public void jsFunction_print(String text) {
        System.out.println(text);
    }

    public String jsFunction_getUrl(String url) {
        try {
            return HTTPClient.sendGet(url);
        } catch (MalformedURLException ex) {
            Logger.getLogger(JSUmbrella.class.getName()).log(Level.SEVERE, null, ex);
            throw Context.throwAsScriptRuntimeEx(ex);
        } catch (IOException ex) {
            Logger.getLogger(JSUmbrella.class.getName()).log(Level.SEVERE, null, ex);
            throw Context.throwAsScriptRuntimeEx(ex);
        }
    }

    public String jsFunction_postUrl(String url, String postData) {
        try {
            return HTTPClient.sendPost(url, postData);
        } catch (MalformedURLException ex) {
            Logger.getLogger(JSUmbrella.class.getName()).log(Level.SEVERE, null, ex);
            throw Context.throwAsScriptRuntimeEx(ex);
        } catch (IOException ex) {
            Logger.getLogger(JSUmbrella.class.getName()).log(Level.SEVERE, null, ex);
            throw Context.throwAsScriptRuntimeEx(ex);
        }
    }
    
    public Object result;

    public Object jsFunction_evalWithCoolSemantics(final String message, final Scriptable scope, final JSBot bot) {
        Thread executor = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    Context cx = Context.enter();
                    cx.setLanguageVersion(Context.VERSION_1_8);
                    //Scriptable scope = channel;
                    ScriptableObject.defineProperty(JSUmbrella.this.umbrella.getStandardScope(), "bot", bot, 0);
                    cx.setClassShutter(new ClassShutter() {

                        @Override
                        public boolean visibleToScripts(String fullClassName) {
                            return false;
                        }
                    });
                    result = cx.evaluateString(scope, message, "evalWithCoolSemantics", 1, null);
                } catch (Throwable t) {
                    Logger.getLogger(MojoBot.class.getName()).log(Level.SEVERE, null, t);
                    String error = t.getMessage();
                    for (String line : error.split("\n")) {
                        bot.getBot().sendNotice("IJzerenRita", line);
                    }
                } finally {
                    ScriptableObject.deleteProperty(umbrella.getStandardScope(), "bot");
                    Context.exit();
                }
            }
        });
        try {
            //CommandContext.initialize(channel.jsGet_name(), CommandContext.get().getSender(), CommandContext.get().getLogin(), CommandContext.get().getHostname(), message, bot.getBot(), executor);
            executor.start();
            executor.join();
        } catch (InterruptedException ex) {
            Logger.getLogger(MojoBot.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            //CommandContext.destroy(executor);
        }

        return result;
    }

    public Object jsFunction_system(String command) {
        try {
            Runtime run = Runtime.getRuntime();
            Process pr = run.exec(command);
            pr.waitFor();
            BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
            StringBuilder builder = new StringBuilder();
            String line = "";
            while ((line = buf.readLine()) != null) {
                builder.append(line).append("\n");
            }
            return builder.toString();

        } catch (InterruptedException ex) {
            Logger.getLogger(MojoBot.class.getName()).log(Level.SEVERE, null, ex);
            return Undefined.instance;
        } catch (IOException ex) {
            Logger.getLogger(MojoBot.class.getName()).log(Level.SEVERE, null, ex);
            return Undefined.instance;
        }

    }

    public void addEventListenerWithoutFile(String eventName, Scriptable listener) {
        if (listener instanceof Function) {
            synchronized (listeners) {

                ArrayList<Function> listenerList = listeners.get(eventName);
                if (listenerList == null) {
                    System.out.println("listenerList is null, creating a list now");
                    listenerList = new ArrayList<Function>();
                    listeners.put(eventName, listenerList);
                } else if (listenerList.contains(listener)) {
                    System.out.println("Listener already exists");
                    return;
                }
                listenerList.add((Function) listener);

                System.out.println("Listener has been added");

            }
        } else {
            System.out.println("Listener is not a function");
        }
    }
    
    public void jsFunction_load(String fileName) {
        fileName = umbrella.getConfigFolder() + "/" + fileName.replace("\\.\\.", "");
        
        FileReader fileReader;
        try {
            Context cx = Context.enter();
            cx.setOptimizationLevel(-1);
            fileReader = new FileReader(fileName);
            cx.evaluateReader(umbrella.getStandardScope(), fileReader, fileName, 1, null);
            fileReader.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MojoBot.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MojoBot.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            Context.exit();
        }
    }
}
