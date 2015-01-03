package com.oculrtech.oculr.results;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ImageView;

public class ImageDownloadTask extends AsyncTask<String, Void, Bitmap> {
  private ImageView view;
  private IDCallback callback;

  private static Map<String, Bitmap> cache = new HashMap<String, Bitmap>();

  public static interface IDCallback {
    public void onImageDownloaded(ImageView iv, Bitmap image);
  }

  public ImageDownloadTask(ImageView iv) {
    this.view = iv;
  }

  public ImageDownloadTask(ImageView iv, IDCallback cb) {
    this(iv);
    callback = cb;
  }

  protected Bitmap doInBackground(String... urls) {
    String url = urls[0];

    Bitmap bmp = cache.get(url);
    if(bmp != null)
      return bmp;

    try {
      BitmapFactory.Options options = new BitmapFactory.Options();

      options.inScaled = false;

      InputStream in = new java.net.URL(url).openStream();
      bmp = BitmapFactory.decodeStream(in, null, options);
    } catch (Exception e) {
      e.printStackTrace();
    }

    cache.put(url, bmp);
    return bmp;
  }

  protected void onPostExecute(Bitmap result) {
    if(result != null) {
      view.setImageBitmap(result);
      view.setVisibility(View.VISIBLE);
      view.invalidate();
      view.postInvalidate();
    }

    if(callback != null) {
      callback.onImageDownloaded(view, result);
    }
  }

  /**
   * Only for bitmaps that have already been downloaded!
   */
  public static Bitmap getFromCache(String url) {
    return cache.get(url);
  }
}