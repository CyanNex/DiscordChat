package me.koenn.serverchat.api.discord;

import me.koenn.serverchat.api.ServerchatAPI;
import me.koenn.serverchat.api.discord.model.DiscordMessage;
import org.jetbrains.annotations.NotNull;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;

public class Webhook {

    private static final String USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64; rv:49.0) Gecko/20100101 Firefox/49.0";
    private final URL url;
    private final ServerchatAPI api;

    public Webhook(@NotNull String url, @NotNull ServerchatAPI api) throws MalformedURLException {
        this.url = new URL(Objects.requireNonNull(url));
        this.api = Objects.requireNonNull(api);
    }

    private Optional<HttpURLConnection> connect() {
        try {
            HttpURLConnection connection = (HttpURLConnection) this.url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("User-Agent", USER_AGENT);
            connection.setRequestProperty("Content-type", "application/json; charset=UTF-8");
            connection.setDoOutput(true);
            connection.setDoInput(true);

            return Optional.of(connection);
        } catch (IOException ex) {
            ex.printStackTrace();
            return Optional.empty();
        }
    }

    public void sendMessage(@NotNull DiscordMessage message) {
        Objects.requireNonNull(message);

        Optional<HttpURLConnection> connection = this.connect();
        if (!connection.isPresent()) {
            this.api.error("Unable to connect to Discord!");
            return;
        }
        HttpURLConnection discordConnection = connection.get();

        String payload = message.toJSON().toString();
        byte[] encodedPayload = payload.getBytes(StandardCharsets.UTF_8);
        discordConnection.setRequestProperty("Content-Length", String.valueOf(encodedPayload.length));
        discordConnection.setConnectTimeout(1000);
        discordConnection.setReadTimeout(1000);

        try {
            DataOutputStream outputStream = new DataOutputStream(discordConnection.getOutputStream());
            outputStream.write(encodedPayload);
            outputStream.flush();
            outputStream.close();
        } catch (IOException ex) {
            this.api.error("Unable to send message to Discord!");
            return;
        }

        try {
            if (discordConnection.getResponseCode() != 200 && discordConnection.getResponseCode() != 204) {
                this.api.error(String.format("Got response code %s from Discord!", discordConnection.getResponseCode()));
            }
        } catch (IOException ex) {
            this.api.error("Unable to connect to Discord!");
        }
    }
}
