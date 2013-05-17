package org.kathryn.wordsearch;

import java.util.HashSet;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

public class WordSearchView extends View {
  //private WordSearchActivity word_search;

  private int bg_color     = 0xFFAAAAAA;
  private int black_color  = 0xFF000000;
  private int red_color    = 0xFFFF0000;
  private int gray_color   = 0xFF888888;
  private int select_color = 0x44FF0000;
  private int found_color  = 0x4400FF00;

  private int width_  = 400;
  private int height_ = 400;

  private WordGrid grid_;
  private int      s_;
  private int      b_;
  private int      dx_, dy_;
  private boolean  pressed_;
  private Pos      start_, end_;

  private Button   restartButton;

  public WordSearchView(Context context) {
    super(context);

    //this.word_search = (WordSearchActivity) context;

    setFocusable(true);
    setFocusableInTouchMode(true);

    //srand(time(NULL));

    grid_ = new WordGrid(17);

    pressed_ = false;

    addWord(new String("XYLOPHONE"));
    addWord(new String("TYPHOON"));
    addWord(new String("STRATOSPHERE"));
    addWord(new String("SOPHOMORE"));
    addWord(new String("SAPPHIRE"));
    addWord(new String("PHOENIX"));
    addWord(new String("PHOBIA"));
    addWord(new String("PHENOMENAL"));
    addWord(new String("PHEASANT"));
    addWord(new String("PHARMACY"));
    addWord(new String("PHANTOM"));
    addWord(new String("HYPHEN"));
    addWord(new String("EMPHASIS"));
    addWord(new String("DECIPHER"));
    addWord(new String("CATASTROPHE"));
    addWord(new String("ATMOSPHERE"));
    addWord(new String("APOSTROPHE"));

    restartButton = new Button("Restart");
    
    generate();
  }

  void addWord(String word) {
    grid_.addWord(word);
  }

  void generate() {
    grid_.generate();
  }

