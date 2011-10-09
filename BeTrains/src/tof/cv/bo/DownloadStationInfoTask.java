package tof.cv.bo;

import java.util.ArrayList;
import java.util.Date;

import tof.cv.misc.ConnectionMaker;
import tof.cv.ui.InfoStationActivity;
import android.os.AsyncTask;
import android.util.Log;

public class DownloadStationInfoTask extends AsyncTask<String, Integer, Long> {

	private InfoStationActivity infoStationAct;
	private ArrayList<Station> stationStops = new ArrayList<Station>();
	String stationsName;
	String lang;
	String hour;
	String minute;
	String choice;

	public DownloadStationInfoTask(InfoStationActivity infoStationAct,String stationsName, String lang, String hour,
			String minute, String choice) {
		this.infoStationAct = infoStationAct;
		this.stationsName=stationsName;
		this.lang=lang;
		this.hour=hour;
		this.minute=minute;
		this.choice=choice;
	}
	
	public DownloadStationInfoTask(InfoStationActivity infoStationAct,String stationsName, String lang, int timestamp, String choice) {
		this.infoStationAct = infoStationAct;
		this.stationsName=stationsName;
		this.lang=lang;
		this.hour=ConnectionMaker.getHourFromDate(""+timestamp, false);
		this.minute=ConnectionMaker.getHourFromDate(""+timestamp, false);;
		this.choice=choice;
	}

	protected Long doInBackground(String... params) {
		getStationInfo(stationsName, lang, hour, minute,choice);
		return null;
	}

	protected void onPostExecute(Long result) {
		String title = "Train";
		String body = "Time";
		Date date= new Date();
		System.out.println(date.toString());
		date.setHours(Integer.decode(hour));
		date.setMinutes(Integer.decode(minute));
		infoStationAct.setTimestamp(date.getTime());
		System.out.println(new Date(infoStationAct.getTimestamp()).toString());
		// I have to get the station name and time parameters given by website
		if (stationStops != null)
			if (stationStops.size() > 0) {
				title = stationStops.get(0).getStation();
				body = stationStops.get(0).getVehicle();
				infoStationAct.setGPS(stationStops.get(0).getVehicle(),stationStops.get(0).getPlatform());
				System.out.println("GPS"+stationStops.get(0).getVehicle()+"/"+stationStops.get(0).getPlatform());
				stationStops.remove(0);
			}

		try {
			System.out.println(infoStationAct);
			System.out.println(body);
			infoStationAct.setAllText(title);
			infoStationAct.setStationStops(stationStops);
			infoStationAct.fillTrainStops();
		} catch (Exception e) {
			e.printStackTrace();
			infoStationAct.setConnectionTitleText();

		}

	}

	public void getStationInfo(String stationsName, String lang, String hour,
			String minute, String choice) {
		if(hour.length()==1)
			hour="0"+hour;
		if(minute.length()==1)
			minute="0"+minute;
		
		String url = "http://api.irail.be/liveboard/?station="
				+ stationsName.replace(" ", "%20") + "&lang=" + lang + "&time="
				+ hour + "" + minute + "&arrdep=" + choice;
		try {
			stationStops = ConnectionMaker.afficheGareL(url, infoStationAct);
		} catch (StringIndexOutOfBoundsException e) {
			Log.e("InfoGare.java", e.toString());
		}

	}

}
