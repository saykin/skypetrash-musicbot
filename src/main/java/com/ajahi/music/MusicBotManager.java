package com.ajahi.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;

public class MusicBotManager {
    public final AudioPlayer player;
    public final MusicBotTrackScheduler scheduler;

    public MusicBotManager(AudioPlayerManager manager) {
        player = manager.createPlayer();
        scheduler = new MusicBotTrackScheduler(player);
        player.addListener(scheduler);
    }

   public MusicBotAudioPlayerSendHandler getSendHandler() {
        return new MusicBotAudioPlayerSendHandler(player);
   }
}
