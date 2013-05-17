package org.kathryn.wordsearch;

import android.app.Activity;
import android.os.Bundle;

public class WordSearchActivity extends Activity {
  private WordSearchView view;

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    //setContentView(R.layout.main);

    view = new WordSearchView(this);

    setContentView(view);

    view.requestFocus();
  }
}
