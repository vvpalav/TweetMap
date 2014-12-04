import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.amazonaws.util.json.JSONArray;
import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;

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
		TwipMapSQSHandler.getSQSHandler();
		String word = req.getParameter("input");
		String type = req.getParameter("type");
		log.info("received request with type " + type + " and word " + word);
		JSONObject json = retrieveTweetData(word, type);
		resp.setContentType("text/json");
		PrintWriter out = resp.getWriter();
		out.println(json.toString());
		out.flush(); out.close();
	}

	public static JSONObject retrieveTweetData(String word, String type) {
		JSONObject json = new JSONObject();
		JSONArray array = new JSONArray();
		DBHelper db = new DBHelper();
		try {
			for (TweetNode node : db.getAllTweetsFromDB(word, type)) {
				array.put(node.toJSON().toString());
			}
			json.put("data", array);
			json.put("count", array.length());
			if(type != null){
				json.put("type", type);
			} else {
				json.put("type", "general");
			}
			if(array.length() == 0 && type != null && type.equals("live")
					&& AlchemyAPIHandler.getLiveThreadsValue() == 0){
				System.out.println("Request type live: No more tweets");
				json.put("msg", "done");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return json;
	}
}