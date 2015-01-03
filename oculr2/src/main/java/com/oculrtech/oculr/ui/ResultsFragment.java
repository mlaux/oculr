package com.oculrtech.oculr.ui;

import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.evernote.client.android.OnClientCallback;
import com.evernote.edam.type.Data;
import com.evernote.edam.type.Note;
import com.evernote.edam.type.Resource;
import com.oculrtech.oculr.R;
import com.oculrtech.oculr.SelectionResult;
import com.oculrtech.oculr.ENAdapter;
import com.oculrtech.oculr.results.ImageDownloadTask;
import com.oculrtech.oculr.results.ImageURLBinder;
import com.oculrtech.oculr.results.YoutubeQuery;
import com.oculrtech.oculr.results.WolframQuery;

public class ResultsFragment extends Fragment {
  private static final String TITLE = "title";
  private static final String TEXT = "text";
  private static final String IMAGE = "image";

  private static final String[] FROM = { TITLE, TEXT, IMAGE };
  private static final int[] TO = { R.id.title, R.id.text, R.id.image };

  private static final List<String> OCULR_TAG = Arrays.asList("oculr");

  private List<HashMap<String, Object>> data;
  private List<YoutubeQuery.Video> videos = new ArrayList<YoutubeQuery.Video>();
  private boolean needSave;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    // TODO Auto-generated method stub
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_results, container, false);

    ListView listView = (ListView) view.findViewById(R.id.lv_card_container);
    if (data != null) {
      SimpleAdapter adapter = new SimpleAdapter(getActivity(), data, R.layout.result_list_item, FROM, TO);
      adapter.setViewBinder(new ImageURLBinder());

      view.findViewById(R.id.ll_wolf_loading).setVisibility(View.GONE);
      listView.setAdapter(adapter);
    }

    if (videos != null && !videos.isEmpty()) {
      addVideos(view, (ViewGroup) view.findViewById(R.id.ll_videos));
    }

    return view;
  }

  public void setResults(List<WolframQuery.ResultItem> results) {
    data = new ArrayList<HashMap<String, Object>>();

    Bitmap pi = SelectionResult.getInstance().processedImage;
    if(pi != null) {
      HashMap<String, Object> first = new HashMap<String, Object>();
      first.put(TITLE, "Processed image");
      first.put(IMAGE, pi);
      data.add(first);
    }

    for(WolframQuery.ResultItem res : results) {
      HashMap<String, Object> map = new HashMap<String, Object>();
      if(res.key.equals("Input"))
        continue;
      map.put(TITLE, res.key);
      map.put(TEXT, res.value);
      if(res.imageUrl != null)
        map.put(IMAGE, res.imageUrl);

      data.add(map);
    }

    if(getActivity() != null) {
      SimpleAdapter adapter = new SimpleAdapter(getActivity(), data, R.layout.result_list_item, FROM, TO);
      adapter.setViewBinder(new ImageURLBinder());

      if(getView() != null) {
        getView().findViewById(R.id.ll_wolf_loading).setVisibility(View.GONE);
        ListView listView = (ListView) getView().findViewById(R.id.lv_card_container);
        listView.setAdapter(adapter);
      }
    }
  }

  public void setVideos(List<YoutubeQuery.Video> results) {
    videos = results;

    if(getView() != null) {
      if(videos == null) {
        getView().findViewById(R.id.hsv_vids).setVisibility(View.GONE);
        getView().findViewById(R.id.v_shadow).setVisibility(View.GONE);
      } else {
        addVideos(getView(), (ViewGroup) getView().findViewById(R.id.ll_videos));
      }
    }
  }

  private void addVideos(View root, ViewGroup layout) {
    root.findViewById(R.id.hsv_vids).setVisibility(View.VISIBLE);
    root.findViewById(R.id.v_shadow).setVisibility(View.VISIBLE);

    layout.removeAllViews();

    for(YoutubeQuery.Video vid : videos) {
      VidThumbView vtv = new VidThumbView(getActivity(), vid.title, vid.thumbURL, vid.videoURL);
      layout.addView(vtv, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
    }
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
      inflater.inflate(R.menu.fragment_results, menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if(item.getItemId() == R.id.menu_evernote) {
      if(!ENAdapter.signedIn()) {
        needSave = true;
        ENAdapter.signIn(getActivity());
      } else {
        saveEvernote();
      }
    }

    return super.onOptionsItemSelected(item);
  }

  public void saveEvernote() {
    if(data == null)
      return;

    Note note = new Note();
    note.setTagNames(OCULR_TAG);
    String[] hf = addHeaderandFooter(note);

    String nBody = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
    nBody += "<!DOCTYPE en-note SYSTEM \"http://xml.evernote.com/pub/enml2.dtd\">";
    nBody += "<en-note style=\"display:block;padding:1.5rem 0 0;background: #eeeeee;\">";

    nBody += "<en-media style=\"display: block;  margin: auto;  max-width: 100%;\" hash=\"" + hf[0] + "\" type=\"image/png\" />";

    for(Map<String, Object> row : data) {
      Object obj = row.get(IMAGE);
      if(obj != null) {
        Bitmap bit;
        if(obj instanceof String)
          bit = ImageDownloadTask.getFromCache((String) obj);
        else
          bit = (Bitmap) obj; // already a bitmap

        if(bit == null)
          continue;

        Data data = imageToData(bit);

        Resource resource = new Resource();
        resource.setData(data);
        resource.setMime("image/png");
        note.addToResources(resource);

        String hash = bytesToHex(data.getBodyHash());

        nBody += "<div style=\"padding:1.5rem; padding-top:0; box-shadow:0 1px 3px #999;  background:white;  margin: 1rem auto;  border-radius:3px;  width: 25rem;  display: block; \">";
        nBody += "<h1 style=\"font-size:2rem; padding-top: 1.25rem;  font-weight:100 !important;  font-family: 'Roboto', sans-serif;\">";
        nBody += TextUtils.htmlEncode((String) row.get(TITLE));
        nBody += "</h1>";
        nBody += "<div style=\"margin-top: .5rem;\">";
        nBody += "<en-media style=\"display: block;  margin: auto;  max-width: 100%;\" type=\"image/png\" hash=\"" + hash + "\" />";
        nBody += "</div>";
        nBody += "</div>";
      }
    }

    nBody += "<div style=\"width: 28rem; margin: 0 auto;\">";
    nBody +=   "<a href=\"http://oculrtech.com\">";
    nBody +=     "<en-media style=\"border-right: 1px solid #666;  padding-right: 1rem;    display: inline-block;  vertical-align: middle;  margin: 1rem auto 0 auto;  padding-left: 4rem;\" hash=\"" + hf[1] + "\" type=\"image/png\" />";
    nBody +=   "</a>";
    nBody +=   "<div style=\"font-size:1rem;  font-weight:300;  font-family: 'Roboto', sans-serif;   margin: 1rem auto;  vertical-align: middle;    display: inline-block;  padding: 1.5rem .5rem .5rem 1rem;  line-height: 1.2;\"> <a style=\"text-decoration: none; color: #666;\" href=\"http://oculrtech.com\">oculrtech.com</a><br /><a style=\"text-decoration: none; color: #666;\" href=\"http://blog.oculr.me\">blog.oculr.me</a><br /><a style=\"text-decoration: none; color: #666;\" href=\"http://twitter.com/oculr\">@oculr</a> </div>";
    nBody += "</div>";

    nBody += "</en-note>";

    note.setTitle("Oculr " + DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime()));
    note.setContent(nBody);

    ENAdapter.getNoteStoreClient().createNote(note, new OnClientCallback<Note>() {
      @Override
      public void onSuccess(Note data) {
        Toast.makeText(getActivity(), "Note created.", Toast.LENGTH_LONG).show();
      }

      @Override
      public void onException(Exception exception) {
        exception.printStackTrace();
      }
    });
  }

  private String[] addHeaderandFooter(Note note) {
    Data header = imageToData(decodeNoScale(R.drawable.everheader));
    Resource resource = new Resource();
    resource.setData(header);
    resource.setMime("image/png");
    note.addToResources(resource);

    Data footer = imageToData(decodeNoScale(R.drawable.tag));
    resource = new Resource();
    resource.setData(footer);
    resource.setMime("image/png");
    note.addToResources(resource);

    return new String[] { bytesToHex(header.getBodyHash()), bytesToHex(footer.getBodyHash()) };
  }

  private Bitmap decodeNoScale(int res) {
    BitmapFactory.Options opt = new BitmapFactory.Options();
    opt.inScaled = false;
    return BitmapFactory.decodeResource(getResources(), res, opt);
  }

  private static Data imageToData(Bitmap bit) {
    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      bit.compress(CompressFormat.PNG, 100, baos);
      byte[] bytes = baos.toByteArray();

      Data data = new Data();
      data.setSize(bytes.length);
      data.setBody(bytes);
      data.setBodyHash(MessageDigest.getInstance("MD5").digest(bytes));

      return data;
    } catch(Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  /** @author evernote */
  private static String bytesToHex(byte[] bytes) {
    StringBuilder sb = new StringBuilder();
    for (byte hashByte : bytes) {
      int intVal = 0xff & hashByte;
      if (intVal < 0x10) {
        sb.append('0');
      }
      sb.append(Integer.toHexString(intVal));
    }
    return sb.toString();
  }

  public boolean needSave() {
    return needSave;
  }
}
