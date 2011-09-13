/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.yuana.mojo.javascript;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;
import org.yuana.mojo.CommandContext;
import org.yuana.mojo.MojoBot;

/**
 *
 * @author Kester Everts
 */
public class JSBot extends ScriptableObject {

    private MojoBot bot;

    public JSBot() {
    }

    public void initFurther() {
        Context cx = Context.enter();

        try {
            ScriptableObject ctcp = (ScriptableObject) cx.newObject(this);
            ctcp.defineProperty("finger", this, JSBot.class.getMethod("__jsGet_ctcp_finger", ScriptableObject.class), JSBot.class.getDeclaredMethod("__jsSet_ctcp_finger", ScriptableObject.class, String.class), 0);
            ctcp.defineProperty("version", this, JSBot.class.getMethod("__jsGet_ctcp_version", ScriptableObject.class), JSBot.class.getDeclaredMethod("__jsSet_ctcp_version", ScriptableObject.class, String.class), 0);
            ScriptableObject.defineProperty(this, "ctcp", ctcp, 0);
        } catch (NoSuchMethodException ex) {
            Logger.getLogger(JSBot.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(JSBot.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            Context.exit();
        }
    }

    public void setBot(MojoBot bot) {
        this.bot = bot;
    }
    
    public MojoBot getBot() {
        return bot;
    }

    @Override
    public String getClassName() {
        return "Bot";
    }

    public String jsGet_name() {
        if (bot.isConnected()) {
            return bot.getNick();
        } else {
            return bot.getName();
        }
    }

    public void jsSet_name(String name) {
        if (bot.isConnected()) {
            bot.changeNick(name);
        } else {
            bot.setName(name);
        }
    }

    public String jsGet_encoding() {
        return bot.getEncoding();
    }

    public void jsSet_encoding(String encoding) {
        try {
            bot.setEncoding(encoding);
        } catch (UnsupportedEncodingException ex) {
            Context.throwAsScriptRuntimeEx(ex);
            Logger.getLogger(JSBot.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String __jsGet_ctcp_finger(ScriptableObject s) {
        return bot.getFinger();
    }

    public void __jsSet_ctcp_finger(ScriptableObject s, String message) {
        bot.setFinger(message);
    }

    public String __jsGet_ctcp_version(ScriptableObject s) {
        return bot.getVersion();
    }

    public void __jsSet_ctcp_version(ScriptableObject s, String message) {
        bot.setVersion(message);
    }

    public String jsGet_login() {
        return bot.getLogin();
    }

    public void jsSet_login(String login) {
        bot.setLogin(login);
    }

    public String jsGet_commandPrefix() {
        return bot.getCommandPrefix();
    }

    public void jsSet_commandPrefix(String prefix) {
        bot.setCommandPrefix(prefix);
    }

    public String jsGet_voidCommandPrefix() {
        return bot.getVoidCommandPrefix();
    }

    public void jsSet_voidCommandPrefix(String prefix) {
        bot.setVoidCommandPrefix(prefix);
    }

    public double jsGet_messageDelay() {
        return (double)bot.getMessageDelay();
    }

    public void jsSet_messageDelay(double delay) {
        bot.setMessageDelay((long)delay);
    }
    
    public String jsGet_owner() {
        return bot.getOwner();
    }

    public void jsSet_owner(String owner) {
        bot.setOwner(owner);
    }

    public void jsFunction_connect(Object hostname, Object port, Object password) {
        if (hostname instanceof Undefined) {
            throw Context.reportRuntimeError("connect requires are least 1 paramenter");
        }
        try {
            if (!(password instanceof Undefined)) {
                bot.connect(Context.toString(hostname), (int) Context.toNumber(port), Context.toString(password), false);
            } else if (!(port instanceof Undefined)) {
                bot.connect(Context.toString(hostname), (int) Context.toNumber(port), false);
            } else {
                bot.connect(Context.toString(hostname), false);
            }
        } catch (Exception ex) {
            Context.throwAsScriptRuntimeEx(ex);
            Logger.getLogger(JSBot.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void jsFunction_connectSSL(Object hostname, Object port, Object password) {
        if (hostname instanceof Undefined) {
            throw Context.reportRuntimeError("connectSSL requires are least 1 paramenter");
        }
        try {
            if (!(password instanceof Undefined)) {
                bot.connect(Context.toString(hostname), (int) Context.toNumber(port), Context.toString(password), true);
            } else if (!(port instanceof Undefined)) {
                bot.connect(Context.toString(hostname), (int) Context.toNumber(port), true);
            } else {
                bot.connect(Context.toString(hostname), true);
            }
        } catch (Exception ex) {
            Context.throwAsScriptRuntimeEx(ex);
            Logger.getLogger(JSBot.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Object jsFunction_getChannel(String channel) throws IllegalAccessException, InstantiationException, InvocationTargetException {
        Object jsChannel = bot.addJSChannel(channel);
        return jsChannel == null ? Undefined.instance : jsChannel;
    }

    public void jsFunction_raw(String raw) {
        bot.sendRawLineViaQueue(raw);
    }

    public void jsFunction_join(Object channel) {
        String chan;
        if (channel instanceof Undefined) {
            if (CommandContext.get().getChannel() == null) {
                throw Context.reportRuntimeError("when not in channel context, join requires at least 1 parameter");
            }
            chan = CommandContext.get().getChannel();
        } else {
            chan = Context.toString(channel);
        }
        bot.joinChannel(chan);
    }

    public void jsFunction_joinWithKey(Object channel, Object key) {
        String chan;
        if (channel instanceof Undefined) {
            throw new IllegalArgumentException("This function requires at least 1 argument.");
        }
        if (key instanceof Undefined) {
            key = Context.toString(channel);
            if (CommandContext.get().getChannel() == null) {
                throw Context.reportRuntimeError("channel argument must be defined");
            }
            chan = CommandContext.get().getChannel();
        } else {
            chan = Context.toString(channel);
        }
        String strKey = Context.toString(key);
        bot.joinChannel(chan, strKey);
    }

    public void jsFunction_part(Object channel) {
        String chan;
        if (channel instanceof Undefined) {
            if (CommandContext.get().getChannel() == null) {
                throw Context.reportRuntimeError("when not in channel context, part requires at least 1 parameter");
            }
            chan = CommandContext.get().getChannel();
        } else {
            chan = Context.toString(channel);
        }
        bot.partChannel(chan);
    }

    public void jsFunction_partWithReason(Object channel, Object reason) {
        String chan;
        if (channel instanceof Undefined) {
            throw new IllegalArgumentException("This function requires at least 1 argument.");
        }
        if (reason instanceof Undefined) {
            reason = Context.toString(channel);
            if (CommandContext.get().getChannel() == null) {
                throw Context.reportRuntimeError("channel argument must be defined");
            }
            chan = CommandContext.get().getChannel();
        } else {
            chan = Context.toString(channel);
        }
        String strReason = Context.toString(reason);
        bot.partChannel(chan, strReason);
    }

    public void jsFunction_clearQueue() {
        bot.getQueue().clear();
    }

    public void jsFunction_multiThreaded(boolean value) {
        bot.setMultiThreadedInput(value);
    }

    public Object jsGet_currentChannel() throws IllegalAccessException, InvocationTargetException, InstantiationException {
        return jsFunction_getChannel(CommandContext.get().getChannel());
    }

    public Object jsGet_currentUser() throws IllegalAccessException, InstantiationException, InvocationTargetException {
        Object channel = jsFunction_getChannel(CommandContext.get().getChannel());
        if (channel instanceof JSChannel) {
            return ((JSChannel) channel).jsGet_users().get(CommandContext.get().getSender(), ((JSChannel) channel).jsGet_users());
        }
        return Undefined.instance;
    }

    public void jsFunction_destroy() {
        bot.getUmbrella().removeBot(bot.getIdentifier());
    }

    public boolean jsGet_connected() {
        return bot.isConnected();
    }

    public String jsGet_host() {
        return bot.getServer();
    }

    public int jsGet_port() {
        return bot.getPort();
    }

    public boolean jsGet_ssl() {
        return bot.getUseSSL();
    }

    public String jsGet_identifier() {
        return bot.getIdentifier();
    }
}
