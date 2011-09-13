/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.yuana.mojo.javascript;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.yuana.ircbot.Channel;
import org.yuana.ircbot.ChannelUser;
import org.yuana.ircbot.NetworkUser;
import org.yuana.ircbot.PircBot;

/**
 *
 * @author Kester Everts
 */
public class JSChannelUserList extends ScriptableObject {

    private PircBot bot;
    private Channel channel;
    private Scriptable scope;
    private static final String JS_CLASS_NAME = "ChannelUserList";



    public JSChannelUserList(Scriptable standardScope, PircBot bot, Channel channel) {
        if(channel == null) {
            throw new NullPointerException("channel cannot be null");
        }
        this.channel = channel;
        this.bot = bot;
        this.scope = standardScope;
    }

    @Override
    public String getClassName() {
        return JS_CLASS_NAME;
    }

    @Override
    public Object get(String name, Scriptable start) {
        NetworkUser nUser = bot.getNetworkUser(name);
        if(nUser == null) {
            return super.get(name, start);
        }
        ChannelUser user = channel.getUser(nUser);
        if(user == null) {
            return super.get(name, start);
        }
        try {
            return JSChannelUser.getJSInstance(scope, bot, channel, user);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(JSChannelUserList.class.getName()).log(Level.SEVERE, null, ex);
            return NOT_FOUND;
        } catch (InstantiationException ex) {
            Logger.getLogger(JSChannelUserList.class.getName()).log(Level.SEVERE, null, ex);
            return NOT_FOUND;
        } catch (InvocationTargetException ex) {
            Logger.getLogger(JSChannelUserList.class.getName()).log(Level.SEVERE, null, ex);
            return NOT_FOUND;
        }
    }

    @Override
    public boolean has(String name, Scriptable start) {
        NetworkUser nUser = bot.getNetworkUser(name);
        if(nUser == null) {
            return super.has(name, start);
        }
        return channel.getUser(nUser) != null;
    }

    @Override
    public Object[] getIds() {
        ChannelUser[] users = channel.getUsers();
        Object[] objects = super.getIds();
        Object[] ids = new Object[users.length + objects.length];

        for(int i = 0; i < users.length; i++) {
            ids[i] = users[i].getNetworkUser().getNick();
        }
        
        for(int i = users.length; i < objects.length; i++) {
            ids[i] = objects[i];
        }

        return ids;
    }

    @Override
    public Object getDefaultValue(Class hint) {
        int length = channel.getUsers().length;
        if(hint == String.class) {
            return "" + length;
        } else {
            return length;
        }
    }

    @Override
    public String getTypeOf() {
    	return "channeluserlist";
    }

}
