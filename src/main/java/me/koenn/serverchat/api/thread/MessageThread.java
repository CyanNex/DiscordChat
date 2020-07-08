package me.koenn.serverchat.api.thread;

import me.koenn.serverchat.api.ServerchatAPI;
import me.koenn.serverchat.api.discord.model.IDiscordMessage;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class MessageThread implements IMessageThread {

    private final BlockingQueue<IDiscordMessage> messageQueue = new LinkedBlockingQueue<>();

    private final ServerchatAPI api;

    public MessageThread(@NotNull ServerchatAPI api) {
        this.api = Objects.requireNonNull(api);
    }

    @Override
    public void enqueue(@NotNull IDiscordMessage message) {
        this.messageQueue.add(message);
    }

    @Override
    public void run() {
        while (true) {
            try {
                this.api.getWebhook().sendMessage(messageQueue.take());
            } catch (InterruptedException e) {
                this.api.log("Closing message thread");
                return;
            }
        }
    }
}
