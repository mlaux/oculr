package com.oculrtech.oculr;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;

public class AssetExtractor extends AsyncTask<Void, Integer, Void> {
  private Activity act;
  private String path;
  private File destDir;
  private ProgressDialog dlg;
  private boolean error;

  public AssetExtractor(Activity a, String p, File dest) {
    this.act = a;
    this.path = p;
    this.destDir = dest;

    dlg = new ProgressDialog(a);
    dlg.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    dlg.setMessage("Loading recognition data...");

    try {
      dlg.setMax(a.getAssets().list(path).length);
    } catch(Exception e) {

    }

    dlg.show();
  }

  protected void onProgressUpdate(Integer... values) {
    dlg.setProgress(values[0].intValue());
  }

  protected Void doInBackground(Void... params) {
    try {
      String[] files = act.getAssets().list(path);

      for(int k = 0; k < files.length; k++) {
        File dest = new File(destDir, files[k]);

        if(dest.exists())
          continue;

        InputStream in = act.getAssets().open(path + "/" + files[k]);
        FileOutputStream out = new FileOutputStream(dest);

        int read;
        byte[] buf = new byte[1024];
        while((read = in.read(buf)) != -1)
          out.write(buf, 0, read);

        out.flush();
        out.close();
        in.close();

        publishProgress(k);
      }
    } catch(IOException e) {
      error = true;
      e.printStackTrace();
    }

    return null;
  }

  @Override
  protected void onPostExecute(Void result) {
    dlg.dismiss();
    if(error) {
      new AlertDialog.Builder(act)
        .setMessage("Oculr requires some kind of storage on your phone. Sorry.")
        .setPositiveButton(R.string.dlg_close,
          new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
              System.exit(0);
            }
        })
        .setTitle("Oops...")
        .show();
    }
  }
}
