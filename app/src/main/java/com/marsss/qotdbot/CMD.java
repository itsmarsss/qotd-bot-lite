package com.marsss.qotdbot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;

public class CMD extends ListenerAdapter {
    private MessageReceivedEvent e;

    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot() || !event.getGuild().getId().equals(QOTDBot.config.getServerID()))
            return;

        Message msg = event.getMessage();
        String raw = msg.getContentRaw();
        String[] rawSplit = raw.toLowerCase().split(" ");
        // [prefix - 0] [cmd - 1] [parameter - 2 to ???]
        if (!rawSplit[0].equals(QOTDBot.config.getPrefix()) || rawSplit.length == 1) {
            return;
        }
        e = event;

        if ("help".equals(rawSplit[1])) {
            help();
        }

        if (hasPerm(QOTDBot.config.getManagerRoleID()) || isAdmin()) {
            switch (rawSplit[1]) {
                case "qotdtest" -> qotdTest();
                case "postnext" -> postNext();
                case "qotdchannel" -> setQOTDChannel(raw);
                case "prefix" -> setPrefix(raw);
                case "embedcolor" -> setColor(raw);
                case "info" -> sendInfo();
                case "version" -> checkVersion();
            }
        }
        if (isAdmin()) {
            switch (rawSplit[1]) {
                case "managerrole" -> qotdManager(raw);
            }
        }
    }

    private boolean hasPerm(String ID) {
        if (ID.equals("everyone"))
            return true;
        for (Role r : e.getMember().getRoles()) {
            if (r.getId().equals(ID)) {
                return true;
            }
        }
        return false;
    }

    private boolean isAdmin() {
        return e.getMember().hasPermission(Permission.ADMINISTRATOR);
    }

    private void qotdTest() {
        // qotd testqotd
        EmbedBuilder QOTDEmbed = new EmbedBuilder();
        QOTDEmbed.setAuthor("Added by: *author here*", null, QOTDBot.jda.getSelfUser().getAvatarUrl())
                .setTitle("**Question:** *question here*")
                .setDescription("*footer here*")
                .setColor(QOTDBot.config.getColor());
        e.getMessage().replyEmbeds(QOTDEmbed.build()).queue();
    }

    private void postNext() {
        // qotd post
        QOTDBot.postQOTD();
    }

    private void setQOTDChannel(String raw) {
        // qotd qotdchannel
        try {
            String param = raw.substring(QOTDBot.config.getPrefix().length() + 1 + 11).trim();
            boolean exists = false;
            for (GuildChannel ch : e.getGuild().getChannels()) {
                if (ch.getId().equals(param)) {
                    exists = true;
                }
            }
            if (exists) {
                QOTDBot.config.setChannelID(param);
                e.getMessage().replyEmbeds(se("QOTD channel has been changed to <#" + param + ">.")).queue();
            } else {
                e.getMessage().replyEmbeds(se("Invalid channel id.")).queue();
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.e.getMessage().replyEmbeds(se("Unable to look for channel.")).queue();
        }
    }

    private void setPrefix(String raw) {
        // qotd prefix
        try {
            String param = raw.split(" ")[2].trim();
            QOTDBot.config.setPrefix(param);
            e.getMessage().replyEmbeds(se("QOTD prefix has been changed to `" + param + "`.")).queue();
            QOTDBot.jda.getPresence().setActivity(Activity.watching("for '" + QOTDBot.config.getPrefix() + " help'"));
        } catch (Exception e) {
            e.printStackTrace();
            this.e.getMessage().replyEmbeds(se("Invalid prefix.")).queue();
        }
    }

    private void setColor(String raw) {
        // qotd embedcolor
        try {
            String param = raw.substring(QOTDBot.config.getPrefix().length() + 1 + 10).trim().replace("#", "");
            QOTDBot.config.setQOTDColor(param);
            this.e.getMessage().replyEmbeds(new EmbedBuilder()
                            .setDescription("Set embed color to **#" + QOTDBot.config.getQOTDColor() + "**.")
                            .setColor(QOTDBot.config.getColor())
                            .build())
                    .queue();
        } catch (Exception e) {
            e.printStackTrace();
            this.e.getMessage().replyEmbeds(se("Unable to set color.")).queue();
        }
    }

    private void sendInfo() {
        // qotd info
        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd/MM/yyyy • hh:mm");

        MessageEmbed infoEm = new EmbedBuilder()
                .setTitle("__Bot Info__")
                .setDescription("[ *Version: " + QOTDBot.version + "* ]")
                .addField("Prefix:", QOTDBot.config.getPrefix(), true)
                .addBlankField(true)
                .addField("Interval:", QOTDBot.config.getInterval() + " minute(s)", true)
                .addField("Perm role ID:", QOTDBot.config.getPermRoleID().equals("everyone") ? "everyone" : "<@&" + QOTDBot.config.getPermRoleID() + ">", true)
                .addBlankField(true)
                .addField("Manager role ID:", QOTDBot.config.getManagerRoleID().equals("everyone") ? "everyone" : "<@&" + QOTDBot.config.getManagerRoleID() + ">", true)
                .addField("Manager review status:", QOTDBot.config.getManagerReview() + "", true)
                .addBlankField(true)
                .addField("Manager review channel:", "<#" + QOTDBot.config.getReviewChannel() + ">", true)
                .addField("Dynamic Config:", QOTDBot.config.getDynamicConfig() + "", false)
                .setThumbnail(QOTDBot.jda.getSelfUser().getAvatarUrl())
                .setFooter(format.format(LocalDateTime.now()), e.getAuthor().getAvatarUrl())
                .setColor(QOTDBot.config.getColor())
                .build();
        e.getMessage().replyEmbeds(infoEm).queue();
    }

    private void checkVersion() {
        // qotd version
        e.getMessage().replyEmbeds(new EmbedBuilder()
                        .setTitle(QOTDBot.version)
                        .setDescription(QOTDBot.versionCheck()
                                .replaceAll("#", "")
                                .replace("This program is up to date!", "__**This program is up to date!**__")
                                .replace("[There is a newer version of QOTD Bot]", "__**[There is a newer version of QOTD Bot]**__")
                                .replace("Author's Note:", "**Author's Note:**")
                                .replace("New version:", "**New version:**"))
                        .setColor(QOTDBot.config.getColor())
                        .build())
                .queue();
    }

    private void qotdManager(String raw) {
        // qotd managerrole
        try {
            String param = raw.substring(QOTDBot.config.getPrefix().length() + 1 + 11).trim();
            if (param.equalsIgnoreCase("everyone")) {
                e.getMessage().replyEmbeds(se("QOTD manager role has been changed; `everyone` can approve or deny questions")).queue();
                QOTDBot.config.setManagerRoleID("everyone");
                return;
            }
            boolean exists = false;
            for (Role r : e.getGuild().getRoles()) {
                if (r.getId().equals(param)) {
                    exists = true;
                    break;
                }
            }
            if (exists) {
                QOTDBot.config.setManagerRoleID(param);
                e.getMessage().replyEmbeds(se("QOTD manager role has been changed to <@&" + param + ">.")).queue();
            } else {
                e.getMessage().replyEmbeds(se("Invalid role id.")).queue();
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.e.getMessage().replyEmbeds(se("Unable to look for role.")).queue();
        }
    }

    static MessageEmbed se(String desc) {
        return new EmbedBuilder()
                .setDescription(desc)
                .setColor(QOTDBot.config.getColor())
                .build();
    }

    private void help() {
        // qotd help
        e.getMessage().replyEmbeds(
                new EmbedBuilder()
                        .setTitle("__**Commands**__")
                        .addField("Main",
                                "`" + QOTDBot.config.getPrefix() + " help` - This message", false)
                        .addBlankField(true)
                        .addField("Manager commands",
                                "`" + QOTDBot.config.getPrefix() + " qotdtest` - Send a sample QOTD" + "\n`" +
                                        QOTDBot.config.getPrefix() + " postnext` - Post next QOTD" + "\n`" +
                                        QOTDBot.config.getPrefix() + " prefix <prefix, no space>` - Change bot prefix" + "\n`" +
                                        QOTDBot.config.getPrefix() + " qotdcolor <color in hex>` - Set QOTD embed color" + "\n`" +
                                        QOTDBot.config.getPrefix() + " info` - See bot info" + "\n`" +
                                        QOTDBot.config.getPrefix() + " version` - See bot version", false)
                        .addBlankField(true)
                        .addField("Admin commands",
                                "`" + QOTDBot.config.getPrefix() + " managerrole <role id/'everyone'>` - Set QOTD manager role", false)
                        .setThumbnail(QOTDBot.jda.getSelfUser().getAvatarUrl())
                        .setColor(QOTDBot.config.getColor())
                        .build()).queue();
    }
}
