package tof.cv.mpp;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class MenuFragment extends ListFragment {

    private TextView greeting;

    public static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";


    private DrawerLayout mDrawerLayout;
    private ListView mDrawerListView;
    private View mFragmentContainerView;
    private ActionBarDrawerToggle mDrawerToggle;
    private boolean mUserLearnedDrawer;
    private boolean mFromSavedInstanceState;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.list, null);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final String[] items = getResources().getStringArray(R.array.menu);
        final String[] itemsFdroid = getResources().getStringArray(R.array.menuFdroid);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);

        try {
            if (mUserLearnedDrawer)
                getActivity().findViewById(R.id.tuto).setVisibility(View.GONE);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (savedInstanceState != null) {
            mFromSavedInstanceState = true;
        }

        final int[] drawableArray = {R.drawable.ab_planner, R.drawable.ab_traffic,
                R.drawable.ab_chat,
                R.drawable.ab_starred, R.drawable.ab_sncb, R.drawable.ab_closest, R.drawable.ic_game,
                R.drawable.ab_irail};

        final int[] drawableArrayFdroid = {R.drawable.ab_planner, R.drawable.ab_traffic,
                R.drawable.ab_chat,
                R.drawable.ab_starred, R.drawable.ab_sncb, R.drawable.ab_irail};

        setListAdapter(new ArrayAdapter<String>(this.getActivity(), R.layout.row_menu,
                getResources().getBoolean(R.bool.isFdroid) ? itemsFdroid : items) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                // final View renderer = super.getView(position, convertView,
                // parent);
                View currentView = convertView;
                LayoutInflater currentViewInflater = (LayoutInflater) getContext()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                currentView = currentViewInflater.inflate(R.layout.row_menu,
                        null);
                ImageView iv = (ImageView) currentView.findViewById(R.id.icon);
                TextView tv = (TextView) currentView.findViewById(R.id.label);

                tv.setText(getResources().getBoolean(R.bool.isFdroid) ? itemsFdroid[position] : items[position]);
                iv.setBackgroundResource(getResources().getBoolean(R.bool.isFdroid) ? drawableArrayFdroid[position] : drawableArray[position]);


                return currentView;
            }
        });

        getListView().setVerticalScrollBarEnabled(false);

        //updateUI();
    }

    /**
     * Users of this fragment must call this method to set up the navigation drawer interactions.
     *
     * @param fragmentId   The android:id of this fragment in its activity's layout.
     * @param drawerLayout The DrawerLayout containing this fragment's UI.
     */
    public void setUp(final Activity a, int fragmentId, DrawerLayout drawerLayout) {
        mFragmentContainerView = a.findViewById(fragmentId);
        mDrawerLayout = drawerLayout;

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener

        //ActionBar actionBar = getActionBar();
        //((MainActivity)getActivity()).getT .setDisplayHomeAsUpEnabled(false);
        //actionBar.setHomeButtonEnabled(true);

        mDrawerToggle = new ActionBarDrawerToggle(
                a,
                drawerLayout,
                (Toolbar) a.findViewById(R.id.my_awesome_toolbar),
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        ) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);

                a.invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                //super.onDrawerSlide(drawerView, slideOffset);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (!mUserLearnedDrawer) {
                    // The user manually opened the drawer; store this flag to prevent auto-showing
                    // the navigation drawer automatically in the future.
                    mUserLearnedDrawer = true;
                    SharedPreferences sp = PreferenceManager
                            .getDefaultSharedPreferences(getActivity());
                    sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).apply();
                }
                try {
                    a.findViewById(R.id.tuto).setVisibility(View.GONE);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                a.invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }
        };
        mDrawerToggle.setDrawerIndicatorEnabled(false);

        mDrawerToggle.setHomeAsUpIndicator(R.drawable.ic_menu_white);
        mDrawerToggle.setToolbarNavigationClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDrawerLayout.isDrawerOpen(Gravity.START))
                    mDrawerLayout.closeDrawer(Gravity.START);
                else
                    mDrawerLayout.openDrawer(Gravity.START);
            }
        });

        // If the user hasn't 'learned' about the drawer, open it to introduce them to the drawer,
        // per the navigation drawer design guidelines.
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(a);
        mUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);
        if (!mUserLearnedDrawer && !mFromSavedInstanceState) {
            mDrawerLayout.openDrawer(mFragmentContainerView);
        }

        // Defer code dependent on restoration of previous instance state.
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        //mDrawerListView=(ListView)getView().findViewById(android.R.id.list);
        //mDrawerListView.setItemChecked(1, true);
        //mDrawerListView.setSelector(R.color.darkblue);
    }


    @Override
    public void onListItemClick(ListView lv, View v, int position, long id) {
        Fragment newContent = null;
        if (getResources().getBoolean(R.bool.isFdroid)) {
            switch (position) {
                case 0:
                    newContent = new PlannerFragment();
                    break;
                case 1:
                    newContent = new TrafficFragment();
                    break;
                case 2:
                    newContent = new ChatFragment();
                    break;
                case 3:
                    newContent = new StarredFragment();
                    break;
                case 4:
                    newContent = new CompensationFragment();
                    break;
                case 5:
                    newContent = new ExtraFragment();
                    break;

            }
        } else {
            switch (position) {
                case 0:
                    newContent = new PlannerFragment();
                    break;
                case 1:
                    newContent = new TrafficFragment();
                    break;
                case 2:
                    newContent = new ChatFragment();
                    break;
                case 3:
                    newContent = new StarredFragment();
                    break;
                case 4:
                    newContent = new CompensationFragment();
                    break;
                case 5:
                    newContent = new ClosestFragment();
                    break;
                case 6:
                    newContent = new GameFragment();
                    break;
                case 7:
                    newContent = new ExtraFragment();
                    break;

            }
        }


        if (newContent != null)
            switchFragment(newContent, position);

        getListView().setSelection(position);
    }

    // the meat of switching the above fragment
    private void switchFragment(Fragment fragment, int position) {
        if (getActivity() == null)
            return;

        if (getActivity() instanceof WelcomeActivity) {
            WelcomeActivity ra = (WelcomeActivity) getActivity();
            ra.switchContent(fragment, position);
        }
    }
}
