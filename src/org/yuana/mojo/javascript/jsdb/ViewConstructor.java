/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.yuana.mojo.javascript.jsdb;

import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

/**
 *
 * @author Kester Everts
 */
public class ViewConstructor extends BaseFunction {
    @Override
    public String getClassName() {
        return "ViewConstructor";
    }

    public void jsConstructor(Object getter, Object setter) throws NoSuchMethodException {
        Scriptable prototype = getClassPrototype();
        ScriptableObject.defineProperty(prototype, "get", getter, EMPTY);
        ScriptableObject.defineProperty(prototype, "set", setter, EMPTY);
        //FunctionObject get = new FunctionObject("get", View.class.getMethod("get", Context.class, Scriptable.class, (new Object[] {}).getClass(), Function.class), prototype);
        //FunctionObject set = new FunctionObject("set", View.class.getMethod("set", Context.class, Scriptable.class, (new Object[] {}).getClass(), Function.class), prototype);
        //ScriptableObject.defineProperty(prototype, "get", get, EMPTY);
        //ScriptableObject.defineProperty(prototype, "set", set, EMPTY);
        ((ScriptableObject)prototype).defineProperty("database", null, View.class.getMethod("getDatabase"), null, READONLY | PERMANENT);
    }

    @Override
    public Scriptable createObject(Context cx, Scriptable scope)
    {
        Scriptable newInstance = new View();
        newInstance.setPrototype(getClassPrototype());
        newInstance.setParentScope(getParentScope());
        return newInstance;
    }


    @Override
    public Object call(Context cx, Scriptable scope, Scriptable thisObj,
                       Object[] args)
    {
        if(!(thisObj instanceof View)) {
            throw Context.reportRuntimeError("Cannot call a ViewConstructor this way");
        }
        View view = (View) thisObj;
        if(args.length == 0) {
            throw Context.reportRuntimeError("View expects at least 1 argument");
        }
        if(!(args[0] instanceof JSDB)) {
            throw Context.reportRuntimeError("Argument 1 should be of type jsdb");
        }

        view.setDatabase((JSDB) args[0]);
        return thisObj;
    }

}
