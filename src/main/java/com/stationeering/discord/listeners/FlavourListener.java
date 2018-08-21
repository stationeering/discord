package com.stationeering.discord.listeners;

import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlavourListener extends ListenerAdapter {
    private static Logger logger = LoggerFactory.getLogger(FlavourListener.class.getName());

    @Override
    public void onReady(ReadyEvent readyEvent) {
        logger.info("Discord is ready, setting status flavour...");
        readyEvent.getJDA().getPresence().setGame(Game.of(Game.GameType.WATCHING, "Stationeers", "https://store.steampowered.com/app/544550/Stationeers/"));
    }
}
