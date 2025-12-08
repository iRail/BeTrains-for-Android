package tof.cv.mpp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
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

import androidx.preference.PreferenceFragmentCompat;

import static android.content.ContentValues.TAG;

public class ProfileSettingsFragment extends PreferenceFragmentCompat {

    GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 0;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.activity_preferences, rootKey);

        setSummary((EditTextPreference) findPreference("prefname"));

        if (PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("donator", false))
            findPreference("donator").setEnabled(false);

        if (PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("beta", false))
            findPreference("beta").setEnabled(false);

        if (PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("google", false)) {
            findPreference("hidepic").setEnabled(true);
            findPreference("login").setTitle(R.string.deleteaccount);
            findPreference("login").setSummary(R.string.deleteaccountsum);
        }

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);
        findPreference("login").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (GoogleSignIn.getLastSignedInAccount(getContext()) == null) {
                    Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                    startActivityForResult(signInIntent, RC_SIGN_IN);
                } else {
                    mGoogleSignInClient.signOut();
                    PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putBoolean("google", false)
                            .putString("profilepic", "").putString("prefname", "").commit();
                    getActivity().recreate();
                }

                return false;
            }
        });

        String pic = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("profilepic", "");
        if (pic.length() > 0) {
            Target target = new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    findPreference("hidepic").setIcon(new BitmapDrawable(getResources(), bitmap));
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

        findPreference("donator").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                String mail = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("prefmail", "X").replace(".", "");
                FirebaseDatabase.getInstance().getReference().child("donator").child(mail).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot == null || dataSnapshot.getValue() == null)
                            return;
                        long value = (long) dataSnapshot.getValue();

                        if (value == 1) {
                            PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putBoolean("donator", true);
                            findPreference("donator").setEnabled(false);
                            Toast.makeText(getActivity(), "OK", Toast.LENGTH_LONG).show();
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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewCompat.setOnApplyWindowInsetsListener(getListView(), (v, insets) -> {
            int bottomPadding = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;
            v.setPadding(0, 0, 0, bottomPadding);
            return insets;
        });
    }

    private void setSummary(final EditTextPreference etPref) {
        etPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                etPref.setSummary(newValue.toString());
                return true;
            }
        });
        etPref.setSummary(etPref.getText());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            firebaseAuthWithGoogle(account);
            updateUI(account);
        } catch (ApiException e) {
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
                            Log.d(TAG, "signInWithCredential:success");
                        } else {
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                        }
                    }
                });
    }

    private void updateUI(GoogleSignInAccount account) {
        if (account == null) {
            Toast.makeText(getActivity(), "Probleme avec le login", Toast.LENGTH_LONG).show();
        } else {
            PreferenceManager.getDefaultSharedPreferences(getActivity()).edit()
                    .putString("prefname", account.getGivenName())
                    .putString("preflastname", account.getFamilyName())
                    .putString("profilepic", account.getPhotoUrl().toString())
                    .putString("prefmail", account.getEmail()).putBoolean("google", true).apply();

            getActivity().recreate();
        }
    }
}
