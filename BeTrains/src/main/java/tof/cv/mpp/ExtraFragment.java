package tof.cv.mpp;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;


public class ExtraFragment extends Fragment {
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_extra, null);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		try {
			((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(R.string.nav_drawer_extras);
			((AppCompatActivity)getActivity()).getSupportActionBar().setSubtitle(null);
		} catch (Exception e) {
			e.printStackTrace();
		}


	}
}