package me.koenn.serverchat.api.discord;

import me.koenn.serverchat.api.ServerchatAPI;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import javax.security.auth.login.LoginException;

public class DiscordBot extends ListenerAdapter {

    private final ServerchatAPI api;
    private final String verifyToken;

    private long guild;
    private long channel;
    private JDA jda;

    private String status;

    public DiscordBot(String discordToken, String verifyToken, ServerchatAPI api) {
        this.verifyToken = verifyToken;
        this.api = api;
        try {
            this.jda = new JDABuilder(AccountType.BOT)
                    .setToken(discordToken)
                    .setAudioEnabled(false)
                    .setGame(Game.listening("to you!"))
                    .build().awaitReady();
            jda.addEventListener(this);
        } catch (LoginException e) {
            this.api.error("Unable to login to Discord, check if your token is correct and if you have an internet connection.");
        } catch (InterruptedException e) {
            this.api.error("Discord Bot thread interrupted! Please restart your server!");
        }
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        User author = event.getAuthor();
        Guild guild = event.getGuild();
        Message message = event.getMessage();
        TextChannel channel = event.getChannel();

        if (this.guild == 0 && author.getName().equals("VERIFY")) {
            String content = message.getContentRaw();
            String[] split = content.split(" ");
            String token = split[split.length - 1].trim();
            if (this.verifyToken.equals(token)) {
                message.delete().queue();
                this.linkToChannel(channel);
                return;
            }
        }

        if (guild.getIdLong() == this.guild && channel.getIdLong() == this.channel && !author.isBot()) {
            Member member = guild.getMember(author);
            String name = member.getNickname() == null ? author.getName() : member.getNickname();
            this.api.userChat(name, message.getContentDisplay());
        }
    }

    private void linkToChannel(TextChannel channel) {
        this.guild = channel.getGuild().getIdLong();
        this.channel = channel.getIdLong();
        this.api.log(String.format(
                "Successfully linked to Discord server \'%s#%s\'!",
                channel.getGuild().getName(), channel.getName()
        ));
        channel.sendMessage("**Connected to Minecraft server!**").queue();
    }

    public void updateStatus(String format, Game.GameType type, int playerCount) {
        if (this.jda != null) {
            String status = format.replace("{players}", String.valueOf(playerCount));
            if (!status.equals(this.status)) {
                this.status = status;
                this.jda.getPresence().setGame(Game.of(type, this.status));
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
