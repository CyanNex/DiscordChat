package me.koenn.serverchat.api.discord.model;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

public interface IDiscordMessage {

    @NotNull JsonObject toJSON();
}
