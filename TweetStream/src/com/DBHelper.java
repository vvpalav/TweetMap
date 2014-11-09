package com;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.util.LinkedList;
import java.util.List;

import com.mysql.jdbc.Connection;

public class DBHelper {
	private final String dbURL = "jdbc:mysql://tweetmap.cjimqvmene65.us-west-2.rds.amazonaws.com:3306/tweetrecorder";
	private final String dbUser = "edge";
	private final String dbPassword = "edge_123";
	private Connection conn;

	public static void main(String[] args) {

		DBHelper db = new DBHelper();
		db.insertTweetIntoDB(new TweetNode(5, "9109", "181", "2014/11/01 10:00:00"));
		//db.deleteAllTweetsFromDB();
		for (TweetNode node : db.getAllTweetsFromDB()) {
			System.out.println(node);
		}
		db.close();
	}

	public DBHelper() {
		try {
			System.out.println("Connecting to database");
			Class.forName("com.mysql.jdbc.Driver");
			conn = (Connection) DriverManager.getConnection(dbURL, dbUser, dbPassword);
			System.out.println("Connected to database");
		} catch (SQLException | ClassNotFoundException e) {
			System.out.println("Cannot connect the database!");
			e.printStackTrace();
		}
	}

	public void insertTweetIntoDB(TweetNode node) {
		String SQL = "insert into tweets values (?, ?, ?, ?)";
		try {
			PreparedStatement stmt = conn.prepareStatement(SQL);
			stmt.setObject(1, node.getId(), java.sql.Types.INTEGER);
			stmt.setObject(2, node.getLatitude(), java.sql.Types.VARCHAR);
			stmt.setObject(3, node.getLongitude(), java.sql.Types.VARCHAR);
			stmt.setObject(4, node.getTimestamp(), java.sql.Types.TIMESTAMP);
			stmt.executeUpdate();
		} catch (SQLException e) {
			System.out.println("Error while inserting tweet into database");
			e.printStackTrace();
		}
	}

	public List<TweetNode> getAllTweetsFromDB() {
		List<TweetNode> list = new LinkedList<TweetNode>();
		String SQL = "select tweetId, latitude, longitude, tweetdatetime from tweets";
		try {
			ResultSet rs = conn.createStatement().executeQuery(SQL);
			while (rs.next()) {
				TweetNode node = new TweetNode(rs.getInt(1), rs.getString(2),
						rs.getString(3), rs.getObject(4, String.class));
				list.add(node);
			}
		} catch (SQLException e) {
			System.out.println("Error while fetching tweets from database");
			e.printStackTrace();
		}
		return list;
	}
	
	public void deleteAllTweetsFromDB(){
		try {
			System.out.println("Deleting all the tweets from database");
			conn.createStatement().executeUpdate("delete from tweets");
		} catch (SQLException e) {
			System.out.println("Deletion of all the tweets failed");
			e.printStackTrace();
		}
	}

	public void close() {
		try {
			conn.close();
			System.out.println("Closing connection to database");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
