package org.kathryn.wordsearch;

public class Word {
  public Word(String word) {
    word_  = word;
    start_ = new Pos();
    end_   = new Pos();
    found_ = false;
  }
  
  public Word(String word, Pos start, Pos end) {
    word_  = word;
    start_ = start;
    end_   = end;
    found_ = false;
  }

  public int length() { return word_.length(); }

  public String getWord() { return word_; }

  public char getChar(int i) { return word_.charAt(i); }

  public Pos getStart() { return start_; }
  public Pos getEnd  () { return end_  ; }

  public void setStart(Pos pos) { start_ = pos; }
  public void setEnd  (Pos pos) { end_   = pos; }

  public boolean isFound() { return found_; }

  public void setFound(boolean found) { found_ = found; }

  private String   word_;
  private Pos      start_;
  private Pos      end_;
  private boolean found_;
};
