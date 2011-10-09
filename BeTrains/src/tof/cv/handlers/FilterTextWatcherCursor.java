package tof.cv.handlers;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.SimpleCursorAdapter;

public class FilterTextWatcherCursor implements TextWatcher {
	
	private SimpleCursorAdapter adapter;	
	
	public FilterTextWatcherCursor(SimpleCursorAdapter adapter) {		
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
