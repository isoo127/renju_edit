package gamesParsing;

import io.realm.RealmList;
import io.realm.RealmObject;

public class Game extends RealmObject {
	
	private int id;
	private Rule rule;
	private Player black;
	private Player white;
	private String bresult;
	private String move;
	private RealmList<byte[]> moveList;

	public Game(int id, Rule rule, Player black, Player white,
                String bresult, String move, RealmList<byte[]> moveList) {
		this.id = id;
		this.rule = rule;
		this.black = black;
		this.white = white;
		this.bresult = bresult;
		this.move = move;
		this.moveList = moveList;
	}

	public Game() {
		this(-1,null,null,null,null,null,null);
	}

	public int getId() {
		return id;
	}

	public Rule getRule() {
		return rule;
	}

	public Player getBlack() {
		return black;
	}

	public Player getWhite() {
		return white;
	}

	public String getBresult() {
		return bresult;
	}

	public String getMove() {
		return move;
	}

}
