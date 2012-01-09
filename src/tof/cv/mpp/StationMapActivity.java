package tof.cv.mpp;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import tof.cv.mpp.Utils.FixedMyLocationOverlay;
import tof.cv.mpp.Utils.MyOverLay;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
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
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class StationMapActivity extends MapActivity implements LocationListener {

	private static final String TAG = "BETRAINS";
	/** Called when the activity is first created. */

	private int stationsId = 0;
	private SharedPreferences pref;
	private MapView mMap;
	private MapController mController;
	private FixedMyLocationOverlay myLocationOverlay;
	private Drawable marker;
	private ItemizedOverlayPerso stationsOverlay;
	private String name;
	private GeoPoint gpStation;
	private double geoLatitude;
	private double geoLongitude;
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
		
		String lat = "";
		String lon = "";
		
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			lat = extras.getString("lat");
			lon = extras.getString("lon");
			name = extras.getString("nom");
			Log.i(TAG,"Station position: " + lat + ", " + lon);

		} else
			Toast.makeText(this, "Error while getting train position",
					Toast.LENGTH_LONG).show();

		// centrer la carte sur la gare de Charleroi-Sud
		double glat = Double.parseDouble(lat);
		double glon = Double.parseDouble(lon);
		gpStation = new GeoPoint((int) (glat * 1E6), (int) (glon * 1E6));

		marker = getResources().getDrawable(R.drawable.ic_station_pixelart);
		stationsOverlay = new ItemizedOverlayPerso(marker);
		stationsOverlay.addPoint(gpStation);
		mMap.getOverlays().add(stationsOverlay);

		mController.setCenter(gpStation);
		mController.setZoom(15);

		// Gets the extras from the launching intent to know what logo to show
		// on the map ...
		// will then use a SWITCH case of food_ID to select the right icon ...
		if (this.getIntent().getExtras() != null) {
			setStationsId(this.getIntent().getExtras().getInt("GARE_ID"));
		}

		// adding me= MyLocation(GPS) and also a compass ...
		
		myLocationOverlay = new FixedMyLocationOverlay(this, mMap);
		myLocationOverlay.enableMyLocation();
		myLocationOverlay.enableCompass();
		mMap.getOverlays().add(myLocationOverlay);

		// when coming from the List_of_places/Long Click on a specific place :
		// zoom at a lower level ...
		// setcenter on this exact place ...

		// KEEPS TRACK of how many requests on QUICK, MCDO, PIZZAHUT, ETC...
		// reading the Preferences

		// over-writing a Pref ... using the Editor
		// casting String to int
		// increasing the counter by 1
		// casting int back to to String
		// TODO
		/*
		 * c = myDbHelper.getPlaces_of_Brand("EXKi"); startManagingCursor(c);
		 * c.moveToPosition(1);
		 */
		// nom=nom.replace(this.getString(R.string.Depart),"");
		stationDetailDialog(name, 0);

	}

	@Override
	protected void onResume() {
		super.onResume();

		myLocationOverlay.enableMyLocation();
		myLocationOverlay.enableCompass();
		Log.i("Profete162", "enable compass");
	}

	@Override
	protected void onPause() {
		
		super.onPause();		
		myLocationOverlay.disableCompass();
		myLocationOverlay.disableMyLocation();
		Log.i("Profete162", "compass" + myLocationOverlay.isMyLocationEnabled());

	}

	/*
	 * maybe consider this class to be deleted and using the PersonalItemizedOverlay defined in Collections
	 */
	public class ItemizedOverlayPerso extends ItemizedOverlay<OverlayItem> {

		private List<GeoPoint> points = new ArrayList<GeoPoint>();

		public ItemizedOverlayPerso(Drawable defaultMarker) {
			super(boundCenterBottom(defaultMarker));
		}

		@Override
		protected OverlayItem createItem(int i) {
			GeoPoint point = points.get(i);
			return new OverlayItem(point, "Title", "Description");
		}

		@Override
		public int size() {
			return points.size();
		}

		public void addPoint(GeoPoint point) {
			this.points.add(point);
			populate();
		}

		public void clearPoint() {
			this.points.clear();
			populate();
		}

		//
		// method for events when the user clicks on any marker ...
		//
		@Override
		protected boolean onTap(int index) {
			// Log.i("Hub",
			// "Tap registered on ItemizedOverlay on ITEM #"+index);
			// animate to the point that has been tapped on the map ...
			stationDetailDialog(name, index);
			return true;
		}
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

							mController.zoomToSpan((int) spanLat,
									(int) spanLon);
							mMap.invalidate();
						} else
							Toast.makeText(
									StationMapActivity.this,
									"Waiting GPS fix\nActivate network localisation in settings or enable you GPS.",
									Toast.LENGTH_LONG).show();
						
					}
				});		
		ad.show();
	}

	private void DrawPath(GeoPoint src, GeoPoint dest, int color,
			MapView mMapView01) {
		// connect to map web service
		StringBuilder urlString = new StringBuilder();
		urlString.append("http://maps.google.com/maps?f=d&hl=en");
		urlString.append("&saddr=");// from
		urlString.append(Double.toString((double) src.getLatitudeE6() / 1.0E6));
		urlString.append(",");
		urlString
				.append(Double.toString((double) src.getLongitudeE6() / 1.0E6));
		urlString.append("&daddr=");// to
		urlString
				.append(Double.toString((double) dest.getLatitudeE6() / 1.0E6));
		urlString.append(",");
		urlString
				.append(Double.toString((double) dest.getLongitudeE6() / 1.0E6));
		urlString.append("&ie=UTF8&0&om=0&output=kml");
		// Log.d("xxx","URL="+urlString.toString());
		// get the kml (XML) doc. And parse it to get the coordinates(direction
		// route).
		Document doc = null;
		HttpURLConnection urlConnection = null;
		URL url = null;
		try {
			url = new URL(urlString.toString());
			urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestMethod("GET");
			urlConnection.setDoOutput(true);
			urlConnection.setDoInput(true);
			urlConnection.connect();

			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			doc = db.parse(urlConnection.getInputStream());

			if (doc.getElementsByTagName("GeometryCollection").getLength() > 0) {
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
						.item(0).getFirstChild().getFirstChild()
						.getFirstChild().getNodeValue();
				// //Log.d("xxx","path="+ path);
				String[] pairs = path.split(" ");
				String[] lngLat = pairs[0].split(","); // lngLat[0]=longitude
														// lngLat[1]=latitude
														// lngLat[2]=height
				// src
				GeoPoint startGP = new GeoPoint(
						(int) (Double.parseDouble(lngLat[1]) * 1E6),
						(int) (Double.parseDouble(lngLat[0]) * 1E6));
				mMapView01.getOverlays()
						.add(new MyOverLay(startGP, startGP, 1));
				GeoPoint gp1;
				GeoPoint gp2 = startGP;
				for (int i = 1; i < pairs.length; i++) // the last one would be
														// crash
				{
					lngLat = pairs[i].split(",");
					gp1 = gp2;
					// watch out! For GeoPoint, first:latitude, second:longitude
					gp2 = new GeoPoint(
							(int) (Double.parseDouble(lngLat[1]) * 1E6),
							(int) (Double.parseDouble(lngLat[0]) * 1E6));
					mMapView01.getOverlays().add(
							new MyOverLay(gp1, gp2, 2, color));
					// //Log.d("xxx","pair:" + pairs[i]);
				}
				mMapView01.getOverlays().add(new MyOverLay(dest, dest, color)); // use
																				// the
																				// default
																				// color
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}
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

	public void setStationsId(int stationsId) {
		this.stationsId = stationsId;
	}

	public int getStationsId() {
		return stationsId;
	}

	public void setPreferences(SharedPreferences pref) {
		this.pref = pref;
	}

	public SharedPreferences getPreferences() {
		return pref;
	}
}