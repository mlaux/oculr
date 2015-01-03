package com.oculrtech.oculr.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

import com.oculrtech.oculr.ui.anim.LoadAnimation;

public class LoadingView extends View {
  private RenderThread thread;

  private LoadAnimation anim;

  public LoadingView(Context context) {
    this(context, null);
  }

  public LoadingView(Context ctx, AttributeSet as) {
    super(ctx, as);

    thread = new RenderThread(this);
    anim = new LoadAnimation();

    thread.start();
  }
  public void stop() {
    thread.interrupt();
  }

  protected void onDraw(Canvas canvas) {
    canvas.save();
      canvas.translate(getWidth() / 2, getHeight() / 2);
      anim.paint(canvas);
    canvas.restore();
  }
}
