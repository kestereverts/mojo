/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.yuana.ircbot;

/**
 *
 * @author Kester Everts
 */
public class ChannelUser {

    private Channel channel;
    private NetworkUser networkUser;
    private char sign = '\0';

    public ChannelUser(Channel channel, NetworkUser networkUser) {
        this(channel, networkUser, '\0');
    }
    
    public ChannelUser(Channel channel, NetworkUser networkUser, char sign) {
        if(networkUser == null) {
            throw new IllegalArgumentException("networkUser cannot be null");
        }
        if(channel == null) {
            throw new IllegalArgumentException("channel cannot be null");
        }
        this.channel = channel;
        this.networkUser = networkUser;
        this.sign = sign;
    }

    public Channel getChannel() {
        return channel;
    }

    public NetworkUser getNetworkUser() {
        return networkUser;
    }

    public char getSign() {
        return sign;
    }

    public void setSign(char sign) {
        this.sign = sign;
    }

    public boolean isOwner() {
        return isOwner(sign);
    }

    public boolean isAdmin() {
        return isAdmin(sign);
    }

    public boolean isOp() {
        return isOp(sign);
    }

    public boolean isHop() {
        return isHop(sign);
    }

    public boolean isVoice() {
        return isVoice(sign);
    }

    public static boolean isOwner(char sign) {
        return sign == '~';
    }

    public static boolean isAdmin(char sign) {
        return sign == '&';
    }

    public static boolean isOp(char sign) {
        return sign == '@';
    }

    public static boolean isHop(char sign) {
        return sign == '%';
    }

    public static boolean isVoice(char sign) {
        return sign == '+';
    }


}
