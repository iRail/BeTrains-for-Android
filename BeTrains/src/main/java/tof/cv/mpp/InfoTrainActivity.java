package tof.cv.mpp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class InfoTrainActivity extends AppCompatActivity {

    ViewPager mViewPager;
    TabsAdapter mTabsAdapter;
    long timestamp;
    String fromTo;
    String name;
    AHBottomNavigation bottomNavigation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_info_train);
        setSupportActionBar((Toolbar) findViewById(R.id.my_awesome_toolbar));

        Bundle bundle = this.getIntent().getExtras();
        timestamp = bundle.getLong("timestamp") * 1000;
        name = bundle.getString("Name");//.replaceAll("[^0-9]+", "");
        Log.i("***", "bundle: " + bundle.getString("Name"));
        Log.i("***", "NAME: " + name);
        fromTo = bundle.getString("fromto");

        mViewPager = (ViewPager) findViewById(R.id.pager);
        if (mViewPager != null)
            mTabsAdapter = new TabsAdapter(this, mViewPager);
        InfoTrainFragment fragment = (InfoTrainFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
        String fileName = bundle.getString("FileName");
        //if (fileName != null)
        //    fragment.displayInfoFromMemory(fileName, name);
        //else
        if (fragment != null)
            fragment.displayInfo(name, fromTo, timestamp);


        bottomNavigation = (AHBottomNavigation) findViewById(R.id.bottom_navigation);

        final int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        if ((!(status == ConnectionResult.SUCCESS)) &&bottomNavigation != null) {
            //bottomNavigation.setVisibility(View.GONE);
        }

        if (bottomNavigation != null) {

            AHBottomNavigationItem item1 = new AHBottomNavigationItem(R.string.app_name, R.drawable.ic_nav_plan, R.color.primarycolor);
            item1.setTitle(name);

            AHBottomNavigationItem item2 = new AHBottomNavigationItem(R.string.activity_label_chat, R.drawable.ic_nav_chat, R.color.primarycolor);

            bottomNavigation.addItem(item1);
            bottomNavigation.addItem(item2);

            // bottomNavigation.setDefaultBackgroundColor(Color.parseColor("#FEFEFE"));

            // bottomNavigation.setAccentColor(Color.parseColor("#F63D2B"));
            // bottomNavigation.setInactiveColor(Color.parseColor("#747474"));

            bottomNavigation.setForceTint(true);

// Force the titles to be displayed (against Material Design guidelines!)
            //bottomNavigation.setForceTitlesDisplay(true);

            bottomNavigation.setColored(true);

            bottomNavigation.setNotificationBackgroundColor(Color.parseColor("#F63D2B"));

            //bottomNavigation.setNotification("4", 1);
            //bottomNavigation.setNotification("", 1);

            bottomNavigation.setOnTabSelectedListener(new AHBottomNavigation.OnTabSelectedListener() {
                @Override
                public boolean onTabSelected(int position, boolean wasSelected) {
                    mViewPager.setCurrentItem(position);
                    return true;
                }
            });
            bottomNavigation.setOnNavigationPositionListener(new AHBottomNavigation.OnNavigationPositionListener() {
                @Override
                public void onPositionChange(int y) {
                    // Manage the new y position
                }
            });

            if(mViewPager!=null)
                mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                    @Override
                    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                    }

                    @Override
                    public void onPageSelected(int position) {
                        bottomNavigation.setCurrentItem(position);
                    }

                    @Override
                    public void onPageScrollStateChanged(int state) {

                    }
                });
        }
    }

    public void setChatBadge(int i) {
        if (bottomNavigation != null)
            bottomNavigation.setNotification(""+i, 1);
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

    public class TabsAdapter extends FragmentPagerAdapter {
        private final AppCompatActivity mContext;
        private final ViewPager mViewPager;


        public TabsAdapter(AppCompatActivity activity, ViewPager pager) {
            super(activity.getSupportFragmentManager());
            mContext = activity;
            mViewPager = pager;
            mViewPager.setAdapter(this);
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    InfoTrainFragment fragment = new InfoTrainFragment();
                    fragment.setInfo(InfoTrainActivity.this.name, InfoTrainActivity.this.fromTo, InfoTrainActivity.this.timestamp);
                    return fragment;
                case 1:
                    ChatFragment chatFragment = new ChatFragment();
                    chatFragment.trainId = InfoTrainActivity.this.name;
                    return chatFragment;

            }
            return null;
        }

    }

}
