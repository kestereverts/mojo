/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.yuana.mojo.javascript;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mozilla.javascript.Callable;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.yuana.ircbot.Channel;
import org.yuana.ircbot.PircBot;
import org.yuana.mojo.CommandContext;
import org.yuana.mojo.MojoBot;

/**
 *
 * @author Kester Everts
 */
public class JSChannel extends ScriptableObject {

    private MojoBot bot;
    private org.yuana.ircbot.Channel channel;
    private JSChannelUserList users;
    private static Scriptable scope;
    private static final String JS_CLASS_NAME = "Channel";
    private final HashMap<String, ArrayList<Function>> listeners = new HashMap<String, ArrayList<Function>>();

    public static void establishScope(Scriptable standardScope) {
        if (scope != null) {
            return;
        }
        try {
            Context cx = Context.enter();
            scope = standardScope;
            ScriptableObject.defineClass(scope, JSChannel.class);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(JSChannel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(JSChannel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex) {
            Logger.getLogger(JSChannel.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            Context.exit();
        }
    }
    
    public void jsConstructor(Object bot, Object channel, JSChannelUserList users) {
    }

    public static JSChannel getJSInstance(Scriptable standardScope, MojoBot bot, Channel channel) throws IllegalAccessException, InstantiationException, InvocationTargetException {
        try {
            Context cx = Context.enter();
            //establishScope(standardScope);
            JSChannel instance = (JSChannel) cx.newObject(scope, JS_CLASS_NAME);
            instance.bot = bot;
            instance.channel = channel;
            instance.users = new JSChannelUserList(standardScope, bot, channel);
            return instance;
        } finally {
            Context.exit();
        }
    }

    @Override
    public String getClassName() {
        return JS_CLASS_NAME;
    }

    @Override
    public String getTypeOf() {
        return "channel";
    }

    @Override
    public Object call(Context cx, Scriptable scope, Scriptable thisObj,
                       Object[] args)
    {
        return ((Callable) this.get("__call__", this)).call(cx, scope, thisObj, args);
    }

    public String jsGet_name() {
        return channel.getChannelName();
    }

    public void jsFunction_say(String say) {
        bot.sendMessage(channel.getChannelName(), say);
    }

    public JSChannelUserList jsGet_users() {
        return users;
    }

    public void jsFunction_mode(String modeString) {
        bot.setMode(channel.getChannelName(), modeString);
    }

    public void jsFunction_who() {
        bot.sendRawLineViaQueue("WHO " + channel.getChannelName());
        //bot.sendMessage(channel.getChannelName(), "Resynching.");
    }

    public String jsGet_topic() {
        return channel.getTopic();
    }

    public void jsSet_topic(String topic) {
        bot.setTopic(channel.getChannelName(), topic);
    }

    public String jsGet_topicSetBy() {
        return channel.getTopicSetBy();
    }

    public Date jsGet_topicSetAt() {
        return channel.getTopicSetAt();
    }

    public int jsFunction_getAttributeValue(String property) {
        return getAttributes(property);
    }



    public void jsFunction_addEventListener(String eventName, Scriptable listener) {
        if (listener instanceof Function) {
            synchronized (listeners) {
                try {
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
                    String path = bot.getConfigFolder() + "/events/" + channel.getChannelName().substring(1).replace("\\.\\.|\\|/", "") + "/" + eventName.replace("\\.\\.|\\|/", "");
                    File file;
                    do {
                        file = new File(path + "/" + Double.toString(Math.random()).replace(".", "").substring(1) + ".js");
                    } while (file.exists());
                    ScriptableObject.defineProperty(listener, "fileName", file.getPath(), ScriptableObject.DONTENUM | ScriptableObject.READONLY);
                    new File(path).mkdirs();
                    file.createNewFile();
                    //FileWriter writer = new FileWriter(file);
                    BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
                    String function = "(\n" + Context.toString(listener).trim().replaceAll("^function [^(]+\\(", "function(") + "\n)";
                    byte[] functionBytes = function.getBytes("UTF-8");
                    out.write(functionBytes, 0, functionBytes.length);
                    out.close();
                    System.out.println("Listener has been added");
                } catch (IOException ex) {
                    Logger.getLogger(JSChannel.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } else {
            System.out.println("Listener is not a function");
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

    public void jsFunction_removeEventListener(String eventName, Scriptable listener) {
        synchronized (listeners) {
            ArrayList<Function> listenerList = listeners.get(eventName);
            if (listenerList == null) {
                System.out.println("listenerList is null, not removing");
                return;
            }
            boolean result;
            if (listener instanceof Function) {
                result = listenerList.remove(listener);
            } else {
                int index = (int) Context.toNumber(listener);
                if (index >= 0 && index < listenerList.size()) {
                    listener = listenerList.remove(index);
                    result = true;
                } else {
                    result = false;
                }
            }
            if (result) {
                File file = new File(Context.toString( Context.toString(listener.get("fileName", listener))));
                System.out.println("File path: " + file.getAbsolutePath());
                boolean deleted = file.delete();
                System.out.println("Deleted: " + deleted + " " + file.exists());
                System.out.println("Listener removed");
            } else {
                System.out.println("Listener doesn't exist, not removing");
            }
        }
    }

    public void removeAllListeners() {
        listeners.clear();
    }

    public static void jsFunction_fireEvent(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
        if (!(thisObj instanceof JSChannel)) {
            System.out.println("fireEvent: not instance of JSChannel");
            return;
        }
        if (args.length == 0) {
            CommandContext context = CommandContext.get();
            MojoBot bot = context.getBot();
            bot.sendMessage(context.getChannel(), "Invalid amount of args");
            return;
        }
        Object[] actualArgs = new Object[args.length - 1];
        System.arraycopy(args, 1, actualArgs, 0, actualArgs.length);
        ((JSChannel) thisObj).fireEvent(Context.toString(args[0]), actualArgs, false);
    }

    public Scriptable jsFunction_getListeners(String eventName) {
        try {
            Context cx = Context.enter();
            ArrayList<Function> listenerList;
            synchronized (listeners) {
                ArrayList<Function> gListenerList = listeners.get(eventName);
                if (gListenerList == null) {
                    System.out.println("listenerList is null, not listing");
                    return cx.newArray(scope, 0);
                }
                listenerList = new ArrayList<Function>(gListenerList);
            }
            return cx.newArray(scope, listenerList.toArray());
        } finally {
            Context.exit();
        }
    }

    public String jsFunction_listListeners(String eventName) {

        ArrayList<Function> listenerList;
        synchronized (listeners) {
            ArrayList<Function> gListenerList = listeners.get(eventName);
            if (gListenerList == null || gListenerList.size() == 0) {
                System.out.println("listenerList is null, not listing");
                return "There are no listeners for this event";
            }
            listenerList = new ArrayList<Function>(gListenerList);
        }
        StringBuilder builder = new StringBuilder();
        int size = listenerList.size();
        for (int i = 0; i < size; i++) {
            builder.append(i).
                    append(": ").
                    append(Context.toString(listenerList.get(i))).
                    append("\n");
        }
        builder.deleteCharAt(builder.length() - 1);
        return builder.toString();

    }

    

    public void fireEvent(String eventName, final Object[] args) {
        fireEvent(eventName, args, true);
    }

    public void fireEvent(String eventName, final Object[] args, boolean switchContext) {
        final ArrayList<Function> listenerList;
        synchronized (listeners) {
            ArrayList<Function> gListenerList = listeners.get(eventName);
            if (gListenerList == null) {
                //System.out.println("listenerList is null, not firing");
                return;
            }
            listenerList = new ArrayList<Function>(gListenerList);
        }

        Thread executor = new Thread(new Runnable() {

            @Override
            public void run() {
                int size = listenerList.size();
                for (int i = 0; i < size; i++) {
                    try {
                        //System.out.println("Calling listener #" + (i + 1));
                        listenerList.get(i).call(Context.enter(), JSChannel.this, JSChannel.this, args);
                    } catch (Throwable t) {
                        Logger.getLogger(JSChannel.class.getName()).log(Level.SEVERE, null, t);
                    } finally {
                        Context.exit();
                    }

                }
            }
        });
        try {
            if(switchContext) {
                CommandContext.initialize(channel.getChannelName(), "#", "#", "#", "#", bot, executor);
            }
            executor.start();
            executor.join();
        } catch (InterruptedException ex) {
            Logger.getLogger(JSChannel.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if(switchContext) {
                CommandContext.destroy(executor);
            }
        }


    }
}
