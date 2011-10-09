package tof.cv.ui;

import tof.cv.collections.PersonalItemizedOverlay;
import tof.cv.mpp.R;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;

public class TrainMapActivity extends MapActivity implements LocationListener {

	/** Called when the activity is first created. */

	private MapView mMap;
	private MapController mMapController;
	private Drawable marker;

	private double geoLatitude;
	private double geoLongitude;

	private GeoPoint mGeoPoint;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.my_map);

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			String lat = extras.getString("lat");
			String lon = extras.getString("lon");
			System.out.println("Train position: " + geoLatitude + ", "
					+ geoLongitude);
			geoLatitude = Double.parseDouble(lat);
			geoLongitude = Double.parseDouble(lon);

		} else
			Toast.makeText(this, "Error while getting train position",
					Toast.LENGTH_LONG).show();

		mMap = (MapView) findViewById(R.id.myGmap);
		mMap.setBuiltInZoomControls(true);
		mMap.setSatellite(false);
		setmMapController(mMap.getController());

		GeoPoint gp = new GeoPoint((int) (geoLatitude * 1E6),
				(int) (geoLongitude * 1E6));

		marker = getResources().getDrawable(R.drawable.train);
		PersonalItemizedOverlay myOverlay = new PersonalItemizedOverlay(marker);
		myOverlay.addPoint(gp);
		mMap.getOverlays().add(myOverlay);

		/*
		 * MapController mController = mMap.getController();
		 * mController.setCenter(gp); mController.setZoom(15);
		 */
		mMap.getController().setCenter(gp);
		mMap.getController().setZoom(15);

	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

	public void Train_Detail_Dialog() {

	}

	public void onResume() {
		super.onResume();

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
		// case 100: monControler.setZoom(maMap.getZoomLevel() + 1) ;break;
		// case 101: monControler.setZoom(maMap.getZoomLevel() - 1) ;break;
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

	public void onLocationChanged(Location location) {
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

	public void setmMapController(MapController mMapController) {
		this.mMapController = mMapController;
	}

	public MapController getmMapController() {
		return mMapController;
	}

	public void setmGeoPoint(GeoPoint mGeoPoint) {
		this.mGeoPoint = mGeoPoint;
	}

	public GeoPoint getmGeoPoint() {
		return mGeoPoint;
	}

}