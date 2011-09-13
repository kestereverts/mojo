/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.yuana.ircbot;

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Logger;

/**
 *
 * @author Kester Everts
 */
public class Channel {
    private String channelName = "";
    private String topic = "";
    private String topicSetBy = "";
    private Date topicSetAt = new Date(0);
    private LinkedList<ChannelUser> users = new LinkedList<ChannelUser>();

    public Channel(String channelName) {
        if(channelName == null) {
            throw new IllegalArgumentException("channelName cannot be null");
        }
        this.channelName = channelName;
    }

    public String getChannelName() {
        return channelName;
    }

    public String getTopic() {
        return topic;
    }

    void setTopic(String topic) {
        if(topic == null) {
            this.topic = "";
            Logger.getLogger(Channel.class.getCanonicalName()).info("Topic was null");
        }
        this.topic = topic;
    }

    public Date getTopicSetAt() {
        return topicSetAt;
    }

    void setTopicSetAt(Date topicSetAt) {
        if(topicSetAt == null) {
            throw new NullPointerException("topicSetAt cannot be null");
        }
        this.topicSetAt = topicSetAt;
    }

    public String getTopicSetBy() {
        return topicSetBy;
    }

    public void setTopicSetBy(String topicSetBy) {
        if(topicSetBy == null) {
            return;
        }
        this.topicSetBy = topicSetBy;
    }

    public synchronized void addUser(NetworkUser networkUser, char sign) {
        if(networkUser == null) {
            throw new NullPointerException("networkUser cannot be null");
        }
        for(ChannelUser user : users) {
            if(user.getNetworkUser() == networkUser) {
                user.setSign(sign);
                return;
            }
        }

        for(ChannelUser user : users) {
            if(user.getNetworkUser().getNick().equals(networkUser.getNick())) {
                user.setSign(sign);
                return;
            }
        }

        users.add(new ChannelUser(this, networkUser, sign));
    }

    public synchronized ChannelUser getUser(NetworkUser networkUser) {
        if(networkUser == null) {
            return null;
        }
        for(ChannelUser user : users) {
            if(user.getNetworkUser() == networkUser) {
                return user;
            }
        }
        
        for(ChannelUser user : users) {
            if(user.getNetworkUser().getNick().equals(networkUser.getNick())) {
                return user;
            }
        }


        return null;
    }

    public synchronized ChannelUser[] getUsers() {
        ChannelUser[] userArray = new ChannelUser[users.size()];
        users.toArray(userArray);
        return userArray;
    }

    public synchronized void removeUser(NetworkUser networkUser) {
        if(networkUser == null) {
            return;
        }
        Iterator<ChannelUser> i = users.iterator();
        while(i.hasNext()) {
            if(i.next().getNetworkUser() == networkUser) {
                i.remove();
                return;
            }
        }

        i = users.iterator();
        while(i.hasNext()) {
            if(i.next().getNetworkUser().getNick().equals(networkUser.getNick())) {
                i.remove();
                return;
            }
        }
    }

    


}
