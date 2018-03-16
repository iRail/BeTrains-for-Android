package tof.cv.mpp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.readystatesoftware.systembartint.SystemBarTintManager;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.List;

import static android.content.ContentValues.TAG;


public class MyPreferenceActivity extends PreferenceActivity implements
        OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        this.getActionBar().setDisplayHomeAsUpEnabled(true);


        SystemBarTintManager tintManager = new SystemBarTintManager(this);
        // enable status bar tint
        tintManager.setStatusBarTintEnabled(true);
        // enable navigation bar tint
        tintManager.setNavigationBarTintEnabled(true);
        tintManager.setTintResource(R.color.primarycolor);

    }


    protected boolean isValidFragment(String fragmentName) {
        return true;
    }


    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.preference_headers, target);
    }

    /**
     * This fragment shows the preferences for the first header.
     */
    public static class Prefs1Fragment extends PreferenceFragment {
        GoogleSignInClient mGoogleSignInClient;
        private static final int RC_SIGN_IN = 0;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.activity_preferences);
            setSummary((EditTextPreference) getPreferenceScreen().findPreference("prefname"));

            if (PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("donator", false))
                getPreferenceScreen().findPreference("donator").setEnabled(false);

            if (PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("beta", false))
                getPreferenceScreen().findPreference("beta").setEnabled(false);

            if (PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("google", false))
                getPreferenceScreen().findPreference("hidepic").setEnabled(true);
            String pic = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("profilepic", "");

            if (pic.length() > 0) {
                Target target = new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        getPreferenceScreen().findPreference("hidepic").setIcon(new BitmapDrawable(getResources(), bitmap));
                    }

                    @Override
                    public void onBitmapFailed(Drawable errorDrawable) {
                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {
                    }
                };
                Picasso.with(getActivity()).load(pic).into(target);
            }

            getPreferenceScreen().findPreference("donator").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    String mail = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("prefmail", "X").replace(".", "");
                    FirebaseDatabase.getInstance().getReference().child("donator").child(mail).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                           // Log.e("CVE", "Status " + dataSnapshot);
                            if (dataSnapshot == null || dataSnapshot.getValue() == null)
                                return;
                            long value = (long) dataSnapshot.getValue();

                            if (value == 1) {
                                PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putBoolean("donator", true);
                                getPreferenceScreen().findPreference("donator").setEnabled(false);
                                Toast.makeText(getActivity(),"OK",Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    return true;
                }
            });
        }


        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();

            mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);
            getPreferenceScreen().findPreference("login").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                    startActivityForResult(signInIntent, RC_SIGN_IN);
                    return false;
                }
            });
        }

        private void setSummary(final EditTextPreference etPref) {

            etPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference,
                                                  Object newValue) {
                    etPref.setSummary(newValue.toString());
                    return true;
                }
            });
            etPref.setSummary(etPref.getText());
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);

            // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
            if (requestCode == RC_SIGN_IN) {
                // The Task returned from this call is always completed, no need to attach
                // a listener.
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                handleSignInResult(task);
            }
        }

        private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
            try {
                GoogleSignInAccount account = completedTask.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
                // Signed in successfully, show authenticated UI.
                updateUI(account);
            } catch (ApiException e) {
                // The ApiException status code indicates the detailed failure reason.
                // Please refer to the GoogleSignInStatusCodes class reference for more information.
                Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
                updateUI(null);
            }
        }

        public void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
            Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
            mAuth.signInWithCredential(credential)
                    .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "signInWithCredential:success");
                                //FirebaseUser user = mAuth.getCurrentUser();
                                //updateUI(user);
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "signInWithCredential:failure", task.getException());
                                //Snackbar.make(findViewById(R.id.main_layout), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                                //updateUI(null);
                            }

                            // ...
                        }
                    });
        }

        private void updateUI(GoogleSignInAccount account) {
            if (account == null)
                Toast.makeText(getActivity(), "Probleme avec le login", Toast.LENGTH_LONG).show();
            else {
                PreferenceManager.getDefaultSharedPreferences(getActivity()).edit()
                        .putString("prefname", account.getGivenName())
                        .putString("preflastname", account.getFamilyName())
                        .putString("profilepic", account.getPhotoUrl().toString())
                        .putString("prefmail", account.getEmail()).putBoolean("google", true).apply();


                onCreate(getArguments());

            }

        }

        protected boolean isValidFragment(String fragmentName) {
            return true;
        }
    }


    public static class Prefs2Fragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Can retrieve arguments from headers XML.
            Log.i("args", "Arguments: " + getArguments());

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.activity_planner_preferences);

            Preference pref = findPreference(getString(R.string.key_planner_da));
            if (pref != null) {
                pref.setSummary(((ListPreference) pref).getEntry());
                pref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

                    @Override
                    public boolean onPreferenceChange(Preference preference,
                                                      Object newValue) {
                        try {//Wrong number in previous app. Need to try/catch
                            preference.setSummary(((ListPreference) preference)
                                    .getEntries()[Integer.valueOf(newValue
                                    .toString()) - 1]);
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                        return true;
                    }
                });
            }

            Preference pref2 = findPreference(getString(R.string.key_activity));
            if (pref2 != null) {
                pref2.setSummary(((ListPreference) pref2).getEntry());
                pref2.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

                    @Override
                    public boolean onPreferenceChange(Preference preference,
                                                      Object newValue) {
                        preference.setSummary(((ListPreference) preference)
                                .getEntries()[Integer.valueOf(newValue
                                .toString()) - 1]);
                        return true;
                    }
                });
            }
        }

        protected boolean isValidFragment(String fragmentName) {
            return true;
        }
    }

    /**
     * This fragment shows the preferences for the third header.
     */
    public static class Prefs3Fragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Can retrieve arguments from headers XML.
            Log.i("args", "Arguments: " + getArguments());

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.activity_twitter_preferences);
        }

        protected boolean isValidFragment(String fragmentName) {
            return true;
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {

        if (key.contentEquals("prefPseudo")) {
            Preference pref = findPreference("prefPseudo");
            pref.setSummary(((EditTextPreference) pref).getText());
        }
        if (key.contentEquals(getString(R.string.key_activity))) {
            Preference pref = findPreference(getString(R.string.key_activity));
            pref.setSummary(((ListPreference) pref).getEntry());
        }
        if (key.contentEquals(getString(R.string.key_planner_da))) {
            Preference pref = findPreference(getString(R.string.key_planner_da));
            pref.setSummary(((ListPreference) pref).getEntry());
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case (android.R.id.home):
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
