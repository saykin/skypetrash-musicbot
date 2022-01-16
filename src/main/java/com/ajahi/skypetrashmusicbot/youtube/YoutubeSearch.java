package com.ajahi.skypetrashmusicbot.youtube;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.gson.Gson;
import org.json.JSONObject;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

public class YoutubeSearch {
    private final String DEVELOPER_KEY = System.getenv("YOUTUBE_TOKEN");
    private final String APPLICATION_NAME = "SkypeTrashMusicBot";

    private final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    /**
     * Build and return an authorized API client service.
     *
     * @return an authorized API client service
     * @throws GeneralSecurityException, IOException
     */
    private YouTube getService() throws GeneralSecurityException, IOException {
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

        return new YouTube.Builder(httpTransport, JSON_FACTORY, null)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    /**
     * Call function to create API service object. Define and
     * execute API request. Print API response.
     *
     * @throws GeneralSecurityException, IOException, GoogleJsonResponseException
     */
    public List<YoutubeResult> searchYouTube(String searchString)
            throws GeneralSecurityException, IOException {
        YouTube youtubeService = getService();
        // Define and execute the API request
        YouTube.Search.List request = youtubeService.search()
                .list("snippet");
        SearchListResponse response = request.setKey(DEVELOPER_KEY)
                .setMaxResults(3L)
                .setQ(searchString)
                .setType("playlist,video")
                .setPrettyPrint(true)
                .execute();

        List<YoutubeResult> youtubeResults = new ArrayList<>();
        for (SearchResult results : response.getItems()) {
            String videoId = results.getId().getVideoId();
            String videoTitle = results.getSnippet().getTitle();
            if (videoTitle.contains("&quot;") || videoTitle.contains("&#39;"))
                videoTitle = videoTitle.replaceAll("&quot;|&#39;", "\"");
            String videoThumbnail = results.getSnippet().getThumbnails().getMedium().getUrl();
            youtubeResults.add(new YoutubeResult(videoId, videoTitle, videoThumbnail));
        }
        return youtubeResults;
    }
}
