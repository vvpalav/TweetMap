import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import twitter4j.JSONArray;
import twitter4j.JSONException;
import twitter4j.JSONObject;

import com.DBHelper;
import com.TweetNode;

public class TweetMapServer extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		System.out.println("Got the request");
		try {
			JSONArray array = new JSONArray();
			DBHelper db = new DBHelper();
			List<TweetNode> list = db.getAllTweetsFromDB();
			for (TweetNode node : list) {
				array.put(node.getValue());
			}
			JSONObject json = new JSONObject();
			json.put("latlon", array);
			resp.setContentType("text/html");
			resp.getWriter().println(json.toString());
			resp.flushBuffer();
			db.close();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		super.doGet(req, resp);
	}
}