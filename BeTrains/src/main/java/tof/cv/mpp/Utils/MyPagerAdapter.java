package tof.cv.mpp.Utils;

import android.content.Context;
import android.os.Parcelable;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.view.LayoutInflater;
import android.view.View;

import tof.cv.mpp.R;

/**
 * Created by CVE on 31/01/14.
 */
public class MyPagerAdapter extends PagerAdapter{

    String[] titles;

    public MyPagerAdapter(String[] titles){
        this.titles=titles;
    }

    @Override
    public int getCount() {
        return titles.length;
    }
    public Object instantiateItem(View collection, int position) {
        LayoutInflater inflater = (LayoutInflater) collection.getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        int resId = 0;
        switch (position) {
            case 0:
                resId = R.layout.page_checkin;
                break;
            case 1:
                resId = R.layout.page_highscore;
                break;
            case 2:
                resId = R.layout.page_achievement;
                break;
        }
        View view = inflater.inflate(resId, null);
        ((ViewPager) collection).addView(view, 0);
        return view;
    }
    @Override
    public void destroyItem(View arg0, int arg1, Object arg2) {
        ((ViewPager) arg0).removeView((View) arg2);
    }
    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        return arg0 == ((View) arg1);
    }
    @Override
    public Parcelable saveState() {
        return null;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titles[position];
    }

}
