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
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.event.EventHandler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

public final class ServerchatBungee extends Plugin implements Listener, MessageCallback {

    public ServerchatAPI api;

    @Override
    public void onEnable() {
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
        ProxyServer.getInstance().getScheduler().schedule(this, this.api.getMessageThread(), 0, 25, TimeUnit.MILLISECONDS);
    }

    @Override
    public void onDisable() {
        this.api.disable();
    }

    @EventHandler
    public void onChat(ChatEvent event) {
        ProxiedPlayer player = (ProxiedPlayer) event.getSender();
        this.api.playerChat(player.getName(), player.getUniqueId(), event.getMessage());
    }

    @EventHandler
    public void onServerConnected(ServerConnectedEvent event) {
        this.api.playerJoin(event.getPlayer().getName(), event.getServer().getInfo().getName());
    }

    @EventHandler
    public void onPlayerDisconnect(PlayerDisconnectEvent event) {
        if (event.getPlayer() == null || event.getPlayer().getServer() == null) {
            return;
        }
        this.api.playerQuit(event.getPlayer().getName(), event.getPlayer().getServer().getInfo().getName());
    }

    @Override
    public void message(String message) {
        ProxyServer.getInstance().broadcast(new TextComponent(message));
    }
}
