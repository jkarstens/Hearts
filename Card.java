/* Card.java */

import java.awt.Toolkit;
import java.awt.Image;

/**
 *  A class to represent a playing card.
 **/
public class Card {
  private SUIT suit;
  private int rank; // zero-indexed from 2 to ace, no jokers
  private Image image; // original size: 167 x 243
  private String string;

  Card(SUIT s, int i) {
    suit = s;
    rank = i;
    String imageFile = "images/";
    switch (suit) {
      case SPADES: imageFile += "s"; break;
      case HEARTS: imageFile += "h"; break;
      case DIAMONDS: imageFile += "d"; break;
      case CLUBS: imageFile += "c"; break;
    }
    imageFile += rank + ".png";
    image = Toolkit.getDefaultToolkit().createImage(imageFile).getScaledInstance(88, 128, Image.SCALE_SMOOTH);
    if (rank < 9) {
      string = (rank + 2) + "";
    } else if (rank == 9) {
      string = "J";
    } else if (rank == 10) {
      string = "Q";
    } else if (rank == 11) {
      string = "K";
    } else if (rank == 12) {
      string = "A";
    }
    char suitChar;
    switch (suit) {
      case SPADES: suitChar = 6; break;
      case HEARTS: suitChar = 3; break;
      case DIAMONDS: suitChar = 4; break;
      case CLUBS: suitChar = 5; break;
      default: suitChar = 0;
    }
    string += suitChar;
  }

  public SUIT getSuit() {
    return suit;
  }

  public int getRank() {
    return rank;
  }

  public Image getImage() {
    return image;
  }

  public boolean equals(Card c) {
    return suit == c.suit && rank == c.rank;
  }

  public String toString() {
    return string;
  }

  /**
   *  Get a standard deck of cards (jokers not included).
   **/
  public static Card[] getStandardDeck() {
    Card[] deck = new Card[52];
    int s = 0;
    for (SUIT suit : SUIT.values()) {
      for (int i = 12; i > -1; i--) {
        deck[s + i] = new Card(suit, i);
      }
      s += 13;
    }
    return deck;
  }

  /**
   *  Shuffle an array of cards uniformly with a Fisher-Yates shuffle.
   **/
  public static void shuffle(Card[] cards) {
    for (int i = 0; i < cards.length - 1; i++) {
      int j = i + (int)((cards.length - i) * Math.random());
      swap(i, j, cards);
    }
  }

  public static void sort(Card[] cards) {
    sort(0, cards.length, cards);
  }

  /**
   *  Sort an array of cards with an insertion sort. Cards are sorted high to
   *  low first by suit (♠, ♥, ♣, ♦) and then by rank (A, K, Q, J, ..., 3, 2).
   *  The sorting is bounded inclusively by lo and exclusively by hi.
   **/
  public static void sort(int lo, int hi, Card[] cards) {
    for (int i = lo + 1; i < hi; i++) {
      for (int j = i; j > 0 &&
      (cards[j].suit.ordinal() < cards[j - 1].suit.ordinal() ||
      (cards[j].suit.ordinal() == cards[j - 1].suit.ordinal() &&
      cards[j].rank > cards[j - 1].rank)); j--) {
        swap(j, j - 1, cards);
      }
    }
  }

  public static void swap(int i, int j, Card[] cards) {
    Card c = cards[i];
    cards[i] = cards[j];
    cards[j] = c;
  }

  public enum SUIT { SPADES, HEARTS, CLUBS, DIAMONDS }

  public static void main(String[] args) {
    Card[] deck = getStandardDeck();
    for (Card c : deck) {
      System.out.print(c + " ");
    }
    System.out.println("\nShuffling...");
    shuffle(deck);
    for (Card c : deck) {
      System.out.print(c + " ");
    }
    System.out.println("\nSorting...");
    sort(deck);
    for (Card c : deck) {
      System.out.print(c + " ");
    }
  }
}
