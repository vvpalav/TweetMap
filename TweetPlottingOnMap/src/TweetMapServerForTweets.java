import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import twitter4j.JSONArray;
import twitter4j.JSONException;
import twitter4j.JSONObject;

public class TweetMapServerForTweets extends HttpServlet {

	private static final long serialVersionUID = 10283173239L;

	public TweetMapServerForTweets(){
		super();
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doPost(req, resp);
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String word = req.getParameter("input");
		if (word != null && word.equals("NoKeyword"))
			word = null;
		JSONObject json = retrieveTweetData(word);
		resp.setContentType("text/json");
		PrintWriter out = resp.getWriter();
		out.println(json.toString());
		out.flush(); out.close();
	}

	public static JSONObject retrieveTweetData(String word) {
		JSONObject json = new JSONObject();
		JSONArray array = new JSONArray();
		DBHelper db = new DBHelper();
		try {
			for (TweetNode node : db.getAllTweetsFromDB(word)) {
				array.put(node.getValue());
			}
			json.put("latlon", array);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json;
	}
}