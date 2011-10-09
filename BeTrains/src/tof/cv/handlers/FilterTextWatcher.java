package tof.cv.handlers;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;

public class FilterTextWatcher implements TextWatcher {
	
	private ArrayAdapter<String> adapter;	
	
	public FilterTextWatcher(ArrayAdapter<String> adapter) {		
		this.adapter = adapter;
	}

	
	
	public void afterTextChanged(Editable s) {
	}

	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
	}

	public void onTextChanged(CharSequence s, int start, int before,
			int count) {
		adapter.getFilter().filter(s);
	}

}
