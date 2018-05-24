/**
 * Copyright 2010 Lukasz Szmit <devmail@szmit.eu>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tof.cv.mpp.view;

import android.app.Dialog;
import android.content.Context;
import androidx.appcompat.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import tof.cv.mpp.PlannerFragment;
import tof.cv.mpp.R;
import tof.cv.mpp.Utils.Utils;

public class DateTimePicker extends Dialog implements OnDateChangedListener,
        OnTimeChangedListener {

    // DatePicker reference
    private DatePicker datePicker;
    // TimePicker reference
    private TimePicker timePicker;
    // Calendar reference
    private Calendar mCalendar;

    public DateTimePicker(Context context, final PlannerFragment fragment) {
        super(context);

        // Get LayoutInflater instance
        final LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        // Inflate myself
        View v = inflater.inflate(R.layout.datetimepicker, null, true);
        final DateTimePicker mThis = this;
        // Grab a Calendar instance
        mCalendar = Calendar.getInstance();
        mCalendar.setTime(fragment.mDate.getTime());
        // Grab the ViewSwitcher so we can attach our picker views to it

        // Init date picker
        datePicker = (DatePicker) v.findViewById(R.id.DatePicker);
        datePicker.init(mCalendar.get(Calendar.YEAR),
                mCalendar.get(Calendar.MONTH),
                mCalendar.get(Calendar.DAY_OF_MONTH), this);

        // Init time picker
        timePicker = (TimePicker) v.findViewById(R.id.TimePicker);

        Button okButton = (Button) v.findViewById(R.id.positiveButton);
        okButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                if (((AppCompatActivity) fragment.getActivity()) != null)
                    ((AppCompatActivity) fragment.getActivity()).getSupportActionBar().setSubtitle(
                            getFormatedDate(PlannerFragment.abDatePattern) + " - " + getFormatedDate(PlannerFragment.abTimePattern));
                fragment.mDate = mCalendar;
                fragment.doSearch();
                mThis.dismiss();
            }
        });

        setTitle(Utils.formatDate(mCalendar.getTime(),
                PlannerFragment.datePattern));
        setContentView(v);
    }

    // Called every time the user changes DatePicker values
    public void onDateChanged(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
        // Update the internal Calendar instance
        mCalendar.set(year, monthOfYear, dayOfMonth,
                mCalendar.get(Calendar.HOUR_OF_DAY),
                mCalendar.get(Calendar.MINUTE));
        setTitle(Utils.formatDate(mCalendar.getTime(),
                PlannerFragment.datePattern));

    }


    // Called every time the user changes TimePicker values
    public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
        // Update the internal Calendar instance
        mCalendar.set(mCalendar.get(Calendar.YEAR),
                mCalendar.get(Calendar.MONTH),
                mCalendar.get(Calendar.DAY_OF_MONTH), hourOfDay, minute);
        setTitle(Utils.formatDate(mCalendar.getTime(),
                PlannerFragment.datePattern));
    }

    // Convenience wrapper for internal Calendar instance
    public int get(final int field) {
        return mCalendar.get(field);
    }

    // Convenience wrapper for internal Calendar instance
    public long getDateTimeMillis() {
        return mCalendar.getTimeInMillis();
    }

    // Convenience wrapper for internal TimePicker instance
    public void setIs24HourView(boolean is24HourView) {

        timePicker.setIs24HourView(is24HourView);
        timePicker.setOnTimeChangedListener(this);
        timePicker.setCurrentHour(mCalendar.get(Calendar.HOUR_OF_DAY));

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
