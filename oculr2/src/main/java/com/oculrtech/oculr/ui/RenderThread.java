package com.oculrtech.oculr.ui;

import android.view.View;

public class RenderThread extends Thread {
  private final View view;

  public RenderThread(View v) {
    view = v;
  }

  public void run() {
    try {
      while(true) {
        view.postInvalidate();
        if(Thread.interrupted())
          return;
        Thread.sleep(20);
      }
    } catch(InterruptedException e) {
      return;
    }
  }
}