package me.koenn.serverchat.api.discord;

import me.koenn.serverchat.api.ServerchatAPI;
import me.koenn.serverchat.api.discord.model.DiscordMessage;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class Webhook {

    private static final String USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64; rv:49.0) Gecko/20100101 Firefox/49.0";
    private final URL url;
    private final ServerchatAPI api;

    public Webhook(String url, ServerchatAPI api) throws MalformedURLException {
        this.url = new URL(url);
        this.api = api;
    }

    private HttpURLConnection connect() {
        try {
            HttpURLConnection connection = (HttpURLConnection) this.url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("User-Agent", USER_AGENT);
            connection.setRequestProperty("Content-type", "application/json; charset=UTF-8");
            connection.setDoOutput(true);
            connection.setDoInput(true);

            return connection;
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public void sendMessage(DiscordMessage message) {
        HttpURLConnection connection = this.connect();
        if (connection == null) {
            this.api.error("Unable to connect to Discord!");
            return;
        }

        String payload = message.toJSON().toString();
        byte[] encodedPayload = payload.getBytes(StandardCharsets.UTF_8);
        connection.setRequestProperty("Content-length", String.valueOf(encodedPayload.length));

        try {
            DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
            outputStream.write(encodedPayload);
            outputStream.flush();
            outputStream.close();
        } catch (IOException ex) {
            this.api.error("Unable to send message to Discord!");
            return;
        }

        try {
            if (connection.getResponseCode() != 200 && connection.getResponseCode() != 204) {
                this.api.error(String.format("Got response code %s from Discord!", connection.getResponseCode()));
            }
        } catch (IOException ex) {
            this.api.error("Unable to connect to Discord!");
        }
    }
}
