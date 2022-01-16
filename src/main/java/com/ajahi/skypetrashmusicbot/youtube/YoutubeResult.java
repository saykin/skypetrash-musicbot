package com.ajahi.skypetrashmusicbot.youtube;

public class YoutubeResult {

    private final String videoId;
    private final String videoTitle;
    private final String videoThumbnail;

    public YoutubeResult(String videoId, String videoTitle, String videoThumbnail) {
        this.videoId = videoId;
        this.videoTitle = videoTitle;
        this.videoThumbnail = videoThumbnail;
    }

    public String getVideoId() {
        return videoId;
    }

    public String getVideoTitle() {
        return videoTitle;
    }

    public String getVideoThumbnail() {
        return videoThumbnail;
    }
}
