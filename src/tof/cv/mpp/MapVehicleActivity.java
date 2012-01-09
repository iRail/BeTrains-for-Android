package tof.cv.mpp;

import tof.cv.mpp.map.ItemizedOverlayVehicle;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;

public class MapVehicleActivity extends MapActivity {

	// PSOTIT: Pour le train:
	// http://railtime.be/website/apercu-du-trafic-trains?tn=3835

	private MapView mMap;
	private MapController mController;
	private Drawable marker;
	private ItemizedOverlayVehicle stationsOverlay;
	private GeoPoint gpStation;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.my_map);
		setResult(RESULT_OK);
		mMap = (MapView) findViewById(R.id.myGmap);
		mMap.setBuiltInZoomControls(true);
		mMap.setSatellite(false);
		mController = mMap.getController();

		Bundle extras = getIntent().getExtras();
		String name="";
		if (extras != null) {
			name = extras.getString("Name");
		} else
			Toast.makeText(this, "Error while getting train position",
					Toast.LENGTH_LONG).show();

		double glat = 0;
		double glon = 0;

		gpStation = new GeoPoint((int) (glat * 1E6), (int) (glon * 1E6));

		marker = getResources().getDrawable(R.drawable.train);
		stationsOverlay = new ItemizedOverlayVehicle(marker,name,this);
		stationsOverlay.addPoint(gpStation);
		mMap.getOverlays().add(stationsOverlay);

		mController.setCenter(gpStation);
		mController.setZoom(15);
	}
	
	@Override
	protected boolean isRouteDisplayed() {
		// Auto-generated method stub
		return false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// menu.add(0,100,0,"Zoom In");
		// menu.add(0,101,0,"Zoom Out");
		menu.add(0, 102, 0, "Satellite");
		// menu.add(0,103,0,"Trafic");
		// menu.add(0,104,0,"Street view");
		menu.add(0, 105, 0, "Exit").setIcon(
				android.R.drawable.ic_menu_close_clear_cancel);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 100:
			mController.setZoom(mMap.getZoomLevel() + 1);
			break;
		case 101:
			mController.setZoom(mMap.getZoomLevel() - 1);
			break;
		case 102:
			mMap.setSatellite(!mMap.isSatellite());
			break;
		// case 103: maMap.setTraffic(!maMap.isTraffic()) ;break;
		// case 104: maMap.setStreetView(!maMap.isStreetView()) ;break;
		case 105:
			finish();

		}
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(102).setIcon(
				mMap.isSatellite() ? android.R.drawable.checkbox_on_background
						: android.R.drawable.checkbox_off_background);
		return true;
	}

	public void onLocationChanged(Location arg0) {
		// TODO Auto-generated method stub

	}

	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub

	}

	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub

	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}
}