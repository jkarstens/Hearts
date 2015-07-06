/* Trick.java */

/**
 *  A class to represent a trick, which consists of four cards, one from each
 *  player. A player must follow the lead card's suit if possible.
 **/

public class Trick {

  /**
   *  A Trick keeps track of which Players played which Cards. One index
   *  identifies a card as well as the Player who played that card.
   **/
  private Player[] players;
  private Card[] cards;
  private int numCards;

  public Trick(Player player, Card lead) {
    players = new Player[4];
    players[0] = player;
    cards = new Card[4];
    cards[0] = lead;
    numCards = 1;
  }

  public Trick() {
    players = new Player[4];
    cards = new Card[4];
    numCards = 0;
  }

  public void addCard(Player player, Card card) {
    players[numCards] = player;
    cards[numCards] = card;
    numCards++;
  }

  public Player getWinner() {
    if (numCards == 4) {
      Card.SUIT suit = cards[0].getSuit();
      int max = 0;
      for (int i = 1; i < cards.length; i++) {
        if (cards[i].getSuit() == cards[max].getSuit() &&
            cards[i].getRank() > cards[max].getRank()) {
          max = i;
        }
      }
      return players[max];
    } else {
      return null;
    }
  }

  public int getNumCards() {
    return numCards;
  }

  public Player getPlayer(int i) {
    return players[i];
  }

  public Card getCard(int i) {
    return cards[i];
  }
}
