package com.oculrtech.oculr.results;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class YoutubeQuery {
  private static final String URL = "http://gdata.youtube.com/feeds/api/videos?max-results=3&alt=json&q=%s&author=%s";

  public static List<Video> getVideos(String channel, String query) throws IOException, JSONException {
    String escapedQuery;

    try {
      escapedQuery = URLEncoder.encode(query, "UTF-8");
    } catch(UnsupportedEncodingException e) {
      throw new RuntimeException("Java is broken");
    }

    String reqUrl = String.format(URL, escapedQuery, channel);
    URLConnection uc = new URL(reqUrl).openConnection();

    BufferedReader br = new BufferedReader(new InputStreamReader(uc.getInputStream()));

    String json = "", line;
    while((line = br.readLine()) != null)
      json += line;

    JSONObject response;
    try {
      response = new JSONObject(json);
    } catch (JSONException e) {
      e.printStackTrace();
      throw new RuntimeException("YouTube is broken");
    }

    JSONArray results = response.getJSONObject("feed").getJSONArray("entry");

    List<Video> ret = new ArrayList<Video>();

    for(int k = 0; k < results.length(); k++) {
      JSONObject videoObj = results.getJSONObject(k);
      JSONObject groupObj = videoObj.getJSONObject("media$group");

      Video vid = new Video();

      vid.title = videoObj.getJSONObject("title").getString("$t");
      vid.desc = groupObj.getJSONObject("media$description").getString("$t");
      vid.videoURL = groupObj.getJSONArray("media$content").getJSONObject(0).getString("url");

      JSONArray thumbs = groupObj.getJSONArray("media$thumbnail");
      for(int t = 0; t < thumbs.length(); t++) {

        JSONObject thumb = thumbs.getJSONObject(t);
        if(thumb.getString("url").contains("0.jpg")) {
          vid.thumbURL = thumb.getString("url");
          break;
        }
      }

      ret.add(vid);
    }

    return ret;
  }

  public static class Video {
    public String title;
    public String desc;
    public String videoURL;
    public String thumbURL;

    public String toString() {
      return title + "(url: " + videoURL + ", thumbnail: " + thumbURL + ")";
    }
  }
}