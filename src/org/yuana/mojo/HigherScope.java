/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.yuana.mojo;

import org.mozilla.javascript.ScriptableObject;

/**
 *
 * @author Kester Everts
 */
public class HigherScope extends ScriptableObject {

    @Override
    public String getClassName() {
        return "HigherScope";
    }

    public void jsFunction_print(String printage, Object something) {
        System.out.println(printage);
        System.out.println(something.getClass().getCanonicalName());
    }

    public int jsFunction_addOne(int i) {
        return ++i;
    }
}
