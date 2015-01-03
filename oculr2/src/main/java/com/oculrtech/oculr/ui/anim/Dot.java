package com.oculrtech.oculr.ui.anim;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class Dot {
  private static final Paint paint = new Paint();
  static {
    paint.setColor(Color.argb(255, 164, 59, 187));
  }

  public double xPos;
  public double yPos;
  public double angle;
  public double length;
  public double speed;
  public double size;
  public double perc;

  public Dot(double angle, double length, double speed, double size) {
    this.angle = angle;
    this.length = length;
    this.speed = speed;
    this.size = size;
  }

  public void update() {
    angle += speed;

    double percUse = 1 - perc * perc;

    xPos = Math.cos(angle) * length * percUse;
    yPos = Math.sin(angle) * length * percUse;
  }

  public void setPerc(double perc) {
    this.perc = perc;
  }

  public void draw(Canvas c) {
    c.drawCircle((float) xPos, (float) yPos, (float) size / 2, paint);
  }
}
