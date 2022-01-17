package com.ajahi.skypetrashmusicbot.listener;


import com.ajahi.skypetrashmusicbot.cleaner.MusicBotMessageCleaner;
import com.ajahi.skypetrashmusicbot.music.MusicBotManager;
import com.ajahi.skypetrashmusicbot.youtube.YoutubeResult;
import com.ajahi.skypetrashmusicbot.youtube.YoutubeSearch;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MusicBotListener extends ListenerAdapter {

    private final AudioPlayerManager playerManager;
    private final Map<Long, MusicBotManager> musicBotManagers;
    private final String musicTextChannelId;
    private MusicBotMessageCleaner cleaner;
    private List<YoutubeResult> searchResults;

    public MusicBotListener() {
        this.musicBotManagers = new HashMap<>();
        this.playerManager = new DefaultAudioPlayerManager();
        this.musicTextChannelId = "701187321453871144";

        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioSourceManagers.registerLocalSource(playerManager);
    }

    private synchronized MusicBotManager getBotAudioPlayer(Guild guild) {
        long guildId = Long.parseLong(guild.getId());
        MusicBotManager musicBotManager = musicBotManagers.get(guildId);

        if (musicBotManager == null) {
            musicBotManager = new MusicBotManager(playerManager);
            musicBotManagers.put(guildId, musicBotManager);

            //cleaner = new MusicBotMessageCleaner(guild.getTextChannelById(musicTextChannelId));
        }

        guild.getAudioManager().setSendingHandler(musicBotManager.getSendHandler());

        return musicBotManager;
    }

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {

        if (event.getAuthor().isBot()) return;
        String[] command = event.getMessage().getContentRaw().split(" ", 2);
        String authorName = event.getMessage().getAuthor().getName();
        Guild guild = event.getGuild();

        if (command[0].equalsIgnoreCase("!play") && command.length == 2) {
            loadAndPlay(guild, authorName, event.getChannel(), command[1]);
        } else if (command[0].equalsIgnoreCase("!skip")) {
            skipTrack(event.getChannel());
        } else if (command[0].equalsIgnoreCase("!stop")) {
            stopAllMusic(event.getChannel());
        } else {
            if (event.getMessage().getTextChannel().getId().equals(musicTextChannelId)) {
                System.out.println(event.getAuthor().getName() + " " + event.getMessage().getContentRaw());
                event.getChannel().sendMessage("Your message didn't include !play, !skip or !stop, it will be deleted.").queue();
                event.getMessage().delete().queueAfter(3, TimeUnit.SECONDS);
            }
        }

        super.onGuildMessageReceived(event);
    }

    @Override
    public void onGuildMessageReactionAdd(@NotNull GuildMessageReactionAddEvent event) {
        String youtubeUrl = "https://www.youtube.com/watch?v=";

        if (event.getUser().isBot()) return;
        if (!searchResults.isEmpty()) {
            Guild guild = event.getGuild();
            String authorName = event.getUser().getName();
            switch (event.getReactionEmote().getAsCodepoints()) {
                case "U+31U+fe0fU+20e3":
                    youtubeUrl += searchResults.get(0).getVideoId();
                case "U+32U+fe0fU+20e3":
                    youtubeUrl += searchResults.get(1).getVideoId();
                case "U+33U+fe0fU+20e3":
                    youtubeUrl += searchResults.get(2).getVideoId();
            }
            loadAndPlay(guild, authorName, event.getChannel(), youtubeUrl);
        }
        super.onGuildMessageReactionAdd(event);
    }

    private void loadAndPlay(final Guild guild, final String authorName, final TextChannel channel, final String trackUrl) {
        MusicBotManager musicManager = getBotAudioPlayer(channel.getGuild());

        if (!trackUrl.matches("(.*youtube.com/watch\\?v=.*)|(.*youtu.be/.*)")) {
            YoutubeSearch search = new YoutubeSearch();

            try {
                searchResults = search.searchYouTube(trackUrl);
            } catch (Exception e) {
                e.printStackTrace();
            }
            channel.sendMessage("Top three results are as follows:").queue();
            List<String> emoteList = new ArrayList<>();
            emoteList.add(":one:");
            emoteList.add(":two:");
            emoteList.add(":three:");
            int count = 0;
            for (YoutubeResult results : searchResults) {
                channel.sendMessage(emoteList.get(count++) + " " + results.getVideoTitle()).queue();
                channel.sendMessage(results.getVideoThumbnail()).queue();
            }

            channel.sendMessage("Use the react buttons to choose song").queue(message -> {
                String messageId = message.getId();
                channel.addReactionById(messageId, "U+31U+fe0fU+20e3").queue();
                channel.addReactionById(messageId, "U+32U+fe0fU+20e3").queue();
                channel.addReactionById(messageId, "U+33U+fe0fU+20e3").queue();
            });
            return;
        }
        playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                channel.sendMessage("Adding to queue " + track.getInfo().title).queue();
                play(guild, authorName, musicManager, track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                for (AudioTrack track : playlist.getTracks()) {
                    musicManager.scheduler.queue(track);
                }
                AudioTrack firstTrack = playlist.getSelectedTrack();
                if (firstTrack == null) {
                    firstTrack = playlist.getTracks().get(0);
                }

                //firstTrack.getInfo().title
                channel.sendMessage("Adding to queue " + musicManager.player.getPlayingTrack().getInfo().title
                        + " (first track of playlist " + playlist.getName() + ")").queue();


                play(guild, authorName, musicManager, firstTrack);
            }

            @Override
            public void noMatches() {
                channel.sendMessage("Nothing found by " + trackUrl).queue();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                channel.sendMessage("Could not play: " + exception.getMessage()).queue();
            }
        });
    }

    private void play(Guild guild, String authorName, MusicBotManager musicBotManager, AudioTrack track) {
        AudioManager manager = guild.getAudioManager();
        List<VoiceChannel> channels = guild.getVoiceChannels();
        VoiceChannel channel = getVoiceChannel(authorName, channels, guild);
        manager.openAudioConnection(channel);

        musicBotManager.scheduler.queue(track);
    }

    private void skipTrack(TextChannel channel) {
        MusicBotManager musicManager = getBotAudioPlayer(channel.getGuild());
        try {
            musicManager.scheduler.nextTrack();
        } catch (NullPointerException e) {
            channel.sendMessage("Nothing to skip, playlist is empty").queue();
        }
        try {
            channel.sendMessage("Skipped to next track: " + musicManager.player.getPlayingTrack().getInfo().title).queue();
        } catch (NullPointerException e) {
            channel.sendMessage("There is no next song, stopping").queue();
        }
    }

    private void stopAllMusic(TextChannel channel) {
        MusicBotManager musicManager = getBotAudioPlayer(channel.getGuild());
        musicManager.scheduler.emptyQueue();
        musicManager.player.stopTrack();

        channel.sendMessage("Removing everything in the queue").queue();
    }

    private VoiceChannel getVoiceChannel(String authorName, List<VoiceChannel> channels, Guild guild) {
        for (VoiceChannel vc : channels) {
            if (vc.getMembers().size() > 0) {
                List<Member> members = vc.getMembers();
                for (Member m : members) {
                    if (m.getEffectiveName().equals(authorName))
                        return vc;
                }
            }
        }
        return guild.getVoiceChannelsByName("Music", true).get(0);
    }
}
