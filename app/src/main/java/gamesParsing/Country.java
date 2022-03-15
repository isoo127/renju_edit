package gamesParsing;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;

public class Country extends RealmObject {

	private String name;
	@Ignore
	private int id;
	
	public Country(int id, String name, String abbr) {
		this.id = id;
		this.name = name;
	}

	public Country() {
		this(-1,null,null);
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}
	
}
