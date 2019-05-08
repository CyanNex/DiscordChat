package me.koenn.serverchat.api;

import me.koenn.serverchat.api.discord.DiscordBot;
import me.koenn.serverchat.api.discord.Webhook;
import me.koenn.serverchat.api.discord.model.DiscordEmbed;
import me.koenn.serverchat.api.discord.model.DiscordMessage;
import me.koenn.serverchat.api.util.IConfigManager;
import me.koenn.serverchat.api.util.MessageCallback;
import me.koenn.serverchat.api.util.MessageThread;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;

import java.net.MalformedURLException;
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

    private final MessageThread messageThread = new MessageThread(this);
    private final MessageCallback callback;
    private final Logger logger;

    private Webhook webhook;
    private DiscordBot bot;

    public ServerchatAPI(IConfigManager configManager, Logger logger, MessageCallback callback) {
        this.globalUsername = configManager.getString("global_message_username", "config");
        this.globalAvatar = configManager.getString("global_message_avatar", "config");
        this.disableMentionAll = Boolean.parseBoolean(configManager.getString("disable_mention_all", "config"));
        this.enableDeathMessages = Boolean.parseBoolean(configManager.getString("enable_death_messages", "config"));
        this.enableJoinLeaveMessages = Boolean.parseBoolean(configManager.getString("enable_join_leave_messages", "config"));
        this.minecraftMessageFormat = parseColor(configManager.getString("minecraft_message_format", "config"));
        this.webhookURL = configManager.getString("webhook_url", "config");
        this.discordToken = configManager.getString("discord_token", "config");

        this.logger = logger;
        this.callback = callback;
    }

    public void init() {
        if (this.webhookURL.contains("YOUR WEBHOOK URL HERE")) {
            this.error("Please put your Discord webhook URL in the config file to use this plugin!");
            return;
        }

        try {
            webhook = new Webhook(this.webhookURL, this);
        } catch (MalformedURLException e) {
            this.error("Your provided Discord webhook URL is invalid!");
            return;
        }

        UUID token = UUID.randomUUID();
        this.bot = new DiscordBot(this.discordToken, token.toString(), this);

        webhook.sendMessage(new DiscordMessage(
                "VERIFY", "https://i.imgur.com/PVFIJhW.png",
                String.format("If you see this message, you setup your DiscordChat plugin incorrectly! VERIFY TOKEN: %s", token.toString())
        ));
    }

    public void disable() {
        if (this.bot != null && this.bot.getJda() != null) {
            this.bot.getJda()
                    .getGuildById(this.bot.getGuild())
                    .getTextChannelById(this.bot.getChannel())
                    .sendMessage("**Disconnected from Minecraft server!**")
                    .queue();
            this.bot.getJda().shutdown();
        }
    }

    public void playerChat(String playerName, UUID playerUUID, String message) {
        String avatar = String.format("https://crafatar.com/avatars/%s?overlay", String.valueOf(playerUUID));
        message = disableMentionAll ? message.replace("@everyone", "everyone").replace("@here", "here") : message;
        TextChannel channel = bot.getJda().getGuildById(bot.getGuild()).getTextChannelById(bot.getChannel());

        for (Member member : channel.getMembers()) {
            String mention = "@" + member.getEffectiveName();

            if (message.toLowerCase().contains(mention.toLowerCase())) {
                message = message.replace(mention, member.getAsMention());
            }
        }

        MessageThread.MESSAGE_QUEUE.add(new DiscordMessage(playerName, avatar, message));
    }

    public void playerDeath(String deathMessage) {
        if (this.enableDeathMessages) {
            MessageThread.MESSAGE_QUEUE.add(
                    new DiscordMessage(
                            this.globalUsername, this.globalAvatar,
                            new DiscordEmbed(deathMessage, "", RED)
                    )
            );
        }
    }

    public void playerJoin(String playerName) {
        this.playerJoin(playerName, "the game");
    }

    public void playerJoin(String playerName, String serverName) {
        if (this.enableJoinLeaveMessages) {
            String joinMessage = String.format("%s joined %s", playerName, serverName);
            MessageThread.MESSAGE_QUEUE.add(
                    new DiscordMessage(
                            this.globalUsername, this.globalAvatar,
                            new DiscordEmbed(joinMessage, "", GREEN)
                    )
            );
        }
    }

    public void playerQuit(String playerName) {
        this.playerQuit(playerName, "the game");
    }

    public void playerQuit(String playerName, String serverName) {
        if (this.enableJoinLeaveMessages) {
            String leaveMessage = String.format("%s left %s", playerName, serverName);
            MessageThread.MESSAGE_QUEUE.add(
                    new DiscordMessage(
                            this.globalUsername, this.globalAvatar,
                            new DiscordEmbed(leaveMessage, "", RED)
                    )
            );
        }
    }

    public void userChat(String userName, String message) {
        String formatted = this.minecraftMessageFormat
                .replace("{user}", userName)
                .replace("{message}", stripColor(message));
        this.callback.message(formatted);
    }

    public void error(String message) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < message.length(); i++) {
            builder.append("#");
        }
        String line = builder.toString();

        this.logger.severe(line);
        this.logger.severe(message);
        this.logger.severe(line);
    }

    private static String parseColor(String string) {
        char[] b = string.toCharArray();

        for (int i = 0; i < b.length - 1; ++i) {
            if (b[i] == '&' && "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(b[i + 1]) > -1) {
                b[i] = 167;
                b[i + 1] = Character.toLowerCase(b[i + 1]);
            }
        }

        return new String(b);
    }

    private static String stripColor(String string) {
        return string == null ? null : STRIP_COLOR_PATTERN.matcher(string).replaceAll("");
    }

    public void log(String message) {
        this.logger.info(message);
    }

    public MessageThread getMessageThread() {
        return this.messageThread;
    }

    public Webhook getWebhook() {
        return this.webhook;
    }
}
