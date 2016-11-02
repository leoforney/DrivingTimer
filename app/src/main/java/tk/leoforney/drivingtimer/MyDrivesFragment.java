package tk.leoforney.drivingtimer;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class MyDrivesFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    static RecyclerView recycler;

    static String TAG;

    static CoordinatorLayout coordinatorLayout;

    static TextView totalTextView;

    static SharedPreferences pref;

    static HashMap<String, ArrayList<Long>> drives;

    final static String PREF_KEY = "DRIVE_PREF_KEY";

    static long TotalSeconds;

    static long THours;
    static long TMinutes;
    static long TSeconds;

    public MyDrivesFragment() {
        // Required empty public constructor
    }

    static Snackbar NoDrivesSnackbar;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        View v = getView();

        coordinatorLayout = (CoordinatorLayout) v.findViewById(R.id.snackbarPosition);

        pref = getActivity().getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE);
        if (pref.getString("email", null) == null && (pref.getString("password", null) == null)) {
            MainActivity.mBottomBar.selectTabAtPosition(2, true);
        }

        NoDrivesSnackbar = Snackbar.make(coordinatorLayout, "You have no drives!", Snackbar.LENGTH_INDEFINITE);

        TAG = "DrivesFragment";

        recycler = (RecyclerView) v.findViewById(R.id.rv);
        recycler.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity().getApplicationContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recycler.setLayoutManager(llm);

        totalTextView = (TextView) v.findViewById(R.id.totalTextView);

        MainActivity.mAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {

                    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReferenceFromUrl("https://drives-timer.firebaseio.com/");

                    mDatabase.child(user.getUid()).child("drives").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            ArrayList<ArrayList<Long>> drivesList = null;
                            String[] datesArray = new String[0];
                            drives = MainActivity.drives;

                            if (drives != null) {
                                NoDrivesSnackbar.dismiss();
                                totalTextView.setVisibility(View.VISIBLE);

                                drivesList = new ArrayList<>(drives.size());
                                datesArray = new String[drives.size()];

                                TotalSeconds = 0;

                                for (int i = 0; i < drives.size(); i++) {
                                    Object Key = drives.keySet().toArray()[i];

                                    ArrayList<Long> timeList = new ArrayList<>(3);

                                    long Hours = drives.get(Key).get(0);
                                    long Minutes = drives.get(Key).get(1);
                                    long Seconds = drives.get(Key).get(2);

                                    Log.d(TAG, "H: " + String.valueOf(Hours) + ", M: " + String.valueOf(Minutes) + ", S: " + Seconds);

                                    TotalSeconds += Seconds;
                                    TotalSeconds += Minutes * 60;
                                    TotalSeconds += Hours * 3600;

                                    timeList.add(0, Hours);
                                    timeList.add(1, Minutes);
                                    timeList.add(2, Seconds);

                                    drivesList.add(i, timeList);

                                    datesArray[i] = Key.toString();

                                }

                                THours = TotalSeconds / 3600;
                                TMinutes = (TotalSeconds % 3600) / 60;
                                TSeconds = TotalSeconds % 60;

                                if (drivesList.size() == 0) {
                                    totalTextView.setVisibility(View.GONE);
                                    NoDrivesSnackbar.show();
                                } else {
                                    totalTextView.setVisibility(View.VISIBLE);

                                    RVAdapter adapter = new RVAdapter(drivesList, datesArray, coordinatorLayout);
                                    recycler.setAdapter(adapter);
                                    adapter.notifyDataSetChanged();
                                }

                            }

                            RVAdapter adapter = new RVAdapter(drivesList, datesArray, coordinatorLayout);
                            recycler.setAdapter(adapter);
                            adapter.notifyDataSetChanged();

                            totalTextView.setText("Total: " +
                                    Long.toString(THours) + " h, " +
                                    Long.toString(TMinutes) + " m, " +
                                    Long.toString(TSeconds) + " s");

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


                    Log.d(TAG, "Data changed!");
                }

            }
        });


    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_drives, container, false);
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
