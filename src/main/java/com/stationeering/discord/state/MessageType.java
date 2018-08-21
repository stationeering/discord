package com.stationeering.discord.state;

public enum MessageType {
    VERSIONS("version_channel_id");

    private final String dynamoDbField;

    MessageType(String dynamoDbField) {
        this.dynamoDbField = dynamoDbField;
    }

    public String getDynamoDbField() {
        return dynamoDbField;
    }
}
