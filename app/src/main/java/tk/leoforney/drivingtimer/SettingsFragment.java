package tk.leoforney.drivingtimer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.codetroopers.betterpickers.calendardatepicker.CalendarDatePickerDialogFragment;
import com.codetroopers.betterpickers.hmspicker.HmsPickerBuilder;
import com.codetroopers.betterpickers.hmspicker.HmsPickerDialogFragment;
import com.codetroopers.betterpickers.radialtimepicker.RadialTimePickerDialogFragment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class SettingsFragment extends Fragment implements View.OnClickListener {

    EditText email_edittext;
    EditText password_edittext;
    Button signin_button;
    Button register_button;

    Button datepicker_button;
    Button timepicker_button;
    Button lengthpicker_button;

    Button uploadbutton_drive_importer;

    private TextView date_picker_textview;
    private TextView time_picker_textview;
    private TextView length_picker_textview;

    private String MonthDateYear;
    private String Time24Hour;
    private ArrayList<Long> LengthArrayList;

    private LinearLayout driveimporter;

    private SharedPreferences pref;

    private SharedPreferences.Editor editor;

    private CoordinatorLayout coordinatorLayout;

    private FirebaseAuth mAuth;

    private static FirebaseUser user;

    private String TAG;

    private final static String PREF_KEY = "DRIVE_PREF_KEY";


    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        View v = getView();

        TAG = "SettingsFragment";

        Log.d(TAG, "Signin Triggered!");

        coordinatorLayout = (CoordinatorLayout) v.findViewById(R.id.snackbarPosition);

        pref = getActivity().getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE);
        editor = pref.edit();

        driveimporter = (LinearLayout) v.findViewById(R.id.driveimporter);

        date_picker_textview = (TextView) v.findViewById(R.id.date_picker_textview);
        time_picker_textview = (TextView) v.findViewById(R.id.time_picker_textview);
        length_picker_textview = (TextView) v.findViewById(R.id.length_picker_textview);

        uploadbutton_drive_importer = (Button) v.findViewById(R.id.uploadButtonDriveImporter);

        datepicker_button = (Button) v.findViewById(R.id.datapicker_button);
        datepicker_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CalendarDatePickerDialogFragment cdp = new CalendarDatePickerDialogFragment()
                        .setOnDateSetListener(new CalendarDatePickerDialogFragment.OnDateSetListener() {
                            @Override
                            public void onDateSet(CalendarDatePickerDialogFragment dialog, int year, int monthOfYear, int dayOfMonth) {
                                String adjustedYear = String.valueOf(year);
                                if (adjustedYear.length() == 1) {
                                    adjustedYear = "0" + String.valueOf(year);
                                }
                                String adjustedMonth = String.valueOf(monthOfYear);
                                if (adjustedMonth.length() == 1) {
                                    adjustedMonth = "0" + String.valueOf(monthOfYear);
                                }
                                String adjustedDay = String.valueOf(dayOfMonth);
                                if (adjustedDay.length() == 1) {
                                    adjustedDay = "0" + String.valueOf(dayOfMonth);
                                }
                                MonthDateYear = String.valueOf(adjustedMonth + "-" + adjustedDay + "-" + adjustedYear);
                                Log.d(TAG, MonthDateYear);
                                date_picker_textview.setText(MonthDateYear);
                            }
                        });
                cdp.show(getActivity().getSupportFragmentManager(), "datepicker");
            }
        });
        timepicker_button = (Button) v.findViewById(R.id.timepicker_button);
        timepicker_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                RadialTimePickerDialogFragment rtp = new RadialTimePickerDialogFragment()
                        .setOnTimeSetListener(new RadialTimePickerDialogFragment.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(RadialTimePickerDialogFragment dialog, int hourOfDay, int minute) {
                                String adjustedHour = String.valueOf(hourOfDay);
                                if (adjustedHour.length() == 1) {
                                    adjustedHour = "0" + String.valueOf(hourOfDay);
                                }
                                String adjustedMinute = String.valueOf(minute);
                                if (adjustedMinute.length() == 1) {
                                    adjustedMinute = "0" + String.valueOf(minute);
                                }
                                Time24Hour = String.valueOf(adjustedHour + ":" + adjustedMinute + ":00");
                                Log.d(TAG, Time24Hour);
                                time_picker_textview.setText(Time24Hour);
                            }
                        });
                rtp.show(getActivity().getSupportFragmentManager(), "timepicker");

            }
        });
        lengthpicker_button = (Button) v.findViewById(R.id.length_button);
        lengthpicker_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HmsPickerBuilder hpb = new HmsPickerBuilder()
                        .setFragmentManager(getActivity().getSupportFragmentManager())
                        .setStyleResId(R.style.BetterPickersDialogFragment)
                        .addHmsPickerDialogHandler(new HmsPickerDialogFragment.HmsPickerDialogHandlerV2() {
                            @Override
                            public void onDialogHmsSet(int reference, boolean isNegative, int hours, int minutes, int seconds) {
                                LengthArrayList = new ArrayList<>(3);
                                if (LengthArrayList.size() >= 3) {
                                    LengthArrayList.set(0, (long) hours);
                                    LengthArrayList.set(1, (long) minutes);
                                    LengthArrayList.set(2, (long) seconds);
                                }
                                if (LengthArrayList.size() == 0) {
                                    LengthArrayList.add(0, (long) hours);
                                    LengthArrayList.add(1, (long) minutes);
                                    LengthArrayList.add(2, (long) seconds);
                                }
                                String lengthString = String.valueOf(hours) + " h, " + String.valueOf(minutes) + " m, " + String.valueOf(seconds) + " s";
                                length_picker_textview.setText(lengthString);
                                Log.d(TAG, lengthString);
                            }
                        });
                hpb.show();

            }
        });

        uploadbutton_drive_importer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (user != null) {
                    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReferenceFromUrl("https://drives-timer.firebaseio.com/").child(user.getUid()).child("drives");
                    if (MonthDateYear != null && Time24Hour != null && LengthArrayList != null) {
                        mDatabase.child(MonthDateYear + " " + Time24Hour).setValue(LengthArrayList).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast toast = Toast.makeText(getActivity().getApplicationContext(), "Drive uploaded from: " + MonthDateYear, Toast.LENGTH_SHORT);
                                toast.show();
                            }
                        });

                    } else {
                        Toast.makeText(getActivity().getApplicationContext(), "Please fill in drive data", Toast.LENGTH_LONG).show();
                    }

                }

            }
        });


        MainActivity.mAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    driveimporter.setVisibility(View.VISIBLE);
                }
                if (user == null) {
                    driveimporter.setVisibility(View.INVISIBLE);
                }
            }
        });


        mAuth = FirebaseAuth.getInstance();

        email_edittext = (EditText) v.findViewById(R.id.email_edittext);
        password_edittext = (EditText) v.findViewById(R.id.password_edittext);

        signin_button = (Button) v.findViewById(R.id.signin_button);
        signin_button.setOnClickListener(this);
        register_button = (Button) v.findViewById(R.id.register_button);
        register_button.setOnClickListener(this);

        String email = pref.getString("email", null);
        String password = pref.getString("password", null);

        if (email != null && password != null) {
            email_edittext.setText(email);
            password_edittext.setText(password);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onClick(View v) {
        if (!email_edittext.getText().toString().equals("") && !password_edittext.getText().toString().equals("")) {
            switch (v.getId()) {
                case R.id.signin_button:
                    Log.d(TAG, "Button pressed!");
                    Task<AuthResult> signin = mAuth.signInWithEmailAndPassword(email_edittext.getText().toString(), password_edittext.getText().toString());

                    final Context context = v.getContext();

                    signin.addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            // If you successfully login, alert the user
                            Toast toast = Toast.makeText(getActivity().getApplicationContext(), "Successfully signed in!", Toast.LENGTH_SHORT);
                            toast.show();
                            // Put the correct information inside a sharedpreferences
                            editor.putString("email", email_edittext.getText().toString());
                            editor.putString("password", password_edittext.getText().toString());
                            editor.commit();

                            pref.edit().remove("cache").apply();

                            Intent intent = new Intent(context, MainActivity.class);
                            startActivity(intent);
                        }
                    });

                    signin.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });
                    break;
                case R.id.register_button:
                    Task<AuthResult> createaccount = mAuth.createUserWithEmailAndPassword(email_edittext.getText().toString(), password_edittext.getText().toString());

                    createaccount.addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {

                            Task<AuthResult> signin = mAuth.signInWithEmailAndPassword(email_edittext.getText().toString(), password_edittext.getText().toString());

                            signin.addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                @Override
                                public void onSuccess(AuthResult authResult) {
                                    // Alert the user that their account was created and logged into
                                    Toast toast = Toast.makeText(getActivity().getApplicationContext(), "Successfully created & signed in!", Toast.LENGTH_SHORT);
                                    toast.show();
                                    // Put the credentials in a sharedpreferences
                                    editor.putString("email", email_edittext.getText().toString());
                                    editor.putString("password", password_edittext.getText().toString());
                                    editor.commit();
                                    // Redirect them back home

                                    android.support.v4.app.FragmentTransaction ft = getFragmentManager().beginTransaction();
                                    ft.replace(R.id.fragmentcontent, new TimerFragment());
                                    ft.commit();

                                    MainActivity.mBottomBar.selectTabAtPosition(0, true);
                                }
                            });

                            signin.addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getActivity().getApplicationContext(), "Created account, but failed to signin", Toast.LENGTH_LONG).show();
                                }
                            });

                        }
                    });

                    createaccount.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getActivity().getApplicationContext(), "Failed to make account!", Toast.LENGTH_LONG).show();
                        }
                    });


                    break;
            }
        }
        if (email_edittext.getText().toString().equals("") && password_edittext.getText().toString().equals("")) {
            Toast.makeText(getActivity().getApplicationContext(), "Please enter email and password", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
