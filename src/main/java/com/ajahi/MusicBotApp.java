package com.ajahi;

import com.ajahi.listener.MusicBotListener;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import javax.security.auth.login.LoginException;

public class MusicBotApp {
    public static void main(String[] args) throws LoginException {
        JDABuilder.createDefault(System.getenv("API_TOKEN"))
                .addEventListeners(new MusicBotListener())
                .setActivity(Activity.listening("degenerate music"))
                .setStatus(OnlineStatus.DO_NOT_DISTURB)
                .enableCache(CacheFlag.VOICE_STATE)
                .build();
    }
}
