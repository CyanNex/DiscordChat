package me.koenn.serverchat.bungee;

import me.koenn.serverchat.api.ServerchatAPI;
import me.koenn.serverchat.api.util.MessageCallback;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginDescription;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.event.EventHandler;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class ServerchatBungee extends Plugin implements Listener, MessageCallback {

    public ServerchatAPI api;

    private ScheduledExecutorService service;

    @Override
    public void onEnable() {
        PluginDescription description = this.getDescription();
        this.getLogger().info(String.format("Loading %s v%s for BungeeCord %s",
                description.getName(), description.getVersion(),
                this.getProxy().getVersion()
        ));

        if (!this.getDataFolder().exists()) {
            this.getDataFolder().mkdir();
        }

        File file = new File(this.getDataFolder(), "config.yml");
        if (!file.exists()) {
            try (InputStream in = getResourceAsStream("config.yml")) {
                Files.copy(in, file.toPath());
            } catch (IOException e) {
                this.api.error("Unable to copy config file to plugin directory");
                return;
            }
        }
        Configuration configuration;
        try {
            configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
        } catch (IOException e) {
            this.api.error("Unable to read config file. Try deleting it and restarting your server");
            return;
        }

        this.api = new ServerchatAPI(new ConfigManager(configuration), this.getLogger(), this);
        this.api.init();

        ProxyServer.getInstance().getPluginManager().registerListener(this, this);

        this.service = Executors.newScheduledThreadPool(2);
        this.service.submit(this.api.getMessageThread());
        this.service.scheduleAtFixedRate(this::updatePlayerCount, 0, 1, TimeUnit.SECONDS);
    }

    @Override
    public void onDisable() {
        this.api.disable();
        this.service.shutdownNow();
    }

    @EventHandler
    public void onChat(@NotNull ChatEvent event) {
        ProxiedPlayer player = (ProxiedPlayer) event.getSender();
        String message = event.getMessage();
        if (!message.startsWith("/")) {
            this.api.playerChat(player.getName(), player.getUniqueId(), event.getMessage());
        }
    }

    @EventHandler
    public void onServerConnected(@NotNull ServerConnectedEvent event) {
        this.api.playerJoin(event.getPlayer().getName(), event.getServer().getInfo().getName());
    }

    @EventHandler
    public void onPlayerDisconnect(@NotNull PlayerDisconnectEvent event) {
        if (event.getPlayer() == null || event.getPlayer().getServer() == null) {
            return;
        }
        this.api.playerQuit(event.getPlayer().getName(), event.getPlayer().getServer().getInfo().getName());
    }

    @Override
    public void message(@NotNull String message) {
        ProxyServer.getInstance().broadcast(new TextComponent(message));
    }

    private void updatePlayerCount() {
        this.api.updatePlayerCount(ProxyServer.getInstance().getOnlineCount());
    }
}
