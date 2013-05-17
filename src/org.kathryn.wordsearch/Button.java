package org.kathryn.wordsearch;

import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Paint.FontMetrics;

class Button {
  private String         str_;
  private int            x_;
  private int            y_;
  private int            w_;
  private int            h_;
  private int            ib_, ob_;
  private Paint          bg_paint_;
  private Paint          fg_paint_;
  private Paint          hi_paint_;
  private Paint          lo_paint_;
  private RectF          irect_;
  private RectF          orect_;
  private Rect           trect_;
  private LinearGradient lgrad_;
  private boolean        pressed_;

  public Button(String str) {
    this(str, 0, 0);
  }
  
  public Button(String str, int x, int y) {
    str_ = str;
    x_   = x;
    y_   = y;
    ib_  = 4;
    ob_  = 4;

    pressed_ = false;
    
    bg_paint_ = new Paint();
    fg_paint_ = new Paint();
    hi_paint_ = new Paint();
    lo_paint_ = new Paint();

    bg_paint_.setColor(0xFFFFFFFF);
    bg_paint_.setStyle(Paint.Style.FILL);

    fg_paint_.setColor(0xFF000000);
    fg_paint_.setStyle(Paint.Style.STROKE);
    
    hi_paint_.setColor(0xFFFFFFFF);
    hi_paint_.setStyle(Paint.Style.FILL);
    
    lo_paint_.setColor(0xFF666666);
    lo_paint_.setStyle(Paint.Style.FILL);

    setFontSize(20.0f);
    
    update();
  }

  public void setFontSize(float s) {
  	fg_paint_.setTextSize(s);
  	
  	update();
  }
  
  public void move(int x, int y) {
    x_ = x;
    y_ = y;

    update();
  }

  void setPressed(boolean pressed) {
    pressed_ = pressed;
  }
  
  private void update() {
    trect_ = new Rect();

    fg_paint_.getTextBounds(str_, 0, str_.length(), trect_);
    
    Rect trect1 = new Rect();
    
    fg_paint_.getTextBounds("ABCxyz", 0, 6, trect1);

    trect_.bottom = trect1.bottom;
    trect_.top    = trect1.top;
    
    w_ = trect_.right  - trect_.left + 2*ib_ + 2*ob_;
    h_ = trect_.bottom - trect_.top  + 2*ib_ + 2*ob_;

    irect_ = new RectF(x_ - ib_      , y_ - h_ - ib_      , x_ + w_ + ib_      , y_ + ib_);   
    orect_ = new RectF(x_ - ib_ - ob_, y_ - h_ - ib_ - ob_, x_ + w_ + ib_ + ob_, y_ + ib_ + ob_);

    int colors[] = new int [2];

    colors[0] = 0xFF444444;
    colors[1] = 0xFFFFFFFF;

    lgrad_ = new LinearGradient(irect_.left, irect_.top, irect_.right, irect_.bottom,
                                colors, null, Shader.TileMode.CLAMP);

    bg_paint_.setShader(lgrad_);
  }

  public void draw(Canvas canvas) {  	
  	// top edge
    Path path = new Path();
    
    path.moveTo(orect_.left       , orect_.top         );
    path.lineTo(orect_.right      , orect_.top         );
    path.lineTo(orect_.right - ob_, orect_.top    + ob_);
    path.lineTo(orect_.left  + ob_, orect_.bottom - ob_);
    path.lineTo(orect_.left       , orect_.bottom      );
    path.close();
    
    if (! pressed_)
      canvas.drawPath(path, hi_paint_);
    else
      canvas.drawPath(path, lo_paint_);
    
    path = new Path();
    
    path.moveTo(orect_.right      , orect_.bottom      );
    path.lineTo(orect_.right      , orect_.top         );
    path.lineTo(orect_.right - ob_, orect_.top    + ob_);
    path.lineTo(orect_.left  + ob_, orect_.bottom - ob_);
    path.lineTo(orect_.left       , orect_.bottom      );
    path.close();
    
    if (! pressed_)
      canvas.drawPath(path, lo_paint_);
    else
      canvas.drawPath(path, hi_paint_);
    
    canvas.drawRect(irect_, bg_paint_);

    FontMetrics fm = fg_paint_.getFontMetrics();
    
    int tx = (int) ((irect_.right - irect_.left - fg_paint_.measureText(str_))/2.0);
    int ty = (int) ((irect_.top + irect_.bottom)/2.0f - (fm.ascent + fm.descent)/2.0f + fm.descent/2.0f);
    
    canvas.drawText(str_, irect_.left + tx, ty, fg_paint_);
  }

  public boolean contains(float x, float y) {
    return orect_.contains(x, y);
  }
  
  public int getWidth() {
    return (int) (orect_.right - orect_.left);
  }
}
