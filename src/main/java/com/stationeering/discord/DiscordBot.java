package com.stationeering.discord;

import com.stationeering.discord.listeners.CommandListener;
import com.stationeering.discord.listeners.FlavourListener;
import com.stationeering.discord.listeners.GuildListener;
import com.stationeering.discord.managers.GuildManager;
import com.stationeering.discord.managers.VersionManager;
import com.stationeering.discord.persistence.GuildPersistence;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;

public class DiscordBot {
    private static Logger logger = LoggerFactory.getLogger(DiscordBot.class.getName());

    public static void main(String[] args) {
        final String DISCORD_TOKEN = System.getenv("DISCORD_TOKEN");
        final String AWS_SQS_URL = System.getenv("AWS_SQS_URL");

        logger.info("Welcome to stationeering.com discord bot, starting...");

        if (DISCORD_TOKEN == null) {
            logger.error("Discord token was not provided!");
            System.exit(-1);
        }

        if (AWS_SQS_URL == null) {
            logger.error("SQS URL was not provided!");
            System.exit(-1);
        }

        final GuildManager guildManager = new GuildManager(new GuildPersistence());
        final JDA jda;

        final JDABuilder jdaBuilder = JDABuilder.createDefault (DISCORD_TOKEN);
        jdaBuilder.addEventListeners(new FlavourListener());
        jdaBuilder.addEventListeners(new GuildListener(guildManager));
        jdaBuilder.addEventListeners(new CommandListener(guildManager));

        try {
            jda = jdaBuilder.build();
        } catch (LoginException e) {
            logger.error("Could not log into Discord!", e);
            return;
        }

        new Thread(new VersionManager(jda, AWS_SQS_URL, guildManager)).start();
    }
}
