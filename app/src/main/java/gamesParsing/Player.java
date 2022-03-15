package gamesParsing;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;

public class Player extends RealmObject {

	private String name;
	private String surname;
	private String fullname;
	private int rating;
	private Country country;
	@Ignore
	private int id;

	public Player(int id, String name, String surname, Country country, int rating) {
		this.id = id;
		this.name = name;
		this.surname = surname;
		this.fullname = surname + " " + name;
		this.country = country;
		this.rating = rating;
	}

	public Player() {
		this(-1,null,null,null,-1);
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getSurname() {
		return surname;
	}

	public String getFullname() {
		return fullname;
	}

	public Country getCountry() {
		return country;
	}

	public int getRating() {
		return rating;
	}

	public void setRating(int rating) {
		this.rating = rating;
	}

}
