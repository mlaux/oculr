package com.oculrtech.oculr.results;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.oculrtech.oculr.OculrActivity;
import com.wolfram.alpha.WAEngine;
import com.wolfram.alpha.WAException;
import com.wolfram.alpha.WAImage;
import com.wolfram.alpha.WAPlainText;
import com.wolfram.alpha.WAPod;
import com.wolfram.alpha.WAQuery;
import com.wolfram.alpha.WAQueryResult;
import com.wolfram.alpha.WASubpod;

public class WolframQuery {
  // WA APP ID
  //private static final String APP_ID = "3H4296-5YPAGQUJK7";
  // basic xor encryption
  private static final String APP_ID = "UA5YHW-52LAJAJL7V";

  public static List<ResultItem> getResults(String queryStr) throws IOException, WAException {
    List<ResultItem> ret = new ArrayList<ResultItem>();

    WAEngine engine = new WAEngine();
    engine.setAppID(APP_ID);
    engine.addFormat("plaintext");
    engine.addFormat("image");
    engine.setMagnification(2.0);
    engine.setWidth(600);
    engine.addPodState("Step-by-step solution");

    WAQuery query = engine.createQuery();

    query.setInput(queryStr);

    WAQueryResult result = engine.performQuery(query);

    if (result.isError()) {
      ResultItem item = new ResultItem();
      item.key = "Error";
      item.value = "<e>" + result.getErrorMessage();
      ret.add(item);
    } else if (!result.isSuccess()) {
      ResultItem item = new ResultItem();
      item.key = "Error";
      item.value = "<e>Query was not understood; no results available.";
      ret.add(item);
    } else {
      ResultItem stepByStep = null;
      for (WAPod pod : result.getPods()) {
        if (!pod.isError()) {
          ResultItem item = new ResultItem();
          item.key = pod.getTitle();
          String value = "";

          for (WASubpod subpod : pod.getSubpods()) {
            if(subpod.getTitle() != null && subpod.getTitle().contains("steps")) {
              item.key = "Step-by-step solution";
              stepByStep = item;
            }

            for (Object element : subpod.getContents()) {
              if (element instanceof WAImage) {
                item.imageUrl = ((WAImage) element).getURL();
              }

              else if (element instanceof WAPlainText) {
                String text = ((WAPlainText) element).getText();
                text = text.replace("amount", "");
                text = text.replaceAll("\\s+\\|\\s+", ", ");
                value += text + "\n";
              }
            }
          }
          item.value = value;
          if(item != stepByStep)
            ret.add(item);
        }
      }

      if(stepByStep != null)
        ret.add(stepByStep);
    }

    return ret;
  }

  public static class ResultItem {
    public String key;
    public String value;
    public String imageUrl;

    public String toString() {
      return key + " - " + value;
    }
  }
}
