package com.oculrtech.oculr.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

public class AspectImageView extends ImageView {

  public AspectImageView(Context context) {
    super(context);
  }

  public AspectImageView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public AspectImageView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int width = MeasureSpec.getSize(widthMeasureSpec);
    int height = width * getDrawable().getIntrinsicHeight() / getDrawable().getIntrinsicWidth();
    setMeasuredDimension(width, height);
  }
}