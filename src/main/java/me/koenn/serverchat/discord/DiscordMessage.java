package me.koenn.serverchat.discord;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

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

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("username", this.username);
        json.put("avatar_url", this.avatarUrl);
        json.put("content", this.content);
        if (this.embeds != null) {
            JSONArray embeds = new JSONArray();
            for (DiscordEmbed embed : this.embeds) {
                embeds.add(embed.toJSON());
            }
            json.put("embeds", embeds);
        }
        return json;
    }
}
