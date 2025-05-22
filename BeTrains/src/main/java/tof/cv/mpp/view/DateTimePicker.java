package tof.cv.mpp.view;

import static com.google.android.material.timepicker.MaterialTimePicker.INPUT_MODE_CLOCK;

import android.content.Context;
import android.text.format.DateFormat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import tof.cv.mpp.PlannerFragment;

public class DateTimePicker {

    public static void show(@NonNull Context context, @NonNull PlannerFragment fragment) {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            // Initialize calendar with selected date
            Calendar selectedDate = Calendar.getInstance(TimeZone.getDefault());
            selectedDate.setTimeInMillis(selection);

            MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_24H)
                    .setInputMode(INPUT_MODE_CLOCK)
                    .setHour(Calendar.getInstance().get(Calendar.HOUR_OF_DAY))
                    .setMinute(Calendar.getInstance().get(Calendar.MINUTE))
                    .build();

            timePicker.addOnPositiveButtonClickListener(v -> {
                selectedDate.set(Calendar.HOUR_OF_DAY, timePicker.getHour());
                selectedDate.set(Calendar.MINUTE, timePicker.getMinute());
                selectedDate.set(Calendar.SECOND, 0);
                selectedDate.set(Calendar.MILLISECOND, 0);

                // Apply changes to fragment
                if (fragment.getActivity() != null &&
                        ((AppCompatActivity) fragment.getActivity()).getSupportActionBar() != null) {

                    ((AppCompatActivity) fragment.getActivity()).getSupportActionBar().setTitle(
                            format(selectedDate, PlannerFragment.abDatePattern) + " - " +
                                    format(selectedDate, PlannerFragment.abTimePattern));
                }

                fragment.mDate = selectedDate;
                fragment.doSearch();
            });

            timePicker.show(((FragmentActivity) context).getSupportFragmentManager(), "MaterialTimePicker");
        });

        datePicker.show(((FragmentActivity) context).getSupportFragmentManager(), "MaterialDatePicker");
    }

    private static String format(Calendar calendar, String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.getDefault());
        sdf.setTimeZone(calendar.getTimeZone());
        return sdf.format(calendar.getTime());
    }
}
