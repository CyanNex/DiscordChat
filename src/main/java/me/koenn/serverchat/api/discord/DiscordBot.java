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

    public DiscordBot(String discordToken, String verifyToken, ServerchatAPI api) {
        this.verifyToken = verifyToken;
        this.api = api;
        try {
            this.jda = new JDABuilder(AccountType.BOT)
                    .setToken(discordToken)
                    .setGame(Game.listening("to you!"))
                    .build().awaitReady();
            jda.addEventListener(this);
        } catch (LoginException e) {
            this.api.error("Unable to login to Discord, do you have an internet connection?");
        } catch (InterruptedException e) {
            this.api.error("Thread interrupted! Please restart your server!");
        }
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        User author = event.getAuthor();
        Guild guild = event.getGuild();
        Message message = event.getMessage();
        TextChannel channel = event.getChannel();

        if (this.guild == 0 && author.getName().equals("VERIFY")) {
            String token = message.getContentRaw().split(" ")[message.getContentRaw().split(" ").length - 1].trim();
            if (this.verifyToken.equals(token)) {
                this.guild = guild.getIdLong();
                this.channel = channel.getIdLong();
                this.api.log("Successfully linked to Discord server \'" + guild.getName() + "\'!");
                channel.sendMessage("**Connected to Minecraft server!**").queue();
                message.delete().queue();
            }
            return;
        }

        if (guild.getIdLong() != this.guild || channel.getIdLong() != this.channel || author.isBot()) {
            return;
        }

        Member member = guild.getMember(author);
        String name = member.getNickname() == null ? author.getName() : member.getNickname();
        this.api.userChat(name, message.getContentDisplay());
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
