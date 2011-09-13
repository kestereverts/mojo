/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.yuana.mojo.javascript;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;
import org.yuana.mojo.HTTPClient;

/**
 *
 * @author Kester Everts
 */
public class Utilities extends ScriptableObject {

    private static Scriptable scope;
    private static final String JS_CLASS_NAME = "Utilities";

    public static void establishScope(Scriptable standardScope) {
        if (scope != null) {
            return;
        }
        try {
            Context cx = Context.enter();
            scope = standardScope;
            ScriptableObject.defineClass(scope, Utilities.class);
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

    public static Utilities getJSInstance(Scriptable standardScope) throws IllegalAccessException, InstantiationException, InvocationTargetException {
        try {
            Context cx = Context.enter();
            establishScope(standardScope);
            Utilities instance = (Utilities) cx.newObject(scope, JS_CLASS_NAME);
            return instance;
        } finally {
            Context.exit();
        }
    }


    @Override
    public String getClassName() {
        return JS_CLASS_NAME;
    }
    
    public String jsFunction_controlv(String paste) {
        final String API_KEY = "1240975862";
        if (paste.length() < 15) {
            return "Paste too short";
        }
        try {
            String postData = String.format("api_key=%s&function=paste&title=Paste%%20by%%20Mojo&name=Mojo&expiration=d&private=y&paste=%s", API_KEY, URLEncoder.encode(paste, "UTF-8"));
            String response = HTTPClient.sendPost("http://felle.mavecoder.com/controlv/api/api.php", postData);
            System.out.println(response);
            Pattern pattern = Pattern.compile("<url>(.*?)</url>");
            Matcher m = pattern.matcher(response);
            if (m.find()) {
                return ("http://www." + m.group(1).substring(7)).replaceAll("www.controlv\\.net", "felle.mavecoder.com/controlv");
            } else {
                return "";
            }
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(Utilities.class.getName()).log(Level.SEVERE, null, ex);
            return ex.toString();
        } catch (MalformedURLException ex) {
            Logger.getLogger(Utilities.class.getName()).log(Level.SEVERE, null, ex);
            return ex.toString();
        } catch (IOException ex) {
            Logger.getLogger(Utilities.class.getName()).log(Level.SEVERE, null, ex);
            return ex.toString();
        }
    }

    public String jsFunction_paste(String paste, Object language) {
        try {
            String postData = "paste_code=" + URLEncoder.encode(paste, "UTF-8") + "&paste_name=Mojo&paste_private=1&paste_expire_date=1D";
            if(!(language instanceof Undefined)) {
                postData += "&paste_format=" + URLEncoder.encode(Context.toString(language), "UTF-8");
            }
            String response = HTTPClient.sendPost("http://pastebin.com/api_public.php", postData);
            return response;
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(Utilities.class.getName()).log(Level.SEVERE, null, ex);
            return ex.toString();
        } catch (MalformedURLException ex) {
            Logger.getLogger(Utilities.class.getName()).log(Level.SEVERE, null, ex);
            return ex.toString();
        } catch (IOException ex) {
            Logger.getLogger(Utilities.class.getName()).log(Level.SEVERE, null, ex);
            return ex.toString();
        }
    }

    //public void jsFunction_say(String text) {
    //    bot.sendMessage("#mojo.test", text);
    //}


   
    
    

    /*

    public void protoArrayFunction_say(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
        if(!(thisObj instanceof NativeArray)) {
            return;
        }
        NativeArray a = (NativeArray) thisObj;

        Object[] ids = a.getIds();

        for (int i = 0; i < ids.length; i++) {
            if (ids[i] instanceof Integer) {
                bot.sendMessage(CommandContext.get().getChannel(), Context.toString(thisObj.get((Integer) ids[i], thisObj)));
            } else {
                bot.sendMessage(CommandContext.get().getChannel(), Context.toString(thisObj.get(Context.toString(ids[i]), thisObj)));
            }
        }

//        for(int i = 0; i < length; i++) {
//            //jsFunction_say(Context.toString(thisObj.get(i, thisObj)));
//            bot.sendMessage("#mojo.test", Context.toString(thisObj.get(i, thisObj)));
//        }
    }
*/


}
