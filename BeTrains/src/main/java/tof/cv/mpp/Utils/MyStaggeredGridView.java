package tof.cv.mpp.Utils;

import android.content.Context;
import android.util.AttributeSet;

import com.etsy.android.grid.StaggeredGridView;

/**
 * Created by CVE on 20/04/14.
 */
public class MyStaggeredGridView extends StaggeredGridView{
    public MyStaggeredGridView(Context context) {
        super(context);
    }

    public MyStaggeredGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyStaggeredGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public int getLastVisiblePosition() {
        return Math.min(mFirstPosition + getChildCount() - 1, getAdapter() != null ? getAdapter().getCount() - 1 : 0);
    }
}
