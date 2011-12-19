package tof.cv.mpp.adapter;

import java.util.ArrayList;

import tof.cv.mpp.R;
import android.app.Activity;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

public class MenuAdapter extends PagerAdapter{

	ArrayList<View> views;
	
	public MenuAdapter(Activity context){
		views = new ArrayList<View>();
		views.add(context.getLayoutInflater().inflate(R.layout.menu_1, null));
		views.add(context.getLayoutInflater().inflate(R.layout.menu_2, null));
	}
	
	@Override
	public int getCount() {
		return views.size();
	}

	@Override
	public Object instantiateItem(View container, int position) {
		View view=views.get(position);
		((ViewPager) container).addView(view);
		return view;
	}

	@Override
	public void destroyItem(View container, int position, Object object) {
		((ViewPager) container).removeView((View)object);
		
	}

	@Override
	public void finishUpdate(View container) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view == object;
	}

	@Override
	public void restoreState(Parcelable state, ClassLoader loader) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Parcelable saveState() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void startUpdate(View container) {
		// TODO Auto-generated method stub
		
	}

}
