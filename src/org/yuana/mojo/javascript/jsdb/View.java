/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.yuana.mojo.javascript.jsdb;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

/**
 *
 * @author Kester Everts
 */
public class View extends ScriptableObject {

    private JSDB database;

    @Override
    public String getClassName() {
        return "View";
    }
    @Override
    public String getTypeOf() {
        return "jsdbview";
    }
    
    public static Object get(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
        View view = (View) thisObj;
        Object[] newArgs = new Object[args.length];
        newArgs[0] = view.database;
        System.arraycopy(args, 0, newArgs, 1, args.length);
        return ScriptableObject.callMethod(cx, view, "getter", newArgs);
    }
    
    public static Object set(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
        View view = (View) thisObj;
        Object[] newArgs = new Object[args.length];
        newArgs[0] = view.database;
        System.arraycopy(args, 0, newArgs, 1, args.length);
        return ScriptableObject.callMethod(cx, view, "setter", newArgs);
    }

    public JSDB getDatabase() {
        return this.database;
    }

    void setDatabase(JSDB database) {
        this.database = database;
    }




}
