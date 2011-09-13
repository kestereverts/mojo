/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.yuana.mojo.javascript;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;
import org.yuana.ircbot.Channel;
import org.yuana.ircbot.ChannelUser;
import org.yuana.ircbot.NetworkUser;
import org.yuana.ircbot.PircBot;
import org.yuana.mojo.MojoBot;

/**
 *
 * @author Kester Everts
 */
public class JSChannelUser extends ScriptableObject {

    private PircBot bot;
    private Channel channel;
    private ChannelUser user;
    private NetworkUser nUser;
    
    private static Scriptable scope;
    private static final String JS_CLASS_NAME = "ChannelUser";

    public static void establishScope(Scriptable standardScope) {
        if(scope != null) {
            return;
        }
        try {
            Context cx = Context.enter();
            scope = standardScope;
            ScriptableObject.defineClass(scope, JSChannelUser.class);

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

    @Override
    public String getClassName() {
        return JS_CLASS_NAME;
    }

    public static JSChannelUser getJSInstance(Scriptable standardScope, PircBot bot, Channel channel, ChannelUser user) throws IllegalAccessException, InstantiationException, InvocationTargetException {
        try {
            Context cx = Context.enter();
            establishScope(standardScope);
            JSChannelUser instance = (JSChannelUser) cx.newObject(scope, JS_CLASS_NAME);
            instance.bot = bot;
            instance.channel = channel;
            instance.user = user;
            instance.nUser = user.getNetworkUser();
            return instance;
        } finally {
            Context.exit();
        }
    }

    public String jsGet_sign() {
        return Character.toString(user.getSign());
    }

    public boolean jsGet_isOwner() {
        return user.isOwner();
    }

    public boolean jsGet_isAdmin() {
        return user.isAdmin();
    }

    public boolean jsGet_isOp() {
        return user.isOp();
    }

    public boolean jsGet_isHop() {
        return user.isHop();
    }

    public boolean jsGet_isVoice() {
        return user.isVoice();
    }

    public String jsGet_ident() {
        return nUser.getIdent();
    }

    public String jsGet_host() {
        return nUser.getHost();
    }

    public String jsGet_server() {
        return nUser.getServer();
    }

    public String jsGet_nick() {
        return nUser.getNick();
    }

    public boolean jsGet_isAway() {
        return nUser.isAway();
    }

    public boolean jsGet_isRegistered() {
        return nUser.isRegistered();
    }

    public boolean jsGet_isBot() {
        return nUser.isBot();
    }

    public boolean jsGet_isIrcOp() {
        return nUser.isIrcOp();
    }

    public int jsGet_hopCount() {
        return nUser.getHopCount();
    }

    public String jsGet_realName() {
        return nUser.getRealName();
    }

    public Object jsGet_channel() throws IllegalAccessException, InstantiationException, InvocationTargetException {
        return ((MojoBot)bot).getUmbrella().createJSBotInstance((MojoBot)bot).jsFunction_getChannel(channel.getChannelName());
    }

    public void jsFunction_kick(Object reason) {
        if (reason instanceof Undefined) {
            bot.kick(channel.getChannelName(), nUser.getNick());
        } else {
            bot.kick(channel.getChannelName(), nUser.getNick(), Context.toString(reason));
        }
    }

    public void jsFunction_ban(String mask, Object reason) {
        bot.setMode(channel.getChannelName(), "+b *!*@" + user.getNetworkUser().getHost());
        jsFunction_kick(reason);
    }

    @Override
    public Object getDefaultValue(Class hint) {
        if (hint == String.class) {
            return "" + (user.getSign() == '\0' ? "" : user.getSign()) + nUser.getNick().trim() + "!" + nUser.getIdent().trim() +
                    "@" + nUser.getHost().trim() + ":" + nUser.getServer().trim() +
                    " (" + nUser.getRealName().trim() + ")" +
                    (nUser.isRegistered() ? " [registered] " : "") +
                    (nUser.isAway() ? "[away] " : "") +
                    (nUser.isIrcOp() ? "[ircOp] " : "") +
                    (nUser.isBot() ? "[bot] " : "");
        } else {
            return user.hashCode();
        }
    }

    @Override
    public String getTypeOf() {
        return "channeluser";
    }
}
