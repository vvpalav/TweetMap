package com;

import java.sql.Date;
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

	public static void main(String[] args){
		DBHelper db = new DBHelper();
		TweetNode node = new TweetNode(2, "vinayak", "sometext", 
				38.898556, -77.037852, new java.util.Date());
		db.insertTweetIntoDB(node);
		for(TweetNode n : db.getAllTweetsFromDB("")){
			System.out.println(n);
		}
		
		for(String str : db.getListOfKeywords()){
			System.out.println(str);
		}
		db.close();
	}
	
	public DBHelper() {
		try {
			System.out.println("Connecting to database");
			Class.forName("com.mysql.jdbc.Driver");
			conn = (Connection) DriverManager.getConnection(dbURL, dbUser,
					dbPassword);
			System.out.println("Connected to database");
		} catch (SQLException  e) {
			System.out.println("Cannot connect the database!");
			e.printStackTrace();
		} catch (ClassNotFoundException ex){
			System.out.println("Cannot connect the database!");
			ex.printStackTrace();
		}
	}

	public void insertTweetIntoDB(TweetNode node) {
		String SQL = "insert into tweets values (?, ?, ?, ?, ?, ?)";
		try {
			PreparedStatement stmt = conn.prepareStatement(SQL);
			stmt.setObject(1, node.getId(), java.sql.Types.BIGINT);
			stmt.setObject(2, node.getUsername(), java.sql.Types.VARCHAR);
			stmt.setObject(3, node.getText(), java.sql.Types.VARCHAR);
			stmt.setObject(4, node.getLatitude(), java.sql.Types.DOUBLE);
			stmt.setObject(5, node.getLongitude(), java.sql.Types.DOUBLE);
			stmt.setObject(6, node.getTimestamp(), java.sql.Types.TIMESTAMP);
			stmt.executeUpdate();
		} catch (SQLException e) {
			System.out.println("Error while inserting tweet into database");
			e.printStackTrace();
		}
	}

	public List<TweetNode> getAllTweetsFromDB(String word) {
		List<TweetNode> list = new LinkedList<TweetNode>();
		String SQL = "select * from tweets";
		if(word != null && word.length() > 0){
			SQL += " where text like '%@" + word + "%'";
		}
		try {
			ResultSet rs = conn.createStatement().executeQuery(SQL);
			while (rs.next()) {
				TweetNode node = new TweetNode(
						rs.getObject(1, long.class),
						rs.getObject(2, String.class),
						rs.getObject(3, String.class),
						rs.getObject(4, double.class), 
						rs.getObject(5, double.class), 
						rs.getObject(6, Date.class));
				list.add(node);
			}
		} catch (SQLException e) {
			System.out.println("Error while fetching tweets from database");
			e.printStackTrace();
		}
		return list;
	}

	public void deleteAllTweetsFromDB() {
		try {
			System.out.println("Deleting all the tweets from database");
			conn.createStatement().executeUpdate("delete from tweets");
		} catch (SQLException e) {
			System.out.println("Deletion of all the tweets failed");
			e.printStackTrace();
		}
	}

	public void deleteTweetWithStatusId(LinkedList<Long> list) {
		StringBuilder ids = new StringBuilder();
		ids.append("(");
		for(long no : list){
			ids.append(no).append(", ");
		}
		ids.setLength(ids.length()-2);
		ids.append(")");
		//System.out.println(ids);
		
		String SQL = "delete from tweets where id in " + ids.toString();
		try {
			PreparedStatement stmt = conn.prepareStatement(SQL);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public List<String> getListOfKeywords(){
		List<String> list = new LinkedList<String>();
		String SQL = "Select text from tweets where text like '%@%'";
		ResultSet rs;
		try {
			rs = conn.createStatement().executeQuery(SQL);
			while(rs.next()){
				String word = rs.getString(1);
				if(word.contains("@")){
					word = word.substring(word.indexOf("@")+1);
					int index = word.indexOf(" ");
					if(index > 0){
						word = word.substring(0, index);
					} else {
						word = word.substring(0);	
					}
					if(!word.contains(" ")) list.add(word);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return list;
	}
	
	public int getTweetCount() {
		String SQL = "select count(*) from tweets";
		try{
			ResultSet rs = conn.createStatement().executeQuery(SQL);
			rs.next();
			return rs.getObject(1, int.class);
		} catch(SQLException e){
			e.printStackTrace();
		}
		return 0;
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
