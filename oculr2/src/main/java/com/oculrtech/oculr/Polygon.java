package com.oculrtech.oculr;

import java.io.Serializable;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;

public class Polygon implements Serializable {
  private static final long serialVersionUID = -6429983737168582766L;

  private Path path;

  public int[] xpoints;
  public int[] ypoints;
  public int npoints;

  public Polygon() {
    this.xpoints = new int[16];
    this.ypoints = new int[16];
    this.npoints = 0;

    this.path = new Path();
  }

  public void addPoint(int x, int y) {
    if(npoints == 0) {
      path.moveTo(x, y);
    } else {
      path.lineTo(x, y);
    }

    if(npoints > xpoints.length - 1) {
      int[] temp = xpoints.clone();
      xpoints = new int[xpoints.length * 2];
      System.arraycopy(temp, 0, xpoints, 0, temp.length);

      temp = ypoints.clone();
      ypoints = new int[ypoints.length * 2];
      System.arraycopy(temp, 0, ypoints, 0, temp.length);
    }

    xpoints[npoints] = x;
    ypoints[npoints] = y;
    npoints++;
  }

  public Polygon(int[] xpoints, int[] ypoints, int npoints) {
    this.xpoints = xpoints;
    this.ypoints = ypoints;
    this.npoints = npoints;

    this.path = new Path();

    if(npoints < 3)
      throw new IllegalArgumentException("npoints must be at least 3");

    path.moveTo(xpoints[0], ypoints[0]);

    for(int k = 1; k < npoints; k++) {
      path.lineTo(xpoints[k], ypoints[k]);
    }

    path.close();
  }

  public void close() {
    if(npoints == 0)
      return;
    addPoint(xpoints[0], ypoints[0]);
  }

  public void draw(Canvas canvas, Paint paint) {
    path.setFillType(Path.FillType.EVEN_ODD);
    canvas.drawPath(path, paint);
  }

  public Rect getBoundingRect() {
    RectF r = new RectF();
    path.computeBounds(r, true);
    return new Rect((int) r.left, (int) r.top, (int) r.right, (int) r.bottom);
  }

  private int getLeftMostIndex() {
    int lowestX = Integer.MAX_VALUE, index = 0;

    for(int k = 0; k < npoints; k++) {
      if(xpoints[k] < lowestX) {
        lowestX = xpoints[k];
        index = k;
      }
    }

    return index;
  }

  public Polygon getInverse(int w, int h, boolean shift) {
    int[] newPolyX = new int[npoints + 7];
    int[] newPolyY = new int[npoints + 7];

    int leftMostIndex = getLeftMostIndex();

    int addIndex = 0;

    for (int i = 0; i < npoints; i++) {
      newPolyX[i + addIndex] = xpoints[i] - (shift ? xpoints[leftMostIndex] : 0);
      newPolyY[i + addIndex] = ypoints[i] - (shift ? ypoints[leftMostIndex] : 0);

      if (i == leftMostIndex) {
        addIndex++;
        newPolyX[i + addIndex] = 0;
        newPolyY[i + addIndex] = newPolyY[i];

        addIndex++;
        newPolyX[i + addIndex] = 0;
        newPolyY[i + addIndex] = 0;

        addIndex++;
        newPolyX[i + addIndex] = w;
        newPolyY[i + addIndex] = 0;

        addIndex++;
        newPolyX[i + addIndex] = w;
        newPolyY[i + addIndex] = h;

        addIndex++;
        newPolyX[i + addIndex] = 0;
        newPolyY[i + addIndex] = h;

        addIndex++;
        newPolyX[i + addIndex] = 0;
        newPolyY[i + addIndex] = newPolyY[i];

        addIndex++;
        newPolyX[i + addIndex] = newPolyX[i];
        newPolyY[i + addIndex] = newPolyY[i];
      }
    }

    return new Polygon(newPolyX, newPolyY, newPolyX.length);
  }

  public void scale(double sx, double sy) {
    int[] center = getCenter();
    for(int k = 0; k < npoints; k++) {
      xpoints[k] = (int) (sx * (xpoints[k] - center[0]) + (sx * center[0]));
      ypoints[k] = (int) (sy * (ypoints[k] - center[1]) + (sy * center[1]));
    }

    path = new Path();

    path.moveTo(xpoints[0], ypoints[0]);

    for(int k = 1; k < npoints; k++) {
      path.lineTo(xpoints[k], ypoints[k]);
    }

    path.close();
  }

  public String toString() {
    String s = "Polygon [ ";

    if(npoints > 0) {
      for(int k = 0; k < npoints; k++) {
        s += "(" + xpoints[k] + ", " + ypoints[k] + "), ";
      }

      s = s.substring(0, s.lastIndexOf(','));
    }

    return s + " ]";
  }

  public void rotate(double angle) {
    int[] center = getCenter();
    double rad = Math.toRadians(angle);

    for(int k = 0; k < npoints; k++) {
      int x = xpoints[k] - center[0], y = ypoints[k] - center[1];

      xpoints[k] = center[0] + (int) (x * Math.cos(rad) - y * Math.sin(rad));
      ypoints[k] = center[1] + (int) (x * Math.sin(rad) + y * Math.cos(rad));
    }

    path = new Path();

    path.moveTo(xpoints[0], ypoints[0]);

    for(int k = 1; k < npoints; k++) {
      path.lineTo(xpoints[k], ypoints[k]);
    }

    path.close();
  }

  public int[] getCenter() {
    int sumX = 0, sumY = 0;

    for(int k = 0; k < npoints; k++) {
      sumX += xpoints[k];
      sumY += ypoints[k];
    }

    return new int[] { sumX / npoints, sumY / npoints };
  }

  public Path getPath() {
    return path;
  }

  public void translate(int x, int y) {
    for(int k = 0; k < npoints; k++) {
      xpoints[k] += x;
      ypoints[k] += y;
    }

    path = new Path();

    path.moveTo(xpoints[0], ypoints[0]);

    for(int k = 1; k < npoints; k++) {
      path.lineTo(xpoints[k], ypoints[k]);
    }

    path.close();
  }

  public int getNumPoints() {
    return npoints;
  }
}
