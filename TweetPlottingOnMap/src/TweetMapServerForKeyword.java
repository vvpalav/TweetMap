import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import twitter4j.JSONArray;
import twitter4j.JSONException;
import twitter4j.JSONObject;

public class TweetMapServerForKeyword extends HttpServlet {

	private static final long serialVersionUID = 10283173239L;

	public TweetMapServerForKeyword(){
		super();
	}
	
	public static void main(String[] args) throws ServletException, IOException{
		new TweetMapServerForKeyword().doPost(null, null);
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doPost(req, resp);
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		try {
			JSONObject json = new JSONObject();
			DBHelper db = new DBHelper();
			JSONArray keywords = new JSONArray();
			for (String str : db.getListOfKeywords()) {
				keywords.put(str);
			}
			json.put("keywords", keywords);
			resp.setContentType("text/json");
			PrintWriter out = resp.getWriter();
			out.println(json.toString());
			out.flush();
			out.close();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}