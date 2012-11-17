package tof.cv.mpp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.View;

import com.actionbarsherlock.view.MenuItem;
import com.slidingmenu.lib.SlidingMenu;
import com.slidingmenu.lib.app.SlidingFragmentActivity;

public class WelcomeActivity extends SlidingFragmentActivity {

	private Fragment mContent;

	/** Called when the activity is first created. */

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.responsive_content_frame);
		setSlidingActionBarEnabled(false);
		// check if the content frame contains the menu frame
		if (findViewById(R.id.menu_frame) == null) {
			setBehindContentView(R.layout.menu_frame);
			getSlidingMenu().setSlidingEnabled(true);
			getSlidingMenu()
					.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
			// show home as up so we can toggle
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		} else {
			// add a dummy view
			View v = new View(this);
			setBehindContentView(v);
			getSlidingMenu().setSlidingEnabled(false);
			getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
		}

		// set the Above View Fragment
		if (savedInstanceState != null)
			mContent = getSupportFragmentManager().getFragment(
					savedInstanceState, "mContent");
		if (mContent == null)
			mContent = new PlannerFragment();
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.content_frame, mContent).commit();

		// set the Behind View Fragment
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.menu_frame, new MenuFragment()).commit();

		// customize the SlidingMenu
		SlidingMenu sm = getSlidingMenu();
		sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		sm.setShadowWidthRes(R.dimen.shadow_width);
		sm.setShadowDrawable(R.drawable.shadow);
		sm.setBehindWidth((int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, 200, getResources()
						.getDisplayMetrics()));
		sm.setBehindScrollScale(0.2f);
		sm.setFadeDegree(0.25f);

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			if (!this.getResources().getBoolean(R.bool.tablet_layout))
				toggle();
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		try {
			getSupportFragmentManager().putFragment(outState, "mContent",
					mContent);
		} catch (Exception e) {
		}
	}

	public void switchContent(final Fragment fragment) {
		mContent = fragment;

		Fragment f = (Fragment) getSupportFragmentManager().findFragmentById(
				R.id.content_frame);

		if (!fragment.getClass().equals(f.getClass())) {
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.content_frame, fragment).addToBackStack(null)
					.commit();
			Handler h = new Handler();
			h.postDelayed(new Runnable() {
				public void run() {
					getSlidingMenu().showAbove();
				}
			}, 50);
		}
		else
			toggle();
	}

	public void onPlusClick(View v) {
		String url = "https://plus.google.com/b/108315424589085456181/108315424589085456181/posts";
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setData(Uri.parse(url));
		startActivity(i);
	}

	public void onMailClick(View v) {
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("plain/text");
		intent.putExtra(Intent.EXTRA_EMAIL, "christophe.versieux@gmail.com");
		intent.putExtra(Intent.EXTRA_SUBJECT, "BeTrains Android");
		startActivity(Intent.createChooser(intent, "Mail"));
	}

	public void oniRailClick(View v) {
		Intent marketLaunch = new Intent(Intent.ACTION_VIEW);
		marketLaunch.setData(Uri
				.parse("market://details?id=be.irail.liveboards"));
		startActivity(marketLaunch);
	}
}
