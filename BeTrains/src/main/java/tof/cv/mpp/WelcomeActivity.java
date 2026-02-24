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
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.navigationrail.NavigationRailView;

import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import androidx.fragment.app.FragmentManager;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import tof.cv.mpp.Utils.DbAdapterConnection;
import tof.cv.mpp.view.LetterTileProvider;

//import com.teragence.client.SdkControls;

public class WelcomeActivity extends AppCompatActivity
        implements PreferenceFragmentCompat.OnPreferenceStartFragmentCallback,
        FragmentManager.OnBackStackChangedListener {

    private Fragment mContent;
    public DrawerLayout drawerLayout = null;
    NavigationView navigationView;
    NavigationRailView navigationRail;
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

        setContentView(R.layout.responsive_content_frame);
        setProgressBarIndeterminateVisibility(false);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        settings = PreferenceManager.getDefaultSharedPreferences(this);

        // settings.edit().putBoolean("beta",true).apply();

        navigationView = findViewById(R.id.navigation);

        if (navigationView != null) {
            navigationView.getMenu().clear();
            drawerLayout = findViewById(R.id.drawer);
            if (GoogleApiAvailability.getInstance()
                    .isGooglePlayServicesAvailable(WelcomeActivity.this) == ConnectionResult.SUCCESS)
                navigationView.inflateMenu(R.menu.nav);
            else
                navigationView.inflateMenu(R.menu.nav_nogps);

            navigationView.getMenu().getItem(0).setChecked(true);

            if (drawerLayout != null)
                setupDrawer();

            setupNavigation();
        }

        if (navigationRail != null)
            setupRail();

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
        int id;
        switch (pos) {
            case 1:
                mContent = new PlannerFragment();
                id = 0;
                break;
            case 2:
                mContent = new TrafficFragment();
                id = 1;
                break;
            case 3:
                mContent = new ChatFragment();
                id = 2;
                break;
            case 4:
                mContent = new StarredFragment();
                id = -1;
                break;
            case 5:
                mContent = new ClosestFragment();
                id = 4;
                break;
            default:
                mContent = new PlannerFragment();
                id = 0;
                close = getString(R.string.activity_label_planner);
                break;
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_frame, mContent).commit();
        if (id >= 0)
            navigationView.getMenu().getItem(id).setChecked(true);

        getSupportFragmentManager().addOnBackStackChangedListener(this);

    }

    private void setupRail() {
        navigationRail.setOnItemSelectedListener(item -> {
            navigateToItem(item);
            return true;
        });
    }

    private void setupNavigation() {
        navigationView.setNavigationItemSelectedListener(menuItem -> {
            if (drawerLayout != null)
                drawerLayout.closeDrawers();
            navigateToItem(menuItem);
            return true;
        });
    }

    public void navigateToItem(MenuItem menuItem) {
        int id = menuItem.getItemId();
        if (id == R.id.navigation_item_plan) {
            mContent = new PlannerFragment();
            setTitle(R.string.app_name);
        } else if (id == R.id.navigation_item_iss) {
            mContent = new TrafficFragment();
            setTitle(R.string.nav_drawer_issues);
        } else if (id == R.id.navigation_item_chat) {
            mContent = new ChatFragment();
            setTitle(R.string.nav_drawer_chat);
        } else if (id == R.id.navigation_item_star) {
            mContent = new StarredFragment();
            setTitle(R.string.activity_label_starred);
        } else if (id == R.id.navigation_item_closest) {
            mContent = new ClosestFragment();
            setTitle(R.string.nav_drawer_closest);
        } else if (id == R.id.navigation_item_comp) {
            mContent = new CompensationFragment();
            setTitle(R.string.nav_drawer_compensation);
        } else if (id == R.id.navigation_item_extras) {
            mContent = new ExtraFragment();
            setTitle(R.string.nav_drawer_extras);
        } else if (id == R.id.navigation_item_settings) {
            mContent = new SettingsFragment();
            setTitle(R.string.action_settings);
        } else {
            mContent = new PlannerFragment();
            setTitle(R.string.app_name);
            close = getString(R.string.activity_label_planner);
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_frame, mContent).addToBackStack("").commit();

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
                PreferenceManager.getDefaultSharedPreferences(WelcomeActivity.this).edit()
                        .putBoolean("navigation_drawer_learned", true).apply();
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
                                                (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                                                        ? Icon.createWithAdaptiveBitmap(tileProvider.getLetterTile(item,
                                                                item, tileSize, tileSize))
                                                        : Icon.createWithBitmap(tileProvider.getLetterTile(item, item,
                                                                tileSize, tileSize)))
                                        .setIntent(i.setAction(""))
                                        .build();
                            } catch (Exception e) {
                                // TODO Why is ID null here? To investigate
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
                                            (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                                                    ? Icon.createWithAdaptiveBitmap(tileProvider.getLetterTile(numbers,
                                                            numbers, tileSize, tileSize))
                                                    : Icon.createWithBitmap(tileProvider.getLetterTile(numbers, numbers,
                                                            tileSize, tileSize)))
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
                                            (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                                                    ? Icon.createWithAdaptiveBitmap(
                                                            tileProvider.getLetterTile(item, item, tileSize, tileSize))
                                                    : Icon.createWithBitmap(
                                                            tileProvider.getLetterTile(item, item, tileSize, tileSize)))
                                    // .setIntent(i)
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
    public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, Preference pref) {
        final Bundle args = pref.getExtras();
        final Fragment fragment = getSupportFragmentManager().getFragmentFactory().instantiate(
                getClassLoader(),
                pref.getFragment());
        fragment.setArguments(args);
        fragment.setTargetFragment(caller, 0);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_frame, fragment)
                .addToBackStack(null)
                .commit();
        setTitle(pref.getTitle());
        return true;
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

    public void onMailClick(View v) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("plain/text");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[] { "christophe.versieux+betrains@gmail.com" });
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

    public void onWhatsAppClick(View v) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/33622280414"));
        startActivity(browserIntent);
    }

    @Override
    public void onBackStackChanged() {
        updateDrawerSelection();
    }

    private void updateDrawerSelection() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.content_frame);
        if (currentFragment instanceof PlannerFragment) {
            navigationView.setCheckedItem(R.id.navigation_item_plan);
        } else if (currentFragment instanceof TrafficFragment) {
            navigationView.setCheckedItem(R.id.navigation_item_iss);
        } else if (currentFragment instanceof ChatFragment) {
            navigationView.setCheckedItem(R.id.navigation_item_chat);
        } else if (currentFragment instanceof StarredFragment) {
            navigationView.setCheckedItem(R.id.navigation_item_star);
        } else if (currentFragment instanceof ClosestFragment) {
            navigationView.setCheckedItem(R.id.navigation_item_closest);
        } else if (currentFragment instanceof CompensationFragment) {
            navigationView.setCheckedItem(R.id.navigation_item_comp);
        } else if (currentFragment instanceof ExtraFragment) {
            navigationView.setCheckedItem(R.id.navigation_item_extras);
        } else if (currentFragment instanceof SettingsFragment) {
            navigationView.setCheckedItem(R.id.navigation_item_settings);
        }
    }
}
