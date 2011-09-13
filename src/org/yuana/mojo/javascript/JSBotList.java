/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.yuana.mojo.javascript;

import java.util.Collection;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.yuana.mojo.MojoBot;
import org.yuana.mojo.Umbrella;

/**
 *
 * @author Kester Everts
 */
public class JSBotList extends ScriptableObject {

    private final Umbrella umbrella;

    public JSBotList(Umbrella umbrella) {
        this.umbrella = umbrella;
    }

    @Override
    public String getClassName() {
        return "BotList";
    }

    @Override
    public Object get(String name, Scriptable start) {
        MojoBot bot = umbrella.getBot(name);
        if(bot == null) {
            return super.get(name, start);
        }
        return umbrella.createJSBotInstance(bot);
    }

    @Override
    public boolean has(String name, Scriptable start) {
        MojoBot bot = umbrella.getBot(name);
        if(bot == null) {
            return super.has(name, start);
        }
        return true;
    }

    @Override
    public Object[] getIds() {
        Collection<String> keys = umbrella.getIdentifiers();
        Object[] objects = super.getIds();
        Object[] ids = new Object[keys.size() + objects.length];

        keys.toArray(ids);
        System.arraycopy(objects, 0, ids, keys.size(), objects.length);

        return ids;
    }

    @Override
    public void delete(String name) {
        umbrella.removeBot(name);
    }



    @Override
    public String getTypeOf() {
        return "botlist";
    }
}
