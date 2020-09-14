package com.stationeering.discord.listeners;

import com.stationeering.discord.managers.GuildManager;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.GuildAvailableEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.GuildUnavailableEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GuildListener extends ListenerAdapter {
    private static Logger logger = LoggerFactory.getLogger(GuildListener.class.getName());

    private final GuildManager guildManager;

    public GuildListener(GuildManager guildManager) {
        this.guildManager = guildManager;
    }

    @Override
    public void onReady(ReadyEvent readyEvent) {
        logger.info("Discord is ready, enumerating current guilds...");
        readyEvent.getJDA().getGuilds().stream().forEach((g) -> {
            guildManager.start(g.getIdLong(), g.getName());
        });
    }

    @Override
    public void onGuildJoin(GuildJoinEvent guildJoinEvent) {
        long guildId = guildJoinEvent.getGuild().getIdLong();
        String guildName = guildJoinEvent.getGuild().getName();

        logger.info("Bot joining new guild: " + guildId + " / " + guildName);
        guildManager.start(guildId, guildName);
    }

    @Override
    public void onGuildLeave(GuildLeaveEvent guildLeaveEvent) {
        long guildId = guildLeaveEvent.getGuild().getIdLong();

        logger.info("Bot joining leaving guild: " + guildId);
        guildManager.leave(guildId);
    }

    @Override
    public void onGuildAvailable(GuildAvailableEvent guildAvailableEvent) {
        long guildId = guildAvailableEvent.getGuild().getIdLong();

        logger.info("Guild has become available: " + guildId);
        guildManager.changeAvailability(guildId, true);
    }

    @Override
    public void onGuildUnavailable(GuildUnavailableEvent guildUnavailableEvent) {
        long guildId = guildUnavailableEvent.getGuild().getIdLong();

        logger.info("Guild has become unavailable: " + guildId);
        guildManager.changeAvailability(guildId, false);
    }
}
