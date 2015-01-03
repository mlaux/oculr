package com.oculrtech.oculr.results;

import java.util.List;

public class QueryResult {
  public String query;
  public List<WolframQuery.ResultItem> wolf;
  public List<YoutubeQuery.Video> videos;

  public QueryResult(String q, List<WolframQuery.ResultItem> w, List<YoutubeQuery.Video> k) {
    query = q;
    wolf = w;
    videos = k;
  }
}