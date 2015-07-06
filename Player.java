/* Player.java */

/**
 *  A class to represent a Hearts player.
 **/

public class Player {
  /**
   *  This Player's unplayed Cards are sorted from high to low throughout the
   *  game according to Card.sort, with the lowest unplayed card at the twelfth
   *  index. This Player's played Cards are placed in the order in which they
   *  were played, with the most recently played Card at the zeroth index.
   *  Once a Card is played, it is moved to the beginning of the array, the
   *  unplayed Cards are shifted towards the end of the array, and the round
   *  pointer is incremented. The round pointer points to the highest unplayed
   *  Card in this Player's hand.
   **/
  private Card[] cards;
  private int round;

  /**
   *  An array of four pointers that indicate the start of each suit in this
   *  Player's cards array. Suits are indexed by their ordinals in Card.SUIT.
   *  If suitStarts[i] is positive, suitStarts[i] must be at least as large
   *  as the round pointer. If suitStarts[i] is negative, this Player is void
   *  of the suit. The values in this array are based only on unplayed Cards in
   *  cards.
   **/
  private int[] suitStarts;

  /**
   *  A matrix of Cards for keeping track of and guessing opponent hands.
   *  The matrix is indexed first by opponent (South, West, North, East) then
   *  by round. This matrix includes this Player's cards.
   **/
  private Card[][] allCards;

  /**
   *  A table of boolean values to keep track of which cards have been played.
   *  The array is indexed first by suit according to Card.SUIT ordinals, then
   *  by rank.
   **/
  private boolean[][] playedCards;

  /**
   *  An indicator to signal if hearts have been broken.
   **/
  private boolean brokenHearts;

  /**
   *  An integer 0 - 4 indicating this Player's absolute position, where 0
   *  corresponds to South, 1 to West, 2 to North, and 3 to East.
   *
   *  A Player's name and position may not be altered once set.
   **/
  private final int position;
  private final String name;

  /**
   *  The Player to the left of this Player. This variable cannot be changed
   *  once it is set.
   **/
  private Player next;

  public Player(Card[] c, int i, Player p, String s) {
    cards = c;
    Card.sort(cards);
    round = 0;
    updateSuitStarts(round);
    brokenHearts = false;
    allCards = new Card[4][13];
    allCards[i] = cards;
    playedCards = new boolean[4][13];
    position = i;
    next = p;
    name = s;
  }

  public Player(Card[] c, int i, String s) {
    this(c, i, null, s);
  }

  /**
   *  Give this player cards for a new game. This method is only intended
   *  for use at the end of a game to start a new game.
   **/
  public void giveCards(Card[] c) {
    if (round % 13 == 0) {
      cards = c;
      Card.sort(cards);
      round = 0;
      updateSuitStarts(round);
      brokenHearts = false;
      allCards = new Card[4][13];
      allCards[position] = cards;
      playedCards = new boolean[4][13];
    }
  }

  /**
   *  Find out if this Player starts the game. This method is only intended
   *  for use at the start of a game.
   **/
  public boolean startsGame() {
    Card start = new Card(Card.SUIT.CLUBS, 0);
    for (int i = 0; i < cards.length; i++) {
      if (cards[i].equals(start)) {
        return true;
      }
    }
    return false;
  }

  /**
   *  Find 3 cards to pass before the trick-taking starts. This method is only
   *  intended for use at the start of a game.
   **/
  public Card[] pass() {
    if (round == 0) {
      Card[] pass = new Card[3];
      for (int i = 0; i < 3; i++) { // TODO: ai; pass the first 3 cards for now
        pass[i] = cards[i];
        cards[i] = null;
      }
      return pass;
    } else {
      return null;
    }
  }

  /**
   *  Receive 3 passed cards from another Player before the trick-taking starts.
   *  This method is only intended for use at the start of a game.
   **/
  public void receive(Card[] pass) {
    if (round == 0) {
      int j = 0;
      for (int i = 0; i < cards.length; i++) {
        if (cards[i] == null) {
          cards[i] = pass[j];
          j++;
        }
      }
      Card.sort(cards);
    }
  }

  /**
   *  Play a card in the current trick. If force is null, this Player will
   *  choose a valid Card to play. Otherwise, this Player will play force
   *  if it is valid. If force is not null and not valid, this method returns
   *  null prematurely.
   *
   *  The AI for this method (used when force is null) is currently written
   *  assuming perfect knowledge of all cards in the game and without regard to
   *  shooting or blocking the moon.
   **/
  public Card playCard(Trick trick, Card force) {
    // update data structures with values from the current trick
    for (int i = 0; i < trick.getNumCards(); i++) {
      Player player = trick.getPlayer(i);
      Card card = trick.getCard(i);
      allCards[player.position][round] = card;
      playedCards[card.getSuit().ordinal()][card.getRank()] = true;
      if (!brokenHearts && card.getSuit() == Card.SUIT.HEARTS) {
        brokenHearts = true;
      }
    }
    // pick a Card to play
    Card play;
    if (force == null) {
      Card[] validCards = getValidCards(trick);
      play = validCards[0]; // TODO: ai; play the first valid card for now
    } else if (isValidCard(force, trick)) {
      play = force;
    } else {
      return null; // TODO: notify human must choose a valid card
    }
    trick.addCard(this, play);
    // update data structures
    playedCards[play.getSuit().ordinal()][play.getRank()] = true;
    for (int i = index(play); i > 0; i--) {
      Card.swap(i, i - 1, cards);
    }
    round++;
    updateSuitStarts(round);
    return play;
  }

