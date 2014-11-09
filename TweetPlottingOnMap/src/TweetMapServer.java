import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import twitter4j.JSONArray;

import com.DBHelper;
import com.TweetNode;


public class TweetMapServer extends HttpServlet{

	private static final long serialVersionUID = 1L;
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		super.doGet(req, resp);
		
		JSONArray array = new JSONArray();
		DBHelper db = new DBHelper();
		List<TweetNode> list = db.getAllTweetsFromDB();
		for(TweetNode node : list){
			array.put(node.toJSON());
		}
		resp.getWriter().println(array.toString());
		db.close();
	}
}