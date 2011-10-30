package tof.cv.mpp.Utils;

import java.io.StringReader;
import java.util.ArrayList;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import tof.cv.mpp.R;
import tof.cv.mpp.adapter.TweetItemAdapter;
import tof.cv.mpp.bo.Tweet;
import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

public class Tablet {
	public static void loadTweets(final Activity a, final ListView l) {
		new Thread(new Runnable() {
			public void run() {
				try {
					String url="BETRAINS";
					SharedPreferences mDefaultPrefs = PreferenceManager.getDefaultSharedPreferences(a);;					
					if(mDefaultPrefs.getBoolean("mNMBS", a.getResources().getBoolean(R.bool.nmbs)))
						url+="%20OR%20NMBS";
					
					if(mDefaultPrefs.getBoolean("mSNCB", a.getResources().getBoolean(R.bool.sncb)))
						url+="%20OR%20SNCB";
					
					if(mDefaultPrefs.getBoolean("miRail", true))
						url+="%20OR%20irail";
					
					if(mDefaultPrefs.getBoolean("mNavetteurs", a.getResources().getBoolean(R.bool.navetteurs)))
						url+="%20OR%20navetteurs";

					final ArrayList<Tweet> tweets = Tablet.getTweets(url
							, 1);
					a.runOnUiThread(new Thread(new Runnable() {
						public void run() {
							
							l.setAdapter(new TweetItemAdapter(a,
									R.layout.row_tweet, tweets));
						}
					}));
				} catch (Exception e) {
					e.printStackTrace();
					a.runOnUiThread(new Thread(new Runnable() {
						public void run() {
							TextView tv = (TextView) a.findViewById(R.id.fail);
							tv.setVisibility(View.VISIBLE);

						}
					}));

				}

			}
		}).start();


	}

	public static ArrayList<Tweet> getTweets(String searchTerm, int page) {
		String searchUrl = "http://search.twitter.com/search.json?q="
				+ searchTerm + "&rpp=50&page=" + page;
		
		ArrayList<Tweet> tweets = new ArrayList<Tweet>();

		HttpClient client = new DefaultHttpClient();
		HttpGet get = new HttpGet(searchUrl);

		ResponseHandler<String> responseHandler = new BasicResponseHandler();

		String responseBody = null;
		Log.v("TEST", "***: " + searchUrl);
		try {
			responseBody = client.execute(get, responseHandler);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		/*JSONObject jsonObject = null;
		JSONParser parser = new JSONParser();

		StringReader myStringReader = new StringReader(responseBody);
		try {
			Object obj = parser.parse(myStringReader);
			jsonObject = (JSONObject) obj;
		} catch (Exception ex) {
			Log.v("TEST", "Exception: " + ex.getMessage());
		}

		JSONArray arr = null;

		try {
			Object j = jsonObject.get("results");
			
			arr = (JSONArray) j;
		} catch (Exception ex) {
			Log.v("TEST", "Exception: " + ex.getMessage());
		}

		for (Object t : arr) {
			Tweet tweet = new Tweet(((JSONObject) t).get("from_user")
					.toString(), ((JSONObject) t).get("text").toString(),
					((JSONObject) t).get("profile_image_url").toString());
			tweets.add(tweet);
			Log.v("***", "***"+tweet.message);
		}

		return tweets;
		*/
		return null;
	}

}
