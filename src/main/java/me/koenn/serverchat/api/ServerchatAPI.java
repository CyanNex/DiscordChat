package me.koenn.serverchat.api;

import me.koenn.serverchat.api.discord.DiscordBot;
import me.koenn.serverchat.api.discord.Webhook;
import me.koenn.serverchat.api.discord.model.DiscordEmbed;
import me.koenn.serverchat.api.discord.model.DiscordMessage;
import me.koenn.serverchat.api.thread.IMessageThread;
import me.koenn.serverchat.api.thread.MessageThread;
import me.koenn.serverchat.api.util.IConfigManager;
import me.koenn.serverchat.api.util.MessageCallback;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class ServerchatAPI {

    private static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)" + '§' + "[0-9A-FK-OR]");
    private static final int RED = Integer.parseInt("FF0000", 16);
    private static final int GREEN = Integer.parseInt("00FF00", 16);

    private final String globalUsername;
    private final String globalAvatar;
    private final boolean disableMentionAll;
    private final boolean enableDeathMessages;
    private final boolean enableJoinLeaveMessages;
    private final String minecraftMessageFormat;
    private final String webhookURL;
    private final String discordToken;
    private final String discordStatusFormat;
    private final Activity.ActivityType discordStatusType;

    private final IMessageThread messageThread = new MessageThread(this);
    private final MessageCallback gameMessageCallback;
    private final Logger logger;

    private Webhook webhook;
    private DiscordBot bot;

    public ServerchatAPI(@NotNull IConfigManager configManager, @NotNull Logger logger, @NotNull MessageCallback gameMessageCallback) {
        this.globalUsername = configManager.getString("global_message_username", "config");
        this.globalAvatar = configManager.getString("global_message_avatar", "config");
        this.disableMentionAll = Boolean.parseBoolean(configManager.getString("disable_mention_all", "config"));
        this.enableDeathMessages = Boolean.parseBoolean(configManager.getString("enable_death_messages", "config"));
        this.enableJoinLeaveMessages = Boolean.parseBoolean(configManager.getString("enable_join_leave_messages", "config"));
        this.minecraftMessageFormat = parseColor(configManager.getString("minecraft_message_format", "config"));
        this.webhookURL = configManager.getString("webhook_url", "config");
        this.discordToken = configManager.getString("discord_token", "config");
        this.discordStatusFormat = configManager.getString("discord_status_format", "config");
        this.discordStatusType = Activity.ActivityType.valueOf(configManager.getString("discord_status_type", "config")
                .toUpperCase().replace("PLAYING", "DEFAULT"));

        this.logger = Objects.requireNonNull(logger);
        this.gameMessageCallback = Objects.requireNonNull(gameMessageCallback);
    }

    public void init() {
        if (this.webhookURL.contains("YOUR WEBHOOK URL HERE")) {
            this.error("Please put your Discord webhook URL in the config file to use this plugin!");
            return;
        }

        this.webhook = new Webhook(this.webhookURL, this);

        UUID token = UUID.randomUUID();
        this.bot = new DiscordBot(this.discordToken, token.toString(), this);
        this.bot.updateStatus(this.discordStatusFormat, this.discordStatusType, 0);

        this.webhook.sendMessage(new DiscordMessage(
                "VERIFY", "https://i.imgur.com/PVFIJhW.png",
                String.format("If you see this message, you setup your DiscordChat plugin incorrectly! VERIFY TOKEN: %s", token.toString())
        ));
    }

    public void disable() {
        if (this.bot != null && this.bot.getJda() != null) {
            this.bot.getJda().shutdown();
            this.webhook.sendMessage(new DiscordMessage(
                    this.globalUsername, this.globalAvatar,
                    "**Disconnected from Minecraft server!**"
            ));
        }
    }

    public void playerChat(@NotNull String playerName, @NotNull UUID playerUUID, @NotNull String message) {
        String avatar = String.format("https://crafatar.com/avatars/%s?overlay", playerUUID);
        message = this.disableMentionAll ? message.replace("@everyone", "everyone").replace("@here", "here") : message;
        Guild guild = Objects.requireNonNull(this.bot.getJda().getGuildById(this.bot.getGuild()));

        if (message.contains("@")) {

            for (Member member : guild.getMembers()) {
                String mention = "@" + member.getEffectiveName();

                if (message.toLowerCase().contains(mention.toLowerCase())) {
                    message = message.replace(mention, member.getAsMention());
                }
            }
        }

        this.messageThread.enqueue(new DiscordMessage(playerName, avatar, message));
    }

    public void playerDeath(@NotNull String deathMessage) {
        if (this.enableDeathMessages) {
            this.messageThread.enqueue(
                    new DiscordMessage(
                            this.globalUsername, this.globalAvatar,
                            new DiscordEmbed(deathMessage, "", RED)
                    )
            );
        }
    }

    public void playerJoin(@NotNull String playerName) {
        this.playerJoin(playerName, "the game");
    }

    public void playerJoin(@NotNull String playerName, @NotNull String serverName) {
        if (this.enableJoinLeaveMessages) {
            String joinMessage = String.format("%s joined %s", playerName, serverName);
            this.messageThread.enqueue(
                    new DiscordMessage(
                            this.globalUsername, this.globalAvatar,
                            new DiscordEmbed(joinMessage, "", GREEN)
                    )
            );
        }
    }

    public void playerQuit(@NotNull String playerName) {
        this.playerQuit(playerName, "the game");
    }

    public void playerQuit(@NotNull String playerName, @NotNull String serverName) {
        if (this.enableJoinLeaveMessages) {
            String leaveMessage = String.format("%s left %s", playerName, serverName);
            this.messageThread.enqueue(
                    new DiscordMessage(
                            this.globalUsername, this.globalAvatar,
                            new DiscordEmbed(leaveMessage, "", RED)
                    )
            );
        }
    }

    public void userChat(@NotNull String userName, @NotNull String message, @Nullable String attachmentURL) {
        String formatted = this.minecraftMessageFormat
                .replace("{user}", userName)
                .replace("{message}", stripColor(message));
        this.gameMessageCallback.message(formatted, attachmentURL);
    }

    public void error(@NotNull String message) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < message.length(); i++) {
            builder.append("#");
        }
        String line = builder.toString();

        this.logger.severe(line);
        this.logger.severe(message);
        this.logger.severe(line);
    }

    private static @NotNull String parseColor(@NotNull String string) {
        char[] b = string.toCharArray();

        for (int i = 0; i < b.length - 1; ++i) {
            if (b[i] == '&' && "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(b[i + 1]) > -1) {
                b[i] = 167;
                b[i + 1] = Character.toLowerCase(b[i + 1]);
            }
        }

        return new String(b);
    }

    private static @NotNull String stripColor(@NotNull String string) {
        return STRIP_COLOR_PATTERN.matcher(string).replaceAll("");
    }

    public void updatePlayerCount(int playerCount) {
        if (this.bot != null) {
            this.bot.updateStatus(this.discordStatusFormat, this.discordStatusType, playerCount);
        }
    }

    public void log(@NotNull String message) {
        this.logger.info(Objects.requireNonNull(message));
    }

    public @NotNull IMessageThread getMessageThread() {
        return Objects.requireNonNull(this.messageThread);
    }

    public @NotNull Webhook getWebhook() {
        return Objects.requireNonNull(this.webhook);
    }
}
