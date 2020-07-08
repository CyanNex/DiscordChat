package me.koenn.serverchat.api.discord;

import me.koenn.serverchat.api.discord.model.IDiscordMessage;
import org.jetbrains.annotations.NotNull;

public interface IWebhook {

    void sendMessage(@NotNull IDiscordMessage message);
}
