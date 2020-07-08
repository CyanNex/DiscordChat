package me.koenn.serverchat.api.thread;

import me.koenn.serverchat.api.discord.model.IDiscordMessage;
import org.jetbrains.annotations.NotNull;

public interface IMessageThread extends Runnable {

    void enqueue(@NotNull IDiscordMessage message);
}
