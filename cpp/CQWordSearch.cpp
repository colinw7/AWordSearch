#include <CQWordSearch.h>
//#include <CQApp.h>
#include <QApplication>

#include <QKeyEvent>
#include <QMouseEvent>
#include <QPainter>
#include <QPainterPath>
#include <QSvgGenerator>

#include <iostream>
#include <set>
#include <cmath>

namespace {
  int RandIn(int min_val, int max_val) {
    int number = (rand() % (max_val - min_val + 1)) + min_val;

    return std::min(std::max(number, min_val), max_val);
  }
};

int
main(int argc, char **argv)
{
  srand(time(nullptr));

//CQApp app(argc, argv);
  QApplication app(argc, argv);

  int     n = 0;
  QString filename;

  for (int i = 1; i < argc; ++i) {
    auto arg = std::string(argv[i]);

    if      (arg == "-n") {
      ++i;

      if (i < argc)
        n = atoi(argv[i]);
    }
    else if (arg == "-f") {
      ++i;

      if (i < argc)
        filename = argv[i];
    }
    else {
      std::cerr << "Invalid arg '" << arg << "'\n";
    }
  }

  if (n <= 1) {
    if (filename != "")
      n = 10;
    else
      n = 17;
  }

  //---

  auto *window = new CQWordSearch(n);

  if (filename != "") {
    window->loadFile(filename);
  }
  else {
    window->addWord("CONVERSATIONALIST");
    window->addWord("ENTHUSIASTIC");
    window->addWord("ACCOMPLISH");
    window->addWord("BOMBARDED");
    window->addWord("GUARANTEE");
    window->addWord("PATIENTLY");
    window->addWord("DISGUISE");
    window->addWord("OBSTACLE");
    window->addWord("MURMURED");
    window->addWord("SCOLDED");
    window->addWord("BLARING");
    window->addWord("CLUMSY");
    window->addWord("DROWSY");
  }

  window->generate();

  window->show();

  app.exec();
}

//----------

CQWordSearch::
CQWordSearch(int s) :
 QWidget(), grid_(s)
{
  setFocusPolicy(Qt::StrongFocus);
}

void
CQWordSearch::
loadFile(const QString &filename)
{
  auto *fp = fopen(filename.toLatin1().constData(), "r");

  QString line;

  auto lines = QStringList();

  while (! feof(fp)) {
    auto c = fgetc(fp);

    if (c == '\n') {
      lines.push_back(line);

      line = "";
    }
    else
      line += c;
  }

  fclose(fp);

  for (auto &line : lines)
    addWord(line);
}

void
CQWordSearch::
addWord(const QString &word)
{
  if (word.size() > grid_.getSize()) {
    std::cerr << "Word too long '" << word.toStdString() << "'\n";
    return;
  }

  grid_.addWord(word);
}

void
CQWordSearch::
generate()
{
  (void) grid_.generate();
}

void
CQWordSearch::
checkMatch()
{
  grid_.checkMatch(start_, end_);
}

void
CQWordSearch::
paintEvent(QPaintEvent *)
{
  QPainter painter(this);

  draw(&painter);
}

