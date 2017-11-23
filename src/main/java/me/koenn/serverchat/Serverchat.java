package me.koenn.serverchat;

import com.mrpowergamerbr.temmiewebhook.DiscordEmbed;
import com.mrpowergamerbr.temmiewebhook.DiscordMessage;
import com.mrpowergamerbr.temmiewebhook.TemmieWebhook;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class Serverchat extends JavaPlugin implements Listener {

    private static TemmieWebhook webhook;
    private static Serverchat instance;

    private String globalUsername;
    private String globalAvatar;
    private boolean disableMentionAll;
    private boolean enableDeathMessages;
    private boolean enableJoinLeaveMessages;

    public static void log(String message) {
        instance.getLogger().info(message);
    }

    @Override
    public void onEnable() {
        instance = this;

        ConfigManager configManager = new ConfigManager(this);
        webhook = new TemmieWebhook(configManager.getString("webhook_url", "config"), false);
        globalUsername = configManager.getString("global_message_username", "config");
        globalAvatar = configManager.getString("global_message_avatar", "config");
        disableMentionAll = Boolean.parseBoolean(configManager.getString("disable_mention_all", "config"));
        enableDeathMessages = Boolean.parseBoolean(configManager.getString("enable_death_messages", "config"));
        enableJoinLeaveMessages = Boolean.parseBoolean(configManager.getString("enable_join_leave_messages", "config"));

        if (webhook.url.equals("[YOUR WEBHOOK URL HERE]")) {
            this.getLogger().severe("##########################################################################");
            this.getLogger().severe("Please put your Discord webhook URL in the config file to use this plugin!");
            this.getLogger().severe("##########################################################################");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String avatar = String.format("https://crafatar.com/avatars/%s?overlay", String.valueOf(player.getName()));
        String message = disableMentionAll ? event.getMessage().replace("@everyone", "everyone").replace("@here", "here") : event.getMessage();
        webhook.sendMessage(
                new DiscordMessage(
                        player.getName(),
                        message, avatar
                )
        );
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (enableDeathMessages) {
            webhook.sendMessage(
                    DiscordMessage.builder()
                            .embed(DiscordEmbed.builder()
                                    .title(ChatColor.stripColor(event.getDeathMessage()))
                                    .color(16711680).build()
                            )
                            .username(globalUsername)
                            .avatarUrl(globalAvatar).build()
            );
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (enableJoinLeaveMessages) {
            String joinMessage = String.format("%s joined the game", event.getPlayer().getName());
            webhook.sendMessage(
                    DiscordMessage.builder()
                            .embed(DiscordEmbed.builder()
                                    .title(ChatColor.stripColor(joinMessage))
                                    .color(65280).build()
                            )
                            .username(globalUsername)
                            .avatarUrl(globalAvatar).build()
            );
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (enableJoinLeaveMessages) {
            String leaveMessage = String.format("%s left the game", event.getPlayer().getName());
            webhook.sendMessage(
                    DiscordMessage.builder()
                            .embed(DiscordEmbed.builder()
                                    .title(ChatColor.stripColor(leaveMessage))
                                    .color(16711680).build()
                            )
                            .username(globalUsername)
                            .avatarUrl(globalAvatar).build()
            );
        }
    }
}
