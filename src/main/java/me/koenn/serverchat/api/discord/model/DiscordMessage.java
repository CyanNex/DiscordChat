package me.koenn.serverchat.api.discord.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class DiscordMessage {

    private final String username;
    private final String avatarUrl;
    private final String content;
    private DiscordEmbed[] embeds;

    public DiscordMessage(String username, String avatarUrl, DiscordEmbed... embeds) {
        this(username, avatarUrl, "");
        this.setEmbeds(embeds);
    }

    public DiscordMessage(String username, String avatarUrl, String content) {
        this.username = username;
        this.avatarUrl = avatarUrl;
        this.content = content;
    }

    public void setEmbeds(DiscordEmbed... embeds) {
        this.embeds = embeds;
    }

    public JsonObject toJSON() {
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
