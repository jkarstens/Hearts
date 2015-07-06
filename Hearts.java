import java.applet.Applet;
import javax.swing.JFrame;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.event.*;

public class Hearts extends Applet implements ActionListener, MouseListener, MouseMotionListener, Runnable {
  private Thread thread;
  private Graphics dbGraphics;
  private Image dbImage;
  private int WIDTH, HEIGHT;

  private MenuBar menuBar;
  private Menu[] menus;
  private MenuItem[] gameOptions, cardStyles, backgroundColors;
  private Image emblem;
  private boolean showEmblem, rotateCards; // aesthetic flags
  private Image[] cardBacks; // add one of me and put me on the Q♠
  private Image cardBack; // current card style
  private int CARD_WIDTH, CARD_HEIGHT;
  private int BORDER; // space between cards and the edges of the screen
  private Color[] colors;
  private Font scoresTitleFont, scoresFont;
  private int mousex, mousey;

  private Card[] deck;
  private Player[] players;
  private Player leader, current;
  private Trick trick; // the current Trick
  private boolean play;
  private int[] scores;

  public void init() {
    WIDTH = 900;
    HEIGHT = 650;
    setSize(WIDTH, HEIGHT);
    setPreferredSize(new Dimension(WIDTH, HEIGHT));
    setBackground(Color.WHITE);

    menuBar = new MenuBar();
    menus = new Menu[3];
    gameOptions = new MenuItem[1];
    gameOptions[0] = new MenuItem("New Game", new MenuShortcut(KeyEvent.VK_N, false));
    menus[0] = new Menu("Game Options");
    for (MenuItem m : gameOptions) {
      m.addActionListener(this);
      menus[0].add(m);
    }
    cardStyles = new MenuItem[11];
    cardStyles[0] = new MenuItem("Bicycle Blue");
    cardStyles[1] = new MenuItem("Bicycle Red");
    cardStyles[2] = new MenuItem("Green");
    cardStyles[3] = new MenuItem("Pink");
    cardStyles[4] = new MenuItem("Black");
    cardStyles[5] = new MenuItem("Silver");
    cardStyles[6] = new MenuItem("Orange");
    cardStyles[7] = new MenuItem("Brown");
    cardStyles[8] = new MenuItem("Purple");
    cardStyles[9] = new MenuItem("Light Blue");
    cardStyles[10] = new MenuItem("Curved Hand");
    rotateCards = false;
    menus[1] = new Menu("Card Style");
    for (MenuItem m : cardStyles) {
      m.addActionListener(this);
      menus[1].add(m);
    }
    colors = new Color[5];
    colors[0] = new Color(255, 250, 250);
    colors[1] = new Color(220, 220, 220);
    colors[2] = new Color(255, 228, 225);
    colors[3] = new Color(90, 249, 143);
    colors[4] = new Color(176, 196, 222);
    backgroundColors = new MenuItem[colors.length + 1];
    backgroundColors[0] = new MenuItem("White");
    backgroundColors[1] = new MenuItem("Gray");
    backgroundColors[2] = new MenuItem("Pink");
    backgroundColors[3] = new MenuItem("Green");
    backgroundColors[4] = new MenuItem("Blue");
    backgroundColors[5] = new MenuItem("Hide Emblem");
    menus[2] = new Menu("Background");
    for (MenuItem m : backgroundColors) {
      m.addActionListener(this);
      menus[2].add(m);
    }
    for (Menu m : menus) {
      menuBar.add(m);
    }
    Object f = getParent();
    while (! (f instanceof Frame)) {
      f = ((Component) f).getParent();
    }
    Frame frame = (Frame) f;
    frame.setMenuBar(menuBar);

    Toolkit tk = Toolkit.getDefaultToolkit();
    emblem = tk.createImage("images/emblem.png");
    showEmblem = true;
    CARD_WIDTH = 88;
    CARD_HEIGHT = 128;
    BORDER = 5;
    cardBacks = new Image[cardStyles.length];
    for (int i = 0; i < cardBacks.length; i++) {
      cardBacks[i] = tk.createImage("images/" + cardStyles[i].getLabel().toLowerCase() + ".png").getScaledInstance(CARD_WIDTH, CARD_HEIGHT, Image.SCALE_SMOOTH);
    }
    cardBack = cardBacks[4];

    scoresTitleFont = new Font("Adobe Garamond Pro Bold", Font.BOLD + Font.ITALIC, 26);
    scoresFont = new Font("Adobe Garamond Pro Bold", Font.PLAIN, 20);

    deck = Card.getStandardDeck();
    Card.shuffle(deck);
    players = new Player[4];
    Card[][] hands = new Card[4][13];
    for (int i = 0; i < players.length; i++) {
      for (int j = i * 13; j < i * 13 + 13; j++) {
        hands[i][j % 13] = deck[j];
      }
    }
    players[0] = new Player(hands[0], 0, "South");
    players[1] = new Player(hands[1], 1, "West");
    players[0].setNext(players[1]);
    players[2] = new Player(hands[2], 2, "North");
    players[1].setNext(players[2]);
    players[3] = new Player(hands[3], 3, "East");
    players[2].setNext(players[3]);
    players[3].setNext(players[0]);

    play = false;
    scores = new int[4];

    addMouseListener(this);
    addMouseMotionListener(this);
  }

