package com.oculrtech.oculr.ui;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.oculrtech.oculr.R;

public class IconListAdapter extends BaseAdapter {
  private Context context;
  private String[] text;
  private int[] icons;

  public IconListAdapter(Context ctx, String[] objects, int[] icons) {
    this.context = ctx;
    this.text = objects;
    this.icons = icons;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    View row = inflater.inflate(R.layout.drawer_list_item, parent, false);

    Resources r = context.getResources();
    int dip2 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, r.getDisplayMetrics());
    int dip4 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, r.getDisplayMetrics());

    if(parent instanceof Spinner) {
      row.setPadding(dip4, dip4, dip4, dip2);
    }

    TextView item = (TextView) row.findViewById(R.id.tv_list_text);
    if(parent instanceof Spinner) {
      item.setTextColor(0xff949494);
      item.setTextSize(18);
    }
    item.setText(text[position]);

    ImageView icon = (ImageView) row.findViewById(R.id.iv_list_icon);
    icon.setImageResource(icons[position]);
    if(parent instanceof Spinner) {
      icon.setScaleX(0.75f);
      icon.setScaleY(0.75f);
    } else {
      icon.setScaleX(1f);
      icon.setScaleY(1f);
    }

    return row;
  }

  @Override
  public int getCount() {
    return text.length;
  }

  @Override
  public Object getItem(int arg0) {
    return text[arg0];
  }

  @Override
  public long getItemId(int arg0) {
    return -1;
  }
}
