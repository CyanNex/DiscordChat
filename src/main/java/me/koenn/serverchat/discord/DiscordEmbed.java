package me.koenn.serverchat.discord;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class DiscordEmbed {

    private final String title;
    private final String description;
    private final int color;

    public DiscordEmbed(String title, String description, int color) {
        this.title = title;
        this.description = description;
        this.color = color;
    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("type", "rich");
        json.put("title", this.title);
        json.put("description", this.description);
        json.put("color", this.color);
        json.put("fields", new JSONArray());
        return json;
    }
}
