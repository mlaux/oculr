package com.oculrtech.oculr.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.oculrtech.oculr.OculrActivity;
import com.oculrtech.oculr.R;
import com.oculrtech.oculr.SelectionResult;
import com.oculrtech.oculr.ocr.CropImageTask;
import com.oculrtech.oculr.ocr.Exif;

public class CameraFragment extends Fragment implements Camera.PictureCallback, View.OnClickListener {
  private static final String TAG = CameraFragment.class.getSimpleName();
  private CameraView camView;
  private ImageView camImage;
  private PolyView polyView;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    View root = inflater.inflate(R.layout.fragment_camera, container, false);

    camView = (CameraView) root.findViewById(R.id.cv_camera);
    camImage = (ImageView) root.findViewById(R.id.iv_cam_image);
    polyView = (PolyView) root.findViewById(R.id.pv_poly);

    root.findViewById(R.id.bn_capture).setOnClickListener(this);
    root.findViewById(R.id.bn_done).setOnClickListener(this);
    root.findViewById(R.id.bn_cancel).setOnClickListener(this);

    return root;
  }

  @Override
  public void onClick(View v) {
    switch(v.getId()) {
      case R.id.bn_capture: captureClicked(); break;
      case R.id.bn_done: doneClicked(); break;
      case R.id.bn_cancel: cancelCrop(); break;
    }
  }

  public void captureClicked() {
    camView.takePicture();
  }

  public void doneClicked() {
    cancelCrop();

    OculrActivity oa = (OculrActivity) getActivity();
    oa.switchToResults();
    new CropImageTask(oa).execute();
  }

  public void cancelCrop() {
    polyView.endDrawing();
    camView.startPreview();
    camImage.setVisibility(View.GONE);
    polyView.setVisibility(View.GONE);

    swapButtons();
  }

  public void onPictureTaken(byte[] pic, Camera cam) {
    cam.stopPreview();

    BitmapFactory.Options opt = new BitmapFactory.Options();
    opt.inScaled = false;
    Bitmap bmp = BitmapFactory.decodeByteArray(pic, 0, pic.length, opt);
    int orientation = Exif.getOrientation(pic);

    if(orientation != Exif.NO_ORIENTATION) {
      // orientation was in EXIF, we need to manually rotate the image.
      Matrix matrix = new Matrix();
      matrix.postRotate(orientation);

      Log.d(TAG, "Rotation: " + orientation);

      Bitmap rotated = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
      if(rotated != bmp) {
        bmp.recycle();
      }

      bmp = rotated;
    }

    camImage.setImageBitmap(bmp);
    camImage.setVisibility(View.VISIBLE);

    SelectionResult sr = SelectionResult.getInstance();

    sr.pic = bmp;

    polyView.setVisibility(View.VISIBLE);
    swapButtons();

    if(OculrActivity.cropHowto) {
      OculrActivity.showTextDialog(getActivity(), R.string.crop_howto);
      OculrActivity.cropHowto = false;
    }
  }

  public void swapButtons() {
    View cap = getView().findViewById(R.id.bn_capture);
    View done = getView().findViewById(R.id.ll_donecancel);
    View gradient = getView().findViewById(R.id.iv_cam_gradient);

    if(cap.getVisibility() == View.VISIBLE) {
      cap.setVisibility(View.GONE);
      done.setVisibility(View.VISIBLE);
      gradient.setVisibility(View.VISIBLE);
    } else {
      cap.setVisibility(View.VISIBLE);
      done.setVisibility(View.GONE);
      gradient.setVisibility(View.GONE);
    }
  }

  public boolean isCropping() {
    return polyView.getVisibility() == View.VISIBLE;
  }
}
