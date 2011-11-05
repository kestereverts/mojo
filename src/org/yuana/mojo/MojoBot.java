/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.yuana.mojo;

import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.mozilla.javascript.ClassShutter;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.yuana.ircbot.Channel;
import org.yuana.ircbot.Colors;
import org.yuana.ircbot.Queue;
import org.yuana.mojo.javascript.JSChannel;
import org.yuana.mojo.javascript.Utilities;
import org.yuana.mojo.javascript.jsdb.JSDB;

/**
 *
 * @author Kester Everts
 */
public class MojoBot extends AbstractBot {

    private boolean scriptingMultiThreaded = false;
    private HashMap<String, JSChannel> jsChannels = new HashMap<String, JSChannel>();
    private Pattern vimeoUrlPattern = Pattern.compile("vimeo\\.com/(\\d+)");
    private Pattern spotifyUrlPattern = Pattern.compile("(spotify:[^:\\s]+:[A-Za-z0-9]+)|(http://open\\.spotify\\.com/[^/\\s]+/[A-Za-z0-9]+)");
    private Pattern metacafeUrlPattern = Pattern.compile("watch/(\\d+)");
    private Pattern metacafeDurationPattern = Pattern.compile("\\((\\d+:\\d+)\\)<br/>");
    private Pattern metacafeViewsPattern = Pattern.compile("\\| (\\d+) views \\|");
    public final String configFolder;
    private String lastFound = "";
    private Umbrella umbrella;
    private final String identifier;
    private String commandPrefix = "::";
    private String voidCommandPrefix = ";;";
    private String owner = "";

    
    

    MojoBot(Umbrella umbrella, String identifier) throws NoSuchMethodException {
        if (umbrella == null) {
            throw new IllegalArgumentException("umbrella may not be null");
        }
        this.umbrella = umbrella;
        this.identifier = identifier;

        configFolder = umbrella.getConfigFolder() + "/bots/" + this.identifier;

        //Runtime.getRuntime().addShutdownHook(new Thread() {
//
//            @Override
//            public void run() {
//                System.out.println("Running shutdown hook!");
//            }
//        });
        this.setName("Mojo");
        try {
            this.setEncoding("UTF-8");
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(MojoBot.class.getName()).log(Level.SEVERE, null, ex);
        }
        //this.setName("MojoTest");
        this.setVersion("Mojo by IJzerenRita");
        this.setLogin("Mojo");


    }

    public Umbrella getUmbrella() {
        return this.umbrella;
    }

    public String getIdentifier() {
        return this.identifier;
    }