  void checkMatch() {
    grid_.checkMatch(start_, end_);
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    width_  = w;
    height_ = h;

    s_ = Math.min(width_, height_);
    
    restartButton.move(s_ + 40, height_ - 20);
    
    restartButton.setFontSize(s_/25.0f);
    
    super.onSizeChanged(w, h, oldw, oldh);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    // Draw the background
    Paint bg_paint = new Paint();
    bg_paint.setColor(bg_color);
    bg_paint.setStyle(Paint.Style.FILL);

    Paint grid_paint = new Paint();
    grid_paint.setColor(gray_color);
    grid_paint.setStyle(Paint.Style.FILL);

    Paint select_paint = new Paint();
    select_paint.setColor(select_color);
    select_paint.setStyle(Paint.Style.FILL);
    
    Paint found_paint = new Paint();
    found_paint.setColor(found_color);
    found_paint.setStyle(Paint.Style.FILL);

    Paint red_paint = new Paint();
    red_paint.setColor(red_color);
    red_paint.setStyle(Paint.Style.STROKE);
    red_paint.setTextSize(35.0f);

    Paint black_paint = new Paint();
    black_paint.setColor(black_color);
    black_paint.setStyle(Paint.Style.STROKE);
    black_paint.setTextSize(35.0f);

    Paint gray_paint = new Paint();
    gray_paint.setColor(gray_color);
    gray_paint.setStyle(Paint.Style.STROKE);
    gray_paint.setTextSize(35.0f);

    canvas.drawRect(0, 0, width_, height_, bg_paint);

    s_ = Math.min(width_, height_);
    b_ = 8;

    //int font_size = dx_/2;

    //QFont font("helvetica", font_size);

    //setFont(font);

    //QFontMetrics fm(font);

    //int fh = fm.height();

    int s = grid_.getSize();

    dx_ = (s_ - 2*b_)/s;
    dy_ = (s_ - 2*b_)/s;
    
    red_paint  .setTextSize(dx_*0.65f);
    black_paint.setTextSize(dx_*0.65f);
    gray_paint .setTextSize(dx_*0.65f);

    FontMetrics fm = black_paint.getFontMetrics();

    float fh = fm.ascent + fm.descent;
    float fa = fm.ascent;

    // draw grid
    for (int y = 0; y < s; ++y) {
      for (int x = 0; x < s; ++x) {
        Path path = new Path();

        int x1 = b_ + x*dx_, y1 = b_ + y*dy_;
        int x2 = x1 + dx_  , y2 = y1 + dy_  ;

        path.moveTo(x1, y1);
        path.lineTo(x2, y1);
        path.lineTo(x2, y2);
        path.lineTo(x1, y2);

        path.close();

        canvas.drawPath(path, grid_paint);

        canvas.drawPath(path, black_paint);
      }
    }

    // draw found words
    HashSet<Integer> xy_used = new HashSet<Integer>();

    int num_words = grid_.getNumWords();

    for (int i = 0; i < num_words; ++i) {
      Word word = grid_.getWord(i);

      if (! word.isFound()) continue;

      int num_chars = word.length();

      Pos start = word.getStart();
      Pos end   = word.getEnd  ();

      int dx = (num_chars > 0 ? (end.x - start.x)/(num_chars - 1) : 0);
      int dy = (num_chars > 0 ? (end.y - start.y)/(num_chars - 1) : 0);

      for (int j = 0; j < num_chars; ++j) {
        int x = start.x + j*dx;
        int y = start.y + j*dy;

        char c = word.getChar(j);

        int x1 = b_ + x*dx_, y1 = b_ + y*dy_;
        int x2 = x1 +   dx_, y2 = y1 +   dy_;

        String text = new String(); text += c;

        float fw = black_paint.measureText(text);

        canvas.drawText(text, (x1 + x2)/2 - fw/2, (y1 + y2)/2 - fh/2, red_paint);
  
        xy_used.add(y*s + x);
      }
      
      outlineWord(canvas, start, end, black_paint, found_paint);
    }

    // draw non-found words
    for (int y = 0; y < s; ++y) {
      for (int x = 0; x < s; ++x) {
        if (xy_used.contains(y*s + x)) continue;

        int x1 = b_ + x*dx_, y1 = b_ + y*dy_;
        int x2 = x1 + dx_  , y2 = y1 + dy_  ;

        char c = grid_.getChar(x, y);

        String text = new String(); text += c;

        float fw = black_paint.measureText(text);

        canvas.drawText(text, (x1 + x2)/2 - fw/2, (y1 + y2)/2 - fh/2, black_paint);
      }
    }

    // draw selection
    if (pressed_ && start_ != end_) {
      outlineWord(canvas, start_, end_, black_paint, select_paint);
    }

    //----

    // draw words
    int dy1 = (s_ - 2*b_ - dy_)/num_words;
    
    black_paint.setTextSize(dy1*0.65f);
    gray_paint .setTextSize(dy1*0.65f);

    FontMetrics fm1 = black_paint.getFontMetrics();

    float fa1 = fm1.ascent;

    int xx = s_ + b_;
    int yy = b_;

    for (int i = 0; i < num_words; ++i) {
      Word word = grid_.getWord(i);

      boolean found = word.isFound();

      int yy1 = yy + i*dy1;

      if (! found)
        canvas.drawText(word.getWord(), xx, yy1 - fa1, black_paint);
      else
        canvas.drawText(word.getWord(), xx, yy1 - fa1, gray_paint);
    }
    
    //----
    
    restartButton.draw(canvas);
  }