  /**
   *  Let this Player know which cards were played in the most recent Trick
   *  after this Player's turn and update this Player's data structures
   *  accordingly.
   **/
  public void update(Trick trick) {
    int pos = 1;
    while (trick.getPlayer(pos - 1) != this) { // skip the cards already seen
      pos++;
    }
    for (; pos < 4; pos++) {
      Player player = trick.getPlayer(pos);
      Card card = trick.getCard(pos);
      allCards[player.position][round - 1] = card;
      playedCards[card.getSuit().ordinal()][card.getRank()] = true;
      if (! brokenHearts && card.getSuit() == Card.SUIT.HEARTS) {
        brokenHearts = true;
      }
    }
  }

  /**
   *  Get all legal Cards for the given Trick.
   **/
  private Card[] getValidCards(Trick trick) {
    Card[] validCards = new Card[13 - round];
    int count = 0;
    for (int i = round; i < cards.length; i++) {
      if (isValidCard(cards[i], trick)) {
        validCards[count] = cards[i];
        count++;
      }
    }
    return slice(0, count, validCards);
  }

  /**
   *  Find out if card is a valid play for trick. If trick is empty, then this
   *  Player is leading. This method expects that card is still unplayed.
   **/
  public boolean isValidCard(Card card, Trick trick) {
    Card lead = trick.getCard(0);
    if (lead == null) {
      if (round == 0 && startsGame()) {
        return card.equals(new Card(Card.SUIT.CLUBS, 0)); // 2♣ starts
      } else if (brokenHearts || cards[round].getSuit() == cards[cards.length - 1].getSuit()) { // any card is valid
        return true;
      } else { // any card but a heart is valid
        return card.getSuit() != Card.SUIT.HEARTS;
      }
    } else {
      Card.SUIT suit = lead.getSuit();
      if (suitStarts[suit.ordinal()] == -1) { // void in suit
        if (round == 0) { // can't drop points on the first round
          return card.getSuit() != Card.SUIT.HEARTS && !card.equals(new Card(Card.SUIT.SPADES, 10));
        } else { // any card is valid
          return true;
        }
      } else { // must play in suit
        return card.getSuit() == suit;
      }
    }
  }


  /**
   *  Get the number of unplayed cards this Player has in a particular suit.
   **/
  private int length(Card.SUIT suit) {
    int count = 0;
    for (int i = round; i < cards.length; i++) {
      if (cards[i].getSuit() == suit) {
        count++;
      }
    }
    return count;
  }

  /**
   *  Get an appropriately sized slice of an array of Cards from c[a]
   *  inclusive to c[b] exclusive. If b >= a, this method returns a
   *  zero-length array.
   **/
  private Card[] slice(int a, int b, Card[] c) {
    if (a < b) {
      Card[] slice = new Card[b - a];
      for (int i = 0; i < b - a; i++) {
        slice[i] = c[a + i];
      }
      return slice;
    } else {
      return new Card[0];
    }
  }

  /**
   *  Get an appropriately sized concatenation of multiple Card arrays.
   **/
  private Card[] concatenate(Card[]... c) {
    int length = 0;
    for (Card[] a : c) {
      length += a.length;
    }
    Card[] d = new Card[length];
    int i = 0;
    for (Card[] a : c) {
      for (int j = 0; j < a.length; j++) {
        d[i] = a[j];
        i++;
      }
    }
    return d;
  }

  /**
   *  Get the current index of the Card in cards. If the specified Card is not
   *  in cards, this method returns -1.
   **/
  private int index(Card card) {
    for (int i = 0; i < cards.length; i++) {
      if (card.equals(cards[i])) {
        return i;
      }
    }
    return -1;
  }

  /**
   *  Fill suitStarts with the correct values, starting from the the specified
   *  start value (typically the round).
   **/
  private void updateSuitStarts(int start) {
    suitStarts = new int[4];
    for (int i = 0; i < suitStarts.length; i++) {
      suitStarts[i] = -1;
    }
    for (int i = start; i < cards.length; i++) {
      suitStarts[cards[i].getSuit().ordinal()] = i;
      int j = i;
      while (i + 1 < cards.length && cards[i + 1].getSuit() == cards[j].getSuit()) {
        i++;
      }
    }
  }

  /**
  *  Get unplayed cards for the purpose of displaying them in the Hearts.java
  *  applet.
  **/
  public Card[] getCardsDisplay() {
    return slice(round, cards.length, cards);
  }

  public void setNext(Player p) {
    if (next == null) {
      next = p;
    }
  }

  public Player getNext() {
    return next;
  }

  public int getPosition() {
    return position;
  }

  public String getName() {
    return name;
  }

  public boolean equals(Player p) {
    return cards == p.cards;
  }

  public String toString() {
    String s = name + ": ";
    for (int i = 0; i < 13; i++) {
      if (i == round) {
        s += "| ";
      }
      s += cards[i] + " ";
    }
     s += " Starters: spades: " + suitStarts[0] + ", hearts: " + suitStarts[1] + ", clubs: " + suitStarts[2] + ", diamonds: " + suitStarts[3];
    return s;
  }
}
