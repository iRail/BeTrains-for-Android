package tof.cv.mpp;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

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
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.Locale;

public class SettingsActivity extends AppCompatActivity implements
        PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    private static final String TITLE_TAG = "settingsActivityTitle";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(R.string.action_settings);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new MyPreferenceFragment())
                    .commit();
        } else {
            setTitle(savedInstanceState.getCharSequence(TITLE_TAG));
        }
        getSupportFragmentManager().addOnBackStackChangedListener(
                new FragmentManager.OnBackStackChangedListener() {
                    @Override
                    public void onBackStackChanged() {
                        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                            setTitle(R.string.title_activity_settings);
                        }
                    }
                });
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save current activity title so we can set it again after a configuration change
        outState.putCharSequence(TITLE_TAG, getTitle());
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (getSupportFragmentManager().popBackStackImmediate()) {
            return true;
        }
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, Preference pref) {
        // Instantiate the new Fragment
        final Bundle args = pref.getExtras();
        final Fragment fragment = getSupportFragmentManager().getFragmentFactory().instantiate(
                getClassLoader(),
                pref.getFragment());
        fragment.setArguments(args);
        fragment.setTargetFragment(caller, 0);
        // Replace the existing Fragment with the new Fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.settings, fragment)
                .addToBackStack(null)
                .commit();
        setTitle(pref.getTitle());
        return true;
    }

    public static class MyPreferenceFragment extends PreferenceFragmentCompat {
        GoogleSignInClient mGoogleSignInClient;
        private static final int RC_SIGN_IN = 0;

        @Override
        public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
            addPreferencesFromResource(R.xml.preferences_all);
            Log.i("args", "Arguments: " + getArguments());

            Preference pref = findPreference("preflocale");
            if (pref != null) {
                pref.setSummary(Locale.getDefault().getDisplayName());
                pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        startActivity(new Intent(Settings.ACTION_APP_LOCALE_SETTINGS, Uri.fromParts("package", getContext().getPackageName(), null)));
                        return false;
                    }
                });
            }
            pref = findPreference(getString(R.string.key_planner_da));
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
                pref2.setOnPreferenceChangeListener((preference, newValue) -> {
                    preference.setSummary(((ListPreference) preference)
                            .getEntries()[Integer.valueOf(newValue
                            .toString()) - 1]);
                    return true;
                });
            }


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
                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {
                    }
                };
                Picasso.get().load(pic).into(target);
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
                                Toast.makeText(getActivity(),"OK", Toast.LENGTH_LONG).show();
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

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();

            mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);
            getPreferenceScreen().findPreference("login").setOnPreferenceClickListener(preference -> {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
                return false;
            });
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
                android.preference.PreferenceManager.getDefaultSharedPreferences(getActivity()).edit()
                        .putString("prefname", account.getGivenName())
                        .putString("preflastname", account.getFamilyName())
                        .putString("profilepic", account.getPhotoUrl().toString())
                        .putString("prefmail", account.getEmail()).putBoolean("google", true).apply();


                onCreate(getArguments());

            }

        }

        private void setSummary(final EditTextPreference etPref) {
            etPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()  {
                @Override
                public boolean onPreferenceChange(@NonNull Preference preference, Object newValue) {
                    etPref.setSummary(newValue.toString());
                    return true;
                }
            });
            etPref.setSummary(etPref.getText());
        }

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