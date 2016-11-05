package tk.leoforney.drivingtimer;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnMenuTabClickListener;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    static BottomBar mBottomBar;

    static protected FirebaseAuth mAuth;
    static protected DatabaseReference mDatabase;
    static protected FirebaseUser mUser;

    static protected String uid = null;

    static SharedPreferences pref;

    final static String PREF_KEY = "DRIVE_PREF_KEY";

    static HashMap<String, ArrayList<Integer>> drives;

    public enum TabNames {
        TIMER,
        MYDRIVES,
        SETTINGS
    }

    FragmentManager fm;

    static String TAG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TAG = getLocalClassName();

        pref = getSharedPreferences(PREF_KEY, MODE_PRIVATE);

        mAuth = FirebaseAuth.getInstance();

        FirebaseCrash.report(new Exception("My first Android non-fatal error"));

        fm = getSupportFragmentManager();

        mBottomBar = BottomBar.attach(this, savedInstanceState);

        mBottomBar.setMaxFixedTabs(2);

        mBottomBar.setItems(R.menu.bottombar_items);

        mBottomBar.setOnMenuTabClickListener(new OnMenuTabClickListener() {
            @Override
            public void onMenuTabSelected(@IdRes int menuItemId) {
                switch (menuItemId) {
                    case R.id.timer:
                        switchFrag(newFragment(TabNames.TIMER), "Timer");
                        break;
                    case R.id.my_drives:
                        switchFrag(newFragment(TabNames.MYDRIVES), "MyDrives");
                        break;
                    case R.id.settings:
                        switchFrag(newFragment(TabNames.SETTINGS), "Settings");
                        break;
                }

            }

            @Override
            public void onMenuTabReSelected(@IdRes int menuItemId) {

            }
        });

        mBottomBar.mapColorForTab(0, "#" + Integer.toHexString(ContextCompat.getColor(getApplicationContext(), R.color.tab1)));
        mBottomBar.mapColorForTab(1, "#" + Integer.toHexString(ContextCompat.getColor(getApplicationContext(), R.color.tab2)));
        mBottomBar.mapColorForTab(2, "#" + Integer.toHexString(ContextCompat.getColor(getApplicationContext(), R.color.tab3)));

        final String email = pref.getString("email", null);
        final String password = pref.getString("password", null);

        if (email != null && password != null) {
            mAuth.signInWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {
                    Log.d(TAG, "Logged in!");

                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    database.setPersistenceEnabled(true);
                    mDatabase = database.getReferenceFromUrl("https://drives-timer.firebaseio.com/");

                    mUser = mAuth.getCurrentUser();

                    uid = mUser.getUid();

                    mDatabase.child(uid).child("drives").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            drives = dataSnapshot.getValue(new GenericTypeIndicator<HashMap<String, ArrayList<Integer>>>() {
                            });
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            });
        }


    }

    private static Fragment newFragment(TabNames name) {
        Fragment frag = null;
        switch (name) {
            case TIMER:
                frag = new TimerFragment();
                break;
            case MYDRIVES:
                frag = new MyDrivesFragment();
                break;
            case SETTINGS:
                frag = new SettingsFragment();
                break;
        }
        return frag;
    }

    public void switchFrag(Fragment frag, String tag) {
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.fragmentcontent, frag, tag);
        transaction.commit();
    }

    public static Task<Void> addDrive(String date, ArrayList<Long> TimeArrayList) {
        Log.d(TAG, "Drive attempted to be added!");
        Task<Void> returnValue = null;
        if (mDatabase != null) {
            returnValue = mDatabase.child(uid).child("drives").child(date).setValue(TimeArrayList);
        }
        return returnValue;
    }


}
