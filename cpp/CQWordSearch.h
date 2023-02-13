#include <QWidget>

#include <cassert>

class WordGrid {
 public:
  struct Pos {
    Pos(int x1=0, int y1=0) :
     x(x1), y(y1) {
    }

    int x, y;

    friend bool operator==(const Pos &lhs, const Pos &rhs) {
      return (lhs.x == rhs.x && lhs.y == rhs.y);
    }

    friend bool operator!=(const Pos &lhs, const Pos &rhs) {
      return (lhs.x != rhs.x || lhs.y != rhs.y);
    }
  };

  class Word {
   public:
    Word(const std::string &word="", const Pos &start=Pos(), const Pos &end=Pos()) :
     word_(word), start_(start), end_(end) {
    }

    uint size() const { return uint(word_.size()); }

    const std::string &getWord() const { return word_; }

    char getChar(uint i) const { return word_[i]; }

    const Pos &getStart() const { return start_; }
    const Pos &getEnd  () const { return end_  ; }

    void setStart(const Pos &pos) { start_ = pos; }
    void setEnd  (const Pos &pos) { end_   = pos; }

    bool isFound() const { return found_; }

    void setFound(bool found) { found_ = found; }

   private:
    std::string word_;
    Pos         start_;
    Pos         end_;
    bool        found_ { false };
  };

 public:
  WordGrid(int s) :
   s_(s) {
    int s2 = s_*s_;

    chars_.resize(s2);

    for (int i = 0; i < s2; ++i)
      chars_[i] = ' ';
  }

  int getSize() const { return s_; }

  void addWord(const QString &word) {
    words_.push_back(Word(word.toStdString()));
  }

  uint getNumWords() const { return words_.size(); }

  const Word &getWord(uint i) const { return words_[i]; }

  bool generate();

  void checkMatch(const Pos &start, const Pos &end);

 private:
  bool generateWord(Word &word);

  bool generateWordN (Word &word);
  bool generateWordNE(Word &word);
  bool generateWordE (Word &word);
  bool generateWordSE(Word &word);
  bool generateWordS (Word &word);
  bool generateWordSW(Word &word);
  bool generateWordW (Word &word);
  bool generateWordNW(Word &word);

  bool wordFits(int x, int y, int dx, int dy, const Word &word);

  void wordInsert(int x, int y, int dx, int dy, Word &word);

 public:
  char getChar(int x, int y) const {
    assert(x >= 0 && x < s_ && y >= 0 && y < s_);

    return chars_[y*s_ + x];
  }

  void setChar(int x, int y, char c) {
    assert(x >= 0 && x < s_ && y >= 0 && y < s_);

    chars_[y*s_ + x] = c;
  }

 private:
  typedef std::vector<char> CharArray;
  typedef std::vector<Word> WordArray;

  int       s_;
  CharArray chars_;
  WordArray words_;
};

class CQWordSearch : public QWidget {
  Q_OBJECT

 public:
  CQWordSearch(int s=10);

  void loadFile(const QString &filename);

  void addWord(const QString &word);

  void generate();

  void checkMatch();

 private:
  void paintEvent(QPaintEvent *) override;

  void draw(QPainter *painter);

  void keyPressEvent(QKeyEvent *) override;

  void mousePressEvent  (QMouseEvent *e) override;
  void mouseReleaseEvent(QMouseEvent *e) override;
  void mouseMoveEvent   (QMouseEvent *e) override;

  void outlineWord(QPainter *painter, const WordGrid::Pos &start, const WordGrid::Pos &end,
                   QColor stroke_color, QColor fill_color);

  WordGrid::Pos mapPoint(const QPoint &p) const;

  QSize sizeHint() const override;

 private:
  WordGrid      grid_;
  double        s_ { 100.0 };
  double        b_ { 8.0 };
  double        dx_ { 1.0 };
  double        dy_ { 1.0 };
  bool          pressed_ { false };
  WordGrid::Pos start_;
  WordGrid::Pos end_;
};
