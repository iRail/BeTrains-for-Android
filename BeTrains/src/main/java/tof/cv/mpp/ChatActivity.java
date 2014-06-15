package tof.cv.mpp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.MenuItem;

import tof.cv.mpp.Utils.DbAdapterConnection;

public class ChatActivity extends FragmentActivity {
    public final static String ID = "CgkI9Y3S0soCEAIQAw";


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        String trainId = null;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            trainId = extras.getString(DbAdapterConnection.KEY_NAME);
        }

        ChatFragment fragment = (ChatFragment) this.getSupportFragmentManager()
                .findFragmentById(R.id.fragment);
        if (trainId != null)
            fragment.trainId = getString(R.string.txt_train) + " " + trainId.replaceAll(getString(R.string.txt_train) + " ", "");
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

}