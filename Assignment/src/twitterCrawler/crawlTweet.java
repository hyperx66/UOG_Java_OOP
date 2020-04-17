package twitterCrawler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import twitterCrawler.twitterData;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import twitter4j.GeoLocation;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

public class crawlTweet {
	static List<String> posWords = new ArrayList<String>();
	static List<String> negWords = new ArrayList<String>();
	static List<twitterData> twitterArray = new ArrayList<twitterData>();
	static final String queryString = "coronavirus singapore";
	static final int count = 1000;
	static long sinceId = 0;
	static final GeoLocation location = new GeoLocation(1.290270, 103.851959);
	static Connection conn;

	public static void main(String[] args) {
		try {
			String url = "jdbc:mysql://194.59.164.158:3306/u645071659_makerspace";
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(url, "u645071659_hyperx66", "s9740499b");
			System.out.println("Database connection established");
			init();
			conn.close();
			System.out.println("Database connection terminated");
		} catch (Exception e) {
			System.out.println("Cannot connect to database");
		}
	}

	public static void init() {
		// Initialization

		// Start populating negative and positive
		try {
			BufferedReader negReader = new BufferedReader(
					new FileReader(new File("./src/twitterCrawler/negativeWords.txt")));
			BufferedReader posReader = new BufferedReader(
					new FileReader(new File("./src/twitterCrawler/positiveWords.txt")));
			String word = "";
			try {
				while ((word = negReader.readLine()) != null) {
					negWords.add(word.toLowerCase());
				}
				while ((word = posReader.readLine()) != null) {
					posWords.add(word.toLowerCase());
				}
				negReader.close();
				posReader.close();
			} catch (Exception e) {

			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		// Get all the twitter data
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true).setOAuthConsumerKey("Xv42imr5ty11OFUW2LkxAkNLq")
				.setOAuthConsumerSecret("Kc0RiHxFiIJBYJjJoQyhRSREQBZGL7kkdjWKEENrs2h2ha1d8U")
				.setOAuthAccessToken("1237729274536144896-3FvJ5Bc5gAZY2IMNUuI8gqjE0KvTL5")
				.setOAuthAccessTokenSecret("tT619ED8P6JUpnJvUQH2bE2odXA8zT5Uksj9aHnKqh7b3");
		TwitterFactory tf = new TwitterFactory(cb.build());
		Twitter twitter = tf.getInstance();

		Query querySince = new Query(queryString);
		querySince.setCount(count);
		querySince.setSinceId(sinceId);
		querySince.setGeoCode(location, 4, querySince.KILOMETERS);
		getTweets(querySince, twitter, "sinceId");
		querySince = null;
	}

	private static void getTweets(Query query, Twitter twitter, String mode) {
		boolean getTweets = true;

		while (getTweets) {
			try {
				QueryResult result = twitter.search(query);
				if (result.getTweets() == null || result.getTweets().isEmpty()) {
					getTweets = false;
				} else {
					for (Status status : result.getTweets()) {
						twitterData td = new twitterData(status.getText(), status.getUser().getScreenName(),
								getSentimentScore(status.getText()));
						insertIntoDatabase(td.twitterId, td.twitterText, td.emotion);
					}
				}
			} catch (TwitterException te) {
				System.out.println("Couldn't connect: " + te);
			} catch (Exception e) {
				System.out.println("Something went wrong: " + e);
			}
		}
	}

	private static String getSentimentScore(String input) {
		// normalize!
		input = input.toLowerCase();
		input = input.trim();
		// remove all non alpha-numeric non whitespace chars
		input = input.replaceAll("[^a-zA-Z0-9\\s]", "");

		int negCounter = 0;
		int posCounter = 0;

		// so what we got?
		String[] words = input.split(" ");

		// check if the current word appears in our reference lists...
		for (int i = 0; i < words.length; i++) {
			if (posWords.contains(words[i].toLowerCase())) {
				posCounter++;
			}
			if (negWords.contains(words[i].toLowerCase())) {
				negCounter++;
			}
		}

		// positive matches MINUS negative matches
		int result = (posCounter - negCounter);

		// Check Negative
		if (result < 0) {
			return "Negative";
			// Check Positive
		} else if (result > 0) {
			return "Positive";
		}

		// Neutral
		return "Neutral";
	}

	private static void insertIntoDatabase(String id, String tweetText, String tweetEmotion) {
		try {
			LocalDateTime date = LocalDateTime.now();
			DateTimeFormatter formatArrange = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
			String dateFormatted = date.format(formatArrange).toString();
			if (tweetText != "") {
				String insertQuery = "INSERT INTO assignment (id, twitterId, twitterText, twitterEmotion, timeStamp) VALUES (?,?,?,?,?)";
				PreparedStatement ps = conn.prepareStatement(insertQuery);
				ps.setInt(1, 0);
				ps.setString(2, id);
				ps.setString(3, tweetText);
				ps.setString(4, tweetEmotion);
				ps.setString(5, dateFormatted);
				ps.executeUpdate();
			}
		} catch (Exception e) {
			try {
				System.out.println("Cooldown initiated");
				TimeUnit.SECONDS.sleep(10);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
	}

}
