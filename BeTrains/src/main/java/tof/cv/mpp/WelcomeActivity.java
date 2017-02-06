package tof.cv.mpp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.database.Cursor;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import java.util.Arrays;

import tof.cv.mpp.Utils.DbAdapterConnection;

public class WelcomeActivity extends AppCompatActivity {

    private Fragment mContent;
    public DrawerLayout drawerLayout = null;
    NavigationView navigationView;
    String close;
    /**
     * Called when the activity is first created.
     */
    SharedPreferences settings;
    ActionBarDrawerToggle mDrawerToggle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            int num=0;
            DbAdapterConnection mDbHelper = new DbAdapterConnection(this);
            ShortcutManager shortcutManager = getSystemService(ShortcutManager.class);
            shortcutManager.removeAllDynamicShortcuts();
            mDbHelper.open();
            Cursor mCursor = mDbHelper.fetchAllFav();
            try {
                while (mCursor.moveToNext() && num<=4) {
                    String item = mCursor.getString(mCursor
                            .getColumnIndex(DbAdapterConnection.KEY_FAV_NAME));
                    String itemTwo = mCursor.getString(mCursor
                            .getColumnIndex(DbAdapterConnection.KEY_FAV_NAMETWO));
                    int type = mCursor.getInt(mCursor
                            .getColumnIndex(DbAdapterConnection.KEY_FAV_TYPE));
                    ShortcutInfo shortcut = null;
                    Intent i;
                    switch (type) {
                        case 1:
                            i = new Intent(this, InfoStationActivity.class);
                            i.putExtra("Name", item);
                            i.putExtra("ID", itemTwo);

                            try {
                                shortcut = new ShortcutInfo.Builder(this, itemTwo==null?item:itemTwo)
                                        .setShortLabel(item)
                                        .setLongLabel(item + " - " + itemTwo)
                                        .setIcon(Icon.createWithResource(this, R.drawable.ic_fav_station))
                                        .setIntent(i.setAction(""))
                                        .build();
                            } catch (Exception e) {
                                //TODO Why is ID null here? To investigate
                                e.printStackTrace();
                            }
                            break;
                        case 2:
                            i = new Intent(this, InfoTrainActivity.class);
                            i.putExtra("Name", item);

                            shortcut = new ShortcutInfo.Builder(this, item)
                                    .setShortLabel(item)
                                    .setLongLabel(item)
                                    .setIcon(Icon.createWithResource(this, R.drawable.ic_fav_train))
                                    .setIntent(i.setAction(""))
                                    .build();
                            break;
                        case 3:
                            i = new Intent(this, WelcomeActivity.class);
                            i.putExtra("Departure", item);
                            i.putExtra("Arrival", itemTwo);

                            shortcut = new ShortcutInfo.Builder(this, item + " - " + itemTwo)
                                    .setShortLabel(item + " - " + itemTwo)
                                    .setLongLabel(item + " - " + itemTwo)
                                    .setIcon(Icon.createWithResource(this, R.drawable.ic_fav_map))
                                    //.setIntent(i)
                                    .setIntent(i.setAction(""))
                                    .build();
                            break;
                    }

                    if (shortcut != null)
                        shortcutManager.addDynamicShortcuts(Arrays.asList(shortcut));

                    num++;
                }
            } finally {
                mCursor.close();
            }


