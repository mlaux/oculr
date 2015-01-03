package com.oculrtech.oculr;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CheckBox;
import android.widget.ListView;

import com.evernote.client.android.EvernoteSession;
import com.evernote.client.android.OnClientCallback;
import com.evernote.edam.type.User;
import com.oculrtech.oculr.results.QueryResult;
import com.oculrtech.oculr.ui.AboutFragment;
import com.oculrtech.oculr.ui.CameraFragment;
import com.oculrtech.oculr.ui.HistoryFragment;
import com.oculrtech.oculr.ui.IconListAdapter;
import com.oculrtech.oculr.ui.MenuSpinner;
import com.oculrtech.oculr.ui.OopsFragment;
import com.oculrtech.oculr.ui.ProcessingFragment;
import com.oculrtech.oculr.ui.ResultsFragment;

public class OculrActivity extends FragmentActivity implements OnItemClickListener, OnItemSelectedListener {
  public static final File DATA_PATH = new File(
      Environment.getExternalStorageDirectory() + "/Android/data/com.oculrtech.oculr/files/");

  public static final int INDEX_RESULTS = 0;
  public static final int INDEX_HISTORY = 1;
  public static final int INDEX_PROCESSING = 2;
  public static final int INDEX_ABOUT = 3;

  public static final String SERVLET_URL = "http://ayubs.dyndns.org:8080/ServletTest/UploadServlet";
  public static final String NERVE_URL = "http://ayubs.dyndns.org/oculr/nerve/submit.php";

  public static final String LIMERICK = "Little Johnny took a drink,\nBut he will drink no more,\nFor what he thought was H2O,\nWas H2SO4";

  private static final String[] MENU_ITEMS = { "Results", "History", "Image Processing", "About" };
  private static final int[] MENU_ICONS = { R.drawable.ic_results, R.drawable.ic_history, R.drawable.ic_marketing, R.drawable.ic_about };

  private static final String[] DROPDOWN_LOGGED_OUT = { "Sign in" };
  private static final int[] ICONS_LOGGED_OUT = { R.drawable.ic_user };

  private static final String[] DROPDOWN_LOGGED_IN = { "Signed in", "Sign out" };
  private static final int[] ICONS_LOGGED_IN = { R.drawable.ic_user, R.drawable.ic_logout };

  private static final Fragment[] MENU_FRAGMENTS = { null, new HistoryFragment(), null, new AboutFragment() };

  private static final String HANDWRITING_CHECK_URL = "http://www.oculrtech.com/status.txt";

  private static final String KEY_FIRST_RUN = "firstRun";
  private static final String KEY_HANDWRITING_RECOGNITION_ENABLED = "handwritingRecognitionEnabled";

  private static final int NUM_SETTINGS = 3;

  private DrawerLayout drawerLayout;
  private ActionBarDrawerToggle drawerToggle;

  private MenuSpinner userSpinner;
  private ListView lvNavMenu;

  private SharedPreferences prefs;

