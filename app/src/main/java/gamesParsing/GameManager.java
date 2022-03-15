package gamesParsing;

import android.annotation.SuppressLint;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import io.realm.RealmList;

import static android.content.ContentValues.TAG;

public class GameManager {

	private static class Key {
		public final int id;
		public final String name;

		public Key(int id, String name) {
			this.id = id;
			this.name = name;
		}

		@Override
		public boolean equals(@Nullable @org.jetbrains.annotations.Nullable Object obj) {
			if (obj != null && getClass() != obj.getClass()) return false;
			Key k = (Key)obj;
			boolean b = false;
			if (k.id == this.id || k.name.equals(this.name)) b = true;
			return b;
		}

		@Override
		public int hashCode() {
			return 0;
		}
	}
	
	private final HashMap<Integer, Country> countries = new HashMap<>();
	private final HashMap<Integer, Rule> rules = new HashMap<>();
	private final HashMap<Key, Player> players = new HashMap<>();
	private final Set<Game> games = new HashSet<>();

	private ProgressBar bar;
	private TextView msg;

	@SuppressLint("SetTextI18n")
	public GameManager(InputStream is, TextView msg, ProgressBar bar) {
		try {
			Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
			InputSource inputSource = new InputSource(reader);
			inputSource.setEncoding("UTF-8");

			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document document = db.parse(inputSource);
			document.getDocumentElement().normalize();

			msg.setText("parsing basic data...");
			parseCountry(document.getElementsByTagName("country"));
			parseRule(document.getElementsByTagName("rule"));
			parsePlayer(document.getElementsByTagName("player"));

			this.bar = bar;
			this.msg = msg;
			parseGame(document.getElementsByTagName("game"));
			getRating();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void parseCountry(NodeList nl) {
		for (int i = 0; i < nl.getLength(); i++) {
			int id = Integer.parseInt(nl.item(i).getAttributes().getNamedItem("id").getNodeValue());
			String name = nl.item(i).getAttributes().getNamedItem("name").getNodeValue();
			String abbr = nl.item(i).getAttributes().getNamedItem("abbr").getNodeValue();
			countries.put(id, new Country(id, name, abbr));
		}
	}

	private void parseRule(NodeList nl) {
		for (int i = 0; i < nl.getLength(); i++) {
			int id = Integer.parseInt(nl.item(i).getAttributes().getNamedItem("id").getNodeValue());
			String name = nl.item(i).getAttributes().getNamedItem("name").getNodeValue();
			rules.put(id, new Rule(id, name));
		}
	}

	private void parsePlayer(NodeList nl) {
		for (int i = 0; i < nl.getLength(); i++) {
			int id = Integer.parseInt(nl.item(i).getAttributes().getNamedItem("id").getNodeValue());
			String name = nl.item(i).getAttributes().getNamedItem("name").getNodeValue();
			String surname = nl.item(i).getAttributes().getNamedItem("surname").getNodeValue();
			Country country = countries
					.get(Integer.parseInt(nl.item(i).getAttributes().getNamedItem("country").getNodeValue()));
			players.put(new Key(id, surname + " " + name), new Player(id, name, surname, country,0));
		}
	}

	@SuppressLint("SetTextI18n")
	private void parseGame(NodeList nl) {
		int max = nl.getLength() - 1;
		bar.setIndeterminate(false);
		bar.setMax(max);
		for (int i = 0; i < nl.getLength(); i++) {
			synchronized (this) {
				if(((i % 1000) == 0) || (i == max))
					bar.setProgress(i);
			}
			msg.setText("parsing games (" + i + " / " + max + ")");
			int id = Integer.parseInt(nl.item(i).getAttributes().getNamedItem("id").getNodeValue());
			Rule rule = rules.get(Integer.parseInt(nl.item(i).getAttributes().getNamedItem("rule").getNodeValue()));
			Player black = players
					.get(new Key(Integer.parseInt(nl.item(i).getAttributes().getNamedItem("black").getNodeValue()), ""));
			Player white = players
					.get(new Key(Integer.parseInt(nl.item(i).getAttributes().getNamedItem("white").getNodeValue()), ""));
			String bresult = null;
			if (nl.item(i).getAttributes().getNamedItem("bresult") != null)
				bresult = nl.item(i).getAttributes().getNamedItem("bresult").getNodeValue();
			NodeList childNode = nl.item(i).getChildNodes();
			String move = null;
			for (int j = 0; j < childNode.getLength(); j++) {
				if ("move".equals(childNode.item(j).getNodeName()))
					move = childNode.item(j).getTextContent();
			}

			RealmList<byte[]> moveList = null;
			try {
				assert move != null;
				moveList = Compress.toByteArrayList(move);
			} catch (Exception e) {
				e.printStackTrace();
				Log.d(TAG, "parseGame: " + id);
			}
			games.add(new Game(id, rule, black, white, bresult, move, moveList));
		}
	}

	@SuppressLint("SetTextI18n")
	private void getRating() {
		msg.setText("set rating of players...");
		bar.setIndeterminate(true);
		String URL = "https://renjurating.wind23.com/rating_all.html";
		try {
			org.jsoup.nodes.Document document = Jsoup.connect(URL).get();
			Elements elements = document.select("td");
			List<String> list = elements.eachText();
			Iterator<String> it = list.iterator();

			while(it.hasNext()) {
				String name = it.next();
				// text is not number
				if(!name.matches("[+-]?\\d*(\\.\\d+)?")) {
					Player player = players.get(new Key(-1, name));
					if(player != null) {
						int rating = Integer.parseInt(it.next());
						player.setRating(rating);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Set<Game> getGames() {
		return games;
	}

}