  public void paint(Graphics g) {
    drawStrings(g);
    // draw Cards
    Graphics2D g2 = (Graphics2D) g;
    int rotateAngle = -18; // measured clockwise from North for AffineTransform
    int r = (int) (3000 / Math.PI);
    int numCards = players[0].getCardsDisplay().length;
    for (int i = 0; i < numCards; i++) {
      Image cardImage = play ? players[0].getCardsDisplay()[i].getImage() : cardBack;
      if (rotateCards) {
        AffineTransform at = new AffineTransform();
        int theta = 90 - rotateAngle; // measured counterclockwise from East for trig
        at.translate(WIDTH / 2 - CARD_WIDTH / 2 + r * Math.cos(Math.toRadians(theta)), 550 + r - CARD_HEIGHT / 2 - r * Math.sin(Math.toRadians(theta)));
        at.rotate(Math.toRadians(rotateAngle), CARD_WIDTH / 2, CARD_HEIGHT / 2);
        g2.drawImage(cardImage, at, this);
        // do analog for this for cardBacks...
        // also must implement for mousemotionlistener and choosing
        rotateAngle += 3;
      } else {
        int cardx = (WIDTH - (numCards + 1) * CARD_WIDTH / 2) / 2 + i * CARD_WIDTH / 2;
        int cardy = HEIGHT - CARD_HEIGHT - BORDER;
        int boost = 0; // a valid card with the mouse over it will rise
        if (play && current == players[0] && cardx < mousex &&
            (mousex < cardx + CARD_WIDTH / 2 || (i == numCards - 1 && mousex < cardx + CARD_WIDTH)) &&
            cardy < mousey && mousey < cardy + CARD_HEIGHT /*&&
            players[0].isValidCard(players[0].getCardsDisplay()[i], trick)*/) {
          boost = 30;
        }
        g.drawImage(cardImage, cardx, cardy - boost, this);
        g.drawImage(cardBack, (WIDTH - (numCards + 3) * CARD_WIDTH / 4) / 2 + i * CARD_WIDTH / 4, BORDER, this);
        AffineTransform at = new AffineTransform();
        at.translate(CARD_HEIGHT / 2 - CARD_WIDTH / 2 + BORDER, (HEIGHT - (numCards + 3) * CARD_WIDTH / 4) / 2 + (i - 1) * CARD_WIDTH / 4);
        at.rotate(Math.toRadians(90), CARD_WIDTH / 2, CARD_HEIGHT / 2);
        g2.drawImage(cardBack, at, this);
        at.translate(0, -(WIDTH - (2 * BORDER + CARD_HEIGHT)));
        g2.drawImage(cardBack, at, this);
      }
    }

    if (play) {
      if (numCards == 13) { // start the round!
        //  TODO: pass cards...
        // find the starting player
        for (int i = 0; i < players.length; i++) {
          if (players[i].startsGame()) {
            leader = players[i];
            System.out.println(players[i].getName() + " leads");
            break;
          }
        }
        current = leader;
        trick = new Trick();
      }
      if (trick.getNumCards() < 4) {
        Card play = current.playCard(trick, null);
        System.out.println(current.getName() + " plays the " + play);
        /*if (current == players[0] || current == players[2]) animatePlay(current, play, g);*/
        delay(2000);
        current = current.getNext();
        System.out.println("trick has " + trick.getNumCards() + " cards in it");
      } else { // trick is completed
        delay(2000);
        // have everyone look at the cards
        do {
          current.update(trick);
          current = current.getNext();
        } while (current != leader);
        // get ready to start the next Trick
        leader = trick.getWinner();
        System.out.println(leader.getName() + " wins the trick");
        // TODO: animate trick moving to leader...
        trick = new Trick();
        current = leader;
        if (numCards == 0) { // end the round...
          play = false;
        }
      }
    }

    // draw current Trick
    /*g.drawImage(players[0].getCardsDisplay()[0].getImage(), WIDTH / 2 - CARD_WIDTH / 2, HEIGHT - BORDER - 2 * CARD_HEIGHT - 50, this);
    g.drawImage(players[2].getCardsDisplay()[0].getImage(), WIDTH / 2 - CARD_WIDTH / 2, BORDER + CARD_HEIGHT + 50, this);
    AffineTransform at = new AffineTransform();
    at.translate(WIDTH / 2 - CARD_WIDTH / 2 - CARD_HEIGHT, HEIGHT / 2 - CARD_HEIGHT / 2);
    at.rotate(Math.toRadians(90), CARD_WIDTH / 2, CARD_HEIGHT / 2);
    g2.drawImage(players[1].getCardsDisplay()[0].getImage(), at, this);
    at.translate(0, -257);
    g2.drawImage(players[3].getCardsDisplay()[0].getImage(), at, this);*/
  }

