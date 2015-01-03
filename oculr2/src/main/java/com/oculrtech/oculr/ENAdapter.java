package com.oculrtech.oculr;

import android.content.Context;

import com.evernote.client.android.AsyncNoteStoreClient;
import com.evernote.client.android.AsyncUserStoreClient;
import com.evernote.client.android.EvernoteSession;
import com.evernote.client.android.EvernoteSession.EvernoteService;
import com.evernote.client.android.InvalidAuthenticationException;
import com.evernote.thrift.transport.TTransportException;

public class ENAdapter {
  public static final String EVERNOTE_CONSUMER_KEY = "mlaux95";
  public static final String EVERNOTE_CONSUMER_SECRET = "fe5b46f1b076ff4b";
  public static final EvernoteService EVERNOTE_HOST = EvernoteService.PRODUCTION;
  public static EvernoteSession session;

  public static void initSession(Context ctx) {
    session = EvernoteSession.getInstance(ctx, EVERNOTE_CONSUMER_KEY, EVERNOTE_CONSUMER_SECRET, EVERNOTE_HOST, false);
  }

  public static boolean isSandbox() {
    return EVERNOTE_HOST == EvernoteService.SANDBOX;
  }

  public static void signIn(Context ctx) {
    if(!session.isLoggedIn())
      session.authenticate(ctx);
  }

  public static boolean signedIn() {
    return session.isLoggedIn();
  }

  public static void signOut(Context ctx) {
    if(session.isLoggedIn()) {
      try {
        session.logOut(ctx);
      } catch (InvalidAuthenticationException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }

  public static AsyncUserStoreClient getUserStoreClient() {
    try {
      return session.getClientFactory().createUserStoreClient();
    } catch (IllegalStateException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (TTransportException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return null;
  }

  public static AsyncNoteStoreClient getNoteStoreClient() {
    try {
      return session.getClientFactory().createNoteStoreClient();
    } catch (TTransportException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return null;
  }
}
