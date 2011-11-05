/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.yuana.mojo.javascript;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.yuana.mojo.MojoBot;

/**
 *
 * @author Kester Everts
 */
public class JSChannelList extends ScriptableObject {
     private final MojoBot bot;

    public JSChannelList(MojoBot bot) {
        this.bot = bot;
    }

    @Override
    public String getClassName() {
        return "ChannelList";
    }

    @Override
    public Object get(String name, Scriptable start) {
        String channelName;
        if(!name.startsWith("#")) {
            channelName = "#" + name;
        } else {
            channelName = name;
        }
        try {
            return bot.addJSChannel(channelName);
        } catch (Throwable ex) {
            
        }
        return super.get(name, start);
    }

    @Override
    public boolean has(String name, Scriptable start) {
        String[] channels = bot.getChannels();
        for(int i = 0; i < channels.length; i++) {
            if(channels[i].equals(name)) {
                return true;
            }
        }
        return super.has(name, start);
    }

    @Override
    public Object[] getIds() {
        return bot.getChannels();
    }

    @Override
    public void delete(String name) {
        
    }



    @Override
    public String getTypeOf() {
        return "channellist";
    }
}
