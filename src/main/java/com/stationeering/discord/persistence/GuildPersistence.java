package com.stationeering.discord.persistence;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DeleteItemOutcome;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.stationeering.discord.state.GuildState;
import com.stationeering.discord.state.MessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Stream;

public class GuildPersistence {
    private static Logger logger = LoggerFactory.getLogger(GuildPersistence.class.getName());

    private final AmazonDynamoDB dynamoDBClient;
    private final DynamoDB dynamoDB;
    private final Table guildTable;

    public GuildPersistence() {
        dynamoDBClient = AmazonDynamoDBClientBuilder.standard().withRegion(Regions.EU_WEST_1).build();
        dynamoDB = new DynamoDB(dynamoDBClient);
        guildTable = dynamoDB.getTable("DiscordGuilds");
    }

    public void load(GuildState guildState) {
        logger.info("Loading guild from DynamoDB. Guild: " + guildState.getId());

        Item guildItem = guildTable.getItem("guild_id", guildState.getId());

        if (guildItem == null) {
            guildState.setGuildPersistence(this);
            logger.warn("Could not find guild in DynamoDB, okay if guild just joined: " + guildState.getId());
            return;
        }

        Stream.of(MessageType.values()).forEach((mt) -> {
            if (guildItem.hasAttribute(mt.getDynamoDbField())) {
                guildState.setDestination(mt, guildItem.getLong(mt.getDynamoDbField()));
            }
        });

        guildState.setGuildPersistence(this);

        logger.info("Loaded from DynamoDB: " + guildState.getId());
    }

    public void save(GuildState guildState) {
        logger.info("Saving guild to DynamoDB. Guild: " + guildState.getId());

        Item guildItem = new Item();
        guildItem.withPrimaryKey("guild_id", guildState.getId());

        Stream.of(MessageType.values()).forEach((mt) -> {
            guildState.getDestination(mt).ifPresent((cid) -> guildItem.with(mt.getDynamoDbField(), cid));
        });

        guildTable.putItem(guildItem);

        logger.info("Saved to DynamoDB: " + guildState.getId());
    }

    public void delete(GuildState guildState) {
        logger.info("Deleting guild from DynamoDB. Guild: " + guildState.getId());

        DeleteItemOutcome outcome = guildTable.deleteItem("guild_id", guildState.getId());
    }
}
