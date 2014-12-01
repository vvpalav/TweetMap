import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import twitter4j.JSONArray;
import twitter4j.JSONException;
import twitter4j.JSONObject;

public class TweetMapServerForTweets extends HttpServlet {

	private static final long serialVersionUID = 10283173239L;
	private Logger log = Logger.getLogger(TweetMapServerForTweets.class.getName());
	
	public TweetMapServerForTweets(){
		super();
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doPost(req, resp);
	}
	
	public static void main(String[] args) throws ServletException, IOException{
		new TweetMapServerForTweets().doPost(null, null);
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		log.info("received request");
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
				array.put(node.toJSON());
			}
			json.put("data", array);
		} catch (JSONException e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return json;
	}
}