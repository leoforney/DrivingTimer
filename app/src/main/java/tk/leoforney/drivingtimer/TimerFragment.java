package tk.leoforney.drivingtimer;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Chronometer;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TimerFragment extends Fragment implements View.OnClickListener {

    Date StartDate;

    Button StartButton;
    Button StopButton;
    Button UploadButton;

    Chronometer chronometer;

    Drive drive;

    String DayMonthYearHourMinute;

    String TimeLength;

    CoordinatorLayout coordinatorLayout;

    private final static String TAG = TimerFragment.class.getName();

    private SharedPreferences pref;
    private final static String KEY = "DRIVE_PREF_KEY";

    public TimerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        hideKeyboard(getContext());

        View v = getView();

        if (v != null) {
            final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

            pref = getActivity().getSharedPreferences(KEY, Context.MODE_PRIVATE);
            if (pref.getString("email", null) == null && (pref.getString("password", null) == null)) {
                MainActivity.mBottomBar.selectTabAtPosition(2, true);
            }

            StartButton = (Button) v.findViewById(R.id.StartButton);
            StartButton.setOnClickListener(this);
            StopButton = (Button) v.findViewById(R.id.StopButton);
            StopButton.setOnClickListener(this);
            StopButton.setVisibility(View.GONE);

            UploadButton = (Button) v.findViewById(R.id.UploadButton);
            UploadButton.setOnClickListener(this);
            chronometer = (Chronometer) v.findViewById(R.id.chronometer);

            coordinatorLayout = (CoordinatorLayout) v.findViewById(R.id.snackbarPosition);

        }


    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_timer, container, false);

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.StartButton:
                startTime();
                break;
            case R.id.StopButton:
                stopTime();
                break;
            case R.id.UploadButton:
                upload();
                break;
        }
    }

    public void startTime() {
        StartButton.setVisibility(View.GONE);
        StopButton.setVisibility(View.VISIBLE);
        DateFormat dmyhm = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss", Locale.US);
        StartDate = new Date();
        DayMonthYearHourMinute = dmyhm.format(StartDate);
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();
    }

    public void stopTime() {
        StartButton.setVisibility(View.VISIBLE);
        StopButton.setVisibility(View.GONE);

        chronometer.stop();

        List<Integer> hms = getFromDurationString(chronometer.getText().toString());

        drive = new Drive(DayMonthYearHourMinute, hms.get(0), hms.get(1), hms.get(2));

        TimeLength = hms.get(0) + " h, " + hms.get(1) + " m, " + hms.get(2) + " s";

        upload();

    }

    // Expects a string in the form MM:SS or HH:MM:SS
    public static List<Integer> getFromDurationString(String value) {

        String[] parts = value.split(":");

        List<Integer> hms = new ArrayList<>();

        // Wrong format, no value for you.
        if (parts.length < 2 || parts.length > 3) {
            hms.add(0);
            hms.add(0);
            hms.add(0);
            return hms;
        }

        if (parts.length == 2) {
            hms.add(0, 0);
            hms.add(1, Integer.parseInt(parts[0]));
            hms.add(2, Integer.parseInt(parts[1]));
        } else if (parts.length == 3) {
            hms.add(0, Integer.parseInt(parts[0]));
            hms.add(1, Integer.parseInt(parts[1]));
            hms.add(2, Integer.parseInt(parts[2]));
        }

        return hms;
    }

    public void upload() {
        Task<Void> driveTask = MainActivity.addDrive(drive);
        if (driveTask != null) {
            UploadButton.setVisibility(View.INVISIBLE);
            driveTask.addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d(TAG, "Pushed data! :)");
                    Snackbar snackbar = Snackbar
                            .make(coordinatorLayout, "You drove for a total of: " + TimeLength + ", data uploaded to firebase!", Snackbar.LENGTH_LONG);

                    snackbar.show();
                }
            });
            driveTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "Upload failed");
                    Snackbar snackbar = Snackbar
                            .make(coordinatorLayout, "You drove for a total of: " + TimeLength + ", failed to upload to firebase!", Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
            });
        }
        if (driveTask == null) {
            UploadButton.setVisibility(View.VISIBLE);
        }


    }

    public static void hideKeyboard(Context ctx) {
        InputMethodManager inputManager = (InputMethodManager) ctx
                .getSystemService(Context.INPUT_METHOD_SERVICE);

        // check if no view has focus:
        View v = ((Activity) ctx).getCurrentFocus();
        if (v == null)
            return;

        inputManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }
}
