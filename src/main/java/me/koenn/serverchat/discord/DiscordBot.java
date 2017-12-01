package me.koenn.serverchat.discord;

import me.koenn.serverchat.Serverchat;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import javax.security.auth.login.LoginException;

public class DiscordBot extends ListenerAdapter {

    private long guild;
    private long channel;
    private JDA jda;

    public DiscordBot(String token) {
        try {
            this.jda = new JDABuilder(AccountType.BOT)
                    .setToken(token)
                    .setGame(Game.listening("to you!"))
                    .buildBlocking();
            jda.addEventListener(this);

        } catch (LoginException e) {
            Serverchat.instance.error("Unable to login to Discord, do you have an internet connection?");
        } catch (InterruptedException e) {
            Serverchat.instance.error("Thread interrupted! Please restart your server!");
        } catch (RateLimitedException e) {
            Serverchat.instance.error("Rate-limited by Discord, please restart your server!");
        }
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        User author = event.getAuthor();
        Guild guild = event.getGuild();
        Message message = event.getMessage();
        TextChannel channel = event.getChannel();

        if (this.guild == 0 && author.getName().equals("VERIFY")) {
            String token = message.getContent().split(" ")[message.getContent().split(" ").length - 1].trim();
            if (Serverchat.token.toString().equals(token)) {
                this.guild = guild.getIdLong();
                this.channel = channel.getIdLong();
                Serverchat.log("Successfully linked to Discord server \'" + guild.getName() + "\'!");
                channel.sendMessage("**Connected to Minecraft server!**").queue();
                message.delete().queue();
            }
            return;
        }

        if (guild.getIdLong() != this.guild || channel.getIdLong() != this.channel || author.isBot()) {
            return;
        }

        Member member = guild.getMember(author);
        String formatted = Serverchat.instance.minecraftMessageFormat
                .replace("{user}", member.getNickname() == null ? author.getName() : member.getNickname())
                .replace("{message}", message.getContent());

        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', formatted));
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
