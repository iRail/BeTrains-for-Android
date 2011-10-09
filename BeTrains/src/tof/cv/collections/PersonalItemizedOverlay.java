package tof.cv.collections;

import java.util.ArrayList;
import java.util.List;

import android.graphics.drawable.Drawable;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

public class PersonalItemizedOverlay extends ItemizedOverlay<OverlayItem> {

	private List<GeoPoint> points = new ArrayList<GeoPoint>();

	public PersonalItemizedOverlay(Drawable defaultMarker) {
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
	
	@Override
	protected boolean onTap(int index) {
		// Log.i("Hub",
		// "Tap registered on ItemizedOverlay on ITEM #"+index);
		// animate to the point that has been tapped on the map ...

		// rain_Detail_Dialog();
		return true;
	}
}
