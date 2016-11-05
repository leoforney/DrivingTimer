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

    RecyclerView recycler;

    static String TAG;

    CoordinatorLayout coordinatorLayout;

    TextView totalTextView;

    static SharedPreferences pref;

    static HashMap<String, ArrayList<Integer>> drives;

    final static String PREF_KEY = "DRIVE_PREF_KEY";



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
                final FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {

                    final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReferenceFromUrl("https://drives-timer.firebaseio.com/");

                    mDatabase.child(user.getUid()).child("drives").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            ArrayList<Drive> drivesList = null;
                            drives = MainActivity.drives;

                            if (drives != null) {

                                String totalTime = getTotalTimeStringFromDatabase(mDatabase, user);
                                NoDrivesSnackbar.dismiss();
                                totalTextView.setVisibility(View.VISIBLE);

                                drivesList = new ArrayList<>(drives.size());

                                if (drives.size() > 0) {
                                    for (int i = 0; i < drives.size(); i++) {
                                        String Key = String.valueOf(drives.keySet().toArray()[i]);

                                        int Hours = drives.get(Key).get(0);
                                        int Minutes = drives.get(Key).get(1);
                                        int Seconds = drives.get(Key).get(2);
                                        drivesList.add(new Drive(Key, Hours, Minutes, Seconds));
                                    }
                                }

                                if (totalTime != null) {
                                    totalTextView.setText(compileTotalTimeString());
                                } else {
                                    totalTextView.setText(totalTime);
                                }

                                mDatabase.child(user.getUid()).child("totaltime").setValue(compileTotalTimeString());

                                if (drivesList.size() == 0) {
                                    totalTextView.setVisibility(View.GONE);
                                    NoDrivesSnackbar.show();
                                } else {
                                    totalTextView.setVisibility(View.VISIBLE);
                                }

                                totalTextView.setText(compileTotalTimeString());

                            } else {
                                NoDrivesSnackbar.show();
                            }

                            RVAdapter adapter = new RVAdapter(drivesList, coordinatorLayout);
                            recycler.setAdapter(adapter);
                            adapter.notifyDataSetChanged();



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
    String totalTime;

    String getTotalTimeStringFromDatabase(DatabaseReference reference, FirebaseUser user) {
        reference.child(user.getUid()).child("totaltime").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                totalTime = String.valueOf(dataSnapshot.getValue());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return totalTime;
    }



    String compileTotalTimeString() {
        int TotalSeconds = 0;
        int THours = 0;
        int TMinutes = 0;
        int TSeconds = 0;
        for (int i = 0; i < drives.size(); i++) {
            Object Key = drives.keySet().toArray()[i];

            int Hours = drives.get(Key).get(0);
            int Minutes = drives.get(Key).get(1);
            int Seconds = drives.get(Key).get(2);

            TotalSeconds += Seconds;
            TotalSeconds += Minutes * 60;
            TotalSeconds += Hours * 3600;
        }

        THours = TotalSeconds / 3600;
        TMinutes = (TotalSeconds % 3600) / 60;
        TSeconds = TotalSeconds % 60;

        return "Total: " +
                Long.toString(THours) + " h, " +
                Long.toString(TMinutes) + " m, " +
                Long.toString(TSeconds) + " s";
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