  /**
   *  Draw a Player playing a Card on the current Trick. This method expects
   *  player to be an existing Player in the current game.
   **/
  private void animatePlay(Player player, Card card, Graphics g) {
    int startx = 0, starty = 0;
    int dx = 0, dy = 0;
    int endx = 0, endy = 0;
    if (player == players[0] || player == players[2]) {
      startx = WIDTH / 2 - CARD_WIDTH / 2;
      dx = 0;
      endx = startx;
      if (player == players[0]) {
        starty = HEIGHT - CARD_HEIGHT - BORDER;
        dy = -5;
        endy = HEIGHT - BORDER - 2 * CARD_HEIGHT - 50;
      } else {
        starty = BORDER;
        dy = 5;
        endy = BORDER + CARD_HEIGHT + 50;
      }
    }
    int x = startx, y = starty;
    Image image = card.getImage();
    while (x <= endx && y <= endy) {
      g.drawImage(image, x, y, this);
      delay(100);
      x += dx;
      y += dy;
    }
  }

  /**
   *  Draw a Player taking a completed Trick. This method expects player to be
   *  an existing Player in the current game.
   **/
  private void animateTrickTake(Player player, Graphics g) {

  }

  public void update(Graphics g) {
    if (dbImage == null) {
      dbImage = createImage(WIDTH, HEIGHT);
      dbGraphics = dbImage.getGraphics();
    }
    dbGraphics.setColor(getBackground());
    dbGraphics.fillRect(0, 0, WIDTH, HEIGHT);
    dbGraphics.setColor(getForeground());
    paint(dbGraphics);
    g.drawImage(dbImage, 0, 0, this);
  }

  private void drawStrings(Graphics g) {
    if (showEmblem) {
      g.drawImage(emblem, WIDTH / 2 - emblem.getWidth(this) / 2, HEIGHT / 2 - emblem.getHeight(this) / 2, this);
    }
    Graphics2D g2 = (Graphics2D) g;
    g2.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
    g2.drawRect(670, BORDER, 200, 120);
    g.setFont(scoresTitleFont);
    g.drawString("Scores", 740, 25);
    g.setFont(scoresFont);
    g.drawString("North: " + scores[2], 740, 50);
    g.drawString("East: " + scores[3], 800, 80);
    g.drawString("South: " + scores[0], 740, 110);
    g.drawString("West: " + scores[1], 690, 80);
  }

  private void setUpNewGame() {
    Card.shuffle(deck);
    for (int i = 0; i < players.length; i++) {
      Card[] hand = new Card[13];
      for (int j = i * 13; j < i * 13 + 13; j++) {
        hand[j % 13] = deck[j];
      }
      players[i].giveCards(hand);
    }
    play = true;
  }

  private void delay(int ms) {
    try {
      Thread.sleep(ms);
    } catch (InterruptedException e) {
    }
  }

  public void actionPerformed(ActionEvent e) {
    Object source = e.getSource();
    if (source == gameOptions[0]) {
      setUpNewGame();
      return;
    }
    for (int i = 0; i < cardStyles.length - 1; i++) {
      if (source == cardStyles[i]) {
        cardBack = cardBacks[i];
        return;
      }
    }
    if (source == cardStyles[cardStyles.length - 1]) {
      rotateCards = ! rotateCards;
      if (rotateCards) {
        cardStyles[cardStyles.length - 1].setLabel("Linear Hand");
      } else {
        cardStyles[cardStyles.length - 1].setLabel("Curved Hand");
      }
      return;
    }
    for (int i = 0; i < backgroundColors.length - 1; i++) {
      if (source == backgroundColors[i]) {
        setBackground(colors[i]);
        return;
      }
    }
    if (source == backgroundColors[backgroundColors.length - 1]) {
      showEmblem = ! showEmblem;
      if (showEmblem) {
        backgroundColors[backgroundColors.length - 1].setLabel("Hide Emblem");
      } else {
        backgroundColors[backgroundColors.length - 1].setLabel("Show Emblem");
      }
    }
  }

  public void mouseEntered(MouseEvent e) {}
  public void mouseExited(MouseEvent e) {}
  public void mousePressed(MouseEvent e) {}
  public void mouseReleased(MouseEvent e) {}
  public void mouseClicked(MouseEvent e) {}

  public void mouseMoved(MouseEvent e) {
    mousex = e.getX();
    mousey = e.getY();
  }

  public void mouseDragged(MouseEvent e) {}

  public void start() {
    if (thread == null) {
      thread = new Thread(this);
      thread.start();
    }
  }

  public void run() {
    while (thread != null) {
      repaint();
      delay(20);
    }
  }

  public void stop() {
    thread = null;
  }

  public static void main(String[] args) {
    JFrame frame = new JFrame("HEARTS");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    Applet applet = new Hearts();
    frame.add(applet, BorderLayout.CENTER);
    applet.init();
    applet.start();
    frame.pack();
    frame.setVisible(true);
  }
}
