package com.oculrtech.oculr.ui;

import java.util.List;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.oculrtech.oculr.OculrActivity;
import com.oculrtech.oculr.R;

public class CameraView extends SurfaceView implements SurfaceHolder.Callback {
  private Camera camera;

  public CameraView(Context context) {
    this(context, null);
  }

  @SuppressWarnings("deprecation")
  public CameraView(Context ctx, AttributeSet as) {
    super(ctx, as);

    if(isInEditMode())
      return;

    getHolder().addCallback(this);
  }

  public void surfaceChanged(SurfaceHolder sh, int format, int w, int h) {
    try {
      camera = Camera.open();
      camera.setDisplayOrientation(90);

      Camera.Parameters cp = camera.getParameters();
      List<Size> sizes = cp.getSupportedPictureSizes();

      for (Size s : sizes) {
        if (s.width <= 1280) {
          cp.setPictureSize(s.width, s.height);
          break;
        }
      }

      List<int[]> previewFpsRanges = cp.getSupportedPreviewFpsRange();
      int highestMin = 0;
      int highestMax = 0;
      for(int[] fpsRange : previewFpsRanges) {
        if(fpsRange[0] > highestMin) {
          highestMin = fpsRange[0];
          highestMax = fpsRange[1];
        }
      }
      // TODO This increases fps but makes the image really dark, figure out why
      //cp.setPreviewFpsRange(highestMin, highestMax);

      cp.setRotation(90); // TODO do not force 90
      camera.setParameters(cp);

      camera.setPreviewDisplay(sh);
      camera.startPreview();

    } catch(Exception e) {
      e.printStackTrace();
      // TODO error handling
    }
  }

  public void surfaceCreated(SurfaceHolder sh) {

  }

  public void surfaceDestroyed(SurfaceHolder sh) {
    if(camera != null) {
      camera.stopPreview();
      camera.release();
    }
  }

  public boolean onTouchEvent(MotionEvent event) {
    if(camera == null || event.getAction() != MotionEvent.ACTION_DOWN)
      return false;

    try {
      camera.autoFocus(new Camera.AutoFocusCallback() {
        public void onAutoFocus(boolean success, Camera camera) { }
      });
    } catch(Exception e) {
      e.printStackTrace();
    }
    return true;
  }

  public void takePicture() {
    if(camera == null)
      return;
    try {
      OculrActivity oa = (OculrActivity) getContext();
      CameraFragment cf = (CameraFragment) oa.getSupportFragmentManager().findFragmentById(R.id.fl_content);
      camera.takePicture(null, null, cf);
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

  public void startPreview() {
    try {
      if(camera != null)
        camera.startPreview();
    } catch(Exception e) {
      e.printStackTrace();
    }
  }
}
