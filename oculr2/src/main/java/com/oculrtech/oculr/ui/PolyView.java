package com.oculrtech.oculr.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.oculrtech.oculr.SelectionResult;

public class PolyView extends View {

  // different places on the crop rectangle the user can grab/drag
  private static final int GRIP_NONE = 0;
  private static final int GRIP_TOP_LEFT = 1;
  private static final int GRIP_TOP_RIGHT = 2;
  private static final int GRIP_BOTTOM_LEFT = 3;
  private static final int GRIP_BOTTOM_RIGHT = 4;
  private static final int GRIP_SELECTED_Y = 5;
  private static final int GRIP_WHOLE_RECT = 6;

  // finger fudge distance from the rectangle corners
  private static final int GRIP_DISTANCE = 40;
  // how big do we draw the grips
  private static final int GRIP_RECT_SIZE = 40;
  // how small can the crop rectangle get before we stop making it any smaller
  private static final int RECT_SIZE_MARGIN = 20;

  // how close to the edge of the rectangle can selectedY get
  private static final int SELECTED_Y_MARGIN = 10;

  private static final int GRADIENT_COLOR_CLEAR = 0x00a43bbb;
  private static final int GRADIENT_COLOR_SOLID = 0xffa43bbb;

  private static final int DEFAULT_WIDTH = 400;
  private static final int DEFAULT_HEIGHT = 200;

  private int lastX;
  private int lastY;

  private Rect selection = new Rect();

  // the user drags this horizontal line through the text
  private int selectedY = -1;

  // one of the grip constants defined above
  private int currentGrip;

  private LinearGradient leftGrad;
  private LinearGradient rightGrad;

  public PolyView(Context context) {
    this(context, null);
  }

  public PolyView(Context ctx, AttributeSet as) {
    super(ctx, as);
  }

  public boolean onTouchEvent(MotionEvent event) {
    if(selection == null)
      return false;

    int x = (int) event.getX();
    int y = (int) event.getY();

    int id = event.getAction();

    if(id == MotionEvent.ACTION_DOWN) {
      // check corners of rectangle
      if(dist(x, y, selection.left, selection.top) < GRIP_DISTANCE)
        currentGrip = GRIP_TOP_LEFT;
      else if(dist(x, y, selection.right, selection.top) < GRIP_DISTANCE)
        currentGrip = GRIP_TOP_RIGHT;
      else if(dist(x, y, selection.left, selection.bottom) < GRIP_DISTANCE)
        currentGrip = GRIP_BOTTOM_LEFT;
      else if(dist(x, y, selection.right, selection.bottom) < GRIP_DISTANCE)
        currentGrip = GRIP_BOTTOM_RIGHT;

      // check selectedY line
      else if(Math.abs(y - selectedY) < GRIP_DISTANCE)
        currentGrip = GRIP_SELECTED_Y;

      // check dragging the entire rectangle around
      else if(selection.contains(x, y))
        currentGrip = GRIP_WHOLE_RECT;

      lastX = x;
      lastY = y;
    }

    else if(id == MotionEvent.ACTION_MOVE) {
      int dx = x - lastX;
      int dy = y - lastY;

      // we start these vars with their old values
      // then modify, then check to see if they're valid
      // if so, update selection.*

      int newLeft = selection.left;
      int newTop = selection.top;
      int newRight = selection.right;
      int newBottom = selection.bottom;

      boolean snapCenter = true;

      // check each grip constant
      // don't let the user make the rectangle smaller than RECT_SIZE_MARGIN
      // but don't restrict if they're trying to make the rectangle bigger

      switch(currentGrip) {
        case GRIP_TOP_LEFT:
          if(selection.right - selection.left > RECT_SIZE_MARGIN || dx < 0)
            newLeft = x;
          if(selection.bottom - selection.top > RECT_SIZE_MARGIN || dy < 0)
            newTop = y;
          break;
        case GRIP_TOP_RIGHT:
          if(selection.right - selection.left > RECT_SIZE_MARGIN || dx > 0)
            newRight = x;
          if(selection.bottom - selection.top > RECT_SIZE_MARGIN || dy < 0)
            newTop = y;
          break;
        case GRIP_BOTTOM_LEFT:
          if(selection.right - selection.left > RECT_SIZE_MARGIN || dx < 0)
            newLeft = x;
          if(selection.bottom - selection.top > RECT_SIZE_MARGIN || dy > 0)
            newBottom = y;
          break;
        case GRIP_BOTTOM_RIGHT:
          if(selection.right - selection.left > RECT_SIZE_MARGIN || dx > 0)
            newRight = x;
          if(selection.bottom - selection.top > RECT_SIZE_MARGIN || dy > 0)
            newBottom = y;
          break;
        case GRIP_SELECTED_Y:
          // don't snap selectedY to the center, since the user is dragging it!
          snapCenter = false;

          // make sure it doesn't get too close to the top/bottom
          if(y > selection.top + SELECTED_Y_MARGIN && y < selection.bottom - SELECTED_Y_MARGIN) {
            selectedY = y;
          }
          break;
        case GRIP_WHOLE_RECT:
          // bump rectangle coordinates and selectedY
          newLeft = selection.left + dx;
          newRight = selection.right + dx;
          newTop = selection.top + dy;
          newBottom = selection.bottom + dy;
          selectedY += dy;
          break;
      }

      // check to make sure it's a valid rectangle
      // (right > left, bottom > top)

      if(newRight > newLeft) {
        selection.left = newLeft;
        selection.right = newRight;
      }

      if(newBottom > newTop) {
        selection.top = newTop;
        selection.bottom = newBottom;
        if(snapCenter)
          selectedY = selection.top + (selection.bottom - selection.top) / 2;
      }

      // user dragging the corners could have pushed selectedY too close
      // to the edge. Correct this here
      if(selectedY < selection.top + SELECTED_Y_MARGIN)
        selectedY = selection.top + SELECTED_Y_MARGIN;
      if(selectedY > selection.bottom - SELECTED_Y_MARGIN)
        selectedY = selection.bottom - SELECTED_Y_MARGIN;

      // recompute gradient effects
      leftGrad = new LinearGradient(0, selectedY, selection.left, selectedY,
            GRADIENT_COLOR_CLEAR, GRADIENT_COLOR_SOLID, Shader.TileMode.CLAMP);
      rightGrad = new LinearGradient(selection.right, selectedY, getWidth(), selectedY,
            GRADIENT_COLOR_SOLID, GRADIENT_COLOR_CLEAR, Shader.TileMode.CLAMP);

      lastX = x;
      lastY = y;
    }

    else if(id == MotionEvent.ACTION_UP) {
      currentGrip = GRIP_NONE;
    }

    invalidate();

    return true;
  }

