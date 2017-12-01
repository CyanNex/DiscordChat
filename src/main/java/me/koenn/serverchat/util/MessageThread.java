package me.koenn.serverchat.util;

import me.koenn.serverchat.Serverchat;
import me.koenn.serverchat.discord.DiscordMessage;

import java.util.LinkedList;

public class MessageThread implements Runnable {

    public static final LinkedList<DiscordMessage> MESSAGE_QUEUE = new LinkedList<>();

    @Override
    public void run() {
        while (!MESSAGE_QUEUE.isEmpty()) {
            Serverchat.webhook.sendMessage(MESSAGE_QUEUE.removeFirst());
        }
    }
}
