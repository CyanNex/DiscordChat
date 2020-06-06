package me.koenn.serverchat.api.discord.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class DiscordMessage {

    private final String username;
    private final String avatarUrl;
    private final String content;
    private DiscordEmbed[] embeds;

    public DiscordMessage(@NotNull String username, @NotNull String avatarUrl, @NotNull DiscordEmbed... embeds) {
        this(username, avatarUrl, "");
        this.setEmbeds(embeds);
    }

    public DiscordMessage(@NotNull String username, @NotNull String avatarUrl, @NotNull String content) {
        this.username = Objects.requireNonNull(username);
        this.avatarUrl = Objects.requireNonNull(avatarUrl);
        this.content = Objects.requireNonNull(content);
    }

    public void setEmbeds(@NotNull DiscordEmbed... embeds) {
        this.embeds = Objects.requireNonNull(embeds);
    }

    public @NotNull JsonObject toJSON() {
        JsonObject json = new JsonObject();
        json.add("username", new JsonPrimitive(this.username));
        json.add("avatar_url", new JsonPrimitive(this.avatarUrl));
        json.add("content", new JsonPrimitive(this.content));
        if (this.embeds != null) {
            JsonArray embeds = new JsonArray();
            for (DiscordEmbed embed : this.embeds) {
                embeds.add(embed.toJSON());
            }
            json.add("embeds", embeds);
        }
        return json;
    }
}
