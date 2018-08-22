package com.stationeering.discord.managers;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.stationeering.discord.state.MessageType;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class VersionManager implements Runnable {
    private static Logger logger = LoggerFactory.getLogger(VersionManager.class.getName());

    private final JDA jda;
    private final String sqsURL;
    private final GuildManager guildManager;
    private final AmazonSQS sqs = AmazonSQSClientBuilder.defaultClient();
    private final JsonParser jsonParser = new JsonParser();

    public VersionManager(JDA jda, String sqsURL, GuildManager guildManager) {
        this.jda = jda;
        this.sqsURL = sqsURL;
        this.guildManager = guildManager;
    }

    @Override
    public void run() {
        ReceiveMessageRequest receive_request = new ReceiveMessageRequest()
                .withQueueUrl(sqsURL)
                .withWaitTimeSeconds(20);

        while(true) {
            try {
                ReceiveMessageResult result = sqs.receiveMessage(receive_request);

                List<Message> messages = result.getMessages();

                messages.forEach((msg) -> {
                    logger.info("Processing SQS message: " + msg.getMessageId());
                    handle(msg.getBody());
                    sqs.deleteMessage(sqsURL, msg.getReceiptHandle());
                });
            } catch (RuntimeException exception) {
                logger.error("Exception thrown while processing AWS SQS messages!", exception);
            }
        }
    }

    private void handle(String body) {
        JsonElement parsedBody;

        try {
            parsedBody = jsonParser.parse(body);
        } catch (JsonSyntaxException e) {
            logger.error("Failed to parse JSON! " + body, e);
            return;
        }

        if (!parsedBody.isJsonObject()) {
            logger.warn("Parsed JSON, however not an object. " + body);
            return;
        }

        JsonObject jsonObject = (JsonObject) parsedBody;

        if (!jsonObject.has("operation")) {
            logger.warn("Parsed JSON, however no operation present! " + body);
            return;
        }

        String operation = jsonObject.get("operation").getAsString();

        logger.info("Message operation is: " + operation);

        switch (operation) {
            case "ping":
                break;
            case "create":
            case "update":
                handleCreateUpdate(jsonObject);
                break;
            default:
                logger.info("Warning, unknown operation: " + operation);
                break;
        }
    }

    private void handleCreateUpdate(JsonObject jsonObject) {
        if (!jsonObject.has("type")) {
            logger.warn("Parsed JSON, however no type present!");
            return;
        }

        String type = jsonObject.get("type").getAsString();

        switch (type) {
            case "version":
                handleCreateUpdateVersion(jsonObject);
                break;
            default:
                logger.info("Warning, unknown type: " + type);
                break;
        }

    }

    private void handleCreateUpdateVersion(JsonObject jsonObject) {
        if (!jsonObject.has("version")) {
            logger.warn("Parsed JSON, however version section is not present on a version create/update!");
            return;
        }

        JsonObject version = jsonObject.getAsJsonObject("version");

        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(new Color(0, 0x90, 0xff));
        eb.setAuthor("Stationeers", "https://store.steampowered.com/app/544550/Stationeers/", "https://cdn.discordapp.com/icons/276525882049429515/4ec8c55aea32c65fc0188055de0d3b29.webp");
        eb.setTitle(version.get("version").getAsString());

        JsonElement notesElement = version.get("notes");
        JsonArray notesArray = (notesElement == null ? null : notesElement.getAsJsonArray());
        eb.setDescription(parseVersionNotes(notesArray));

        JsonElement betaTime = version.get("built_date");

        long timestamp = System.currentTimeMillis();

        if (betaTime != null) {
            timestamp = betaTime.getAsLong();
        }

        Instant date = Instant.ofEpochMilli(timestamp);
        eb.setTimestamp(date);

        logger.debug("Output message built!");

        guildManager.getDestinations(MessageType.VERSIONS).forEach((cid) -> {
            logger.debug("Sending message to " + cid);
            jda.getTextChannelById(cid).sendMessage(eb.build()).queue();
        });
    }

    private String parseVersionNotes(JsonArray notesArray) {
        if (notesArray == null || notesArray.size() == 0) {
            return "No change log for this version.";
        }

        Iterable<JsonElement> iterable = notesArray::iterator;
        Stream<JsonElement> targetStream = StreamSupport.stream(iterable.spliterator(), false);

        return targetStream.map(JsonElement::getAsString).map((s) -> {
            String[] parts = s.split(" ", 2);
            return "- **" + parts[0] + "** " + parts[1];
        }).collect(Collectors.joining("\n"));
    }
}
