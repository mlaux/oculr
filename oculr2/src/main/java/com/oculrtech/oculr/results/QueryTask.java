package com.oculrtech.oculr.results;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import android.os.AsyncTask;

import com.oculrtech.oculr.OculrActivity;

public class QueryTask extends AsyncTask<String, Void, QueryResult> {
  private OculrActivity activity;
  private static final Map<String, String> MATH_TYPES = new LinkedHashMap<String, String>();

  private static final String[] YOUTUBE_ACCOUNTS = { "khanacademy", "patrickjmt", "videomathtutor" };

  static {
    MATH_TYPES.put("int", "integral");
    MATH_TYPES.put("/d", "derivative");

    MATH_TYPES.put("arcsin", "inverse trig");
    MATH_TYPES.put("arccos", "inverse trig");
    MATH_TYPES.put("arctan", "inverse trig");

    MATH_TYPES.put("sin", "trig");
    MATH_TYPES.put("cos", "trig");
    MATH_TYPES.put("tan", "trig");

    MATH_TYPES.put("log", "logarithm");
    MATH_TYPES.put("ln", "logarithm");

    // MATH_TYPES.put("^", "exponents");

    MATH_TYPES.put("+", "addition");
    MATH_TYPES.put("-", "subtraction");
    MATH_TYPES.put("*", "multiplication");
    MATH_TYPES.put("/", "division");
  }

  public QueryTask(OculrActivity oa) {
    activity = oa;
  }

  protected QueryResult doInBackground(String... params) {
    String query = params[0];

    List<WolframQuery.ResultItem> wolf;
    List<YoutubeQuery.Video> allVideos = null;

    try {
      wolf = WolframQuery.getResults(query);
    } catch(Exception e) {
      e.printStackTrace();
      wolf = new ArrayList<WolframQuery.ResultItem>();
    }

    String type = null;
    for(WolframQuery.ResultItem result : wolf) {
      if(result.key.equals("Geometric figure"))
        type = result.value;
    }

    if(type == null) {
      String lower = query.toLowerCase();
      for(String eq : MATH_TYPES.keySet()) {
        if(lower.contains(eq)) {
          type = MATH_TYPES.get(eq);
          break;
        }
      }
    }

    if(type != null) {
      allVideos = new ArrayList<YoutubeQuery.Video>();
      for(String account : YOUTUBE_ACCOUNTS) {
        try {
          allVideos.addAll(YoutubeQuery.getVideos(account, type));
        } catch(Exception e) {
          e.printStackTrace();
        }
      }
    } else {
      // type was still null, hide video stuff

    }

    return new QueryResult(query, wolf, allVideos);

  }

  protected void onPostExecute(QueryResult result) {
    activity.setResults(result);
  }
}