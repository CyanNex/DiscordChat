package me.koenn.serverchat.api.util;

import me.koenn.serverchat.api.ServerchatAPI;
import me.koenn.serverchat.api.discord.model.DiscordMessage;

import java.util.LinkedList;

public class MessageThread implements Runnable {

    public static final LinkedList<DiscordMessage> MESSAGE_QUEUE = new LinkedList<>();

    private final ServerchatAPI api;

    public MessageThread(ServerchatAPI api) {
        this.api = api;
    }

    @Override
    public void run() {
        while (!MESSAGE_QUEUE.isEmpty()) {
            this.api.getWebhook().sendMessage(MESSAGE_QUEUE.removeFirst());
        }
    }
}
