package com.ajahi.listener;


import com.ajahi.music.MusicBotManager;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MusicBotListener extends ListenerAdapter {

    private final AudioPlayerManager playerManager;
    private final Map<Long, MusicBotManager> musicBotManagers;

    public MusicBotListener() {
        this.musicBotManagers = new HashMap<>();
        this.playerManager = new DefaultAudioPlayerManager();

        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioSourceManagers.registerLocalSource(playerManager);
    }

    private synchronized MusicBotManager getBotAudioPlayer(Guild guild) {
        long guildId = Long.parseLong(guild.getId());
        MusicBotManager musicBotManager = musicBotManagers.get(guildId);

        if (musicBotManager == null) {
            musicBotManager = new MusicBotManager(playerManager);
            musicBotManagers.put(guildId, musicBotManager);
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
        } else {
            if (event.getMessage().getTextChannel().getId().equals("701187321453871144")) {
                event.getMessage().delete().queueAfter(5, TimeUnit.SECONDS);
            }
        }

        super.onGuildMessageReceived(event);
    }

    private void loadAndPlay(final Guild guild, final String authorName, final TextChannel channel, final String trackUrl) {
        MusicBotManager musicManager = getBotAudioPlayer(channel.getGuild());

        playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                channel.sendMessage("Adding to queue " + track.getInfo().title).queue();

                play(guild, authorName, musicManager, track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                AudioTrack firstTrack = playlist.getSelectedTrack();
                if (firstTrack == null) {
                    firstTrack = playlist.getTracks().get(0);
                }
                channel.sendMessage("Adding to queue " + firstTrack.getInfo().title + " (first track of playlist " + playlist.getName() + ")").queue();

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
        musicManager.scheduler.nextTrack();

        channel.sendMessage("Skipped to next track.").queue();
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
