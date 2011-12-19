/**
 * Copyright 2010 Lukasz Szmit <devmail@szmit.eu>

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
package tof.cv.mpp.view;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import tof.cv.mpp.PlannerFragment;
import tof.cv.mpp.R;
import tof.cv.mpp.Utils.Utils;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;

public class DateTimePicker extends RelativeLayout implements
		OnDateChangedListener, OnTimeChangedListener {

	// DatePicker reference
	private DatePicker datePicker;
	// TimePicker reference
	private TimePicker timePicker;
	// Calendar reference
	private Calendar mCalendar;
	// Dialog title
	private TextView mTitle;

	// Constructor start
	public DateTimePicker(Context context) {
		this(context, null);
	}

	public DateTimePicker(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public DateTimePicker(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		// Get LayoutInflater instance
		final LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		// Inflate myself
		inflater.inflate(R.layout.dtp_datetimepicker, this, true);

		// Grab a Calendar instance
		mCalendar = Calendar.getInstance();
		// Grab the ViewSwitcher so we can attach our picker views to it

		// Init date picker
		datePicker = (DatePicker) this.findViewById(R.id.DatePicker);
		datePicker.init(mCalendar.get(Calendar.YEAR),
				mCalendar.get(Calendar.MONTH),
				mCalendar.get(Calendar.DAY_OF_MONTH), this);

		// Init time picker
		timePicker = (TimePicker) this.findViewById(R.id.TimePicker);
		timePicker.setOnTimeChangedListener(this);

		mTitle = (TextView) this.findViewById(R.id.DateTimePickerTitle);
		setTitle();

	}

	// Constructor end

	public void setTitle() {
		mTitle.setText(Utils.formatDate(mCalendar.getTime(), PlannerFragment.datePattern));
		mTitle.setSelected(true);
	}

	// Called every time the user changes DatePicker values
	public void onDateChanged(DatePicker view, int year, int monthOfYear,
			int dayOfMonth) {
		// Update the internal Calendar instance
		mCalendar.set(year, monthOfYear, dayOfMonth,
				mCalendar.get(Calendar.HOUR_OF_DAY),
				mCalendar.get(Calendar.MINUTE));
		setTitle();

	}

	// Called every time the user changes TimePicker values
	public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
		// Update the internal Calendar instance
		mCalendar.set(mCalendar.get(Calendar.YEAR),
				mCalendar.get(Calendar.MONTH),
				mCalendar.get(Calendar.DAY_OF_MONTH), hourOfDay, minute);
		setTitle();
	}

	// Convenience wrapper for internal Calendar instance
	public int get(final int field) {
		return mCalendar.get(field);
	}

	// Reset DatePicker, TimePicker and internal Calendar instance
	public void reset() {
		final Calendar c = Calendar.getInstance();
		updateDate(c.get(Calendar.YEAR), c.get(Calendar.MONTH),
				c.get(Calendar.DAY_OF_MONTH));
		updateTime(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
		setTitle();
	}

	// Convenience wrapper for internal Calendar instance
	public long getDateTimeMillis() {
		return mCalendar.getTimeInMillis();
	}

	// Convenience wrapper for internal TimePicker instance
	public void setIs24HourView(boolean is24HourView) {
		timePicker.setIs24HourView(is24HourView);
	}

	// Convenience wrapper for internal TimePicker instance
	public boolean is24HourView() {
		return timePicker.is24HourView();
	}

	// Convenience wrapper for internal DatePicker instance
	public void updateDate(int year, int monthOfYear, int dayOfMonth) {
		datePicker.updateDate(year, monthOfYear, dayOfMonth);
	}

	// Convenience wrapper for internal TimePicker instance
	public void updateTime(int currentHour, int currentMinute) {
		timePicker.setCurrentHour(currentHour);
		timePicker.setCurrentMinute(currentMinute);
	}

	// Set ActionBar Title
	public String getFormatedDate(String pattern) {
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		return sdf.format(mCalendar.getTime());
	}

}
