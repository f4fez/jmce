	TAGS	UNSORTED
Terminal	Terminal.java	100	abstract public class Terminal extends jmce.sim.AbstractHardware
Terminal::NORMAL	Terminal.java	171		static public final byte NORMAL		= 0x00;
Terminal::REVERSE	Terminal.java	213		static public final byte REVERSE	= 0x01;
Terminal::HI	Terminal.java	255		static public final byte HI		= 0x02;
Terminal::UNDERLINE	Terminal.java	293		static public final byte UNDERLINE	= 0x04;
Terminal::BLINKING	Terminal.java	337		static public final byte BLINKING	= 0x08;
Terminal::SPACE	Terminal.java	381		static public final byte SPACE = (byte)' ';
Terminal::keys	Terminal.java	427		private Vector<Integer> keys = new Vector<Integer>();
Terminal::numRow	Terminal.java	470		private int numRow,numCol,numStatus;
Terminal::numCol	Terminal.java	477		private int numRow,numCol,numStatus;
Terminal::numStatus	Terminal.java	484		private int numRow,numCol,numStatus;
Terminal::fontSize	Terminal.java	508		private int fontSize;
Terminal::row	Terminal.java	531		private int row,col;
Terminal::col	Terminal.java	535		private int row,col;
Terminal::crts	Terminal.java	564		private ArrayList<CRT> crts = new ArrayList<CRT>();
Terminal::videoAtt	Terminal.java	607		private byte videoAtt[];
Terminal::videoChar	Terminal.java	633		private byte videoChar[];
Terminal::changed	Terminal.java	663		private boolean changed[];
Terminal::insertMode	Terminal.java	691		private boolean insertMode = false;
Terminal::savedRow	Terminal.java	724		private int savedRow,savedCol;
Terminal::savedCol	Terminal.java	733		private int savedRow,savedCol;
Terminal::att	Terminal.java	757		private byte att;
Terminal::Terminal	Terminal.java	765		Terminal(String name)
Terminal::addCRT	Terminal.java	875		public void addCRT(CRT crt)
Terminal::key	Terminal.java	929		public void key(int key)
Terminal::kbhit	Terminal.java	1014		public boolean kbhit()
Terminal::getch	Terminal.java	1083		public int getch()
Terminal::getNumStatus	Terminal.java	1258		public int getNumStatus()
Terminal::setNumStatus	Terminal.java	1314		public void setNumStatus(int n)
Terminal::setNumRow	Terminal.java	1372		public void setNumRow(int row)
Terminal::setNumCol	Terminal.java	1428		public void setNumCol(int col)
Terminal::getNumRow	Terminal.java	1483		public int getNumRow()
Terminal::getNumCol	Terminal.java	1533		public int getNumCol()
Terminal::getScreenSize	Terminal.java	1585		public int getScreenSize()
Terminal::getCol	Terminal.java	1646		public int getCol()
Terminal::getRow	Terminal.java	1688		public int getRow()
Terminal::setCursor	Terminal.java	1732		public void setCursor(int row,int col)
Terminal::setAttribute	Terminal.java	1893		public void setAttribute(int n)
Terminal::setChar	Terminal.java	1950		public void setChar(int pos,char c)
Terminal::reset	Terminal.java	2051		public void reset()
Terminal::init	Terminal.java	2124		public void init()
Terminal::setChanged	Terminal.java	2401		public void setChanged()
Terminal::setChanged	Terminal.java	2463		public void setChanged(int from,int len)
Terminal::getPos	Terminal.java	2618		int getPos(int col)
Terminal::getPos	Terminal.java	2674		int getPos()
Terminal::setChanged	Terminal.java	2732		public void setChanged(int line)
Terminal::home	Terminal.java	2796		public void home()
Terminal::cls	Terminal.java	2841		public void cls()
Terminal::setInsertMode	Terminal.java	2987		void setInsertMode(boolean insertMode)
Terminal::saveCursor	Terminal.java	3073		public void saveCursor()
Terminal::restoreCursor	Terminal.java	3153		public void restoreCursor()
Terminal::clearEol	Terminal.java	3221		public void clearEol()
Terminal::scrollUp	Terminal.java	3371		public void scrollUp(int from,int size,int n)
Terminal::scrollDown	Terminal.java	3814		public void scrollDown(int from,int size,int n)
Terminal::insertChar	Terminal.java	4150		public void insertChar()
Terminal::insertChar	Terminal.java	4275		public void insertChar(char c)
Terminal::insertLine	Terminal.java	4371		public void insertLine()
Terminal::deleteChar	Terminal.java	4473		public void deleteChar()
Terminal::deleteLine	Terminal.java	4583		public void deleteLine()
Terminal::clearLine	Terminal.java	4716		void clearLine()
Terminal::clearFromLine	Terminal.java	4912		public void clearFromLine()
Terminal::clearLine	Terminal.java	5041		void clearLine(int r)
Terminal::clearToCursor	Terminal.java	5237		public void clearToCursor()
Terminal::clearEos	Terminal.java	5352		public void clearEos()
Terminal::up	Terminal.java	5467		public void up()
Terminal::down	Terminal.java	5533		public void down()
Terminal::left	Terminal.java	5623		public void left()
Terminal::right	Terminal.java	5764		public void right()
Terminal::print	Terminal.java	6027		public void print(String s)
Terminal::println	Terminal.java	6129		public void println(String s)
Terminal::putchar	Terminal.java	6186		public void putchar(char c)
GraphicsCRT	GraphicsCRT.java	156	public class GraphicsCRT extends AbstractCRT implements KeyListener,ActionListener
GraphicsCRT::foreGround	GraphicsCRT.java	243		private Color foreGround[] = new Color[3];
GraphicsCRT::backGround	GraphicsCRT.java	287		private Color backGround[] = new Color[3];
GraphicsCRT::image	GraphicsCRT.java	331		private Image image = null;
GraphicsCRT::fm	GraphicsCRT.java	366		private FontMetrics fm;
GraphicsCRT::size	GraphicsCRT.java	389		private Dimension size,sizeChar;
GraphicsCRT::sizeChar	GraphicsCRT.java	394		private Dimension size,sizeChar;
GraphicsCRT::jcrt	GraphicsCRT.java	418		private JCRT jcrt;
GraphicsCRT::line	GraphicsCRT.java	438		private char line[];
GraphicsCRT::JCRT	GraphicsCRT.java	457		class JCRT extends JComponent
GraphicsCRT::JCRT::JCRT	GraphicsCRT.java	486			JCRT()
GraphicsCRT::JCRT::getPreferredSize	GraphicsCRT.java	752			public Dimension getPreferredSize()
GraphicsCRT::JCRT::getMinimumSize	GraphicsCRT.java	1213			public Dimension getMinimumSize()
GraphicsCRT::JCRT::getMaximumSize	GraphicsCRT.java	1288			public Dimension getMaximumSize()
GraphicsCRT::JCRT::paint	GraphicsCRT.java	1359			public void paint(Graphics g)
GraphicsCRT::JCRT::draw	GraphicsCRT.java	1605			private void draw(Graphics g)
GraphicsCRT::JCRT::hasFocus	GraphicsCRT.java	2010			public boolean hasFocus()
GraphicsCRT::JCRT::isFocusable	GraphicsCRT.java	2063			public boolean isFocusable()
GraphicsCRT::GraphicsCRT	GraphicsCRT.java	2115		public GraphicsCRT()
GraphicsCRT::drawLine	GraphicsCRT.java	2441		public void drawLine(Graphics g,int att,int r,int c,int count)
GraphicsCRT::drawLine	GraphicsCRT.java	2891		public void drawLine(Graphics g,int r)
GraphicsCRT::getForeground	GraphicsCRT.java	3561		public Color getForeground(int att)
GraphicsCRT::getBackground	GraphicsCRT.java	3631		public Color getBackground(int att)
GraphicsCRT::defineColor	GraphicsCRT.java	3702		public void defineColor(int att,Color f,Color b)
GraphicsCRT::init	GraphicsCRT.java	3808		public void init()
GraphicsCRT::keyPressed	GraphicsCRT.java	4166		public void keyPressed(KeyEvent e)
GraphicsCRT::keyReleased	GraphicsCRT.java	4423		public void keyReleased(KeyEvent e)
GraphicsCRT::keyTyped	GraphicsCRT.java	4468		public void keyTyped(KeyEvent e)
GraphicsCRT::isFocusTraversable	GraphicsCRT.java	4512		public boolean isFocusTraversable() 
GraphicsCRT::actionPerformed	GraphicsCRT.java	4570		public void actionPerformed(ActionEvent e)
