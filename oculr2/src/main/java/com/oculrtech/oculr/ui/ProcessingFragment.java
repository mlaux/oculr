package com.oculrtech.oculr.ui;

import com.oculrtech.oculr.R;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class ProcessingFragment extends Fragment {
  private Bitmap[] pics;

  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_processing, container, false);

    if(pics != null) {
      refreshPics(view);
    }

    return view;
  }

  public void setPics(Bitmap[] pics) {
    this.pics = pics;

    if(getView() != null)
      refreshPics(getView());
  }

  public void refreshPics(View view) {
    LinearLayout layout = (LinearLayout) view.findViewById(R.id.ll_pics);

    view.findViewById(R.id.sv_pics).setVisibility(View.VISIBLE);

    for(int k = 0; k < pics.length; k++) {
      ImageView image = new ImageView(getActivity());
      image.setImageBitmap(pics[k]);
      layout.addView(image, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
    }
  }
}
