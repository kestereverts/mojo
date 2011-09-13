/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.yuana.mojo.javascript.jsdb;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 *
 * @author Kester Everts
 */
interface Contract {
    public String getDatabase();
    public String getBaseDir();
    public String get(String name) throws IOException;
    public void set(String name, String data) throws IOException;
    public boolean remove(String name);
}
