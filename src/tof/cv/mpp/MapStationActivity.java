package tof.cv.mpp;

import java.util.List;

import org.w3c.dom.Document;

import tof.cv.mpp.Utils.UtilsWeb;
import tof.cv.mpp.map.FixedMyLocationOverlay;
import tof.cv.mpp.map.ItemizedOverlayStation;
import tof.cv.mpp.map.MyOverLay;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class MapStationActivity extends MapActivity implements LocationListener {

	// PSOTIT: Pour le train:
	// http://railtime.be/website/apercu-du-trafic-trains?tn=3835

	private MapView mMap;
	private MapController mController;
	private FixedMyLocationOverlay myLocationOverlay;
	private Drawable marker;
	private ItemizedOverlayStation stationsOverlay;
	private String name;
	private GeoPoint gpStation;
	private GeoPoint gpMyLocation;

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
		if (extras != null) {
			name = extras.getString("Name");
		} else
			Toast.makeText(this, "Error while getting train position",
					Toast.LENGTH_LONG).show();

		double glat = 0;
		double glon = 0;

		gpStation = new GeoPoint((int) (glat * 1E6), (int) (glon * 1E6));

		marker = getResources().getDrawable(R.drawable.ic_station_pixelart);
		stationsOverlay = new ItemizedOverlayStation(marker,name,this);
		stationsOverlay.addPoint(gpStation);
		mMap.getOverlays().add(stationsOverlay);

		mController.setCenter(gpStation);
		mController.setZoom(15);

		// adding me= MyLocation(GPS) and also a compass ...

		myLocationOverlay = new FixedMyLocationOverlay(this, mMap);
		myLocationOverlay.enableMyLocation();
		myLocationOverlay.enableCompass();
		mMap.getOverlays().add(myLocationOverlay);

		stationDetailDialog(name, 0);

	}

	@Override
	protected void onResume() {
		super.onResume();
		myLocationOverlay.enableMyLocation();
		myLocationOverlay.enableCompass();
	}

	@Override
	protected void onPause() {
		super.onPause();
		myLocationOverlay.disableCompass();
		myLocationOverlay.disableMyLocation();
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

	public void stationDetailDialog(String nom, int index) {

		AlertDialog.Builder ad = new AlertDialog.Builder(this);
		ad.setTitle(nom);
		ad.setNeutralButton(android.R.string.ok,
				new android.content.DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int arg1) {
						mController.animateTo(gpStation);
					}
				});

		ad.setPositiveButton("Go there",
				new android.content.DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int arg1) {

						gpMyLocation = myLocationOverlay.getMyLocation();

						if (gpMyLocation != null) {
							double lat1 = gpStation.getLatitudeE6();
							double lat2 = gpMyLocation.getLatitudeE6();
							double lon1 = gpStation.getLongitudeE6();
							double lon2 = gpMyLocation.getLongitudeE6();
							GeoPoint gpMiddle = new GeoPoint(
									(int) ((lat1 + lat2) / 2.0),
									(int) ((lon1 + lon2) / 2.0));

							DrawPath(gpMyLocation, gpStation, Color.BLUE, mMap);
							mController.setCenter(gpMiddle);

							double spanLat;
							double spanLon;

							if (lat1 > lat2)
								spanLat = (lat1 - lat2);
							else
								spanLat = (lat2 - lat1);

							if (lon1 > lon2)
								spanLon = (lon1 - lon2);
							else
								spanLon = (lon2 - lon1);

							mController
									.zoomToSpan((int) spanLat, (int) spanLon);
							mMap.invalidate();
						} else
							Toast.makeText(
									MapStationActivity.this,
									"Waiting GPS fix\nActivate network localisation in settings or enable you GPS.",
									Toast.LENGTH_LONG).show();

					}
				});
		ad.show();
	}

	private void DrawPath(GeoPoint src, GeoPoint dest, int color,
			MapView mMapView01) {

		Document doc = UtilsWeb.getKml(src, dest);

		if (doc != null
				&& doc.getElementsByTagName("GeometryCollection").getLength() > 0) {
			List<Overlay> maliste = mMapView01.getOverlays();
			int pos = maliste.size();
			while (pos > 2) {
				// Codes.mondebug(""+maliste.get(pos-1).getClass());
				mMapView01.getOverlays().remove(pos - 1);

				pos--;
			}
			// String path =
			// doc.getElementsByTagName("GeometryCollection").item(0).getFirstChild().getFirstChild().getNodeName();
			String path = doc.getElementsByTagName("GeometryCollection")
					.item(0).getFirstChild().getFirstChild().getFirstChild()
					.getNodeValue();
			// //Log.d("xxx","path="+ path);
			String[] pairs = path.split(" ");
			String[] lngLat = pairs[0].split(","); // lngLat[0]=longitude
													// lngLat[1]=latitude
													// lngLat[2]=height
			// src
			GeoPoint startGP = new GeoPoint(
					(int) (Double.parseDouble(lngLat[1]) * 1E6),
					(int) (Double.parseDouble(lngLat[0]) * 1E6));
			mMapView01.getOverlays().add(new MyOverLay(startGP, startGP, 1));
			GeoPoint gp1;
			GeoPoint gp2 = startGP;
			Log.i("", "" + pairs.length);
			for (int i = 1; i < pairs.length; i++) // the last one would be
													// crash
			{
				lngLat = pairs[i].split(",");
				gp1 = gp2;
				// watch out! For GeoPoint, first:latitude, second:longitude
				gp2 = new GeoPoint((int) (Double.parseDouble(lngLat[1]) * 1E6),
						(int) (Double.parseDouble(lngLat[0]) * 1E6));
				mMapView01.getOverlays().add(new MyOverLay(gp1, gp2, 2, color));
				// //Log.d("xxx","pair:" + pairs[i]);
			}
			mMapView01.getOverlays().add(new MyOverLay(dest, dest, color)); // use
																			// //
																			// color
		} else
			Toast.makeText(getBaseContext(), R.string.txt_error,
					Toast.LENGTH_LONG).show();
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