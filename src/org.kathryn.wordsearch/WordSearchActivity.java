package org.kathryn.wordsearch;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class WordSearchActivity extends Activity {
  /** Called when the activity is first created. */
  private WordSearchView view;
  
  @Override
  public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      //setContentView(R.layout.main);
      
      view = new WordSearchView(this);
      
      setContentView(view);
      
      view.requestFocus();
  }
}
