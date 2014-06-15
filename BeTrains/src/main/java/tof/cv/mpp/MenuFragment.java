package tof.cv.mpp;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class MenuFragment extends ListFragment {

    private TextView greeting;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.list, null);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final String[] items = getResources().getStringArray(R.array.menu);

        final int[] idArray = {R.drawable.ab_planner, R.drawable.ab_traffic,
                R.drawable.ab_chat,
                R.drawable.ab_starred, R.drawable.ab_sncb, R.drawable.ab_closest, R.drawable.ic_game,
                R.drawable.ab_irail};

        setListAdapter(new ArrayAdapter<String>(this.getActivity(), R.id.label,
                items) {
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

                tv.setText(items[position]);


                try {
                    //Just in case.. Lazy me.
                    iv.setBackgroundResource(idArray[position]);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return currentView;
            }
        });

        getListView().setVerticalScrollBarEnabled(false);

        //updateUI();
    }


    @Override
    public void onListItemClick(ListView lv, View v, int position, long id) {
        Fragment newContent = null;

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
