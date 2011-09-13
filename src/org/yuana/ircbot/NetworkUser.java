/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.yuana.ircbot;

/**
 *
 * @author Kester Everts
 */
public class NetworkUser {

    private String ident = "";
    private String host = "";
    private String server = "";
    private String nick = "";
    private boolean away = false;
    private boolean registered = false;
    private boolean isBot = false;
    private boolean isIrcOp = false;
    private int hopCount = -1;
    private String realName = "";

    public NetworkUser(String ident, String host, String server, String nick, boolean away,
            boolean registered, boolean isBot, boolean isIrcOp, int hopCount, String realName) {
        setAll(ident, host, server, nick, away, registered, isBot, isIrcOp, hopCount, realName);
    }


    public synchronized void setAll(String ident, String host, String server, String nick, boolean away,
            boolean registered, boolean isBot, boolean isIrcOp, int hopCount, String realName) {
        if(ident == null) {
            throw new IllegalArgumentException("ident cannot be null");
        }
        if(host == null) {
            throw new IllegalArgumentException("host cannot be null");
        }
        if(server == null) {
            throw new IllegalArgumentException("server cannot be null");
        }
        if(nick == null) {
            throw new IllegalArgumentException("nick cannot be null");
        }
        if(realName == null) {
            throw new IllegalArgumentException("realName cannot be null");
        }
        if(hopCount < -1) {
            throw new IllegalArgumentException("hopCount cannot be lower than -1");
        }

        this.ident = ident;
        this.host = host;
        this.server = server;
        this.nick = nick;
        this.away = away;
        this.registered = registered;
        this.isBot = isBot;
        this.isIrcOp = isIrcOp;
        this.hopCount = hopCount;
        this.realName = realName;
    }


    public synchronized boolean isAway() {
        return away;
    }

    public synchronized void setAway(boolean away) {
        this.away = away;
    }

    public synchronized int getHopCount() {
        return hopCount;
    }

    public synchronized void setHopCount(int hopCount) {
        if(hopCount < -1) {
            throw new IllegalArgumentException("hopCount cannot be lower than -1");
        }
        this.hopCount = hopCount;
    }

    public synchronized String getHost() {
        return host;
    }

    public synchronized void setHost(String host) {
        if(ident == null) {
            throw new IllegalArgumentException("host cannot be null");
        }
        this.host = host;
    }

    public synchronized String getIdent() {
        return ident;
    }

    public synchronized void setIdent(String ident) {
        if(ident == null) {
            throw new IllegalArgumentException("ident cannot be null");
        }
        this.ident = ident;
    }

    public synchronized boolean isBot() {
        return isBot;
    }

    public synchronized void setIsBot(boolean isBot) {
        this.isBot = isBot;
    }

    public synchronized boolean isIrcOp() {
        return isIrcOp;
    }

    public synchronized void setIsIrcOp(boolean isIrcOp) {
        this.isIrcOp = isIrcOp;
    }

    public synchronized String getNick() {
        return nick;
    }

    public synchronized void setNick(String nick) {
        if(ident == null) {
            throw new IllegalArgumentException("nick cannot be null");
        }
        this.nick = nick;
    }

    public synchronized String getRealName() {
        return realName;
    }

    public synchronized void setRealName(String realName) {
        if(ident == null) {
            throw new IllegalArgumentException("realName cannot be null");
        }
        this.realName = realName;
    }

    public synchronized boolean isRegistered() {
        return registered;
    }

    public synchronized void setRegistered(boolean registered) {
        this.registered = registered;
    }

    public synchronized String getServer() {
        return server;
    }

    public synchronized void setServer(String server) {
        if(ident == null) {
            throw new IllegalArgumentException("server cannot be null");
        }
        this.server = server;
    }
}
