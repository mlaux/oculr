package com.oculrtech.oculr;

import android.graphics.Bitmap;
import android.graphics.Rect;

public class SelectionResult {
  private static final SelectionResult instance = new SelectionResult();

  public static SelectionResult getInstance() {
    return instance;
  }

  private SelectionResult() {

  }

  public Bitmap pic;
  public Bitmap processedImage;
  public Rect selection;
  public int referenceY;
  public int previewWidth;
  public int previewHeight;
}
