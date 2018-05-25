package tof.cv.mpp.view;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ListView;

import androidx.annotation.RequiresApi;
import androidx.core.view.ViewCompat;

public class CoordinatedListView extends ListView {

    public CoordinatedListView(Context context) {
        super(context);
        init();
    }

    public CoordinatedListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CoordinatedListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public CoordinatedListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        ViewCompat.setNestedScrollingEnabled(this, true);
    }
}