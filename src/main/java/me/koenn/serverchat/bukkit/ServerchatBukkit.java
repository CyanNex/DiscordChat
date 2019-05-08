package me.koenn.serverchat.bukkit;

import me.koenn.serverchat.api.ServerchatAPI;
import me.koenn.serverchat.api.util.MessageCallback;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class ServerchatBukkit extends JavaPlugin implements Listener, MessageCallback {

    public ServerchatAPI api;

    @Override
    public void onEnable() {
        if (!this.getDataFolder().exists()) {
            this.getDataFolder().mkdir();
        }

        if (!(new File(this.getDataFolder(), "api/config.yml")).exists()) {
            this.saveDefaultConfig();
        }

        this.api = new ServerchatAPI(new ConfigManager(this.getConfig()), this.getLogger(), this);
        this.api.init();

        Bukkit.getPluginManager().registerEvents(this, this);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, this.api.getMessageThread(), 0, 1);
    }

    @Override
    public void onDisable() {
        this.api.disable();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        this.api.playerChat(player.getName(), player.getUniqueId(), event.getMessage());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        this.api.playerDeath(event.getDeathMessage());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        this.api.playerJoin(event.getPlayer().getName());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        this.api.playerQuit(event.getPlayer().getName());
    }

    @Override
    public void message(String message) {
        Bukkit.broadcastMessage(message);
    }
}
