import java.io.*;

/*
 * This class represents a board on which the game is played. The Board consists of Card Nodes arranged in 
 * a 5x5 square, which begins empty. Board keeps track of the game status including the placement of Cards as
 * well as the cards in each Player's hand.
 * 
 * Author: Eric Wahlquist
 */

public class Board {

	private Player human;
	private Player computer;
	private Deck deck;
	private Deck discardPile;
	private int turn;
	private Node[] board;

	// Explicit private default constructor that prevents a null battlefield
	// from being created

	@SuppressWarnings("unused")
	private Board() {
	}

	// Main constructor used for creating Battlefield objects

	public Board(Player human, Player computer) throws IOException {
		this.human = human;
		this.computer = computer;
		deck = new Deck();
		discardPile = new Deck();
		turn = 1;
		board = new Node[25];
		for (int i = 0; i < 25; i++) {
			board[i] = new Node();
		}
	}

	// Returns the human Player of this board

	public Player getHumanPlayer() {
		return human;
	}

	// Returns the computer Player of this board

	public Player getComputerPlayer() {
		return computer;
	}

	// Returns the current turn number of this board

	public int getTurn() {
		return turn;
	}

	// Returns the Card placed in a given Node on the board. Returns null if the
	// Node is empty

	public Card getCardAtNode(int nodeNumber) {
		return board[nodeNumber - 1].getCard();
	}

	// Returns the Zone that is currently in the given Node. Returns null if
	// there is no zone.

	public Zone getZoneAtNode(int nodeNumber) {
		return board[nodeNumber - 1].getZoneBonus();
	}
	
	//Applies a Zone bonus to a given Node on the board
	
	public void generateZoneBonus(Zone zone, int nodeNumber) {
		board[nodeNumber - 1].setZoneBonus(zone);
	}

	// Places a card in a Node on the Battlefield. Returns true if successful,
	// false otherwise.

	public boolean placeCard(Player player, Card card, int nodeNumber) {
		if (getCardAtNode(nodeNumber) == null) {
			board[nodeNumber - 1].setCurrentCard(card);
			if (player.getHand().contains(card)) {
				player.getHand().remove(card);
			} else {
				throw new IllegalArgumentException("Invalid: Card is not in hand");
			}
			return true;
		}
		return false;
	}

	// Removes a Card from a Node on the Battlefield and returns it

	public Card removeCard(int nodeNumber) {
		Card card = getCardAtNode(nodeNumber);
		if (card == null) {
			throw new IllegalArgumentException("Invalid: Node is empty");
		}
		discardPile.addCard(card);
		return board[nodeNumber - 1].removeCard();
	}

	// Draws a card from the deck into a Player's hand

	public Card drawCard(Player player) {
		if (deckIsEmpty()) {
			throw new IllegalArgumentException("Invalid: Deck is empty");
		}
		Card card = deck.drawCard();
		card.setOwner(player);
		if (player != null) {
			player.getHand().add(card);
		}
		return card;
	}

	// Adds a card to the deck

	public void addCard(Card card) {
		deck.addCard(card);
	}

	// Returns true if the deck of all monster cards to draw from is empty;
	// false otherwise

	public boolean deckIsEmpty() {
		return deck.isEmpty();
	}

	// Returns true if this Board is full (has no open Nodes left to place Cards
	// in)

	public boolean isFull() {
		for (int nodeNumber = 1; nodeNumber < 26; nodeNumber++) {
			if (getCardAtNode(nodeNumber) == null) {
				return false;
			}
		}
		return true;
	}

	// Increments the turn counter by 1

	public void incrementTurn() {
		turn++;
	}

	// Brings the board back to a clean state

	public void resetBoard() {

		// Remove all Cards and zone bonuses from the board
		for (int nodeNumber = 1; nodeNumber < 26; nodeNumber++) {
			if (!(getCardAtNode(nodeNumber) == null)) {
				removeCard(nodeNumber);
			}
			generateZoneBonus(null, nodeNumber);
		}

		// Return all cards from Players' hands
		for (Card card : getHumanPlayer().getHand()) {
			addCard(card);
		}
		for (Card card : getComputerPlayer().getHand()) {
			addCard(card);
		}
		while (!discardPile.isEmpty()) {
			addCard(discardPile.drawCard());
		}

		// Reset the Deck, Players' HP and hands, and reset the turn counter
		deck.reset();
		human.reset();
		computer.reset();
		turn = 1;
		
	}

	// Returns a String representation of the current status of the board

	public String toString() {
		String s = padString("Turn: " + turn, 131) + "\n";
		String humanHP = human.toString();
		String computerHP = computer.toString();
		s += humanHP;
		int spacing = 131 - (humanHP.length() + computerHP.length());
		s += padString(" ", spacing);
		s += computerHP;
		s += "\n\n" + border();
		for (int level = 0; level < 5; level++) {
			s += firstLine(level);
			s += zoneLine(level);
			s += monsterNameLine(level);
			s += hPLine(level);
			s += middleLine(level);
			s += spacerLine();
			s += monsterOwnerLine(level);
			s += zoneLine(level);
			s += lastLine(level);
			s += border();
		}
		return s;
	}

	// Private helper method for toString()

	private String border() {
		String border = "\n";
		for (int dashNumber = 0; dashNumber < 131; dashNumber++) {
			border = "-" + border;
		}
		return border;
	}
	
	//Private helper method for toString()
	
