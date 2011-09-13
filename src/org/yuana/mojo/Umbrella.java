/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.yuana.mojo;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mozilla.javascript.ClassShutter;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.yuana.mojo.javascript.JSBot;
import org.yuana.mojo.javascript.JSBotList;
import org.yuana.mojo.javascript.JSChannel;
import org.yuana.mojo.javascript.JSChannelUser;
import org.yuana.mojo.javascript.JSUmbrella;
import org.yuana.mojo.javascript.Utilities;
import org.yuana.mojo.javascript.jsdb.JSDB;
import org.yuana.mojo.javascript.jsdb.ViewConstructor;

/**
 *
 * @author Kester Everts
 */
public final class Umbrella {

    private HashMap<String, MojoBot> bots = new HashMap<String, MojoBot>();
    private HashMap<MojoBot, JSBot> botmapping = new HashMap<MojoBot, JSBot>();
    private Scriptable standardScope;
    private final String configFolder;

    public Umbrella(String configFolder) {
        this.configFolder = configFolder;
        initJS();
        
    }



    public void start() {
        System.out.println("The class is: " + standardScope.getClass().getCanonicalName());
        String common = configFolder + "/common.js";
        String config = configFolder + "/config.js";
        FileReader fileReader;
        try {
            Context cx = Context.enter();
            cx.setOptimizationLevel(-1);
            fileReader = new FileReader(common);
            cx.evaluateReader(standardScope, fileReader, common, 1, null);
            fileReader.close();
            fileReader = new FileReader(config);
            cx.evaluateReader(standardScope, fileReader, config, 1, null);
            fileReader.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MojoBot.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MojoBot.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            Context.exit();
        }
    }

    private void initJS() {
        try {
            ScriptableObject o = new ScriptableObject() {

                @Override
                public String getClassName() {
                    return "TemporaryGlobalObject";
                }
            };
            Context cx = Context.enter();
            o = (ScriptableObject) cx.initStandardObjects();
            cx.setLanguageVersion(Context.VERSION_1_8);
            ScriptableObject.defineClass(o, JSUmbrella.class);
            JSUmbrella jsumbrella = (JSUmbrella) cx.newObject(o, "Umbrella");
            jsumbrella.setUmbrella(this);
            jsumbrella.setParentScope(null);
            standardScope = cx.initStandardObjects(jsumbrella);
            ScriptableObject.defineProperty(standardScope, "umbrella", jsumbrella, ScriptableObject.PERMANENT);
            ScriptableObject.defineProperty(standardScope, "Umbrella", ScriptableObject.getProperty(o, "Umbrella"), 0);
            jsumbrella.setParentScope(null);

            ScriptableObject.defineClass(standardScope, JSBot.class);

            JSBotList botList = new JSBotList(this);
            botList.sealObject();
            ScriptableObject.defineProperty(standardScope, "bots", botList, ScriptableObject.PERMANENT | ScriptableObject.READONLY);

            Scriptable botListFakeConstructor = cx.newObject(standardScope);
            ScriptableObject.defineProperty(standardScope, botList.getClassName(), botListFakeConstructor, 0);

            Scriptable botListPrototype = cx.newObject(standardScope);
            ScriptableObject.defineProperty(standardScope, "prototype", botListPrototype, 0);

            botList.setPrototype(botListPrototype);


            Scriptable s = Utilities.getJSInstance(standardScope);
            standardScope.put("util", standardScope, s);

            ScriptableObject.defineClass(standardScope, JSDB.class);
            Scriptable JSDBConstructor = (Scriptable) ScriptableObject.getProperty(standardScope, "JSDB");
            ScriptableObject.defineClass(JSDBConstructor, ViewConstructor.class);

            JSChannel.establishScope(standardScope);
            JSChannelUser.establishScope(standardScope);

            cx.setClassShutter(new ClassShutter() {

                @Override
                public boolean visibleToScripts(String fullClassName) {
                    return false;
                }
            });

        } catch (IllegalAccessException ex) {
            Logger.getLogger(Umbrella.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(Umbrella.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex) {
            Logger.getLogger(Umbrella.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            Context.exit();
        }

    }

    public MojoBot createBot(String identifier) {
        if (bots.containsKey(identifier)) {
            throw new IllegalArgumentException("identifier already in use");
        }
        MojoBot bot;
        try {
            bot = new MojoBot(this, identifier);
            bot.setVerbose(true);
        } catch (Exception ex) {
            throw Context.throwAsScriptRuntimeEx(ex);
        }
        bots.put(identifier, bot);
        return bot;
    }

    public JSBot createJSBotInstance(MojoBot bot) {
        try {
            Context cx = Context.enter();
            if (botmapping.containsKey(bot)) {
                return botmapping.get(bot);
            }
            
            JSBot jsbot = (JSBot) cx.newObject(standardScope, "Bot");
            jsbot.setBot(bot);
            jsbot.initFurther();
            ScriptableObject.defineProperty(jsbot, "bot", jsbot, ScriptableObject.PERMANENT);
            jsbot.setParentScope(standardScope);
            botmapping.put(bot, jsbot);
            return jsbot;
        } finally {
            Context.exit();
        }
    }

    public boolean removeBot(String identifier) {
        MojoBot bot = bots.remove(identifier);
        if (bot != null) {
            botmapping.remove(bot);
            bot.disconnect();
            bot.dispose();
            return true;
        } else {
            return false;
        }
    }

    public MojoBot getBot(String identifier) {
        return bots.get(identifier);
    }

    public Collection<MojoBot> getBots() {
        return new ArrayList<MojoBot>(bots.values());
    }

    public Collection<String> getIdentifiers() {
        return new ArrayList<String>(bots.keySet());
    }

    public String getConfigFolder() {
        return configFolder;
    }

    public Scriptable getStandardScope() {
        return standardScope;
    }




    
}
