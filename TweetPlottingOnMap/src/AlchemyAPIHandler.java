import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;

public class AlchemyAPIHandler {



	public static void main(String[] args) {
		AlchemyAPIHandler alchemy = new AlchemyAPIHandler();
		alchemy.performSentimentAnalysisOnTweet("vinayak is a bad boy");
	}

	public JSONObject performSentimentAnalysisOnTweet(String text) {
		try {
			String data = makeParamString(text);
			URL url = new URL(Configuration.alchemyURL);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			conn.setRequestProperty("charset", "utf-8");
			conn.addRequestProperty("Content-Length", Integer.toString(data.length()));
			DataOutputStream ostream = new DataOutputStream(conn.getOutputStream());
	        ostream.write(data.getBytes());
	        ostream.flush();
			InputStreamReader in = new InputStreamReader(conn.getInputStream());
			StringBuilder response = new StringBuilder();
			int charCode = -1;
			while ((charCode = in.read()) != -1) {
				response.append((char) charCode);
			}
			ostream.close();
			in.close();
			conn.disconnect();
			System.out.println("response: " + response.toString());
			return new JSONObject(response.toString());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public String makeParamString(String text){
		StringBuilder data = new StringBuilder();
		try {
			data.append("apikey=").append(Configuration.alchemyAPIKey);
			data.append("&text=").append(URLEncoder.encode(text,"UTF-8"));
			data.append("&outputMode=json").append("&showSourceText=1");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return data.toString();
	}
}