void
CQWordSearch::
draw(QPainter *painter)
{
  // get max word length
  uint wordWidth = 0;

  uint num_words = grid_.getNumWords();

  for (uint i = 0; i < num_words; ++i) {
    const auto &word = grid_.getWord(i);

    wordWidth = std::max(word.size(), wordWidth);
  }

  //---

  // calc grid cell size
  int gridSize = grid_.getSize();

  auto sx = (double(width ()) - 2.0*b_)/(double(gridSize) + 0.3*double(wordWidth));
  auto sy = (double(height()) - 2.0*b_)/double(gridSize);

  auto s = std::min(sx, sy);

  s_ = gridSize*s;

  dx_ = s;
  dy_ = s;

  //---

  // set font for grid cell
  double font_size = dx_/2;

  QFont cellFont("helvetica", font_size);

  QFontMetricsF cellFm(cellFont);

  double cellFontHeight = cellFm.height();

  // set font for word list
  QFont listFont("helvetica", font_size*0.7);

  QFontMetricsF listFm(listFont);

  double listFontHeight = listFm.height();

  //---

  painter->setFont(cellFont);

  //---

  // draw grid
  for (int y = 0; y < gridSize; ++y) {
    for (int x = 0; x < gridSize; ++x) {
      QPainterPath path;

      double x1 = b_ + x*dx_, y1 = b_ + y*dy_;
      double x2 = x1 + dx_  , y2 = y1 + dy_  ;

      path.moveTo(x1, y1);
      path.lineTo(x2, y1);
      path.lineTo(x2, y2);
      path.lineTo(x1, y2);

      path.closeSubpath();

      painter->fillPath(path, QBrush(QColor(200, 200, 200)));

      painter->strokePath(path, QPen(QColor(0, 0, 0)));
    }
  }

  //---

  // draw found words
  painter->setPen(QColor(230, 80, 80));

  std::set<int> xy_used;

  for (uint i = 0; i < num_words; ++i) {
    const WordGrid::Word &word = grid_.getWord(i);

    if (! word.isFound()) continue;

    uint num_chars = word.size();

    const WordGrid::Pos &start = word.getStart();
    const WordGrid::Pos &end   = word.getEnd  ();

    int dx = (num_chars > 0 ? (end.x - start.x)/(int(num_chars) - 1) : 0);
    int dy = (num_chars > 0 ? (end.y - start.y)/(int(num_chars) - 1) : 0);

    for (uint j = 0; j < num_chars; ++j) {
      int x = start.x + j*dx;
      int y = start.y + j*dy;

      char c = word.getChar(j);

      double x1 = b_ + x*dx_, y1 = b_ + y*dy_;
      double x2 = x1 +   dx_, y2 = y1 +   dy_;

      QString text; text += c;

      double fw = cellFm.horizontalAdvance(c);

      painter->drawText((x1 + x2)/2 - fw/2, (y1 + y2)/2 - cellFontHeight/2 + cellFm.ascent(), text);

      xy_used.insert(y*gridSize + x);
    }

    outlineWord(painter, start, end, QColor(0, 0, 0), QColor(0, 255, 0, 80));
  }

  //---

  // draw grid letters
  painter->setPen(QColor(0, 0, 0));

  for (int y = 0; y < gridSize; ++y) {
    for (int x = 0; x < gridSize; ++x) {
      if (xy_used.find(y*gridSize + x) != xy_used.end()) continue;

      double x1 = b_ + x*dx_, y1 = b_ + y*dy_;
      double x2 = x1 + dx_  , y2 = y1 + dy_  ;

      char c = grid_.getChar(x, y);

      QString text; text += c;

      double fw = cellFm.horizontalAdvance(c);

      painter->drawText((x1 + x2)/2 - fw/2, (y1 + y2)/2 - cellFontHeight/2 + cellFm.ascent(), text);
    }
  }

  //---

  // draw selection
  if (pressed_ && start_ != end_) {
    outlineWord(painter, start_, end_, QColor(0, 0, 0), QColor(255, 0, 0, 80));
  }

  //----

  // draw word list
  painter->setFont(listFont);

  double max_width = 0;

  for (uint i = 0; i < num_words; ++i) {
    const WordGrid::Word &word = grid_.getWord(i);

    double w = listFm.horizontalAdvance(word.getWord().c_str());

    max_width = std::max(w, max_width);
  }

  double xx = s_ + 2*b_;
  double yy = b_;

  for (uint i = 0; i < num_words; ++i) {
    const WordGrid::Word &word = grid_.getWord(i);

    double w = listFm.horizontalAdvance(word.getWord().c_str());

    double dx = max_width - w;

    bool found = word.isFound();

    double yy1 = yy + i*listFontHeight;

    painter->setPen(found ? QColor(200, 200, 200) : QColor(0, 0, 0));

    painter->drawText(xx + dx/2, yy1 + listFm.ascent(), word.getWord().c_str());
  }
}

