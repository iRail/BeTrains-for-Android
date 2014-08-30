package tof.cv.mpp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;


public class WelcomeActivity extends FragmentActivity {

    private Fragment mContent;
    int value = -1;
    public DrawerLayout mDrawerLayout;
    private MenuFragment mDrawerList;
    int open;
    String close;
    /**
     * Called when the activity is first created.
     */
    SharedPreferences settings;
    ActionBarDrawerToggle mDrawerToggle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        setContentView(R.layout.responsive_content_frame);
        setProgressBarIndeterminateVisibility(false);

        settings = PreferenceManager.getDefaultSharedPreferences(this);

        // check if the content frame contains the menu frame
        if (findViewById(R.id.menu_frame) == null) ;
        //TODO

        open = R.string.app_name;


        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer icon to replace 'Up' caret */
                open,  /* "open drawer" description */
                open /* "close drawer" description */
        ) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(close);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(open);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                if (PreferenceManager.getDefaultSharedPreferences(WelcomeActivity.this).getBoolean("tuto", true))
                    try {
                        mContent.getView().findViewById(R.id.tuto).setVisibility(View.GONE);
                        PreferenceManager.getDefaultSharedPreferences(WelcomeActivity.this).edit().putBoolean("tuto", false).commit();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
            }
        };

        if (mDrawerLayout != null){
            mDrawerLayout.setDrawerListener(mDrawerToggle);
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setHomeButtonEnabled(true);
        }



        // set the Above View Fragment
        if (savedInstanceState != null)
            mContent = getSupportFragmentManager().getFragment(
                    savedInstanceState, "mContent");

        if (mContent == null) {
          String selected = settings.getString(
              getString(R.string.key_activity), "menu_pref_route_planner");

          // Legacy - To be removed in 2015?
          if (selected.matches("\\d+")) {
            if (selected.equals("1")) selected = "menu_pref_route_planner";
            if (selected.equals("2")) selected = "menu_pref_traffic_issues";
            if (selected.equals("3")) selected = "menu_pref_chat";
            if (selected.equals("4")) selected = "menu_pref_starred";
            if (selected.equals("5")) selected = "menu_pref_closest_stations";
            if (selected.equals("6")) selected = "menu_pref_game";
            if (selected.matches("\\d+")) selected = "menu_pref_route_planner";
          } // -- end Legacy

          if ("menu_pref_route_planner".equals(selected))
            mContent = new PlannerFragment();
          if ("menu_pref_traffic_issues".equals(selected))
            mContent = new TrafficFragment();
          if ("menu_pref_chat".equals(selected))
            mContent = new ChatFragment();
          if ("menu_pref_starred".equals(selected))
            mContent = new StarredFragment();
          if ("menu_pref_closest_stations".equals(selected))
            mContent = new ClosestFragment();
          if ("menu_pref_game".equals(selected))
            mContent = new GameFragment();
          if (mContent == null) {
            mContent = new PlannerFragment();
            selected = "menu_pref_route_planner";
          }

          close = getString(getResources().getIdentifier(
                selected, "string", WelcomeActivity.this.getPackageName()));

          getSupportFragmentManager().beginTransaction()
            .replace(R.id.content_frame, mContent).commit();

        }

        mDrawerList = new MenuFragment();
        // set the Behind View Fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.menu_frame, mDrawerList).commit();

    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        if (mDrawerLayout != null) {
            boolean drawerOpen = mDrawerLayout.isDrawerOpen(Gravity.LEFT);
            for (int i = 0; i < menu.size(); i++)
                menu.getItem(i).setVisible(!drawerOpen);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        switch (item.getItemId()) {
            case android.R.id.home:
                if (!this.getResources().getBoolean(R.bool.tablet_layout))
                    mDrawerLayout.closeDrawers();
                //TODO toggle();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        try {
            getSupportFragmentManager().putFragment(outState, "mContent", mContent);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void switchContent(final Fragment fragment, int position) {
        mContent = fragment;

        close = getResources().getStringArray(R.array.menu)[position];

        Fragment f = (Fragment) getSupportFragmentManager().findFragmentById(
                R.id.content_frame);

        if (f != null) { //TODO && !(String)fragment.getClass().equals(f.getClass())) {
            mContent = fragment;
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.content_frame, fragment)
                    .commit();

            // Highlight the selected item, update the title, and close the drawer
            mDrawerList.getListView().setItemChecked(position, true);


        }
        if (mDrawerLayout != null)
            mDrawerLayout.closeDrawers();
    }

    public void onPlusClick(View v) {
        String url = "https://plus.google.com/b/108315424589085456181/108315424589085456181/posts";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    public void onMailClick(View v) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("plain/text");
        intent.putExtra(Intent.EXTRA_EMAIL, "christophe.versieux@gmail.com");
        intent.putExtra(Intent.EXTRA_SUBJECT, "BeTrains Android");
        startActivity(Intent.createChooser(intent, "Mail"));
    }

    public void oniRailClick(View v) {
        Intent marketLaunch = new Intent(Intent.ACTION_VIEW);
        marketLaunch.setData(Uri
                .parse("market://details?id=be.irail.liveboards"));
        startActivity(marketLaunch);
    }

    public void onGuiardClick(View v) {
        Intent marketLaunch = new Intent(Intent.ACTION_VIEW);
        marketLaunch.setData(Uri.parse("http://sph1re.fr/"));
        startActivity(marketLaunch);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.

        try {
            mDrawerToggle.syncState();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        try {
            mDrawerToggle.onConfigurationChanged(newConfig);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
