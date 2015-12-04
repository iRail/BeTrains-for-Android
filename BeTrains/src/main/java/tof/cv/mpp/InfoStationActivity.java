package tof.cv.mpp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.readystatesoftware.systembartint.SystemBarTintManager;

public class InfoStationActivity extends ActionBarActivity {
	/** Called when the activity is first created. */
    String id;
    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_info_station);

        setSupportActionBar((Toolbar) findViewById(R.id.my_awesome_toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setIcon(null);
		
		Bundle bundle = this.getIntent().getExtras();
		long timestamp = bundle.getLong("timestamp")*1000;
		String name = bundle.getString("Name");
         id = null;
        if(bundle.getString("ID")!=null)
            id = bundle.getString("ID").replace("BE.NMBS.","");

		
		InfoStationFragment fragment = (InfoStationFragment)getSupportFragmentManager().findFragmentById(R.id.fragment);
		fragment.displayInfo(name,timestamp,id);

        SystemBarTintManager tintManager = new SystemBarTintManager(this);
        // enable status bar tint
        tintManager.setStatusBarTintEnabled(true);
        // enable navigation bar tint
        tintManager.setNavigationBarTintEnabled(true);
        tintManager.setTintResource(R.color.primarycolor);


        Application app = getApplication();
        app.registerOnProvideAssistDataListener(new Application.OnProvideAssistDataListener() {
            @Override
            public void onProvideAssistData(Activity activity, Bundle bundle) {
                bundle.putString(Intent.EXTRA_ASSIST_CONTEXT, "BeTrains");
            }
        });
    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
	         Intent intent = new Intent(this, WelcomeActivity.class);            
	         intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
	         intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); 
	         startActivity(intent);  
	         finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

    public void pic(View v){
       AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle(R.string.photos);
        b.setMessage(R.string.photo_explain);
        b.setPositiveButton(R.string.ok_picture,new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("plain/text");
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"betrainsphotos@gmail.com"});
                intent.putExtra(Intent.EXTRA_SUBJECT, "BeTrains Android Photo "+id);
                startActivity(Intent.createChooser(intent, "Mail"));

            }
        });
        b.show();

    }
	
}