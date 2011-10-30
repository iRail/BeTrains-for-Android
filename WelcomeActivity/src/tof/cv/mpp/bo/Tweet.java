package tof.cv.mpp.bo;

import android.text.Html;

public class Tweet {
	  public String username;
	  public String message;
	  public String image_url;
	    
	  public Tweet(String username, String message, String url) {
	    this.username = username;
	    this.message = Html.fromHtml(message).toString();
	    this.image_url = url;
	  }
	}