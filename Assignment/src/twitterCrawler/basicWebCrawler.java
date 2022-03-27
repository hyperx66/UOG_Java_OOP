package twitterCrawler;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class basicWebCrawler {
	String previousKey = "";
	static String dorsConLevel = "";
	List<String> keyWithNoValue = new ArrayList<String>();
	static Map<String, Integer> caseData = new HashMap<String, Integer>();
	static Map<String, Integer> globalData = new HashMap<String, Integer>();
	static Connection conn;

	public void getPageLinks(String URL) {
		try {
			Document document = Jsoup.connect(URL).get();
			Elements textsToScan = document.select("tbody");

			int forCounter = 0;
			for (Element tableData : textsToScan) {
				forCounter++;
				int keyFlag = 0;
				if (forCounter <= 7) {
					// retrieve current case stats
					for (Element td : tableData.select("tr").select("td")) {
						if (keyFlag == 0) {
							String[] arrStr = (td.text()).split(" ");
							String key = arrStr[0];
							if (arrStr.length > 1) {
								for (int i = 1; i < arrStr.length; i++) {
									arrStr[i] = arrStr[i].replaceAll("[^A-Za-z0-9]+", "");
								}
								for (int i = 1; i < arrStr.length; i++) {
									key += " " + arrStr[i];
								}
							}
							keyWithNoValue.add(key);
							keyFlag = 1;
						} else {
							String[] arrStr = (td.text()).split(" ");
							arrStr[0] = arrStr[0].replaceAll("[^A-Za-z0-9]+", "");
							int numData = Integer.parseInt(arrStr[0]);
							Iterator<String> it = keyWithNoValue.iterator();
							String keyData = (String) it.next();
							System.out.println(keyData + " is " + numData);
							caseData.put(keyData, numData);
							keyWithNoValue.remove(0);
							keyFlag = 0;
						}
					}
				} else if (forCounter == 8) {
					// retrieve current dorscon level
					int iterationCounter = 0;
					for (Element td : tableData.select("tr").select("td")) {
						iterationCounter++;
						if (iterationCounter == 2) {
							dorsConLevel = td.text();
							System.out.println("Current Dorscon is " + dorsConLevel);
						}
					}
				} else if (forCounter == 10) {
					for (Element td : tableData.select("tr").select("td")) {
						if (keyFlag == 0) {
							String[] arrStr = (td.text()).split(" ");
							String key = arrStr[0];
							if (arrStr.length > 1) {
								for (int i = 1; i < arrStr.length; i++) {
									arrStr[i] = arrStr[i].replaceAll("[^A-Za-z0-9]+", "");
								}
								for (int i = 1; i < arrStr.length; i++) {
									key += " " + arrStr[i];
								}
							}
							keyWithNoValue.add(key);
							keyFlag = 1;
						} else {
							String[] arrStr = (td.text()).split(" ");
							arrStr[0] = arrStr[0].replaceAll("[^A-Za-z0-9]+", "");
							int numData = Integer.parseInt(arrStr[0]);
							Iterator<String> it = keyWithNoValue.iterator();
							String keyData = (String) it.next();
							System.out.println(keyData + " is " + numData);
							globalData.put(keyData, numData);
							keyWithNoValue.remove(0);
							keyFlag = 0;
						}
					}
				}
			}
		} catch (

		Exception e) {
			e.printStackTrace();
		}
	}

	private static void insertIntoDatabase(String value, String key, String typeOfData) {
		try {
			LocalDateTime date = LocalDateTime.now();
			DateTimeFormatter formatArrange = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
			String dateFormatted = date.format(formatArrange).toString();
			if (value != "") {
				String insertQuery = "INSERT INTO caseFindings VALUES(?, ?, ?, ?, ?)";
				PreparedStatement ps = conn.prepareStatement(insertQuery);
				ps.setInt(1, 0);
				ps.setString(2, value);
				ps.setString(3, key);
				ps.setString(4, typeOfData);
				ps.setString(5, dateFormatted);
				ps.execute();
			}
		} catch (Exception e) {
			System.out.println("Cannot connect to database");
		}
	}

	public static void main(String[] args) {
		try {
			String url = "jdbc:mysql://";
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(url, "","");
			System.out.println("Database connection established");
			String truncateQuery = "TRUNCATE TABLE caseFindings";
			conn.prepareStatement(truncateQuery).execute();
			new basicWebCrawler().getPageLinks("https://www.moh.gov.sg/covid-19");
			for (String key : caseData.keySet()) {
				insertIntoDatabase(caseData.get(key).toString(), key, "singapore");
			}
			for (String key : globalData.keySet()) {
				insertIntoDatabase(globalData.get(key).toString(), key, "global");
			}
			insertIntoDatabase(dorsConLevel, "DORSCON", "singapore");
			conn.close();
			System.out.println("Database connection terminated");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
