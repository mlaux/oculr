package com.oculrtech.oculr.ui.anim;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class LoadAnimation {
  private static final int SIZE = 10;

  private ArrayList<Dot> dotList = new ArrayList<Dot>();
  private ArrayList<Double> fade = new ArrayList<Double>();
  private ArrayList<Double> fadeDir = new ArrayList<Double>();

  private double percDone;
  private double doneFade;

  public LoadAnimation() {
    reset();
  }

  public void reset() {

    doneFade = 1;

    for (int i = 0; i < 50; i++) {
      double angle = Math.random() * Math.PI * 2;
      int length = (int) (Math.sqrt(Math.random()) * 480);
      double speedDir = Math.floor(Math.random() * 2) * 2 - 1;
      double speed = (Math.random() * speedDir * 3 + speedDir/4) / 150;
      dotList.add(new Dot(angle, length, speed, SIZE));

      fade.add(Math.random() * 1.5);
      fadeDir.add((Math.floor(Math.random() * 2) * 2 - 1) / 50);
    }
  }

  private static final Paint paint = new Paint();
  static {
    paint.setColor(Color.argb(255, 164, 59, 187));
    paint.setStrokeWidth(3);
  }

  public void paint(Canvas c) {
    for (int i = 0; i < dotList.size(); i++) {
      Dot dot = dotList.get(i);
      dot.update();
    }

    Dot nextDot = new Dot(0, 0, 0, SIZE);
    for (int i = 0; i < dotList.size(); i++) {
      Dot dot = dotList.get(i);

      dot.setPerc(percDone);

      float value = (float) (fade.get(i).floatValue() + fadeDir.get(i));
      if (value > 1.5) {
        value = (float) 1.5;
        fadeDir.set(i, -fadeDir.get(i));
      }
      if (value < 0) {
        value = 0;
        fadeDir.set(i, -fadeDir.get(i));
      }
      fade.set(i, (double) value);

      if (value > 1) {
        value = 1;
      }

      if (percDone < 1) {
        c.drawLine((int) dot.xPos, (int) dot.yPos, (int) nextDot.xPos, (int) nextDot.yPos, paint);
        dot.draw(c);
      } else {
        doneFade -= 0.0005;

        if (doneFade < 0) {
          doneFade = 0;
        }

        if (i == 0) {
          c.drawCircle(-SIZE / 2, -SIZE / 2, SIZE / 2, paint);
        }
      }
    }
  }
}