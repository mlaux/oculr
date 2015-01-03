package com.oculrtech.oculr.ocr;

import com.oculrtech.oculr.Polygon;

import android.graphics.Bitmap;
import android.graphics.Color;

public class CustomAdaptiveThresholdOCRAlgorithms {

  public static int threshold(Bitmap bi, int startY) {
    double[] colorsOriginal = new double[256];
    double multFactor = bi.getWidth() / 1000;

    if (multFactor < 1) {
      multFactor = 1;
    }

    for (int x = 0; x < bi.getWidth(); x++) {
      //System.out.println(x + " - " + startY + " - " + bi.getRGB(x, startY));
      int biColor = bi.getPixel(x, startY);
      int biRed = (biColor & 16711680) >> 16;
      int biGreen = (biColor & 65280) >> 8;
      int biBlue = biColor & 255;
      int biAvg = (biRed + biGreen + biBlue) / 3;
      colorsOriginal[biAvg]++;
    }

    double[] colors = new double[256];
    for (int i = 0; i < colors.length; i++) {
      double avgTotal = 0;
      int avgCount = 0;

      for (int a = -2; a <= 2; a++) {
        if (i + a >= 0 && i + a <= 255) {
          avgTotal += colorsOriginal[i + a];
          avgCount++;
        }
      }

      colors[i] = avgTotal / avgCount;
    }

    int mostColorIndex = 0;
    int mostColorAmount = 0;
    int darkestColorIndex = 255;
    for (int i = 0; i <= 255; i++) {
      int colorAmount = 0;

      for (int add = (int) (-3 * multFactor); add <= (int) (3 * multFactor); add++) {
        int newIndex = i + add;

        if (newIndex >= 0 && newIndex < 255) {
          if (colorsOriginal[newIndex] > 0) {
            colorAmount += colorsOriginal[newIndex];
          }
        }
      }
      if (Math.abs(i - 135) < 10) {
      }
      if (i < darkestColorIndex && colorAmount >= 4) {
        darkestColorIndex = i;
      }

      if (colorAmount > mostColorAmount) {
        mostColorIndex = i;
        mostColorAmount = colorAmount;
      }
    }

    int foundFirst = (int) colorsOriginal[darkestColorIndex];
    int foundFirstColor = darkestColorIndex;

    for (int i = 0; i <= 255; i++) {
      if (i != 255) {
        if (foundFirst == -1) {
          if (colorsOriginal[i] > 2 && colors[i] > colors[i + 1]) {
            foundFirst = (int) colorsOriginal[i];
            foundFirstColor = i;
          }
        } else {
          //System.out.println((colors[i] < colors[foundFirstColor]) + " - " + (colors[i] < colors[i + 1]));
          if (/*colors[i] < colors[foundFirstColor] && */colors[i] < colors[i + 1]) {
            if (i > foundFirstColor + mostColorIndex / 5) {//edit this / value.... yeah...
              boolean colorTooDark = (mostColorIndex >= i && i < foundFirstColor * 2);
              boolean closerToBlack = Math.abs(foundFirstColor - i) < Math.abs(mostColorIndex - i);//if it starts to include parts of the iamge as black this can be divided and say if the color is closer to the average of mostCOlorIndex and foundFirstColor then set the lightestColorWhatever to the average.
              if (colorTooDark || closerToBlack) {
                return (mostColorIndex + foundFirstColor) / 2;
              } else {
                return (i + foundFirstColor) / 2;
              }
            }
          }
        }
      }
    }

    return 0;
  }

  public static int getBackgroundColor(Bitmap newBI) {
    //int lightestTextColor = 150;//(50, 50, 50)
    int bgRed = 0;
    int bgGreen = 0;
    int bgBlue = 0;
    int bgCount = 0;

    int textSize = 10;
    int lagAmount = 10;

    //g.setColor(Color.BLACK);
    //g.fillPolygon(newPolygon);

    for (int x = 0; x < newBI.getWidth(); x += lagAmount) {
      for (int y = 0; y < newBI.getHeight(); y += lagAmount) {
        int biColor = newBI.getPixel(x, y);
        int biRed = (biColor & 16711680) >> 16;
        int biGreen = (biColor & 65280) >> 8;
        int biBlue = biColor & 255;
        int biAvg = (biRed + biGreen + biBlue) / 3;

        if (biAvg > 50) {
          bgRed += biRed;
          bgGreen += biGreen;
          bgBlue += biBlue;
          bgCount++;
        } else {
          for (int addY = 1; addY <= textSize; addY++) {
            if (y + addY < newBI.getHeight()) {
              int biColorUP = newBI.getPixel(x, y + addY);
              int biRedUP = (biColorUP & 16711680) >> 16;
              int biGreenUP = (biColorUP & 65280) >> 8;
              int biBlueUP = biColorUP & 255;
              int biAvgUP = (biRedUP + biGreenUP + biBlueUP) / 3;

              if (biAvgUP > 50) {
                bgRed += biRedUP;
                bgGreen += biGreenUP;
                bgBlue += biBlueUP;
                bgCount++;
                break;
              }
            }

            if (y - addY >= 0) {
              int biColorDOWN = newBI.getPixel(x, y - addY);
              int biRedDOWN = (biColorDOWN & 16711680) >> 16;
              int biGreenDOWN = (biColorDOWN & 65280) >> 8;
              int biBlueDOWN = biColorDOWN & 255;
              int biAvgDOWN = (biRedDOWN + biGreenDOWN + biBlueDOWN) / 3;

              if (biAvgDOWN > 50) {
                bgRed += biRedDOWN;
                bgGreen += biGreenDOWN;
                bgBlue += biBlueDOWN;
                bgCount++;
                break;
              }
            }
          }
        }
      }
    }

    int backgroundColor = 0xff000000;

    if (bgCount != 0)
      backgroundColor = Color.argb(0xff, bgRed / bgCount, bgGreen / bgCount, bgBlue / bgCount);

    return backgroundColor;
  }

  public static int getAverageY(Polygon p) {
    int totalY = 0;
    double totalLineLength = 0;

    for (int i = 0; i < p.npoints; i++) {
      int curX = p.xpoints[i];
      int curY = p.ypoints[i];
      int nextX;
      int nextY;

      if (i + 1 < p.npoints) {
        nextX = p.xpoints[i + 1];
        nextY = p.ypoints[i + 1];
      } else {
        nextX = p.xpoints[0];
        nextY = p.ypoints[0];
      }

      double distance = Math.hypot(nextX - curX, nextY - curY);

      totalLineLength += distance;
      totalY += (Math.abs(curY - nextY) / 2 + Math.min(nextY, curY)) * distance;
    }

    return (int) (totalY / totalLineLength);
  }
}
