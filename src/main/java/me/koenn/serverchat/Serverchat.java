package me.koenn.serverchat;

import me.koenn.serverchat.discord.DiscordBot;
import me.koenn.serverchat.discord.DiscordEmbed;
import me.koenn.serverchat.discord.DiscordMessage;
import me.koenn.serverchat.discord.Webhook;
import me.koenn.serverchat.util.ConfigManager;
import me.koenn.serverchat.util.MessageThread;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
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
import org.bukkit.permissions.ServerOperator;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.MalformedURLException;
import java.util.UUID;

public final class Serverchat extends JavaPlugin implements Listener {

    private static final int RED = Integer.parseInt("FF0000", 16);
    private static final int GREEN = Integer.parseInt("00FF00", 16);

    public static Serverchat instance;
    public static UUID token;
    public static Webhook webhook;

    public String globalUsername;
    public String globalAvatar;
    public boolean disableMentionAll;
    public boolean enableDeathMessages;
    public boolean enableJoinLeaveMessages;
    public String minecraftMessageFormat;

    private String error;
    private DiscordBot bot;
    private Thread messageThread;

    public static void log(String message) {
        instance.getLogger().info(message);
    }

    public static void severe(String message) {
        instance.getLogger().severe(message);
    }

    @Override
    public void onEnable() {
        instance = this;

        ConfigManager configManager = new ConfigManager(this);

        Bukkit.getPluginManager().registerEvents(this, this);

        String url = configManager.getString("webhook_url", "config");
        if (url.equals("[YOUR WEBHOOK URL HERE]")) {
            this.error("Please put your Discord webhook URL in the config file to use this plugin!");
            return;
        }

        try {
            webhook = new Webhook(url);
        } catch (MalformedURLException e) {
            this.error("Your provided Discord webhook URL is invalid!");
            return;
        }

        this.bot = new DiscordBot(configManager.getString("discord_token", "config"));

        token = UUID.randomUUID();
        webhook.sendMessage(new DiscordMessage(
                "VERIFY", "https://i.imgur.com/PVFIJhW.png",
                String.format("If you see this message, you setup your DiscordChat plugin incorrectly! VERIFY TOKEN: %s", token.toString())
        ));

        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new MessageThread(), 0, 1);

        globalUsername = configManager.getString("global_message_username", "config");
        globalAvatar = configManager.getString("global_message_avatar", "config");
        disableMentionAll = Boolean.parseBoolean(configManager.getString("disable_mention_all", "config"));
        enableDeathMessages = Boolean.parseBoolean(configManager.getString("enable_death_messages", "config"));
        enableJoinLeaveMessages = Boolean.parseBoolean(configManager.getString("enable_join_leave_messages", "config"));
        minecraftMessageFormat = configManager.getString("minecraft_message_format", "config");
    }

    @Override
    public void onDisable() {
        if (this.bot != null && this.bot.getJda() != null) {
            this.bot.getJda()
                    .getGuildById(this.bot.getGuild())
                    .getTextChannelById(this.bot.getChannel())
                    .sendMessage("**Disconnected from Minecraft server!**")
                    .queue();
            this.bot.getJda().shutdown();
        }
    }

    public void error(String message) {
        this.error = message;

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < message.length(); i++) {
            builder.append("#");
        }
        String line = builder.toString();

        this.getLogger().severe(line);
        this.getLogger().severe(message);
        this.getLogger().severe(line);

        Bukkit.getOnlinePlayers().stream().filter(ServerOperator::isOp).forEach(player -> player.sendMessage(String.format(
                "%s%sAn error has occurred in %s: %s",
                ChatColor.RED, ChatColor.BOLD,
                this.getDescription().getName(),
                this.error
        )));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        if (this.error != null) {
            return;
        }

        Player player = event.getPlayer();
        String avatar = String.format("https://crafatar.com/avatars/%s?overlay", String.valueOf(player.getName()));
        String message = disableMentionAll ? event.getMessage().replace("@everyone", "everyone").replace("@here", "here") : event.getMessage();
        TextChannel channel = bot.getJda().getGuildById(bot.getGuild()).getTextChannelById(bot.getChannel());

        for (Member member : channel.getMembers()) {
            String mention = "@" + member.getEffectiveName();

            if (message.contains(mention)) {
                message = message.replace(mention, member.getAsMention());
            }
        }

        MessageThread.MESSAGE_QUEUE.add(new DiscordMessage(
                player.getName(),
                avatar, message
        ));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (this.error != null) {
            return;
        }

        if (this.enableDeathMessages) {
            MessageThread.MESSAGE_QUEUE.add(
                    new DiscordMessage(
                            this.globalUsername, this.globalAvatar,
                            new DiscordEmbed(event.getDeathMessage(), "", RED)
                    )
            );
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (this.error != null && event.getPlayer().isOp()) {
            player.sendMessage(String.format(
                    "%s%sAn error has occurred in %s: %s",
                    ChatColor.RED, ChatColor.BOLD,
                    this.getDescription().getName(),
                    this.error
            ));
            return;
        }

        if (this.enableJoinLeaveMessages) {
            String joinMessage = String.format("%s joined the game", player.getName());
            MessageThread.MESSAGE_QUEUE.add(
                    new DiscordMessage(
                            this.globalUsername, this.globalAvatar,
                            new DiscordEmbed(joinMessage, "", GREEN)
                    )
            );
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (this.error != null) {
            return;
        }

        if (this.enableJoinLeaveMessages) {
            String leaveMessage = String.format("%s left the game", event.getPlayer().getName());
            MessageThread.MESSAGE_QUEUE.add(
                    new DiscordMessage(
                            this.globalUsername, this.globalAvatar,
                            new DiscordEmbed(leaveMessage, "", RED)
                    )
            );
        }
    }
}
