package com.oculrtech.oculr.ocr;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.oculrtech.oculr.OculrActivity;
import com.oculrtech.oculr.results.QueryTask;

public class OCRTask extends AsyncTask<Bitmap, Void, String> {
  private static final String TAG = OCRTask.class.getSimpleName();
  private OculrActivity activity;

  public OCRTask(OculrActivity oa) {
    activity = oa;
  }

  protected String doInBackground(Bitmap... params) {
    TessBaseAPI baseApi = new TessBaseAPI();
    baseApi.init(OculrActivity.DATA_PATH.toString() + "/", "eng");
    baseApi.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SINGLE_LINE);
    // TODO: investigate baseApi.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, "");
    baseApi.setImage(params[0]);

    Log.d(TAG, "STARTING RECOGNIZING");

    String recognizedText = baseApi.getUTF8Text();

    String fixedText = fixCommonMistakes(recognizedText);

    Log.d(TAG, "DONE RECOGNIZING" + ": " + fixedText);

    baseApi.end();

    return fixedText;
  }

  protected void onPostExecute(String result) {
    if(result != null) {
      if(result.equalsIgnoreCase("h2so4"))
        Toast.makeText(activity, OculrActivity.LIMERICK, Toast.LENGTH_LONG).show();
      else
        Toast.makeText(activity, result, Toast.LENGTH_SHORT).show();
      new QueryTask(activity).execute(result);
    }
  }

  private static String fixCommonMistakes(String in) {
    // Some commonly misrecognized symbols
    String out = in.replace("A", "^");
    out = out.replace("\"", "^");
    out = out.replace("'\\", "^");
    out = out.replace("\\'", "^");
    out = out.replace("'", "^");
    out = out.replaceAll("(\u201c|\u201d)", "^");

    out = out.replace("I", "1");
    out = out.replace("~", "-");
    out = out.replace("><", "x");
    out = out.replace("$", "s");
    out = out.replace("^Z", "^2");


    // If we came up with < ... ) or ( ... >, it's probably wrong
    if(out.contains("<") && !out.contains(">")
        || out.contains(">") && !out.contains("<")) {
      out = out.replace("<", "(");
      out = out.replace(">", ")");
    }

    // = is often mistaken for Z, z, or :
    if(out.contains("Z") && !out.contains("="))
      out = out.replace("Z", "=");

    if(out.contains("z") && !out.contains("="))
      out = out.replace("z", "=");

    if(out.contains(":") && !out.contains("="))
      out = out.replace(":", "=");

    // Uppercase letters are less frequently used - if we have an uppercase letter
    // along with the corresponding lowercase letter, assume the lowercase was meant
    // in both places
    for(int c = 'A'; c <= 'Z'; c++) {
      if(out.contains(String.valueOf(c)) && out.contains(String.valueOf(Character.toLowerCase(c))))
        out = out.replace((char) c, (char) Character.toLowerCase(c));
    }

    if(out.contains("+") || out.contains("-") || out.contains("*") || out.contains("/") || out.contains("=")) {
      out = out.replace("O", "0");
      out = out.replace("o", "0");
    }

    if(out.equalsIgnoreCase("H2804"))
      out = "H2SO4";
    return out;
  }
}
