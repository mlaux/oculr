package com.oculrtech.oculr.results;

import com.oculrtech.oculr.R;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class ImageURLBinder implements SimpleAdapter.ViewBinder {
  public boolean setViewValue(View view, Object data, String textRepresentation) {
    if(view instanceof ImageView) {
      if(data != null) {
        if(data instanceof Bitmap) {
          view.setVisibility(View.VISIBLE);
          view.invalidate();
          ((ImageView) view).setImageBitmap((Bitmap) data);
        } else {
          new ImageDownloadTask((ImageView) view, new ImageDownloadTask.IDCallback() {
            @Override
            public void onImageDownloaded(ImageView iv, Bitmap image) {
              ((View) iv.getParent()).findViewById(R.id.loading).setVisibility(View.GONE);
            }
          }).execute((String) data);
        }
        return true;
      }
    } else if(view instanceof TextView) {
      String text = (String) data;
      if(text == null) {
        view.setVisibility(View.GONE);
        return true;
      }

      ((View) view.getParent()).findViewById(R.id.loading).setVisibility(View.GONE);

      if(view.getId() == R.id.title || text.startsWith("<e>")) {
        if(text.startsWith("<e>"))
          text = text.substring(3);
        view.setVisibility(View.VISIBLE);
        ((TextView) view).setText(text);
      } else {
        view.setVisibility(View.GONE);
      }
      return true;
    }

    return false;
  }
}