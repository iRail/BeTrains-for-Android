package tof.cv.mpp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class InfoTrainActivity extends AppCompatActivity {

    ViewPager mViewPager;
    TabsAdapter mTabsAdapter;
    long timestamp;
    String fromTo;
    String name;
    BottomNavigationView bottomNavigation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_info_train);
        setSupportActionBar((Toolbar) findViewById(R.id.my_awesome_toolbar));

        Bundle bundle = this.getIntent().getExtras();
        timestamp = bundle.getLong("timestamp") * 1000;
        Log.e("CVE","Time "+timestamp);
        name = bundle.getString("Name");//.replaceAll("[^0-9]+", "");
        //Log.i("***", "bundle: " + bundle.getString("Name"));
        //Log.i("***", "NAME: " + name);
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


        bottomNavigation =  findViewById(R.id.bottom_nav);

        final int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        if ((!(status == ConnectionResult.SUCCESS)) &&bottomNavigation != null) {
            //bottomNavigation.setVisibility(View.GONE);
        }
        setTitle(name.replace("BE.NMBS.",""));

        if (bottomNavigation != null) {

            bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    switch (item.getItemId()){
                        case R.id.nav_train:
                            mViewPager.setCurrentItem(0);
                            break;
                        case R.id.nav_chat:
                            mViewPager.setCurrentItem(1);
                            break;
                        case R.id.nav_notif:
                            mViewPager.setCurrentItem(2);
                            break;
                    }

                    return true;
                }
            });

           /* AHBottomNavigationItem item1 = new AHBottomNavigationItem(R.string.app_name, R.drawable.ic_nav_plan, R.color.primarycolor);
            item1.setTitle(name.replace("BE.NMBS.",""));
            AHBottomNavigationItem item2 = new AHBottomNavigationItem(R.string.activity_label_chat, R.drawable.ic_nav_chat, R.color.primarycolor);
            AHBottomNavigationItem item3 = new AHBottomNavigationItem(R.string.activity_label_notif, R.drawable.ic_nav_notif, R.color.primarycolor);

            bottomNavigation.addItem(item1);
            bottomNavigation.addItem(item2);
            bottomNavigation.addItem(item3);

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
                });*/
        }
    }

    public void setChatBadge(int i) {
        BadgeDrawable badge = bottomNavigation.getOrCreateBadge(R.id.nav_chat);
        badge.setVisible(true);
        badge.setNumber(i);
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
            return 3;
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

                case 2:
                    NotifFragment notifFragment = new NotifFragment();
                    notifFragment.trainId = InfoTrainActivity.this.name;
                    return notifFragment;

            }
            return null;
        }

    }

}
