package tof.cv.adapters;

import java.util.ArrayList;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public abstract class AbstractAdapter<T> extends ArrayAdapter<T> {

	protected ArrayList<T> items;
	
	public AbstractAdapter(Context context, int textViewResourceId,ArrayList<T> items){
		super(context, textViewResourceId, items);
		this.items = items;
	}
	
	public abstract View getView(int position, View convertView, ViewGroup parent);

	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}
	
}
