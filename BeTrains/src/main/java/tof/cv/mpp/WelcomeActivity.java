package tof.cv.mpp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
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
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.material.navigation.NavigationView;
import com.teragence.client.SdkControls;

import java.util.Arrays;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import tof.cv.mpp.Utils.DbAdapterConnection;
import tof.cv.mpp.view.LetterTileProvider;

//import com.teragence.client.SdkControls;

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

        setupShortcuts();
        SdkControls.initialize(this);

        setContentView(R.layout.responsive_content_frame);
        setProgressBarIndeterminateVisibility(false);

        // setSupportActionBar((BottomAppBar)findViewById(R.id.bar));
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));


        settings = PreferenceManager.getDefaultSharedPreferences(this);

        //settings.edit().putBoolean("beta",true).apply();

        navigationView = findViewById(R.id.navigation);
        navigationView.getMenu().clear();

        drawerLayout =(DrawerLayout)  findViewById(R.id.drawer);

        if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(WelcomeActivity.this) == ConnectionResult.SUCCESS)
            navigationView.inflateMenu(R.menu.nav);
        else
            navigationView.inflateMenu(R.menu.nav_nogps);

        if (drawerLayout != null)
            setupDrawer();


        if (navigationView != null)
            setupNavigation();

        int pos = Integer.valueOf(settings.getString(
                getString(R.string.key_activity), "1"));

        if (getIntent().hasExtra("Departure") && getIntent().hasExtra("Arrival"))
            pos = 1;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("TRAIN_WATCH", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

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

    }

    private void setupNavigation() {
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            // This method will trigger on item Click of navigation menu
            @Override
            public boolean onNavigationItemSelected(final MenuItem menuItem) {
                if (drawerLayout != null)
                    drawerLayout.closeDrawers();

                switch (menuItem.getItemId()) {
                    case R.id.navigation_item_plan:
                        mContent = new PlannerFragment();
                        break;
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
                        .replace(R.id.content_frame, mContent).addToBackStack("").commit();

                return true;
            }
        });
    }

    private void setupDrawer() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                (Toolbar) findViewById(R.id.my_awesome_toolbar),
                R.string.app_name, R.string.app_name) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
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
            }

            @Override
            public void onDrawerClosed(View drawerView) {
            }

            @Override
            public void onDrawerStateChanged(int newState) {
            }
        });
    }

    private void setupShortcuts() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            int num = 0;
            DbAdapterConnection mDbHelper = new DbAdapterConnection(this);
            ShortcutManager shortcutManager = getSystemService(ShortcutManager.class);
            shortcutManager.removeAllDynamicShortcuts();
            mDbHelper.open();
            Cursor mCursor = mDbHelper.fetchAllFav();
            try {
                while (mCursor.moveToNext() && num <= 4) {
                    String item = mCursor.getString(mCursor
                            .getColumnIndex(DbAdapterConnection.KEY_FAV_NAME));
                    String itemTwo = mCursor.getString(mCursor
                            .getColumnIndex(DbAdapterConnection.KEY_FAV_NAMETWO));
                    int type = mCursor.getInt(mCursor
                            .getColumnIndex(DbAdapterConnection.KEY_FAV_TYPE));
                    ShortcutInfo shortcut = null;
                    Intent i;

                    int tileSize = getResources().getDimensionPixelSize(R.dimen.letter_tile_size);
                    ;
                    LetterTileProvider tileProvider = new LetterTileProvider(this);

                    switch (type) {
                        case 1:
                            i = new Intent(this, InfoStationActivity.class);
                            i.putExtra("Name", item);
                            i.putExtra("ID", itemTwo);

                            try {
                                shortcut = new ShortcutInfo.Builder(this, itemTwo == null ? item : itemTwo)
                                        .setShortLabel(item)
                                        .setLongLabel(item + " - " + itemTwo)
                                        .setIcon(
                                                (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) ?
                                                        Icon.createWithAdaptiveBitmap(tileProvider.getLetterTile(item, item, tileSize, tileSize))
                                                        : Icon.createWithBitmap(tileProvider.getLetterTile(item, item, tileSize, tileSize))
                                        )
                                        .setIntent(i.setAction(""))
                                        .build();
                            } catch (Exception e) {
                                //TODO Why is ID null here? To investigate
                                e.printStackTrace();
                            }
                            break;
                        case 2:
                            String numbers = item.replaceAll("\\D+", "");
                            i = new Intent(this, InfoTrainActivity.class);
                            i.putExtra("Name", item);

                            shortcut = new ShortcutInfo.Builder(this, item)
                                    .setShortLabel(item)
                                    .setLongLabel(item)
                                    .setIcon(
                                            (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) ?
                                                    Icon.createWithAdaptiveBitmap(tileProvider.getLetterTile(numbers, numbers, tileSize, tileSize))
                                                    : Icon.createWithBitmap(tileProvider.getLetterTile(numbers, numbers, tileSize, tileSize))
                                    )
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
                                    .setIcon(
                                            (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) ?
                                                    Icon.createWithAdaptiveBitmap(tileProvider.getLetterTile(item, item, tileSize, tileSize))
                                                    : Icon.createWithBitmap(tileProvider.getLetterTile(item, item, tileSize, tileSize))
                                    )
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
