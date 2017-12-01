package me.koenn.serverchat.discord;

import me.koenn.serverchat.Serverchat;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class Webhook {

    private static final String USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64; rv:49.0) Gecko/20100101 Firefox/49.0";
    private final URL url;

    public Webhook(String url) throws MalformedURLException {
        this.url = new URL(url);
    }

    private HttpURLConnection connect() {
        try {
            HttpURLConnection connection = (HttpURLConnection) this.url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("User-Agent", USER_AGENT);
            connection.setRequestProperty("Content-type", "application/json; charset=windows-1252");
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
            Serverchat.severe("Unable to connect to Discord!");
            return;
        }

        String payload = message.toJSON().toJSONString();
        connection.setRequestProperty("Content-length", String.valueOf(payload.length()));

        try {
            DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
            outputStream.writeBytes(payload);
            outputStream.flush();
            outputStream.close();
        } catch (IOException ex) {
            Serverchat.severe("Unable to send message to Discord!");
            return;
        }

        try {
            if (connection.getResponseCode() != 200 && connection.getResponseCode() != 204) {
                Serverchat.severe(String.format("Got response code %s from Discord!", connection.getResponseCode()));
            }
        } catch (IOException ex) {
            Serverchat.severe("Unable to connect to Discord!");
        }
    }
}
