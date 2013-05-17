package org.kathryn.wordsearch;

import java.util.LinkedList;
import java.util.HashSet;
import java.util.Random;

public class WordGrid {
  public WordGrid(int s) {
    s_ = s;

    int s2 = s_*s_;

    chars_ = new char [s2];

    for (int i = 0; i < s2; ++i)
      chars_[i] = ' ';
    
    words_ = new LinkedList<Word>();

    random_ = new Random(/* seed */);
  }

  public int getSize() { return s_; }

  public void addWord(String word) {
    words_.add(new Word(word));
  }

  public int getNumWords() { return words_.size(); }

  public Word getWord(int i) { return words_.get(i); }

  public boolean generate() {
    for (int y = 0; y < s_; ++y)
      for (int x = 0; x < s_; ++x)
        setChar(x, y, ' ');

    int num_words = getNumWords();

    boolean rc = true;
    
    for (int i = 0; i < num_words; ++i) {
      words_.get(i).setFound(false);
      
      if (! generateWord(words_.get(i))) {
        //std::cerr << "Failed for " << words_.get(i).getWord() << std::endl;
        rc = false;
      }
    }

    for (int y = 0; y < s_; ++y) {
      for (int x = 0; x < s_; ++x) {
        char c = getChar(x, y);

        if (c == ' ') {
          int i = RandIn(0, 25);

          setChar(x, y, (char) ('A' + i));
        }
      }
    }

    return rc;
  }

  private boolean generateWord(Word word) {
    HashSet<Integer> tried = new HashSet<Integer>();

    boolean rc = false;

    for (int i = 0; i < 12; ++i) {
      int dir = RandIn(1, 12);

      while (tried.contains(dir))
        dir = RandIn(1, 12);

      switch (dir) {
        case  1: case  2: rc = generateWordN (word); break;
        case  3:          rc = generateWordNE(word); break;
        case  4: case  5: rc = generateWordE (word); break;
        case  6:          rc = generateWordSE(word); break;
        case  7: case  8: rc = generateWordS (word); break;
        case  9:          rc = generateWordSW(word); break;
        case 10: case 11: rc = generateWordW (word); break;
        case 12:          rc = generateWordNW(word); break;
      }

      if (rc) break;

      tried.add(dir);
    }

    if (! rc)
      return false;

    return true;
  }

  private boolean generateWordN(Word word) {
    HashSet<Integer> tried = new HashSet<Integer>();

    int len = word.length();

    int xmin = 0      , xmax = s_ - 1; // Any
    int ymin = len - 1, ymax = s_ - 1; // N

    int num_tries = (xmax - xmin + 1)*(ymax - ymin + 1);

    for (int i = 0; i < num_tries; ++i) {
      int x = RandIn(xmin, xmax);
      int y = RandIn(ymin, ymax);

      int xy = y*s_ + x;

      while (tried.contains(xy)) {
        x = RandIn(xmin, xmax);
        y = RandIn(ymin, ymax);

        xy = y*s_ + x;
      }

      if (wordFits(x, y, 0, -1, word)) {
        wordInsert(x, y, 0, -1, word);
        return true;
      }

      tried.add(xy);
    }

    return false;
  }

  private boolean generateWordNE(Word word) {
    HashSet<Integer> tried = new HashSet<Integer>();

    int len = word.length();

    int xmin = 0      , xmax = s_ - len; // E
    int ymin = len - 1, ymax = s_ - 1  ; // N

    int num_tries = (xmax - xmin + 1)*(ymax - ymin + 1);

    for (int i = 0; i < num_tries; ++i) {
      int x = RandIn(xmin, xmax);
      int y = RandIn(ymin, ymax);

      int xy = y*s_ + x;

      while (tried.contains(xy)) {
        x = RandIn(xmin, xmax);
        y = RandIn(ymin, ymax);

        xy = y*s_ + x;
      }

      if (wordFits(x, y, 1, -1, word)) {
        wordInsert(x, y, 1, -1, word);
        return true;
      }

      tried.add(xy);
    }

    return false;
  }

  private boolean generateWordE(Word word) {
    HashSet<Integer> tried = new HashSet<Integer>();

    int len = word.length();

    int xmin = 0, xmax = s_ - len; // E
    int ymin = 0, ymax = s_ - 1  ; // Any

    int num_tries = (xmax - xmin + 1)*(ymax - ymin + 1);

    for (int i = 0; i < num_tries; ++i) {
      int x = RandIn(xmin, xmax);
      int y = RandIn(ymin, ymax);

      int xy = y*s_ + x;

      while (tried.contains(xy)) {
        x = RandIn(xmin, xmax);
        y = RandIn(ymin, ymax);

        xy = y*s_ + x;
      }

      if (wordFits(x, y, 1, 0, word)) {
        wordInsert(x, y, 1, 0, word);
        return true;
      }

      tried.add(xy);
    }

    return false;
  }

  private boolean generateWordSE(Word word) {
    HashSet<Integer> tried = new HashSet<Integer>();

    int len = word.length();

    int xmin = 0, xmax = s_ - len; // E
    int ymin = 0, ymax = s_ - len; // S

    int num_tries = (xmax - xmin + 1)*(ymax - ymin + 1);

    for (int i = 0; i < num_tries; ++i) {
      int x = RandIn(xmin, xmax);
      int y = RandIn(ymin, ymax);

      int xy = y*s_ + x;

      while (tried.contains(xy)) {
        x = RandIn(xmin, xmax);
        y = RandIn(ymin, ymax);

        xy = y*s_ + x;
      }

      if (wordFits(x, y, 1, 1, word)) {
        wordInsert(x, y, 1, 1, word);
        return true;
      }

      tried.add(xy);
    }

    return false;
  }

