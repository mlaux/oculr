package com.oculrtech.oculr.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.Spannable;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.oculrtech.oculr.R;

public class AboutFragment extends Fragment {
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View root = inflater.inflate(R.layout.fragment_about, container, false);
    TextView tv = (TextView) root.findViewById(R.id.tv_about_right);
    tv.setMovementMethod(LinkMovementMethod.getInstance());
    tv.setText(Html.fromHtml(getActivity().getResources().getString(R.string.about_right)));
    stripUnderlines(tv);
    return root;
  }

  private void stripUnderlines(TextView textView) {
    Spannable s = (Spannable) textView.getText();
    URLSpan[] spans = s.getSpans(0, s.length(), URLSpan.class);
    for (URLSpan span : spans) {
      int start = s.getSpanStart(span);
      int end = s.getSpanEnd(span);
      s.removeSpan(span);
      span = new AboutURLSpan(span.getURL());
      s.setSpan(span, start, end, 0);
    }
    textView.setText(s);
  }

  private static class AboutURLSpan extends URLSpan {
    private static final int TWITTER_BLUE = 0xffa33bbb;

    public AboutURLSpan(String url) {
      super(url);
    }

    @Override
    public void updateDrawState(TextPaint ds) {
      super.updateDrawState(ds);
      ds.setUnderlineText(false);
      ds.setColor(TWITTER_BLUE);
    }
  }
}
