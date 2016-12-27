package tk.leoforney.drivingtimer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TimerFragment extends Fragment implements View.OnClickListener {

    Date StartDate;

    Button StartButton;
    Button StopButton;
    static Button UploadButton;
    TextView timeTextView;

    String DayMonthYearHourMinute;

    String TimeLength;

    long hours;
    long minutes;
    long seconds;

    ArrayList<Long> TimeArrayList = new ArrayList<>(3);

    //Drive drive;

    boolean StopButtonPressed = false;

    CoordinatorLayout coordinatorLayout;

    String TAG;

    private SharedPreferences pref;
    private final static String KEY = "DRIVE_PREF_KEY";

    private OnFragmentInteractionListener mListener;

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
            UploadButton = (Button) v.findViewById(R.id.UploadButton);
            UploadButton.setOnClickListener(this);
            timeTextView = (TextView) v.findViewById(R.id.timeTextView);

            TAG = "TimerFragment";

            coordinatorLayout = (CoordinatorLayout) v.findViewById(R.id.snackbarPosition);

            ScheduledExecutorService scheduleTaskExecutor = Executors.newScheduledThreadPool(5);

            // This schedule a runnable task every 2 minutes
            scheduleTaskExecutor.scheduleAtFixedRate(new Runnable() {
                public void run() {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if ((StartDate != null) && (!StopButtonPressed)) {
                                Date nowTime = new Date();
                                long diff = nowTime.getTime() - StartDate.getTime();//as given

                                long totalSecs = TimeUnit.MILLISECONDS.toSeconds(diff);

                                hours = totalSecs / 3600;
                                minutes = (totalSecs % 3600) / 60;
                                seconds = totalSecs % 60;

                                if (minutes < 1 && hours < 1) {
                                    timeTextView.setText(String.valueOf(seconds) + " s");
                                }
                                if (hours < 1 && minutes > 0) {
                                    timeTextView.setText(String.valueOf(minutes) + " m");
                                }
                                if (hours > 0) {
                                    timeTextView.setText(String.valueOf(hours) + " h, " + String.valueOf(minutes) + " m");
                                }

                                String hoursstring = Long.toString(hours);
                                //Log.d(TAG, hoursstring);
                                String minutesstring = Long.toString(minutes);
                                //Log.d(TAG, minutesstring);
                                String secondsstring = Long.toString(seconds);
                                //Log.d(TAG, secondsstring);

                                TimeLength = hoursstring + " h, " + minutesstring + " m, " + secondsstring + " s";
                                //Log.w(TAG, TimeLength);

                            }
                            if (!StopButtonPressed && StartDate != null) {
                                StartButton.setVisibility(View.GONE);
                                StopButton.setVisibility(View.VISIBLE);
                            } else {
                                StartButton.setVisibility(View.VISIBLE);
                                StopButton.setVisibility(View.GONE);
                            }
                        }
                    });
                }
            }, 0, 1, TimeUnit.SECONDS);
        }


    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_timer, container, false);

    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
        if (StopButtonPressed) {
            StopButtonPressed = false;
        }
        DateFormat dmyhm = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss", Locale.US);
        StartDate = new Date();
        DayMonthYearHourMinute = dmyhm.format(StartDate);

    }

    public void stopTime() {
        StopButtonPressed = true;

        //drive = new Drive(DayMonthYearHourMinute, hours, minutes, seconds);

        if (TimeArrayList.size() >= 3) {
            TimeArrayList.set(0, hours);
            TimeArrayList.set(1, minutes);
            TimeArrayList.set(2, seconds);
        }
        if (TimeArrayList.size() == 0) {
            TimeArrayList.add(0, hours);
            TimeArrayList.add(1, minutes);
            TimeArrayList.add(2, seconds);
        }


        upload();

    }

    public void upload() {
        Task<Void> driveTask = MainActivity.addDrive(DayMonthYearHourMinute, TimeArrayList);
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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
