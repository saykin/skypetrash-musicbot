package com.ajahi.skypetrashmusicbot.listener;

import com.ajahi.skypetrashmusicbot.music.MusicBotManager;
import net.dv8tion.jda.api.entities.Guild;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

public class MusicBotActivity implements Runnable {

    private AtomicLong timestamp;
    private Map<Long, MusicBotManager> musicBotManagers;
    private final Guild guildId;

    public MusicBotActivity(Map<Long, MusicBotManager> musicBotManagers, Guild guildId) {
        this.musicBotManagers = musicBotManagers;
        this.guildId = guildId;
    }

    @Override
    public void run() {
        disconnectBot();
    }

    private void disconnectBot() {
        if (isQueueEmpty()) {
            try {
                Thread.sleep(300_000);
            } catch (InterruptedException e) {
                System.err.println("Something went wrong with the ");
            }
            if (isQueueEmpty() && Objects.requireNonNull(Objects.requireNonNull(guildId.getMemberById("928952274972209202")).getVoiceState()).inVoiceChannel())
                guildId.moveVoiceMember(Objects.requireNonNull(guildId.getMemberById("928952274972209202")), null).queue();
            else
                disconnectBot();
        } else {
            disconnectBot();
        }

    }

    private boolean isQueueEmpty() {
        if (!musicBotManagers.isEmpty())
            return musicBotManagers.get(Long.parseLong(guildId.getId())).scheduler.isQueueEmpty();
        else
            return false;
    }
}
