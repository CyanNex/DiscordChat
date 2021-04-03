package me.koenn.serverchat.api.discord;

import me.koenn.serverchat.api.ServerchatAPI;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.jetbrains.annotations.NotNull;

import javax.security.auth.login.LoginException;
import java.util.Objects;

public class DiscordBot extends ListenerAdapter {

    private final ServerchatAPI api;
    private final String verifyToken;

    private long guild;
    private long channel;
    private JDA jda;

    private String status;

    public DiscordBot(@NotNull String discordToken, @NotNull String verifyToken, @NotNull ServerchatAPI api) {
        this.verifyToken = Objects.requireNonNull(verifyToken);
        this.api = Objects.requireNonNull(api);
        try {
            this.jda = JDABuilder.createDefault(Objects.requireNonNull(discordToken))
                    .setActivity(Activity.listening("to you!"))
                    .setAutoReconnect(true)
                    .setMaxReconnectDelay(500)
                    .enableIntents(GatewayIntent.GUILD_MEMBERS)
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .build().awaitReady();
            this.jda.addEventListener(this);
        } catch (LoginException e) {
            this.api.error("Unable to login to Discord, check if your token is correct and if you have an internet connection.");
        } catch (InterruptedException e) {
            this.api.error("Discord Bot thread interrupted! Please restart your server!");
        }
    }

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        Member author = event.getMember();
        User user = event.getAuthor();
        Guild guild = event.getGuild();
        Message message = event.getMessage();
        TextChannel channel = event.getChannel();

        if (this.guild == 0 && user.getName().equals("VERIFY")) {
            String content = message.getContentRaw();
            String[] split = content.split(" ");
            String token = split[split.length - 1].trim();
            if (this.verifyToken.equals(token)) {
                message.delete().queue();
                this.linkToChannel(channel);
                return;
            }
        }

        if (guild.getIdLong() == this.guild && channel.getIdLong() == this.channel && !user.isBot()) {
            String name;
            if (author != null) {
                name = author.getEffectiveName();
            } else {
                name = user.getName();
            }

            String attachmentURL = null;
            if (!message.getAttachments().isEmpty()) {
                attachmentURL = message.getAttachments().get(0).getUrl();
            }

            this.api.userChat(name, message.getContentDisplay(), attachmentURL);
        }
    }

    private void linkToChannel(@NotNull TextChannel channel) {
        this.guild = channel.getGuild().getIdLong();
        this.channel = channel.getIdLong();
        this.api.log(String.format(
                "Successfully linked to Discord server '%s#%s'!",
                channel.getGuild().getName(), channel.getName()
        ));
        channel.sendMessage("**Connected to Minecraft server!**").queue();

        channel.getGuild().loadMembers();
    }

    public void updateStatus(@NotNull String format, @NotNull Activity.ActivityType type, int playerCount) {
        if (this.jda != null) {
            String status = format.replace("{players}", String.valueOf(playerCount));
            if (!status.equals(this.status)) {
                this.status = status;
                this.jda.getPresence().setActivity(Activity.of(Objects.requireNonNull(type), this.status));
            }
        }
    }

    public JDA getJda() {
        return jda;
    }

    public long getGuild() {
        return guild;
    }

    public long getChannel() {
        return channel;
    }
}
