package me.koenn.serverchat.api.util;

import me.koenn.serverchat.api.ServerchatAPI;
import me.koenn.serverchat.api.discord.model.DiscordMessage;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.Objects;

public class MessageThread implements Runnable {

    public static final LinkedList<DiscordMessage> MESSAGE_QUEUE = new LinkedList<>();

    private final ServerchatAPI api;

    public MessageThread(@NotNull ServerchatAPI api) {
        this.api = Objects.requireNonNull(api);
    }

    @Override
    public void run() {
        while (!MESSAGE_QUEUE.isEmpty()) {
            this.api.getWebhook().sendMessage(MESSAGE_QUEUE.removeFirst());
        }
    }
}
