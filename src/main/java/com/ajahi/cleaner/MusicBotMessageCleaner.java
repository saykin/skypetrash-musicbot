package com.ajahi.cleaner;

import net.dv8tion.jda.api.entities.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MusicBotMessageCleaner {
    private final TextChannel musicTextChannel;
    private List<Message> messages;

    public MusicBotMessageCleaner(TextChannel musicTextChannel) {
        this.musicTextChannel = musicTextChannel;
        this.messages = new ArrayList<>();
        /*ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(this::deleteMessages, 0, 1, TimeUnit.DAYS);*/
        deleteMessages();
    }

    private void deleteMessages() {
        messages = findMessages();
        for (Message message : messages) {
            System.out.println(message);
            if (message.getTimeCreated().minusWeeks(2).isBefore(OffsetDateTime.now())) {
                message.delete().queue();
                System.out.println("deleted");
            }
        }

    }

    private List<Message> findMessages() {
        return musicTextChannel.getHistory().getRetrievedHistory();
    }
}