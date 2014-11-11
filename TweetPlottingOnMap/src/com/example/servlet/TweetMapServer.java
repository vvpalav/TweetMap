package com.example.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.example.utilities.DBHelper;
import com.example.utilities.TweetNode;

import twitter4j.JSONArray;
import twitter4j.JSONException;
import twitter4j.JSONObject;

public class TweetMapServer extends HttpServlet {

	private static final long serialVersionUID = 10283173239L;
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String word = req.getParameter("input");
		if (word != null && word.equals("None"))
			word = null;
		JSONObject json = TweetMapServer.retrieveTweetData(word);
		resp.setContentType("text/json");
		resp.getWriter().println(json.toString());
		resp.flushBuffer();
		super.doGet(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String word = req.getParameter("input");
		if (word != null && word.equals("None"))
			word = null;
		JSONObject json = TweetMapServer.retrieveTweetData(word);
		resp.setContentType("text/json");
		resp.getWriter().println(json.toString());
		resp.flushBuffer();
		super.doPost(req, resp);
	}

	public static JSONObject retrieveTweetData(String word) {
		JSONObject json = new JSONObject();
		JSONArray array = new JSONArray();
		JSONArray keywords = new JSONArray();
		DBHelper db = new DBHelper();
		try {
			for (TweetNode node : db.getAllTweetsFromDB(word)) {
				array.put(node.getValue());
			}
			for (String str : db.getListOfKeywords()) {
				keywords.put(str);
			}
			json.put("latlon", array);
			json.put("keywords", keywords);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json;
	}
}