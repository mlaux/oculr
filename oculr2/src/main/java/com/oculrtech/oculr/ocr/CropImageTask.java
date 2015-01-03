package com.oculrtech.oculr.ocr;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.util.Log;

import com.oculrtech.oculr.OculrActivity;
import com.oculrtech.oculr.SelectionResult;
import com.oculrtech.oculr.results.SendImageTask;

public class CropImageTask extends AsyncTask<Void, Void, Bitmap[]> {
  private static final String TAG = CropImageTask.class.getSimpleName();

  private OculrActivity activity;

  public CropImageTask(OculrActivity a) {
    activity = a;
  }

  protected Bitmap[] doInBackground(Void... params) {
    SelectionResult sel = SelectionResult.getInstance();

    Bitmap pic = sel.pic;
    Rect rect = sel.selection;

    // Clip the selection rectangle to the edges of the screen

    if(rect.left < 0) rect.left = 0;
    if(rect.left >= sel.previewWidth) rect.left = sel.previewWidth - 1;

    if(rect.top < 0) rect.top = 0;
    if(rect.top >= sel.previewHeight) rect.top = sel.previewHeight - 1;

    if(rect.right < 0) rect.right = 0;
    if(rect.right >= sel.previewWidth) rect.right = sel.previewWidth - 1;

    if(rect.bottom < 0) rect.bottom = 0;
    if(rect.bottom >= sel.previewHeight) rect.bottom = sel.previewHeight - 1;

    Log.d(TAG, "Pic dimensions: " + pic.getWidth() + ", " + pic.getHeight());
    Log.d(TAG, "Polyview dimensions " + sel.previewWidth + ", " + sel.previewHeight);
    Log.d(TAG, "Crop rect: " + rect.toShortString());

    float referenceY = (float) (sel.referenceY - rect.top) / (rect.bottom - rect.top);

    float sx = (float) pic.getWidth() / sel.previewWidth;
    float sy = (float) pic.getHeight() / sel.previewHeight;

    rect.left *= sx;
    rect.right *= sx;
    rect.top *= sy;
    rect.bottom *= sy;

    Log.d(TAG, "Scaled crop rect: " + rect.toShortString());

    Matrix mat = new Matrix();
    mat.postScale(sx, sy);
    Bitmap smallOrig = Bitmap.createBitmap(pic, rect.left, rect.top, rect.width(), rect.height(), mat, false);

    float s2;
    if(!OculrActivity.nerve && activity.isHandwritingRecognitionEnabled()) {
      // HW recognition likes images around 512px height
      s2 = 512.0f / smallOrig.getHeight();
    } else {
      // Tesseract likes images around 128px height. Purely empirical
      s2 = 128.0f / smallOrig.getHeight();
    }
    mat.set(null);
    mat.postScale(s2, s2);
    Bitmap small = Bitmap.createBitmap(smallOrig, 0, 0, smallOrig.getWidth(), smallOrig.getHeight(), mat, true);
    referenceY *= small.getHeight();

    // Clamp referenceY to bitmap height
    if(referenceY < 0) referenceY = 0;
    if(referenceY >= small.getHeight()) referenceY = small.getHeight() - 1;

    Bitmap filtered = runCatocra(small, (int) referenceY);
    sel.processedImage = filtered;

    // Uncomment to draw the referenceY line on the processed image
    /*Paint paint = new Paint();
    paint.setColor(0xffff0000);
    paint.setStrokeWidth(2.0f);
    paint.setStyle(Style.STROKE);
    new Canvas(filtered).drawLine(0, referenceY, filtered.getWidth(), referenceY, paint);*/

    /*try {
      catocraFiltered.compress(CompressFormat.PNG, 100, new FileOutputStream(OculrActivity.DATA_PATH.getAbsolutePath() + "/out.png"));
    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }*/

    return new Bitmap[] { pic, smallOrig, filtered };
  }

  private Bitmap runCatocra(Bitmap bit, int referenceY) {
    Bitmap thresh = bit.copy(Bitmap.Config.ARGB_8888, true);

    int textCol = CustomAdaptiveThresholdOCRAlgorithms.threshold(bit, referenceY);

    for(int y = 0; y < bit.getHeight(); y++) {
      for(int x = 0; x < bit.getWidth(); x++) {
        int pix = bit.getPixel(x, y);

        int col = 0;
        col += (pix & 0xff);
        col += (pix >> 8) & 0xff;
        col += (pix >> 16) & 0xff;
        col /= 3;

        if(col >= textCol)
          thresh.setPixel(x, y, Color.WHITE);
        else
          thresh.setPixel(x, y, Color.BLACK);
      }
    }

    return thresh;
  }

  protected void onPostExecute(Bitmap[] result) {
    activity.setMarketingPics(result);

    if(activity.isHandwritingRecognitionEnabled()) {
      new SendImageTask(activity).execute(result[result.length - 1]);
    } else {
      new OCRTask(activity).execute(result[result.length - 1]);
    }
  }
}