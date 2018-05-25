package tof.cv.mpp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class InfoStationActivity extends AppCompatActivity {
    /**
     * Called when the activity is first created.
     */
    String id;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_station);

        setSupportActionBar((Toolbar) findViewById(R.id.my_awesome_toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setIcon(null);

        Bundle bundle = this.getIntent().getExtras();
        long timestamp = bundle.getLong("timestamp") * 1000;
        String name = bundle.getString("Name");
        id = null;
        if (bundle.getString("ID") != null)
            id = bundle.getString("ID").replace("BE.NMBS.", "");


        InfoStationFragment fragment = (InfoStationFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
        fragment.displayInfo(name, timestamp, id);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(this, WelcomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void pic(View v) {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle(R.string.info_station_submit_picture_title);
        b.setMessage(R.string.info_station_submit_picture_text);
        b.setPositiveButton(R.string.info_station_submit_picture_send, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("plain/text");
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"betrainsphotos@gmail.com"});
                intent.putExtra(Intent.EXTRA_SUBJECT, "BeTrains Android Photo " + id);
                startActivity(Intent.createChooser(intent, "Mail"));

            }
        });
        b.show();

    }

}