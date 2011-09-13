/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.yuana.mojo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 *
 * @author Kester Everts
 */
public class HTTPClient {

    public static String sendPost(String url, String postData) throws MalformedURLException, IOException {

        // Send data
        URL urlObj = new URL(url);
        URLConnection conn = urlObj.openConnection();
        conn.setDoOutput(true);
        OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
        wr.write(postData);
        wr.flush();

        // Get the response
        InputStream input = conn.getInputStream();
        StringBuffer buffer = new StringBuffer();
        byte[] b = new byte[1024];
        int a = 0;
        while ((a = input.read(b)) > 0) {
            buffer.append(new String(b, 0, a));
        }
        wr.close();
        input.close();
        return buffer.toString();
    }

    public static String sendGet(String url) throws MalformedURLException, IOException {
        URL urlObj = new URL(url);
        URLConnection conn = urlObj.openConnection();
        InputStream input = conn.getInputStream();
        StringBuffer buffer = new StringBuffer();
        byte[] b = new byte[1024];
        int a = 0;
        while ((a = input.read(b)) > 0) {
            buffer.append(new String(b, 0, a));
        }
        input.close();
        return buffer.toString();
    }
}
