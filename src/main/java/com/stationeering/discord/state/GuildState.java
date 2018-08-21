package com.stationeering.discord.state;

import com.stationeering.discord.persistence.GuildPersistence;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class GuildState {
    private final long id;
    private boolean available;
    private GuildPersistence guildPersistence = null;
    private final Map<MessageType, Long> messageTypeDestinations = new ConcurrentHashMap<>();

    public GuildState(long id) {
        this.id = id;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public Optional<Long> getDestination(MessageType messageType) {
        return Optional.ofNullable(messageTypeDestinations.get(messageType));
    }

    public void setDestination(MessageType messageType, long channelId) {
        messageTypeDestinations.put(messageType, channelId);

        if (guildPersistence != null) {
            guildPersistence.save(this);
        }
    }

    public void setGuildPersistence(GuildPersistence guildPersistence) {
        this.guildPersistence = guildPersistence;
    }

    public long getId() {
        return id;
    }
}
