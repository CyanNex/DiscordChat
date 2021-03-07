package me.koenn.serverchat.api.discord;

import me.koenn.serverchat.api.ServerchatAPI;
import me.koenn.serverchat.api.discord.model.IDiscordMessage;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class Webhook implements IWebhook {

    private static final String USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64; rv:49.0) Gecko/20100101 Firefox/49.0";

    private final ServerchatAPI api;
    private final URI url;

    private final CloseableHttpClient httpClient;

    public Webhook(@NotNull String url, @NotNull ServerchatAPI api) {
        this.api = Objects.requireNonNull(api);

        try {
            this.url = new URI(Objects.requireNonNull(url));
        } catch (URISyntaxException ex) {
            throw new RuntimeException("Unable to parse URL", ex);
        }

        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(2000)
                .setConnectionRequestTimeout(2000)
                .setSocketTimeout(2000)
                .setAuthenticationEnabled(false)
                .setCircularRedirectsAllowed(false)
                .build();

        this.httpClient = HttpClients.custom()
                .setUserAgent(USER_AGENT)
                .setDefaultRequestConfig(config)
                .build();
    }

    @Override
    public void sendMessage(@NotNull IDiscordMessage message) {
        Objects.requireNonNull(message);

        String data = message.toJSON().toString();
        byte[] payload = data.getBytes(StandardCharsets.UTF_8);

        HttpPost post = new HttpPost(this.url);
        post.setEntity(new ByteArrayEntity(payload, ContentType.APPLICATION_JSON));

        try (CloseableHttpResponse response = this.httpClient.execute(post)) {

            int responseCode = response.getStatusLine().getStatusCode();
            if (responseCode != 200 && responseCode != 204) {
                this.api.error(String.format("Got response code %s from Discord!", responseCode));
            }

        } catch (ClientProtocolException ex) {
            this.api.error("HTTP protocol error occurred");
            ex.printStackTrace();
        } catch (IOException ex) {
            this.api.error("Unable to connect to Discord!");
            ex.printStackTrace();
        }
    }
}