  private static final Paint paint = new Paint();
  static {
    paint.setAntiAlias(true);
  }

  protected void onDraw(Canvas canvas) {
    if(selectedY == -1) {
      // this is the first paint. we didn't know the dimensions of screen
      // at instantiation time, but now we do
      // initialize rect to center of screen
      selection.left = getWidth() / 2 - DEFAULT_WIDTH / 2;
      selection.right = getWidth() / 2 + DEFAULT_WIDTH / 2;
      selection.top = getHeight() / 2 - DEFAULT_HEIGHT / 2;
      selection.bottom = getHeight() / 2 + DEFAULT_HEIGHT / 2;

      // center selectedY
      selectedY = getHeight() / 2;
    }

    canvas.drawColor(0);
    paint.setShader(null);

    // gray out non-selected portion
    paint.setColor(0x99000000);
    paint.setStyle(Paint.Style.FILL);
    canvas.drawRect(0, 0, selection.left, getHeight(), paint);
    canvas.drawRect(selection.left, 0, selection.right, selection.top, paint);
    canvas.drawRect(selection.right, 0, getWidth(), getHeight(), paint);
    canvas.drawRect(selection.left, selection.bottom, selection.right, getHeight(), paint);

    // draw selection rect and selectedY line
    paint.setColor(0xffa43bbb);
    paint.setStrokeWidth(3.0f);
    paint.setStyle(Paint.Style.STROKE);
    canvas.drawRect(selection, paint);
    canvas.drawLine(selection.left, selectedY, selection.right, selectedY, paint);

    // draw cool gradients
    paint.setShader(leftGrad);
    canvas.drawLine(0, selectedY, selection.left, selectedY, paint);

    paint.setShader(rightGrad);
    canvas.drawLine(selection.right, selectedY, getWidth(), selectedY, paint);

    // fill selection rect
    paint.setStyle(Paint.Style.FILL);
    paint.setShader(null);
    paint.setColor(0xffd6b4e9);

    // draw each grip
    drawGripRect(canvas, selection.left, selection.top);
    drawGripRect(canvas, selection.right, selection.top);
    drawGripRect(canvas, selection.left, selection.bottom);
    drawGripRect(canvas, selection.right, selection.bottom);
  }

  /** draw one of the four grip corners */
  private void drawGripRect(Canvas c, int x, int y) {
    c.drawRect(x - GRIP_RECT_SIZE / 2, y - GRIP_RECT_SIZE / 2,
          x + GRIP_RECT_SIZE / 2, y + GRIP_RECT_SIZE / 2, paint);
  }

  /**
   * called when the user presses "OK" on the crop screen.
   * Updates the SelectionResult instance with the region the user selected,
   * and the Y-value to start OCR at.
   */
  public void endDrawing() {
    SelectionResult res = SelectionResult.getInstance();

    res.selection = selection;
    res.referenceY = selectedY;

    res.previewWidth = getWidth();
    res.previewHeight = getHeight();

    selectedY = -1;
  }

  public int getSelectedY() {
    return selectedY;
  }

  private static double dist(int x1, int y1, int x2, int y2) {
    return Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
  }
}
