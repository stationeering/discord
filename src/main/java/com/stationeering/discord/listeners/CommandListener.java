package com.stationeering.discord.listeners;

import com.stationeering.discord.managers.GuildManager;
import com.stationeering.discord.state.GuildState;
import com.stationeering.discord.state.MessageType;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CommandListener extends ListenerAdapter {
    private static Logger logger = LoggerFactory.getLogger(CommandListener.class.getName());

    private final GuildManager guildManager;

    public CommandListener(GuildManager guildManager) {
        this.guildManager = guildManager;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getMessage().isMentioned(event.getJDA().getSelfUser(), Message.MentionType.USER)) {
            User author = event.getAuthor();
            Guild guild = event.getGuild();
            Member member = guild.getMember(author);
            boolean guildAdmin = member.getRoles().stream().anyMatch((r) -> r.hasPermission(Permission.ADMINISTRATOR)) || member.isOwner();
            command(event.getGuild(), event.getChannel(), event.getMessage(), guildAdmin);
        }
    }

    private void command(Guild guild, MessageChannel channel, Message message, boolean guildAdmin) {
        String stripped = message.getContentDisplay();
        String[] commandParts = stripped.split(" ");

        if (commandParts.length < 2) {
            return;
        }

        if (!commandParts[0].contains(channel.getJDA().getSelfUser().getName())) {
            logger.debug("Ignoring mention which did not start with our name.");
            return;
        }

        if (!guildAdmin) {
            return;
        }

        switch (commandParts[1]) {
            case "set_channel":
                String types = Stream.of(MessageType.values()).map(MessageType::name).collect(Collectors.joining( ", " ));

                if (commandParts.length < 3) {
                    channel.sendMessage("You must specify a message type: " + types).queue();
                    return;
                }

                final MessageType messageType;

                try {
                    messageType = MessageType.valueOf(commandParts[2]);
                } catch (IllegalArgumentException e) {
                    channel.sendMessage("Unrecognised message type, specify one of these: " + types).queue();
                    return;
                }

                GuildState guildState = guildManager.getGuildState(guild.getIdLong());

                if (guildState == null) {
                    logger.error("Could not get GuildState for ID: " + guild.getIdLong());
                    channel.sendMessage("Sorry an internal error has occurred.").queue();
                    return;
                }

                guildState.setDestination(messageType, channel.getIdLong());

                channel.sendMessage("Roger that, I'll now post " + messageType + " messages here.").queue();
                break;
            default:
                logger.info("Unrecognised command: Admin: " + guildAdmin + " Message: " + stripped);
                channel.sendMessage("Command not found (" + commandParts[1] + ").").queue();
        }
    }
}
