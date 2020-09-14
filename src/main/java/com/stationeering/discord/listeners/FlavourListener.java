package com.stationeering.discord.listeners;

import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlavourListener extends ListenerAdapter {
    private static Logger logger = LoggerFactory.getLogger(FlavourListener.class.getName());

    @Override
    public void onReady(ReadyEvent readyEvent) {
        logger.info("Discord is ready, setting status flavour...");
        readyEvent.getJDA().getPresence().setPresence(OnlineStatus.ONLINE, Activity.of(Activity.ActivityType.WATCHING, "Stationeers", "https://store.steampowered.com/app/544550/Stationeers/"));
    }
}
