package map;

import java.util.ArrayList;
import java.util.List;

import tof.cv.mpp.MapStationActivity;
import android.graphics.drawable.Drawable;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

public class ItemizedOverlayStation extends ItemizedOverlay<OverlayItem> {
	private String name;
	private List<GeoPoint> points = new ArrayList<GeoPoint>();
	private MapStationActivity context;

	public ItemizedOverlayStation(Drawable defaultMarker,String name, MapStationActivity context) {
		super(boundCenterBottom(defaultMarker));
		this.name=name;
		this.context=context;
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
		context.stationDetailDialog(name, index);
		return true;
	}

}
