package com.oculrtech.oculr.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.oculrtech.oculr.R;
import com.oculrtech.oculr.results.ImageDownloadTask;

public class VidThumbView extends RelativeLayout {
  private Context context;

  private String videoUrl;

  public VidThumbView(Context ctx) {
    super(ctx);
  }

  public VidThumbView(Context ctx, String titleText, String thumbUrl,  String vidUrl) {
    super(ctx);
    this.context = ctx;

    this.videoUrl = vidUrl;

    ProgressBar pb = new ProgressBar(ctx);

    RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams
        (RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
    rlp.addRule(RelativeLayout.CENTER_IN_PARENT);
    addView(pb, rlp);

    ImageView image = new ImageView(ctx);
    image.setScaleType(ScaleType.FIT_XY);
    image.setVisibility(View.GONE);

    int width = ctx.getResources().getDimensionPixelSize(R.dimen.vid_thumb_width);
    int height = ctx.getResources().getDimensionPixelSize(R.dimen.vid_thumb_height);

    rlp = new RelativeLayout.LayoutParams(width, height);
    rlp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
    rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

    addView(image, rlp);
    new ImageDownloadTask(image).execute(thumbUrl);

    TextView title = new TextView(ctx);
    title.setText(titleText);
    title.setBackgroundColor(0x7fa33bbb);
    title.setTextSize(20.0f);
    title.setPadding(7, 15, 7, 15);

    rlp = new RelativeLayout.LayoutParams(width, RelativeLayout.LayoutParams.WRAP_CONTENT);
    rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

    addView(title, rlp);

    View v = new View(ctx);
    v.setBackgroundColor(0xffffffff);

    int sepWidth = ctx.getResources().getDimensionPixelSize(R.dimen.vid_sep_width);
    rlp = new RelativeLayout.LayoutParams(sepWidth, RelativeLayout.LayoutParams.MATCH_PARENT);
    rlp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

    addView(v, rlp);

    setOnClickListener(new ClickListener());
  }

  private class ClickListener implements View.OnClickListener {
    @Override
    public void onClick(View v) {
      Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl));
      context.startActivity(myIntent);
    }
  }
}
