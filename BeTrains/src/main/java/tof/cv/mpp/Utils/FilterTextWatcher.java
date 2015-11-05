package tof.cv.mpp.Utils;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.ArrayAdapter;

import tof.cv.mpp.adapter.IndexAdapter;

public class FilterTextWatcher implements TextWatcher {

    private IndexAdapter adapter;

    public FilterTextWatcher(IndexAdapter adapter) {
        this.adapter = adapter;
    }

    public void afterTextChanged(Editable s) {
    }

    public void beforeTextChanged(CharSequence s, int start, int count,
                                  int after) {
    }

    public void onTextChanged(CharSequence s, int start, int before,
                              int count) {
        adapter.getFilter().filter(s.toString());
    }

}
