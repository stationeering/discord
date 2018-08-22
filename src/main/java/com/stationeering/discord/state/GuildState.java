package com.stationeering.discord.state;

import com.stationeering.discord.persistence.GuildPersistence;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class GuildState {
    private final long id;
    private boolean available;
    private GuildPersistence guildPersistence = null;
    private String name;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (this.name != null && this.name.equals(name)) {
            return;
        }

        this.name = name;
        save();
    }

    public Optional<Long> getDestination(MessageType messageType) {
        return Optional.ofNullable(messageTypeDestinations.get(messageType));
    }

    public void setDestination(MessageType messageType, long channelId) {
        messageTypeDestinations.put(messageType, channelId);
        save();
    }

    public void setGuildPersistence(GuildPersistence guildPersistence) {
        this.guildPersistence = guildPersistence;
    }

    public long getId() {
        return id;
    }

    private void save() {
        if (guildPersistence != null) {
            guildPersistence.save(this);
        }
    }
}