void
CQWordSearch::
outlineWord(QPainter *painter, const WordGrid::Pos &start, const WordGrid::Pos &end,
            QColor stroke_color, QColor fill_color)
{
  QPainterPath path;

  double x11 = b_ + start.x*dx_, y11 = b_ + start.y*dy_;
  double x21 = x11 + dx_       , y21 = y11 + dy_       ;

  double x12 = b_ + end  .x*dx_, y12 = b_ + end  .y*dy_;
  double x22 = x12 + dx_       , y22 = y12 + dy_       ;

  int dx = end.x - start.x;
  int dy = end.y - start.y;

  double a = atan2(dy, dx);
  double d = 180.0*a/M_PI;

  path.arcMoveTo(x11, y11, x21 - x11 + 1, y21 - y11 + 1, 90 - d);

  //QPointF p1 = path.currentPosition();

  path.arcTo(x11, y11, x21 - x11 + 1, y21 - y11 + 1, 90 - d, 180.0);

  QPointF p2 = path.currentPosition();

  path.lineTo(p2.x() + dx, p2.y() + dy);

  path.arcTo(x12, y12, x22 - x12 + 1, y22 - y12 + 1, 270 - d, 180.0);

  QPointF p3 = path.currentPosition();

  path.lineTo(p3.x() - dx, p3.y() - dy);

  path.closeSubpath();

  painter->fillPath(path, QBrush(fill_color));

  painter->strokePath(path, QPen(stroke_color));
}

void
CQWordSearch::
mousePressEvent(QMouseEvent *e)
{
  pressed_ = true;

  start_ = mapPoint(e->pos());
  end_   = start_;

  update();
}

void
CQWordSearch::
mouseReleaseEvent(QMouseEvent *e)
{
  pressed_ = false;

  end_ = mapPoint(e->pos());

  checkMatch();

  update();
}

void
CQWordSearch::
mouseMoveEvent(QMouseEvent *e)
{
  if (! pressed_) return;

  end_ = mapPoint(e->pos());

  update();
}

void
CQWordSearch::
keyPressEvent(QKeyEvent *e)
{
  if      (e->key() == Qt::Key_P) {
    QSvgGenerator generator;

    generator.setFileName("word_search.svg");
    generator.setSize(QSize(1024, 1024));
    generator.setViewBox(QRect(0, 0, 192, 192));
    generator.setTitle("Crossword");

    QPainter painter;

    bool rc = painter.begin(&generator);

    if (rc) {
      painter.setFont(this->font());

      draw(&painter);

      painter.end();
    }
  }
  else if (e->key() == Qt::Key_G) {
    generate();

    update();
  }
}

WordGrid::Pos
CQWordSearch::
mapPoint(const QPoint &p) const
{
  int x1 = int((p.x() - b_)/dx_), x2 = x1 + 1;
  int y1 = int((p.y() - b_)/dy_), y2 = y1 + 1;

  double x3 = (abs(p.x() - (b_ + dx_*x1 + dx_/2)) < abs(p.x() - (b_ + dx_*x2 + dx_/2)) ? x1 : x2);
  double y3 = (abs(p.y() - (b_ + dy_*y1 + dy_/2)) < abs(p.y() - (b_ + dy_*y2 + dy_/2)) ? y1 : y2);

  int gridSize = grid_.getSize();

  if (x3 <  0       ) x3 = 0;
  if (x3 >= gridSize) x3 = gridSize - 1;
  if (y3 <  0       ) y3 = 0;
  if (y3 >= gridSize) y3 = gridSize - 1;

  return WordGrid::Pos(x3, y3);
}

QSize
CQWordSearch::
sizeHint() const
{
  return QSize(1400, 800);
}

//----------

bool
WordGrid::
generate()
{
  for (int y = 0; y < s_; ++y)
    for (int x = 0; x < s_; ++x)
      setChar(x, y, ' ');

  uint num_words = getNumWords();

  bool rc = true;

  for (uint i = 0; i < num_words; ++i) {
    words_[i].setFound(false);

    if (! generateWord(words_[i])) {
      //std::cerr << "Failed for " << words_[i].getWord() << std::endl;
      rc = false;
    }
  }

  for (int y = 0; y < s_; ++y) {
    for (int x = 0; x < s_; ++x) {
      char c = getChar(x, y);

      if (c == ' ') {
        int i = RandIn(0, 25);

        setChar(x, y, char('A' + i));
      }
    }
  }

  return rc;
}

