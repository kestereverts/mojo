/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.yuana.mojo;

import java.util.IdentityHashMap;

/**
 *
 * @author Kester Everts
 */
public final class CommandContext {

    private static final IdentityHashMap<Thread, CommandContext> contexts = new IdentityHashMap<Thread, CommandContext>();
    private final String channel;
    private final String sender;
    private final String login;
    private final String hostname;
    private final String message;
    private final MojoBot bot;
    private final Thread associate;
    private final Thread owner;

    public static CommandContext initialize(final String channel, final String sender, final String login, final String hostname, final String message, final MojoBot bot, final Thread associate) {
        Thread currentThread = Thread.currentThread();
        if (associate == currentThread) {
            throw new IllegalArgumentException("Thread to associate with must not be the current thread");
        }

        synchronized (contexts) {
            if(contexts.containsKey(currentThread)) {
                throw new SecurityException("CommandText can only be initialized by threads that are not associated with a CommandContext");
            }
            if (contexts.containsKey(associate)) {
                throw new SecurityException("Thread has already been associated with a CommandContext");
            }
            CommandContext context = new CommandContext(channel, sender, login, hostname, message, bot, associate, currentThread);
            contexts.put(associate, context);
            return context;
        }
    }

    public static CommandContext get() {
        synchronized (contexts) {
            return contexts.get(Thread.currentThread());
        }
    }

    public static void destroy(Thread associate) {
        synchronized (contexts) {
            CommandContext context = contexts.get(associate);
            if(context == null) {
                return;
            }
            if(context.owner != Thread.currentThread()) {
                throw new SecurityException("The current thread is not the owner of the CommandContext");
            }
            contexts.remove(associate);
        }
    }

    private CommandContext(final String channel, final String sender, final String login, final String hostname, final String message, final MojoBot bot, final Thread associate, final Thread owner) {
        this.channel = channel;
        this.sender = sender;
        this.login = login;
        this.hostname = hostname;
        this.message = message;
        this.bot = bot;
        this.associate = associate;
        this.owner = owner;
    }

    public MojoBot getBot() {
        return bot;
    }

    public String getChannel() {
        return channel;
    }

    public String getHostname() {
        return hostname;
    }

    public String getLogin() {
        return login;
    }

    public String getMessage() {
        return message;
    }

    public String getSender() {
        return sender;
    }

    public Thread getAssociate() {
        return associate;
    }
}