  private boolean generateWordS(Word word) {
    HashSet<Integer> tried = new HashSet<Integer>();

    int len = word.length();

    int xmin = 0, xmax = s_ - 1  ; // Any
    int ymin = 0, ymax = s_ - len; // S

    int num_tries = (xmax - xmin + 1)*(ymax - ymin + 1);

    for (int i = 0; i < num_tries; ++i) {
      int x = RandIn(xmin, xmax);
      int y = RandIn(ymin, ymax);

      int xy = y*s_ + x;

      while (tried.contains(xy)) {
        x = RandIn(xmin, xmax);
        y = RandIn(ymin, ymax);

        xy = y*s_ + x;
      }

      if (wordFits(x, y, 0, 1, word)) {
        wordInsert(x, y, 0, 1, word);
        return true;
      }

      tried.add(xy);
    }

    return false;
  }

  private boolean generateWordSW(Word word) {
    HashSet<Integer> tried = new HashSet<Integer>();

    int len = word.length();

    int xmin = len - 1, xmax = s_ - 1;   // W
    int ymin = 0      , ymax = s_ - len; // S

    int num_tries = (xmax - xmin + 1)*(ymax - ymin + 1);

    for (int i = 0; i < num_tries; ++i) {
      int x = RandIn(xmin, xmax);
      int y = RandIn(ymin, ymax);

      int xy = y*s_ + x;

      while (tried.contains(xy)) {
        x = RandIn(xmin, xmax);
        y = RandIn(ymin, ymax);

        xy = y*s_ + x;
      }

      if (wordFits(x, y, -1, 1, word)) {
        wordInsert(x, y, -1, 1, word);
        return true;
      }

      tried.add(xy);
    }

    return false;
  }

  private boolean generateWordW(Word word) {
    HashSet<Integer> tried = new HashSet<Integer>();

    int len = word.length();

    int xmin = len - 1, xmax = s_ - 1; // W
    int ymin = 0      , ymax = s_ - 1; // Any

    int num_tries = (xmax - xmin + 1)*(ymax - ymin + 1);

    for (int i = 0; i < num_tries; ++i) {
      int x = RandIn(xmin, xmax);
      int y = RandIn(ymin, ymax);

      int xy = y*s_ + x;

      while (tried.contains(xy)) {
        x = RandIn(xmin, xmax);
        y = RandIn(ymin, ymax);

        xy = y*s_ + x;
      }

      if (wordFits(x, y, -1, 0, word)) {
        wordInsert(x, y, -1, 0, word);
        return true;
      }

      tried.add(xy);
    }

    return false;
  }

  private boolean generateWordNW(Word word) {
    HashSet<Integer> tried = new HashSet<Integer>();

    int len = word.length();

    int xmin = len - 1, xmax = s_ - 1; // W
    int ymin = len - 1, ymax = s_ - 1; // N

    int num_tries = (xmax - xmin + 1)*(ymax - ymin + 1);

    for (int i = 0; i < num_tries; ++i) {
      int x = RandIn(xmin, xmax);
      int y = RandIn(ymin, ymax);

      int xy = y*s_ + x;

      while (tried.contains(xy)) {
        x = RandIn(xmin, xmax);
        y = RandIn(ymin, ymax);

        xy = y*s_ + x;
      }

      if (wordFits(x, y, -1, -1, word)) {
        wordInsert(x, y, -1, -1, word);
        return true;
      }

      tried.add(xy);
    }

    return false;
  }

  private boolean wordFits(int x, int y, int dx, int dy, Word word) {
    int len = word.length();

    for (int i = 0; i < len; ++i) {
      int x1 = x + i*dx;
      int y1 = y + i*dy;

      char c = word.getChar(i);

      char c1 = getChar(x1, y1);

      if (c1 != c && c1 != ' ')
        return false;
    }

    return true;
  }

  private void wordInsert(int x, int y, int dx, int dy, Word word) {
    int len = word.length();

    word.setStart(new Pos(x, y));
    word.setEnd  (new Pos(x + (len - 1)*dx, y + (len - 1)*dy));

    for (int i = 0; i < len; ++i) {
      int x1 = x + i*dx;
      int y1 = y + i*dy;

      char c = word.getChar(i);

      setChar(x1, y1, c);
    }
  }

  public void checkMatch(Pos start, Pos end) {
    int num_words = getNumWords();

    for (int i = 0; i < num_words; ++i) {
      Pos start1 = words_.get(i).getStart();
      Pos end1   = words_.get(i).getEnd  ();

      if ((start.equal(start1) && end.equal(end1)  ) ||
          (start.equal(end1)   && end.equal(start1))) {
        words_.get(i).setFound(true);
        return;
      }
    }
  }

  public char getChar(int x, int y) {
    if (x < 0 || x >= s_ || y < 0 && y >= s_) return '\0';

    return chars_[y*s_ + x];
  }

  public void setChar(int x, int y, char c) {
    if (x < 0 || x >= s_ || y < 0 && y >= s_) return;

    chars_[y*s_ + x] = c;
  }

  public int RandIn(int min_val, int max_val) {
    int number = random_.nextInt(max_val - min_val + 1) + min_val;

    return Math.min(Math.max(number, min_val), max_val);
  }

  private int              s_;
  private char             chars_[];
  private LinkedList<Word> words_;
  private Random           random_;
}