    @Override
    protected void onMessage(final String channel, final String sender, final String login, final String hostname, final String message) {
        JSChannel chanObj = jsChannels.get(channel);
        if (chanObj != null) {
            chanObj.fireEvent("message", new Object[]{chanObj, chanObj.jsGet_users().get(sender, chanObj.jsGet_users()), message});

        }

        if (true) {
            if ((message.startsWith(commandPrefix) || message.startsWith(voidCommandPrefix)) && ownerCheck(sender, login, hostname)) {
                Thread executor = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            Context cx = Context.enter();
                            cx.setLanguageVersion(Context.VERSION_1_8);
                            Scriptable scope = addJSChannel(channel);
                            ScriptableObject.defineProperty(umbrella.getStandardScope(), "bot", umbrella.createJSBotInstance(MojoBot.this), 0);
                            cx.setClassShutter(new ClassShutter() {

                                @Override
                                public boolean visibleToScripts(String fullClassName) {
                                    return false;
                                }
                            });
                            if (message.startsWith(commandPrefix)) {
                                Object result = cx.evaluateString(scope, message.substring(commandPrefix.length()), "cmd in " + channel, 1, null);
                                if (!(result instanceof Undefined)) {
                                    sendMessage(channel, Context.toString(result));
                                }
                            } else {
                                cx.evaluateString(scope, message.substring(voidCommandPrefix.length()), "cmd in " + channel, 1, null);
                            }
                        } catch (Throwable t) {
                            Logger.getLogger(MojoBot.class.getName()).log(Level.SEVERE, null, t);
                            String error = t.getMessage();
                            for (String line : error.split("\n")) {
                                sendNotice(getNickFromMask(owner), line);
                            }
                        } finally {
                            ScriptableObject.deleteProperty(umbrella.getStandardScope(), "bot");
                            Context.exit();
                        }
                    }
                });
                try {
                    CommandContext.initialize(channel, sender, login, hostname, message, this, executor);
                    executor.start();
                    executor.join();
                } catch (InterruptedException ex) {
                    Logger.getLogger(MojoBot.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    CommandContext.destroy(executor);
                }


            }
        }

        if ((message.startsWith("tinysong: ") || message.equals("tinysong:")) && getSearch("tinysong", channel)) {
            try {
                // http://tinysong.com/b/
                String query = message.substring(9).trim();
                if (query.equals("")) {
                    if (lastFound.equals("")) {
                        return;
                    } else {
                        query = lastFound;
                    }
                }
                String response = HTTPClient.sendGet("http://tinysong.com/b/" + URLEncoder.encode(query, "UTF-8"));
                String[] parts = response.split(";");
                if (parts.length > 5) {
                    String link = parts[0].trim();
                    String name = parts[2].trim();
                    String artist = parts[4].trim();

                    lastFound = name + " " + artist;

                    StringBuilder builder = new StringBuilder();
                    builder.append(Colors.ORANGE).
                            append("[Tinysong link] ").
                            append(Colors.NORMAL).
                            append(link).
                            append(Colors.DARK_GREEN).
                            append(" \"").
                            append(name).
                            append("\" ").
                            append(Colors.NORMAL).
                            append("by ").
                            append(Colors.BLUE).
                            append(artist);
                    sendMessage(channel, builder.toString());
                }
            } catch (MalformedURLException ex) {
                Logger.getLogger(MojoBot.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(MojoBot.class.getName()).log(Level.SEVERE, null, ex);
            }

        } else if ((message.startsWith("spotify: ") || message.equals("spotify:")) && getSearch("spotify", channel)) {
            try {
                // http://tinysong.com/b/
                String query = message.substring(8).trim();
                if (query.equals("")) {
                    if (lastFound.equals("")) {
                        return;
                    } else {
                        query = lastFound;
                    }
                }

                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document doc = db.parse("http://ws.spotify.com/search/1/track?q=" + URLEncoder.encode(query, "UTF-8"));
                doc.getDocumentElement().normalize();

                NodeList nodeList = doc.getElementsByTagName("track");
                if (nodeList.getLength() != 0) {
                    Element track = (Element) nodeList.item(0);
                    String url = track.getAttribute("href").trim();
                    String name = track.getElementsByTagName("name").item(0).getChildNodes().item(0).getNodeValue().trim();
                    String artist = ((Element) track.getElementsByTagName("artist").item(0)).getElementsByTagName("name").item(0).getChildNodes().item(0).getNodeValue().trim();

                    String link = "http://open.spotify.com/" + url.substring(8).replace(':', '/');

                    lastFound = name + " " + artist;

                    StringBuilder builder = new StringBuilder();
                    builder.append(Colors.ORANGE).
                            append("[Spotify link] ").
                            append(Colors.NORMAL).
                            append(link).
                            append(Colors.DARK_GREEN).
                            append(" \"").
                            append(name).
                            append("\" ").
                            append(Colors.NORMAL).
                            append("by ").
                            append(Colors.BLUE).
                            append(artist);
                    sendMessage(channel, builder.toString());

                }


            } catch (ParserConfigurationException ex) {
                Logger.getLogger(MojoBot.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SAXException ex) {
                Logger.getLogger(MojoBot.class.getName()).log(Level.SEVERE, null, ex);
            } catch (MalformedURLException ex) {
                Logger.getLogger(MojoBot.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(MojoBot.class.getName()).log(Level.SEVERE, null, ex);
            }
        }


        //detectYouTube(channel, message);
        detectVimeo(channel, message);
        detectSpotify(channel, message);
        detectMetacafe(channel, message);
    }

    @Override
    protected void onNickChange(String oldNick, String login, String hostname, String newNick) {
        if(ownerCheck(oldNick, login, hostname)) {
            owner = newNick + owner.substring(owner.indexOf('!'));
        }
    }
    

    @Override
    protected void onKick(String channel, String kickerNick, String kickerLogin, String kickerHostname, String recipientNick, String reason) {
        
        JSChannel chanObj = jsChannels.get(channel);
        if (chanObj != null) {
            chanObj.fireEvent("kick", new Object[]{chanObj, chanObj.jsGet_users().get(kickerNick, chanObj.jsGet_users()), chanObj.jsGet_users().get(recipientNick, chanObj.jsGet_users()), reason});

            if (recipientNick.equalsIgnoreCase(getNick())) {
                removeJsChannel(channel);
                joinChannel(channel);
            }
        }
    }

    @Override
    protected void onPart(String channel, String sender, String login, String hostname) {
        if (sender.equalsIgnoreCase(getNick())) {
            removeJsChannel(channel);
        }
    }

    protected void removeJsChannel(String channel) {
        jsChannels.remove(channel);
    }

    private static String reverseString(String str) {
        return new StringBuffer(str).reverse().toString();
    }

    public Queue getQueue() {
        return _outQueue;
    }

    @Override
    protected void onJoin(String channel, String sender, String login, String hostname) {
        try {
            JSChannel chanObj = addJSChannel(channel);
            if (chanObj != null) {
                String path = configFolder + "/events/" + channel.substring(1).replace("\\.\\.|\\|/", "").toLowerCase();
                File eventsDir = new File(path);
                if (sender.equals(this.getNick()) && eventsDir.exists()) {
                    chanObj.removeAllListeners();
                    // + "/" + eventName.replace("\\.\\.|\\|/", "");

                    FileFilter onlyJSFiles = new FileFilter() {

                        @Override
                        public boolean accept(File pathname) {
                            return pathname.isFile() && pathname.getPath().endsWith(".js");
                        }
                    };

                    File[] dirs = eventsDir.listFiles(new FileFilter() {

                        @Override
                        public boolean accept(File pathname) {
                            return pathname.isDirectory();
                        }
                    });

                    for (int i = 0; i < dirs.length; i++) {
                        String eventName = dirs[i].getName();
                        File[] files = dirs[i].listFiles(onlyJSFiles);
                        for (int j = 0; j < files.length; j++) {
                            System.out.println("Found " + files[j].getPath());
                            try {
                                Context cx = Context.enter();
                                cx.setLanguageVersion(Context.VERSION_1_8);
                                Scriptable scope = addJSChannel(channel);
                                cx.setClassShutter(new ClassShutter() {

                                    @Override
                                    public boolean visibleToScripts(String fullClassName) {
                                        return false;
                                    }
                                });
                                FileReader fileReader = new FileReader(files[j]);
                                Object result = cx.evaluateReader(scope, fileReader, files[j].getPath(), 1, null);
                                fileReader.close();
                                System.out.println("RESULT: " + result.getClass().getCanonicalName());
                                if (result instanceof Function) {
                                    Function function = (Function) result;
                                    ScriptableObject.defineProperty(function, "fileName", files[j].getPath(), ScriptableObject.DONTENUM | ScriptableObject.READONLY);
                                    chanObj.addEventListenerWithoutFile(eventName, function);
                                }
                            } catch (Throwable ex) {
                                sendNotice(getNickFromMask(owner), ex.toString());
                                Logger.getLogger(MojoBot.class.getName()).log(Level.SEVERE, null, ex);
                            } finally {
                                Context.exit();
                            }
                        }
                    }
                }
                chanObj.fireEvent("join", new Object[]{chanObj, chanObj.jsGet_users().get(sender, chanObj.jsGet_users())});
            }
        } catch (IllegalAccessException ex) {
            Logger.getLogger(MojoBot.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(MojoBot.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex) {
            Logger.getLogger(MojoBot.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public JSChannel addJSChannel(String channel) throws IllegalAccessException, InstantiationException, InvocationTargetException {
        JSChannel jsChannel = jsChannels.get(channel);
        if (jsChannel != null) {
            System.out.println("Reusing an existing channel object");
            return jsChannel;
        }
        try {
            System.out.println("Creating a new channel object");
            Context.enter();
            Channel channelObj = getChannel(channel);
            if(channelObj == null) {
                throw new RuntimeException("Channel '" + channel + "' does not exist");
            }
            JSChannel scope = JSChannel.getJSInstance(umbrella.createJSBotInstance(this), this, channelObj);
            scope.put("channel", scope, scope);
            scope.put("bot", scope, umbrella.createJSBotInstance(this));
            scope.setParentScope(umbrella.createJSBotInstance(this));
            jsChannels.put(channel, scope);
            return scope;

        } finally {
            Context.exit();
        }
    }

    public static void jsSerialize() {
    }

    

    private void detectVimeo(String channel, String message) {
        Matcher m = vimeoUrlPattern.matcher(message);
        if (!m.find()) {
            return;
        }
        if (!getContextual("vimeo", channel)) {
            return;
        }
        try {
            String id = m.group(1);
            //String response = HTTPClient.sendGet("http://vimeo.com/api/v2/video/" + id + ".xml");

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse("http://vimeo.com/api/v2/video/" + id + ".xml");
            doc.getDocumentElement().normalize();

            String title = lastFound = doc.getElementsByTagName("title").item(0).getChildNodes().item(0).getNodeValue();
            String uploader = doc.getElementsByTagName("user_name").item(0).getChildNodes().item(0).getNodeValue();
            int duration = Integer.parseInt(doc.getElementsByTagName("duration").item(0).getChildNodes().item(0).getNodeValue());
            long durationSeconds = duration % 60;
            StringBuilder builder = new StringBuilder();
            builder.append(Colors.ORANGE).
                    append("[Vimeo video]").
                    append(Colors.DARK_GREEN).
                    append(" \"").
                    append(title).
                    append("\" ").
                    append(Colors.NORMAL).
                    append("by ").
                    append(Colors.BLUE).
                    append(uploader).
                    append(Colors.NORMAL).
                    append(" (").
                    append(duration / 60).
                    append(":").
                    append(durationSeconds < 10 ? "0" + durationSeconds : durationSeconds).
                    append(")");
            sendMessage(channel, builder.toString());
        } catch (SAXException ex) {
            Logger.getLogger(MojoBot.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(MojoBot.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(Utilities.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedURLException ex) {
            Logger.getLogger(Utilities.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Utilities.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NullPointerException ex) {
            Logger.getLogger(MojoBot.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void detectSpotify(String channel, String message) {
        Matcher m = spotifyUrlPattern.matcher(message);
        if (!m.find()) {
            return;
        }
        if (!getContextual("spotify", channel)) {
            return;
        }
        try {
            String id = m.group(1) != null ? m.group(1) : m.group(2);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse("http://ws.spotify.com/lookup/1/?uri=" + URLEncoder.encode(id, "UTF-8"));
            doc.getDocumentElement().normalize();

            String kind = doc.getDocumentElement().getNodeName();

            StringBuilder builder = new StringBuilder();

            if (kind.equals("track")) {
                String title = doc.getDocumentElement().getElementsByTagName("name").item(0).getChildNodes().item(0).getNodeValue();
                String artist = ((Element) doc.getDocumentElement().getElementsByTagName("artist").item(0)).getElementsByTagName("name").item(0).getChildNodes().item(0).getNodeValue();
                int duration = Math.round(Float.parseFloat(doc.getDocumentElement().getElementsByTagName("length").item(0).getChildNodes().item(0).getNodeValue()));
                int durationSeconds = duration % 60;
                lastFound = title + " " + artist;
                builder.append(Colors.ORANGE).
                        append("[Spotify track]").
                        append(Colors.DARK_GREEN).
                        append(" \"").
                        append(title).
                        append("\" ").
                        append(Colors.NORMAL).
                        append("by ").
                        append(Colors.BLUE).
                        append(artist).
                        append(Colors.NORMAL).
                        append(" (").
                        append(duration / 60).
                        append(":").
                        append(durationSeconds < 10 ? "0" + durationSeconds : durationSeconds).
                        append(")");
                sendMessage(channel, builder.toString());
            } else if (kind.equals("artist")) {
                String artist = lastFound = doc.getDocumentElement().getElementsByTagName("name").item(0).getChildNodes().item(0).getNodeValue();
                builder.append(Colors.ORANGE).
                        append("[Spotify artist]").
                        append(Colors.DARK_GREEN).
                        append(" \"").
                        append(artist).
                        append("\"");
                sendMessage(channel, builder.toString());
            } else if (kind.equals("album")) {
                String title = doc.getDocumentElement().getElementsByTagName("name").item(0).getChildNodes().item(0).getNodeValue();
                String artist = ((Element) doc.getDocumentElement().getElementsByTagName("artist").item(0)).getElementsByTagName("name").item(0).getChildNodes().item(0).getNodeValue();
                lastFound = title + " " + artist;
                builder.append(Colors.ORANGE).
                        append("[Spotify album]").
                        append(Colors.DARK_GREEN).
                        append(" \"").
                        append(title).
                        append("\" ").
                        append(Colors.NORMAL).
                        append("by ").
                        append(Colors.BLUE).
                        append(artist);
                sendMessage(channel, builder.toString());
            }
        } catch (SAXException ex) {
            Logger.getLogger(MojoBot.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(MojoBot.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(Utilities.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedURLException ex) {
            Logger.getLogger(Utilities.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Utilities.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NullPointerException ex) {
            Logger.getLogger(MojoBot.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void detectMetacafe(String channel, String message) {
        Matcher m = metacafeUrlPattern.matcher(message);
        if (!m.find()) {
            return;
        }
        if (!getContextual("metacafe", channel)) {
            return;
        }
        try {
            String id = m.group(1);

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse("http://www.metacafe.com/api/item/" + id + "/");
            doc.getDocumentElement().normalize();

            Element item = (Element) ((Element) doc.getElementsByTagName("channel").item(0)).getElementsByTagName("item").item(0);


            String title = lastFound = item.getElementsByTagName("title").item(0).getChildNodes().item(0).getNodeValue();
            String uploader = item.getElementsByTagName("author").item(0).getChildNodes().item(0).getNodeValue();
            String description = item.getElementsByTagName("description").item(0).getChildNodes().item(0).getNodeValue();

            String duration;
            Matcher mDuration = metacafeDurationPattern.matcher(description);
            if (mDuration.find()) {
                duration = mDuration.group(1);
            } else {
                duration = "unknown duration";
            }

            String views = null;
            Matcher mViews = metacafeViewsPattern.matcher(description);
            if (mViews.find()) {
                views = mViews.group(1);
            }



            StringBuilder builder = new StringBuilder();
            builder.append(Colors.ORANGE).
                    append("[Metacafe video]").
                    append(Colors.DARK_GREEN).
                    append(" \"").
                    append(title).
                    append("\" ").
                    append(Colors.NORMAL).
                    append("by ").
                    append(Colors.BLUE).
                    append(uploader).
                    append(Colors.NORMAL);

            //if (views != null) {
            //        builder.append(" viewed ").
            //                append(Colors.MAGENTA).
            //                append(reverseString(reverseString(views).replaceAll("(\\d\\d\\d)", "$1 ").trim())).
            //                append(Colors.NORMAL);
            //    }

            //builder.append(" (").
            //        append(duration).
            //        append(")");
            sendMessage(channel, builder.toString());
        } catch (SAXException ex) {
            Logger.getLogger(MojoBot.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(MojoBot.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(Utilities.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedURLException ex) {
            Logger.getLogger(Utilities.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Utilities.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NullPointerException ex) {
            Logger.getLogger(MojoBot.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected void onAction(String sender, String login, String hostname, String channel, String action) {
        JSChannel chanObj = jsChannels.get(channel);
        if (chanObj != null) {
            chanObj.fireEvent("action", new Object[]{chanObj, chanObj.jsGet_users().get(sender, chanObj.jsGet_users()), action});

        }
    }

    @Override
    protected void onNotice(String sender, String sourceLogin, String sourceHostname, String channel, String notice) {
        JSChannel chanObj = jsChannels.get(channel);
        if (chanObj != null) {
            chanObj.fireEvent("message", new Object[]{chanObj, chanObj.jsGet_users().get(sender, chanObj.jsGet_users()), notice});

        }
    }

    @Override
    public void dispose() {
        super.dispose();
        this.umbrella = null;
    }

    public String getCommandPrefix() {
        return commandPrefix;
    }

    public void setCommandPrefix(String commandPrefix) {
        this.commandPrefix = commandPrefix;
    }

    public String getVoidCommandPrefix() {
        return voidCommandPrefix;
    }

    public void setVoidCommandPrefix(String voidCommandPrefix) {
        this.voidCommandPrefix = voidCommandPrefix;
    }

    public String getConfigFolder() {
        return configFolder;
    }

    public boolean getContextual(String contextual, String channel) {
        try {
            JSDB db = new JSDB();
            db.setParentScope(umbrella.getStandardScope());
            db.jsConstructor("contextual_" + identifier);
            String result = db.get(contextual + "_" + channel.toLowerCase());
            return result != null && result.equals("true");
        } catch (IOException ex) {
            Logger.getLogger(MojoBot.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    public void setContextual(String contextual, String channel, boolean value) {
        try {
            JSDB db = new JSDB();
            db.setParentScope(umbrella.getStandardScope());
            db.jsConstructor("contextual_" + identifier);
            db.set(contextual + "_" + channel.toLowerCase(), value ? "true" : "false");
        } catch (IOException ex) {
            Logger.getLogger(MojoBot.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public boolean getSearch(String search, String channel) {
        try {
            JSDB db = new JSDB();
            db.setParentScope(umbrella.getStandardScope());
            db.jsConstructor("search_" + identifier);
            String result = db.get(search + "_" + channel.toLowerCase());
            return result != null && result.equals("true");
        } catch (IOException ex) {
            Logger.getLogger(MojoBot.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    public void setSearch(String search, String channel, boolean value) {
        try {
            JSDB db = new JSDB();
            db.setParentScope(umbrella.getStandardScope());
            db.jsConstructor("search_" + identifier);
            db.set(search + "_" + channel.toLowerCase(), value ? "true" : "false");
        } catch (IOException ex) {
            Logger.getLogger(MojoBot.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }
    
    private static String getNickFromMask(String mask) {
        return mask.substring(0, Math.max(0, mask.indexOf('!')));
    }
    
    private boolean ownerCheck(String nick, String login, String hostname) {
        return (nick + "!" + login + "@" + hostname).equals(owner);
    }
}
