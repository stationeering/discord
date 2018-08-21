package com.stationeering.discord.managers;

import com.stationeering.discord.persistence.GuildPersistence;
import com.stationeering.discord.state.GuildState;
import com.stationeering.discord.state.MessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class GuildManager {
    private static Logger logger = LoggerFactory.getLogger(GuildManager.class.getName());

    private final Map<Long, GuildState> guildStorage = new ConcurrentHashMap<>();
    private final GuildPersistence guildPersistence;

    public GuildManager(GuildPersistence guildPersistence) {
        this.guildPersistence = guildPersistence;
    }

    public void start(long guildId) {
        GuildState guildState = guildStorage.computeIfAbsent(guildId, GuildState::new);
        guildPersistence.load(guildState);
        guildState.setAvailable(true);
    }

    public void leave(long guildId) {
        GuildState guildState = guildStorage.remove(guildId);
        guildPersistence.delete(guildState);
    }

    public void changeAvailability(long guildId, boolean available) {
        GuildState guildState = guildStorage.get(guildId);

        if (guildState != null) {
            guildState.setAvailable(available);
        } else {
            logger.error("Attempting to change nonexistant guild availability! ID: " + guildId + " New Availability: " + available);
        }
    }

    public GuildState getGuildState(long guildId) {
        return guildStorage.get(guildId);
    }

    public Set<Long> getDestinations(MessageType messageType) {
        return guildStorage.values().stream()
                .filter(GuildState::isAvailable)
                .map((g) -> g.getDestination(messageType))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
    }
}