  public static boolean betaHandwritingSupported;
  public static boolean nerve;
  public static boolean cropHowto;

  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_oculr);

    getActionBar().setDisplayHomeAsUpEnabled(true);
    getActionBar().setHomeButtonEnabled(true);

    prefs = getSharedPreferences("com.oculrtech.oculr", MODE_PRIVATE);
    if(prefs.getBoolean(KEY_FIRST_RUN, true)) {
      cropHowto = true;
      prefs.edit().putBoolean(KEY_FIRST_RUN, false).commit();
    }

    drawerLayout = (DrawerLayout) findViewById(R.id.dl_main);
    drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

    drawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
        R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close) {
      public void onDrawerClosed(View view) {
        invalidateOptionsMenu();
      }

      public void onDrawerOpened(View drawerView) {
        invalidateOptionsMenu();
      }
    };
    drawerLayout.setDrawerListener(drawerToggle);

    userSpinner = (MenuSpinner) findViewById(R.id.sp_userinfo);
    userSpinner.setMenuListener(this);

    lvNavMenu = (ListView) findViewById(R.id.lv_drawer);
    lvNavMenu.setAdapter(new IconListAdapter(this, MENU_ITEMS, MENU_ICONS));
    lvNavMenu.setOnItemClickListener(this);

    findViewById(R.id.cb_handwriting).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        boolean enabled = ((CheckBox) v).isChecked();
        prefs.edit().putBoolean(KEY_HANDWRITING_RECOGNITION_ENABLED, enabled).commit();
        if(enabled)
          showTextDialog(OculrActivity.this, R.string.handwriting_warning);
      }
    });

    if(prefs.getBoolean(KEY_HANDWRITING_RECOGNITION_ENABLED, false)) {
      ((CheckBox) findViewById(R.id.cb_handwriting)).setChecked(true);
    }

    if(savedInstanceState == null) {
      FragmentManager fm = getSupportFragmentManager();
      fm.beginTransaction().replace(R.id.fl_content, new CameraFragment()).commit();
    }

    File destDir = new File(DATA_PATH, "tessdata");
    destDir.mkdirs();

    new AssetExtractor(this, "tessdata", destDir).execute();
    ENAdapter.initSession(this);

    if(ENAdapter.signedIn()) {
      requestUsername();
      userSpinner.setAdapter(new IconListAdapter(this, DROPDOWN_LOGGED_IN, ICONS_LOGGED_IN));
    } else {
      userSpinner.setAdapter(new IconListAdapter(this, DROPDOWN_LOGGED_OUT, ICONS_LOGGED_OUT));
    }

    try {
      checkHandwriting();
    } catch(Exception e) { e.printStackTrace(); }
  }

  public boolean isHandwritingRecognitionEnabled() {
    return betaHandwritingSupported && ((CheckBox) findViewById(R.id.cb_handwriting)).isChecked();
  }

  public static String getSubmissionURL() {
    return nerve ? NERVE_URL : SERVLET_URL;
  }

  private void checkHandwriting() {
    new AsyncTask<Void, Void, Boolean[]>() {
      public Boolean[] doInBackground(Void... params) {
        try {
          URLConnection huc = new URL(HANDWRITING_CHECK_URL).openConnection();
          BufferedReader br = new BufferedReader(new InputStreamReader(huc.getInputStream()));
          Boolean[] settings = new Boolean[NUM_SETTINGS];
          for(int k = 0; k < NUM_SETTINGS; k++)
            settings[k] = br.readLine().endsWith("true");
          return settings;
        } catch(Exception e) {
          e.printStackTrace();
          return new Boolean[NUM_SETTINGS];
        }
      }

      public void onPostExecute(Boolean[] result) {
        if(result[0]) {
          betaHandwritingSupported = true;
          findViewById(R.id.cb_handwriting).setVisibility(View.VISIBLE);
        }
        // guap = result[1];
        nerve = result[2];
      }
    }.execute();
  }

  private void requestUsername() {
    ENAdapter.getUserStoreClient().getUser(new OnClientCallback<User>() {
      @Override
      public void onSuccess(User data) {
        DROPDOWN_LOGGED_IN[0] = data.getUsername();
        userSpinner.setAdapter(new IconListAdapter(OculrActivity.this, DROPDOWN_LOGGED_IN, ICONS_LOGGED_IN));
      }

      @Override
      public void onException(Exception exception) {
        exception.printStackTrace();
        userSpinner.setAdapter(new IconListAdapter(OculrActivity.this, DROPDOWN_LOGGED_OUT, ICONS_LOGGED_OUT));
      }
    });
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
      MenuInflater inflater = getMenuInflater();
      inflater.inflate(R.menu.activity_oculr, menu);
      return true;
  }

  @Override
  protected void onPostCreate(Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);
    drawerToggle.syncState();
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    drawerToggle.onConfigurationChanged(newConfig);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (drawerToggle.onOptionsItemSelected(item))
      return true;

    if(item.getItemId() == R.id.menu_take_pic) {
      if(!(getSupportFragmentManager().findFragmentById(R.id.fl_content) instanceof CameraFragment))
        tryReplace(new CameraFragment());
          drawerLayout.closeDrawer(GravityCompat.START);
    }

    return super.onOptionsItemSelected(item);
  }

  private void tryReplace(Fragment f) {
    FragmentManager fm = getSupportFragmentManager();
    FragmentTransaction ft = fm.beginTransaction();
    ft.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    ft.replace(R.id.fl_content, f);

    ft.commit();
  }

  // APPARENTLY LISTVIEWS USE ONITEMCLICK BUT SPINNERS USE ONITEMSELECTED ????
  // SAME SIGNATURE AND EVERYTHING

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    if(position == INDEX_HISTORY) {
      try {
        String url;
        if(ENAdapter.isSandbox()) {
          url = "https://sandbox.evernote.com/Home.action";
        } else {
          url = "https://www.evernote.com/Home.action";
        }

        Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(myIntent);
      } catch(Exception e) {
        e.printStackTrace();
      }
    } else {
      Fragment f = MENU_FRAGMENTS[position];
      if(f == null) {
        f = new OopsFragment();
      }

      tryReplace(f);

      lvNavMenu.setItemChecked(position, true);
    }

    drawerLayout.closeDrawer(GravityCompat.START);
  }

  @Override
  public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
    String text = (String) parent.getItemAtPosition(position);

    drawerLayout.closeDrawer(GravityCompat.START);

    if(text.equals("Sign in"))
      ENAdapter.signIn(this);
    else if(text.equals("Sign out")) {
      ENAdapter.signOut(this);
      userSpinner.setAdapter(new IconListAdapter(OculrActivity.this, DROPDOWN_LOGGED_OUT, ICONS_LOGGED_OUT));
    }
  }

  @Override
  public void onNothingSelected(AdapterView<?> arg0) { }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    switch (requestCode) {
      case EvernoteSession.REQUEST_CODE_OAUTH:
        if (resultCode == Activity.RESULT_OK) {
          requestUsername();
          checkNeedSave();
        }
        break;
    }
  }

  private void checkNeedSave() {
    FragmentManager fm = getSupportFragmentManager();
    Fragment frag = fm.findFragmentById(R.id.fl_content);
    if(frag instanceof ResultsFragment) {
      ResultsFragment rf = (ResultsFragment) frag;
      if(rf.needSave())
        rf.saveEvernote();
    }
  }

  public void switchToResults() {
    MENU_FRAGMENTS[INDEX_RESULTS] = new ResultsFragment();
    MENU_FRAGMENTS[INDEX_PROCESSING] = new ProcessingFragment();
    try {
      tryReplace(MENU_FRAGMENTS[INDEX_RESULTS]);
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

  public void setMarketingPics(Bitmap[] pics) {
    ((ProcessingFragment) MENU_FRAGMENTS[INDEX_PROCESSING]).setPics(pics);
  }

  public void setResults(QueryResult results) {
    if(results.wolf != null)
      ((ResultsFragment) MENU_FRAGMENTS[INDEX_RESULTS]).setResults(results.wolf);
    ((ResultsFragment) MENU_FRAGMENTS[INDEX_RESULTS]).setVideos(results.videos);
  }

  public static void showTextDialog(Context ctx, int resId) {
    new AlertDialog.Builder(ctx).setMessage(resId).setPositiveButton("OK", new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface arg0, int arg1) { }
    }).show();
  }

  @Override
  public void onBackPressed() {
    try {
      Fragment f = getSupportFragmentManager().findFragmentById(R.id.fl_content);
      if(f instanceof CameraFragment && ((CameraFragment) f).isCropping()) {
        ((CameraFragment) f).cancelCrop();
      } else if(f instanceof CameraFragment) {
        finish();
      } else {
        tryReplace(new CameraFragment());
      }
    } catch(Exception e) {
      finish();
    }
  }
}