  void outlineWord(Canvas canvas, Pos start, Pos end, Paint stroke_paint, Paint fill_paint) {
    Path path = new Path();

    int x11 = b_ + start.x*dx_, y11 = b_ + start.y*dy_;
    int x21 = x11 + dx_       , y21 = y11 + dy_       ;

    int x12 = b_ + end  .x*dx_, y12 = b_ + end  .y*dy_;
    int x22 = x12 + dx_       , y22 = y12 + dy_       ;

    int dx = end.x - start.x;
    int dy = end.y - start.y;

    double a = Math.atan2(dy, dx);
    double d = 180.0*a/Math.PI;

    //path.addArc(new RectF(x11, y11, x21 - x11 + 1, y21 - y11 + 1), 90 - d);

    //QPointF p1 = path.currentPosition();

    int xm1 = (x11 + x21)/2;
    int ym1 = (y11 + y21)/2;
    
    int xm2 = (x12 + x22)/2;
    int ym2 = (y12 + y22)/2;
    
    float xa11 = (float) (xm1 - (dx_/2)*Math.sin(a));
    float ya11 = (float) (ym1 + (dx_/2)*Math.cos(a));
    float xa21 = (float) (xm1 + (dx_/2)*Math.sin(a));
    float ya21 = (float) (ym1 - (dx_/2)*Math.cos(a));
    
    //float xa12 = (float) (xm2 - (dx_/2)*Math.sin(a));
    //float ya12 = (float) (ym2 + (dx_/2)*Math.cos(a));
    float xa22 = (float) (xm2 + (dx_/2)*Math.sin(a));
    float ya22 = (float) (ym2 - (dx_/2)*Math.cos(a));
    
    path.moveTo(xa21, ya21);
    
    path.addArc(new RectF(x11, y11, x21, y21), (float) (d + 90), 180.0f);

    //QPointF p2 = path.currentPosition();

    //path.moveTo(xa11, ya11);
    //path.lineTo(xa21, ya21);
    path.lineTo(xa22, ya22);

    path.addArc(new RectF(x12, y12, x22, y22), (float) (d + 270), 180.0f);

    //QPointF p3 = path.currentPosition();

    //path.lineTo(xa22, ya22);
    //path.lineTo(xa12, ya12);
    path.lineTo(xa11, ya11);

    //path.close();

    canvas.drawPath(path, fill_paint);

    canvas.drawPath(path, stroke_paint);
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
	int mouseX = (int) event.getX();
    int mouseY = (int) event.getY();
      
    if (event.getAction() == MotionEvent.ACTION_DOWN) {
      if (restartButton.contains(mouseX, mouseY)) {
        restartButton.setPressed(true);
        invalidate();
        return true;
      }
    	
      pressed_ = true;

      start_ = mapPoint(mouseX, mouseY);
      end_   = start_;

      invalidate();
    }
    else if (event.getAction() == MotionEvent.ACTION_MOVE) {
      if (pressed_) {
        end_ = mapPoint(mouseX, mouseY);

        invalidate();
      }
    }
    else if (event.getAction() == MotionEvent.ACTION_UP) {
      restartButton.setPressed(false);

      if (restartButton.contains(mouseX, mouseY)) {
    	generate();
        invalidate();
        return true;
      }
      
      pressed_ = false;

      end_ = mapPoint(mouseX, mouseY);

      checkMatch();

      invalidate();
    }
    else {
      return super.onTouchEvent(event);
    }

    return true;
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    generate();
    
    invalidate();

    return true;
  }

  private Pos mapPoint(int x, int y) {
    int x1 = (x - b_ - dx_/2)/dx_, x2 = x1 + 1;
    int y1 = (y - b_ - dy_/2)/dy_, y2 = y1 + 1;

    int x3 = (Math.abs(x - (b_ + dx_*x1 + dx_/2)) < Math.abs(x - (b_ + dx_*x2 + dx_/2)) ? x1 : x2);
    int y3 = (Math.abs(y - (b_ + dy_*y1 + dy_/2)) < Math.abs(y - (b_ + dy_*y2 + dy_/2)) ? y1 : y2);

    int s = grid_.getSize();
    
    if (x3 < 0) x3 = 0; if (x3 >= s) x3 = s - 1;
    if (y3 < 0) y3 = 0; if (y3 >= s) y3 = s - 1;
    
    return new Pos(x3, y3);
  }
}
