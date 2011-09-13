/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.yuana.mojo.javascript.jsdb;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.Context;
import org.yuana.mojo.javascript.JSUmbrella;

/**
 *
 * @author Kester Everts
 */
public class JSDB extends ScriptableObject implements Contract {

    private String database = null;
    private String baseDir = null;
    private Pattern databasePattern = Pattern.compile("^[A-Za-z_][A-Za-z0-9_]*$");

    public JSDB() {
    }

    @Override
    public String getClassName() {
        return "JSDB";
    }

    @Override
    public String getTypeOf() {
        return "jsdb";
    }

    public void jsConstructor(Object sDatabase) {
        String database;
        if (sDatabase instanceof Undefined) {
            throw Context.reportRuntimeError("Argument 1 (database) is undefined");
        } else if (sDatabase instanceof Contract) {
            database = ((Contract) sDatabase).getDatabase();
        } else {
            database = Context.toString(sDatabase);
        }
        this.database = database;

        if (!databasePattern.matcher(database).matches()) {
            throw Context.reportRuntimeError("Argument 1 (database) contains illegal characters");
        }

        this.baseDir = ((JSUmbrella)ScriptableObject.getTopLevelScope(this)).getUmbrella().getConfigFolder() + "/jsdb/" + this.database;
    }

    @Override
    public String getDatabase() {
        return database;
    }

    @Override
    public String getBaseDir() {
        return baseDir;
    }

    @Override
    public String get(String name) throws IOException {
        String nameEnc = encode(name);
        File file = new File(this.baseDir + "/" + nameEnc);
        if (file.isFile() && file.canRead()) {
            FileReader fileReader = new FileReader(file);
            BufferedReader reader = new BufferedReader(fileReader);
            StringBuilder strBuffer = new StringBuilder();
            char[] buffer = new char[1024];
            int read = 0;
            try {
                while ((read = reader.read(buffer, 0, buffer.length)) != -1) {
                    strBuffer.append(buffer, 0, read);
                }
            }
            finally {
                reader.close();
            }

            return strBuffer.toString();
        }
        return null;
    }

    @Override
    public void set(String name, String data) throws IOException {
        String nameEnc = jsStaticFunction_encode(name);
        File file = new File(this.baseDir + "/" + nameEnc);
        new File(this.baseDir).mkdirs();
        file.createNewFile();
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
        try {
            out.write(data.getBytes("UTF-8"));
        }
        finally {
            out.close();
        }
    }

    @Override
    public boolean remove(String name) {
        String nameEnc = encode(name);
        File file = new File(this.baseDir + "/" + nameEnc);
        return file.delete();
    }

    public Object jsFunction_get(Object sName) {
        String name;
        if (sName instanceof Undefined) {
            throw Context.reportRuntimeError("Argument 1 (name) is undefined");
        } else {
            name = Context.toString(sName);
        }
        try {
            String data = get(name);
            if (data == null) {
                return Undefined.instance;
            } else {
                return data;
            }
        } catch (IOException ex) {
            return Undefined.instance;
        }


    }

    public void jsFunction_set(Object sName, Object sData) {
        String name;
        if (sName instanceof Undefined) {
            throw Context.reportRuntimeError("Argument 1 (name) is undefined");
        } else {
            name = Context.toString(sName);
        }

        String data;
        if (sData instanceof Undefined) {
            throw Context.reportRuntimeError("Argument 2 (data) is undefined");
        } else {
            data = Context.toString(sData);
        }
        try {
            set(name, data);
        } catch (IOException ex) {
            Logger.getLogger(JSDB.class.getName()).log(Level.SEVERE, null, ex);
            Context.reportWarning(name, ex);
        }
    }

    public boolean jsFunction_remove(Object sName) {
        String name;
        if (sName instanceof Undefined) {
            name = "";
        } else {
            name = Context.toString(sName);
        }

        return remove(name);

    }

    //public View jsFunction_createView(Object getter, Object setter) {

    //}

    public static String encode(String name) {
        try {
            String nameEnc = URLEncoder.encode(name, "UTF-8");
            nameEnc = nameEnc.replace(".", "%2E");
            return nameEnc;
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(JSDB.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public static String decode(String name) {
        try {
            return URLDecoder.decode(name, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(JSDB.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public static String jsStaticFunction_encode(String name) {
        return encode(name);
    }

    public static String jsStaticFunction_decode(String name) {
        return decode(name);
    }
}