	private String zoneLine(int level) {
		String zoneLine = "|";
		for (int boxNumber = 1; boxNumber < 6; boxNumber++) {
			Zone zone = getZoneAtNode(5 * level + boxNumber);
			if (zone != null) {
				zoneLine += padString("* " + zone.getType().toUpperCase() + " ZONE *", 25) + "|";
			} else {
				zoneLine += spacerBox();
			}
		}
		zoneLine += "\n";
		return zoneLine;
	}

	// Private helper method for toString()

	private String spacerLine() {
		String spacer = "|\n";
		for (int boxNumber = 1; boxNumber < 6; boxNumber++) {
			spacer = padString(" ", 25) + spacer;
			spacer = "|" + spacer;
		}
		return spacer;
	}

	// Private helper method for toString()

	private String spacerBox() {
		String spacer = "";
		spacer += padString(" ", 25);
		spacer += "|";
		return spacer;
	}

	// Private helper method for toString()

	private String firstLine(int level) {
		String top = "|";
		for (int boxNumber = 1; boxNumber < 6; boxNumber++) {
			Card card = getCardAtNode(5 * level + boxNumber);
			if (card == null) {
				top += spacerBox();
			} else {
				top += padString("" + card.getCurrentUpperAP(), 25);
				top += "|";
			}
		}
		top += "\n";
		return top;
	}

	// Private helper method for toString()

	private String lastLine(int level) {
		String bottom = "|";
		for (int boxNumber = 1; boxNumber < 6; boxNumber++) {
			Card card = getCardAtNode(5 * level + boxNumber);
			if (card == null) {
				bottom += spacerBox();
			} else {
				bottom += padString("" + card.getCurrentLowerAP(), 25);
				bottom += "|";
			}
		}
		bottom += "\n";
		return bottom;
	}

	// Private helper method for toString()

	private String monsterNameLine(int level) {
		String mNL = "|";
		for (int boxNumber = 1; boxNumber < 6; boxNumber++) {
			Card card = getCardAtNode(5 * level + boxNumber);
			if (card == null) {
				mNL += spacerBox();
			} else {
				mNL += padString(card.getName(), 25);
				mNL += "|";
			}
		}
		mNL += "\n";
		return mNL;
	}

	// Private helper method for toString()

	private String hPLine(int level) {
		String hPLine = "|";
		for (int boxNumber = 1; boxNumber < 6; boxNumber++) {
			Card card = getCardAtNode(5 * level + boxNumber);
			if (card == null) {
				hPLine += spacerBox();
			} else {
				String hP = card.getCurrentHP() + "/" + card.getMaxHP() + " HP";
				if (card.getContaminatedTurnsLeft() > 0) {
					hP += " (Contaminated)";
				}
				hPLine += padString(hP, 25);
				hPLine += "|";
			}
		}
		hPLine += "\n";
		return hPLine;
	}

	// Private helper method for toString()

	private String middleLine(int level) {
		String middleLine = "|";
		for (int boxNumber = 1; boxNumber < 6; boxNumber++) {
			Card card = getCardAtNode(5 * level + boxNumber);
			if (card == null) {
				middleLine += spacerBox();
			} else {
				String leftAttack = "  " + card.getCurrentLeftAP();
				String rightAttack = card.getCurrentRightAP() + "  ";
				int spacing = 25 - (leftAttack.length() + rightAttack.length());
				String temp = leftAttack;
				temp += padString(" ", spacing);
				temp += rightAttack + "|";
				middleLine += temp;
			}
		}
		middleLine += "\n";
		return middleLine;
	}

	// Private helper method for toString()

	private String monsterOwnerLine(int level) {
		String mOL = "|";
		for (int boxNumber = 1; boxNumber < 6; boxNumber++) {
			Card card = getCardAtNode(5 * level + boxNumber);
			if (card == null) {
				mOL += spacerBox();
			} else {
				mOL += padString(card.getOwner().getName(), 25);
				mOL += "|";
			}
		}
		mOL += "\n";
		return mOL;
	}

	// Private helper method for toString()

	private String padString(String toPad, int totalSpaces) {
		String s = "";
		int spacing = totalSpaces - toPad.length();
		for (int spaceNumber = 0; spaceNumber < (spacing / 2); spaceNumber++) {
			s += " ";
		}
		s += toPad;
		for (int spaceNumber = 0; spaceNumber < spacing - (spacing / 2); spaceNumber++) {
			s += " ";
		}
		return s;
	}

	/*
	 * The Node class represents a single space on the battlefield on which a
	 * card may be placed. It keeps track of the Node's location, the Card that
	 * is placed in it, the owner of the Card, and the current status of the
	 * Card's attributes
	 *
	 * Author: Eric Wahlquist
	 */

	private class Node {

		private Card currentCard;
		private Zone zoneBonus;

		// Constructor takes a node number and creates a blank node with that
		// number

		public Node() {
			setCurrentCard(null);
			zoneBonus = null;
		}

		// Returns the current Card placed in this Node

		public Card getCard() {
			return currentCard;
		}

		// Returns the zone bonus currently in place in this Node. Returns null
		// if no bonus

		public Zone getZoneBonus() {
			return zoneBonus;
		}
		
		public void setZoneBonus(Zone zone) {
			zoneBonus = zone;
		}

		// Sets the current Card placed in this Node

		public void setCurrentCard(Card card) {
			this.currentCard = card;
		}

		// Removes a Card placed in this node and returns it

		public Card removeCard() {
			Card card = getCard();
			currentCard = null;
			return card;
		}
	}
}
