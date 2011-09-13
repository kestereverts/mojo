/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.yuana.mojo.javascript;

import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.yuana.ircbot.Channel;

/**
 *
 * @author Kester Everts
 */
public abstract class JSChannelSettings extends ScriptableObject {
    private Channel channel;
    private String prefix;

    public JSChannelSettings(Channel channel, String prefix) {
        this.channel = channel;
        this.prefix = prefix;
    }

    public Channel getChannel() {
        return channel;
    }

    public String getPrefix() {
        return prefix;
    }

    //@Override
    //public Object get(String name, Scriptable start) {
        //channel.
        //if(bot == null) {
        //    return super.get(name, start);
        //}
        //return umbrella.createJSBotInstance(bot);
    //}
}
