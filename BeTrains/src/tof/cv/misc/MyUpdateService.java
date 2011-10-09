package tof.cv.misc;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.Log;
import android.widget.Toast;

public class MyUpdateService extends IntentService {
	
	private static SharedPreferences prefs;
	private SharedPreferences.Editor editor;
	
	public MyUpdateService() {
		super(MyUpdateService.class.getSimpleName());

	}

	@Override
	protected void onHandleIntent(Intent intent) {
		scheduleNextUpdate();
	    update();
	}
	
	@Override
	public void onCreate() {
	    super.onCreate();
		prefs = PreferenceManager
		.getDefaultSharedPreferences(getBaseContext());
	    if (prefs.getLong("lastUpdate",0)==0){
	    	editor = prefs.edit();
	    	editor.putLong("lastUpdate",System.currentTimeMillis());
	    	update();
	    }
	    scheduleNextUpdate();

	}
	public void update() {
	    Log.e("Service Example", "****************************");
	    Log.e("Service Example", "***** Usefull Things    ****");
	    Log.e("Service Example", "****************************");

	}

	private void scheduleNextUpdate() {
		Intent intent = new Intent(this, this.getClass());
		PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		// The update frequency should often be user configurable. This is not.

		long currentTimeMillis = System.currentTimeMillis();
		long nextUpdateTimeMillis = currentTimeMillis + getOnlyNumerics(prefs.getString("", "99999"))
				* DateUtils.HOUR_IN_MILLIS;
		Time nextUpdateTime = new Time();
		nextUpdateTime.set(nextUpdateTimeMillis);

		String startTime = prefs.getString("prefSelectStartHour", "00:00");
		String endTime = prefs.getString("prefSelectEndHour", "23:59");
		
		Toast.makeText(getBaseContext(), startTime +" "+endTime , Toast.LENGTH_LONG).show();
		

		if (nextUpdateTime.hour < 8 || nextUpdateTime.hour >= 18) {
			nextUpdateTime.hour = 8;
			nextUpdateTime.minute = 0;
			nextUpdateTime.second = 0;
			nextUpdateTimeMillis = nextUpdateTime.toMillis(false)
					+ DateUtils.DAY_IN_MILLIS;
		}
		AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		alarmManager.set(AlarmManager.RTC, nextUpdateTimeMillis, pendingIntent);
	}
	
	public static int getOnlyNumerics(String str) {


	    StringBuffer strBuff = new StringBuffer();
	    char c;
	    
	    for (int i = 0; i < str.length() ; i++) {
	        c = str.charAt(i);
	        
	        if (Character.isDigit(c)) {
	            strBuff.append(c);
	        }
	    }
	    return Integer.valueOf(strBuff.toString());
	}
}
