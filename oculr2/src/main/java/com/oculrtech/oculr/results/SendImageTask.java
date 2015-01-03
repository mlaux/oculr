package com.oculrtech.oculr.results;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.impl.client.DefaultHttpClient;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.AsyncTask;
import android.widget.Toast;

import com.oculrtech.oculr.OculrActivity;

public class SendImageTask extends AsyncTask<Bitmap, Void, String> {

  private OculrActivity activity;

  public SendImageTask(OculrActivity activity) {
    this.activity = activity;
  }

  @Override
  protected String doInBackground(Bitmap... params) {
      // Create a new HttpClient and Post Header
      HttpClient httpclient = new DefaultHttpClient();
      HttpPost httppost = new HttpPost(OculrActivity.getSubmissionURL());

      try {
          // Add your data
      MultipartEntity mpEntity = new MultipartEntity();

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      params[0].compress(CompressFormat.PNG, 100, baos);

      ContentBody cbFile = new ByteArrayBody(baos.toByteArray(), "image/png",
          "oculr_" + System.currentTimeMillis() + ".png");
      mpEntity.addPart("userfile", cbFile);

      httppost.setEntity(mpEntity);
          // Execute HTTP Post Request
          HttpResponse response = httpclient.execute(httppost);
          InputStream in = response.getEntity().getContent();
          BufferedReader br = new BufferedReader(new InputStreamReader(in));
          return br.readLine();
      } catch (Exception e) {
        e.printStackTrace();
      }
      return null;
  }

  @Override
  protected void onPostExecute(String result) {
    if(result != null) {
      if(result.equalsIgnoreCase("h2so4"))
        Toast.makeText(activity, OculrActivity.LIMERICK, Toast.LENGTH_LONG).show();
      else
        Toast.makeText(activity, result, Toast.LENGTH_SHORT).show();
      new QueryTask(activity).execute(result);
    }
  }
}
