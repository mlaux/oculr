package com.oculrtech.oculr.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Spinner;

public class MenuSpinner extends Spinner {
  private OnItemSelectedListener listener;

  public MenuSpinner(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  public void setSelection(int position) {
    super.setSelection(position);
    if (listener != null) {
      listener.onItemSelected(this, getChildAt(position), position, 0);
    }
  }

  /*
   * calls even if the selected item remains unchanged.
   */
  public void setMenuListener(OnItemSelectedListener listener) {
    this.listener = listener;
  }
}