            mDbHelper.close();
        }


        setContentView(R.layout.responsive_content_frame);
        setProgressBarIndeterminateVisibility(false);

        setSupportActionBar((Toolbar) findViewById(R.id.my_awesome_toolbar));

        try {//Just in case setStatusBarColor not available
            getWindow().setStatusBarColor(getResources().getColor(R.color.primarycolortransparent));
        } catch (Error e) {
            e.printStackTrace();
        }

        settings = PreferenceManager.getDefaultSharedPreferences(this);

        navigationView = (NavigationView) findViewById(R.id.navigation);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);

        navigationView.getMenu().clear();

        if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(WelcomeActivity.this) == ConnectionResult.SUCCESS)
            navigationView.inflateMenu(R.menu.nav);
        else
            navigationView.inflateMenu(R.menu.nav_nogps);

        if (drawerLayout != null) {

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            mDrawerToggle = new ActionBarDrawerToggle(
                    this,
                    drawerLayout,
                    (Toolbar) findViewById(R.id.my_awesome_toolbar),
                    R.string.app_name, R.string.app_name) {
                @Override
                public void onDrawerOpened(View drawerView) {
                    super.onDrawerOpened(drawerView);
                    // code here will execute once the drawer is opened

                    invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                }

                @Override
                public void onDrawerClosed(View drawerView) {
                    super.onDrawerClosed(drawerView);
                    // Code here will execute once drawer is closed
                    invalidateOptionsMenu();
                }

                ;
            };

            mDrawerToggle.syncState();

            drawerLayout.setDrawerListener(new DrawerLayout.DrawerListener() {
                @Override
                public void onDrawerSlide(View drawerView, float slideOffset) {
                }

                @Override
                public void onDrawerOpened(View drawerView) {
                    PreferenceManager.getDefaultSharedPreferences(WelcomeActivity.this).edit().putBoolean("navigation_drawer_learned", true).apply();
                    if (mContent instanceof PlannerFragment)
                        findViewById(R.id.tuto).setVisibility(View.GONE);
                }

                @Override
                public void onDrawerClosed(View drawerView) {
                }

                @Override
                public void onDrawerStateChanged(int newState) {
                }
            });
        }

        //Setting Navigation View Item Selected Listener to handle the item click of the navigation menu
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

                // This method will trigger on item Click of navigation menu
                @Override
                public boolean onNavigationItemSelected(final MenuItem menuItem) {

                    //menuItem.setChecked(true);

                    if (drawerLayout != null)
                        drawerLayout.closeDrawers();

                    switch (menuItem.getItemId()) {
                        case R.id.navigation_item_plan:
                            mContent = new PlannerFragment();
                            break;
                        /*case R.id.navigation_item_notif:
                            mContent = new NotifFragment();
                            break;*/
                        case R.id.navigation_item_iss:
                            mContent = new TrafficFragment();
                            break;
                        case R.id.navigation_item_chat:
                            mContent = new ChatFragment();
                            break;
                        case R.id.navigation_item_star:
                            mContent = new StarredFragment();
                            break;
                        case R.id.navigation_item_closest:
                            mContent = new ClosestFragment();
                            break;
                        case R.id.navigation_item_game:
                            mContent = new GameFragment();
                            break;
                        case R.id.navigation_item_comp:
                            mContent = new CompensationFragment();
                            break;
                        case R.id.navigation_item_extras:
                            mContent = new ExtraFragment();
                            break;
                        default:
                            mContent = new PlannerFragment();
                            close = getString(R.string.activity_label_planner);
                            break;
                    }

                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.content_frame, mContent).commit();

                   /*
                    // Log.e("CVE","Hum " + navigationView.hasFocus());
                    Log.e("CVE","pos " + menuItem.getItemId());
                    // This is one of the dirtiest hack I made recently. I am not proud of it. Shame
                    // But in an other hand, the Navigation Drawer should not lose focus on tablets
                    // Or it should keep it if I call requestFocus() without delay...
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            navigationView.clearFocus();
                            navigationView.requestFocus();
                        }
                    }, 500);*/

                    return true;
                }
            });
        }

// Set the default screen at startup from Preferences
        int pos = Integer.valueOf(settings.getString(
                getString(R.string.key_activity), "1"));

        if (getIntent().hasExtra("Departure") && getIntent().hasExtra("Arrival"))
            pos = 1;

        switch (pos) {
            case 1:
                mContent = new PlannerFragment();
                break;
            case 2:
                mContent = new TrafficFragment();
                break;
            case 3:
                mContent = new ChatFragment();
                break;
            case 4:
                mContent = new StarredFragment();
                break;
            case 5:
                mContent = new ClosestFragment();
                break;
            case 6:
                mContent = new GameFragment();
                break;
            default:
                mContent = new PlannerFragment();
                close = getString(R.string.activity_label_planner);
                break;
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_frame, mContent).commit();

        SystemBarTintManager tintManager = new SystemBarTintManager(this);
        //tintManager.setStatusBarTintEnabled(true);
        tintManager.setNavigationBarTintEnabled(true);
        tintManager.setTintResource(R.color.primarycolor);


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
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

    public void onPlusClick(View v) {
        String url = "https://plus.google.com/b/108315424589085456181/108315424589085456181/posts";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    public void onMailClick(View v) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("plain/text");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"christophe.versieux@gmail.com"});
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

    public void onCookClick(View v) {
        Intent marketLaunch = new Intent(Intent.ACTION_VIEW);
        marketLaunch.setData(Uri.parse("http://cookicons.co/"));
        startActivity(marketLaunch);
    }
}
