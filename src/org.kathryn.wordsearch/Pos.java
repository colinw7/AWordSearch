package org.kathryn.wordsearch;

public class Pos {
  public int x, y;

  Pos() {
	x = 0; y = 0;
  }
  
  Pos(int x1, int y1) {
    x = x1; y = y1;
  }

  boolean equal(Pos rhs) {
    return (x == rhs.x && y == rhs.y);
  }

  boolean not_equal(Pos rhs) {
    return (x != rhs.x || y != rhs.y);
  }
}