bool
WordGrid::
generateWord(Word &word)
{
  std::set<int> tried;

  bool rc = false;

  for (uint i = 0; i < 12; ++i) {
    int dir = RandIn(1, 12);

    while (tried.find(dir) != tried.end())
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

    tried.insert(dir);
  }

  if (! rc)
    return false;

  return true;
}

bool
WordGrid::
generateWordN(Word &word)
{
  std::set<int> tried;

  uint len = word.size();

  int xmin = 0      , xmax = s_ - 1; // Any
  int ymin = len - 1, ymax = s_ - 1; // N

  int num_tries = (xmax - xmin + 1)*(ymax - ymin + 1);

  for (int i = 0; i < num_tries; ++i) {
    int x = RandIn(xmin, xmax);
    int y = RandIn(ymin, ymax);

    int xy = y*s_ + x;

    while (tried.find(xy) != tried.end()) {
      x = RandIn(xmin, xmax);
      y = RandIn(ymin, ymax);

      xy = y*s_ + x;
    }

    if (wordFits(x, y, 0, -1, word)) {
      wordInsert(x, y, 0, -1, word);
      return true;
    }

    tried.insert(xy);
  }

  return false;
}

bool
WordGrid::
generateWordNE(Word &word)
{
  std::set<int> tried;

  uint len = word.size();

  int xmin = 0      , xmax = s_ - len; // E
  int ymin = len - 1, ymax = s_ - 1  ; // N

  int num_tries = (xmax - xmin + 1)*(ymax - ymin + 1);

  for (int i = 0; i < num_tries; ++i) {
    int x = RandIn(xmin, xmax);
    int y = RandIn(ymin, ymax);

    int xy = y*s_ + x;

    while (tried.find(xy) != tried.end()) {
      x = RandIn(xmin, xmax);
      y = RandIn(ymin, ymax);

      xy = y*s_ + x;
    }

    if (wordFits(x, y, 1, -1, word)) {
      wordInsert(x, y, 1, -1, word);
      return true;
    }

    tried.insert(xy);
  }

  return false;
}

bool
WordGrid::
generateWordE(Word &word)
{
  std::set<int> tried;

  uint len = word.size();

  int xmin = 0, xmax = s_ - len; // E
  int ymin = 0, ymax = s_ - 1  ; // Any

  int num_tries = (xmax - xmin + 1)*(ymax - ymin + 1);

  for (int i = 0; i < num_tries; ++i) {
    int x = RandIn(xmin, xmax);
    int y = RandIn(ymin, ymax);

    int xy = y*s_ + x;

    while (tried.find(xy) != tried.end()) {
      x = RandIn(xmin, xmax);
      y = RandIn(ymin, ymax);

      xy = y*s_ + x;
    }

    if (wordFits(x, y, 1, 0, word)) {
      wordInsert(x, y, 1, 0, word);
      return true;
    }

    tried.insert(xy);
  }

  return false;
}

bool
WordGrid::
generateWordSE(Word &word)
{
  std::set<int> tried;

  uint len = word.size();

  int xmin = 0, xmax = s_ - len; // E
  int ymin = 0, ymax = s_ - len; // S

  int num_tries = (xmax - xmin + 1)*(ymax - ymin + 1);

  for (int i = 0; i < num_tries; ++i) {
    int x = RandIn(xmin, xmax);
    int y = RandIn(ymin, ymax);

    int xy = y*s_ + x;

    while (tried.find(xy) != tried.end()) {
      x = RandIn(xmin, xmax);
      y = RandIn(ymin, ymax);

      xy = y*s_ + x;
    }

    if (wordFits(x, y, 1, 1, word)) {
      wordInsert(x, y, 1, 1, word);
      return true;
    }

    tried.insert(xy);
  }

  return false;
}

bool
WordGrid::
generateWordS(Word &word)
{
  std::set<int> tried;

  uint len = word.size();

  int xmin = 0, xmax = s_ - 1  ; // Any
  int ymin = 0, ymax = s_ - len; // S

  int num_tries = (xmax - xmin + 1)*(ymax - ymin + 1);

  for (int i = 0; i < num_tries; ++i) {
    int x = RandIn(xmin, xmax);
    int y = RandIn(ymin, ymax);

    int xy = y*s_ + x;

    while (tried.find(xy) != tried.end()) {
      x = RandIn(xmin, xmax);
      y = RandIn(ymin, ymax);

      xy = y*s_ + x;
    }

    if (wordFits(x, y, 0, 1, word)) {
      wordInsert(x, y, 0, 1, word);
      return true;
    }

    tried.insert(xy);
  }

  return false;
}

