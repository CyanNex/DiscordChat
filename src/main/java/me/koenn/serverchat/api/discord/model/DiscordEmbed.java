package me.koenn.serverchat.api.discord.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class DiscordEmbed {

    private final String title;
    private final String description;
    private final int color;

    public DiscordEmbed(@NotNull String title, @NotNull String description, int color) {
        this.title = Objects.requireNonNull(title);
        this.description = Objects.requireNonNull(description);
        this.color = color;
    }

    public @NotNull JsonObject toJSON() {
        JsonObject json = new JsonObject();
        json.add("type", new JsonPrimitive("rich"));
        json.add("title", new JsonPrimitive(this.title));
        json.add("description", new JsonPrimitive(this.description));
        json.add("color", new JsonPrimitive(this.color));
        json.add("fields", new JsonArray());
        return json;
    }
}
