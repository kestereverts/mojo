/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.yuana.mojo.javascript.jsdb;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;



/**
 *
 * @author Kester Everts
 */
public class JSON extends ScriptableObject implements Contract {
    private Contract backingDatabase;
    private Scriptable jsonObject;
    
    public JSON() {

    }

    public void jsConstructor(Object sDatabase) {
        if(sDatabase instanceof Contract) {
            this.backingDatabase = (Contract) sDatabase;
        } else {
            throw Context.reportRuntimeError("Argument 1 (database) is not a JSDB database");
        }
        try {
            Context cx = Context.enter();
            jsonObject = (Scriptable) ScriptableObject.getProperty(cx.initStandardObjects(), "JSON");
        }
        finally {
            Context.exit();
        }
    }

    @Override
    public String getClassName() {
        return "JSON";
    }

    @Override
    public String getTypeOf() {
        return "jsdb";
    }

    @Override
    public void set(String name, String data) throws IOException {
        backingDatabase.set(name, data);
    }

    @Override
    public boolean remove(String name) {
        return backingDatabase.remove(name);
    }

    @Override
    public String getDatabase() {
        return backingDatabase.getDatabase();
    }

    @Override
    public String getBaseDir() {
        return backingDatabase.getBaseDir();
    }

    @Override
    public String get(String name) throws IOException {
        return backingDatabase.get(name);
    }

    public Object jsFunction_get(Object sName) {
        String name;
        if(sName instanceof Undefined) {
            throw Context.reportRuntimeError("Argument 1 (name) is undefined");
        } else {
            name = Context.toString(sName);
        }
        try {
            Context cx = Context.enter();

            String data = get(name);
            if(data == null) {
                return Undefined.instance;
            } else {
                return parse(data);
            }
        } catch (IOException ex) {
            return Undefined.instance;
        }
        finally {
            Context.exit();
        }
    }

    public void jsFunction_set(Object sName, Object sData) {
        String name;
        if(sName instanceof Undefined) {
            throw Context.reportRuntimeError("Argument 1 (name) is undefined");
        } else {
            name = Context.toString(sName);
        }

        Scriptable data;
        if(sData instanceof Undefined) {
            throw Context.reportRuntimeError("Argument 2 (data) is undefined");
        } else {
            data = (Scriptable) sData;
        }

        try {
            Context cx = Context.enter();
            String json = stringify(data);
            set(name, json);
        } catch (IOException ex) {
            Logger.getLogger(JSDB.class.getName()).log(Level.SEVERE, null, ex);
            Context.reportWarning(name, ex);
        }
        finally {
            Context.exit();
        }
    }

    public boolean jsFunction_remove(Object sName) {
        String name;
        if(sName instanceof Undefined) {
            name = "";
        } else {
            name = Context.toString(sName);
        }

        return remove(name);

    }

    private String stringify(Scriptable data) {
        try {
            Context cx = Context.enter();
            return Context.toString(ScriptableObject.callMethod(cx, jsonObject, "stringify", new Object[] {data}));
        }
        finally {
            Context.exit();
        }
    }

    private Object parse(String data) {
        try {
            Context cx = Context.enter();
            System.out.println("parsing this data: " + data);
            return ScriptableObject.callMethod(cx, jsonObject, "parse", new Object[] {data});
        }
        finally {
            Context.exit();
        }
    }


}