package gamesParsing;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;

public class Rule extends RealmObject {

	private String name;
	@Ignore
	private int id;

	public Rule(int id, String name) {
		this.id = id;
		this.name = name;
	}

	public Rule() {
		this(-1,null);
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

}
