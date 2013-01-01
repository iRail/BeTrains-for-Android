package tof.cv.mpp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;


public class ExtraFragment extends SherlockFragment{
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_extra, null);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getSherlockActivity().getSupportActionBar().setIcon(R.drawable.ab_irail);
		getSherlockActivity().getSupportActionBar().setTitle("Extras");
		getSherlockActivity().getSupportActionBar().setSubtitle(null);
	}
}