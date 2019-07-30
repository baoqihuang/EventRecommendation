package external;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TicketMasterClient {
	private static final String HOST = "https://app.ticketmaster.com";
	private static final String ENDPOINT = "/discovery/v2/events.json";
	private static final String DEFAULT_KEYWORD = "event";
	private static final String API_KEY = "AAu5PCmRMphPGRxKZyd00VRv6cq7606I";
	
	public JSONArray search(double lat, double lon, String keyword) {
		if (keyword == null) {
			keyword = DEFAULT_KEYWORD;
		}
		try {
			keyword = URLEncoder.encode(keyword, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String query = String.format("apikey=%s&latlong=%s,%s&keyword=%s&radius=%s", API_KEY, lat, lon, keyword, 50);
		String url = HOST + ENDPOINT + "?" + query;
		
		try {
			//connection format
			HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection(); 
			connection.setRequestMethod("GET");
			//request and get the responsCode from ticketMaster
			int responseCode = connection.getResponseCode();
			System.out.println("Sending request to url: " + url);
			System.out.println("Response code: " + responseCode);
			if (responseCode != 200) {
				return new JSONArray();
			}
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String line = "";
			StringBuilder responseBody = new StringBuilder();
			while ((line = reader.readLine()) != null) {
				responseBody.append(line);
			}
			reader.close();
			
			try {
				JSONObject obj = new JSONObject(responseBody.toString());
				if (!obj.isNull("_embedded")) {
					JSONObject embedded = obj.getJSONObject("_embedded");
					return embedded.getJSONArray("events");
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new JSONArray();
	}
	
	public static void main (String args[]) {
		TicketMasterClient client = new TicketMasterClient();
		JSONArray events = client.search(37.38, -122.08, null);
		
		for (int i = 0; i < events.length(); ++i) {
			try {
				JSONObject event = events.getJSONObject(i);
				System.out.println(event.toString(2));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
	
}