bool
WordGrid::
generateWordSW(Word &word)
{
  std::set<int> tried;

  uint len = word.size();

  int xmin = len - 1, xmax = s_ - 1;   // W
  int ymin = 0      , ymax = s_ - len; // S

  int num_tries = (xmax - xmin + 1)*(ymax - ymin + 1);

  for (int i = 0; i < num_tries; ++i) {
    int x = RandIn(xmin, xmax);
    int y = RandIn(ymin, ymax);

    int xy = y*s_ + x;

    while (tried.find(xy) != tried.end()) {
      x = RandIn(xmin, xmax);
      y = RandIn(ymin, ymax);

      xy = y*s_ + x;
    }

    if (wordFits(x, y, -1, 1, word)) {
      wordInsert(x, y, -1, 1, word);
      return true;
    }

    tried.insert(xy);
  }

  return false;
}

bool
WordGrid::
generateWordW(Word &word)
{
  std::set<int> tried;

  uint len = word.size();

  int xmin = len - 1, xmax = s_ - 1; // W
  int ymin = 0      , ymax = s_ - 1; // Any

  int num_tries = (xmax - xmin + 1)*(ymax - ymin + 1);

  for (int i = 0; i < num_tries; ++i) {
    int x = RandIn(xmin, xmax);
    int y = RandIn(ymin, ymax);

    int xy = y*s_ + x;

    while (tried.find(xy) != tried.end()) {
      x = RandIn(xmin, xmax);
      y = RandIn(ymin, ymax);

      xy = y*s_ + x;
    }

    if (wordFits(x, y, -1, 0, word)) {
      wordInsert(x, y, -1, 0, word);
      return true;
    }

    tried.insert(xy);
  }

  return false;
}

bool
WordGrid::
generateWordNW(Word &word)
{
  std::set<int> tried;

  uint len = word.size();

  int xmin = len - 1, xmax = s_ - 1; // W
  int ymin = len - 1, ymax = s_ - 1; // N

  int num_tries = (xmax - xmin + 1)*(ymax - ymin + 1);

  for (int i = 0; i < num_tries; ++i) {
    int x = RandIn(xmin, xmax);
    int y = RandIn(ymin, ymax);

    int xy = y*s_ + x;

    while (tried.find(xy) != tried.end()) {
      x = RandIn(xmin, xmax);
      y = RandIn(ymin, ymax);

      xy = y*s_ + x;
    }

    if (wordFits(x, y, -1, -1, word)) {
      wordInsert(x, y, -1, -1, word);
      return true;
    }

    tried.insert(xy);
  }

  return false;
}

bool
WordGrid::
wordFits(int x, int y, int dx, int dy, const Word &word)
{
  uint len = word.size();

  for (uint i = 0; i < len; ++i) {
    int x1 = x + i*dx;
    int y1 = y + i*dy;

    char c = word.getChar(i);

    char c1 = getChar(x1, y1);

    if (c1 != c && c1 != ' ')
      return false;
  }

  return true;
}

void
WordGrid::
wordInsert(int x, int y, int dx, int dy, Word &word)
{
  uint len = word.size();

  word.setStart(Pos(x, y));
  word.setEnd  (Pos(x + (len - 1)*dx, y + (len - 1)*dy));

  for (uint i = 0; i < len; ++i) {
    int x1 = x + i*dx;
    int y1 = y + i*dy;

    char c = word.getChar(i);

    setChar(x1, y1, c);
  }
}

void
WordGrid::
checkMatch(const Pos &start, const Pos &end)
{
  uint num_words = getNumWords();

  for (uint i = 0; i < num_words; ++i) {
    const Pos &start1 = words_[i].getStart();
    const Pos &end1   = words_[i].getEnd  ();

    if ((start == start1 && end == end1  ) ||
        (start == end1   && end == start1)) {
      words_[i].setFound(true);
      return;
    }
  }
}
