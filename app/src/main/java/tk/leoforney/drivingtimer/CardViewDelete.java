package tk.leoforney.drivingtimer;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Leo on 6/24/2016.
 */
public class CardViewDelete implements View.OnLongClickListener {

    String Date = null;
    CoordinatorLayout coordinatorLayout;

    public CardViewDelete(CoordinatorLayout coordinatorLayout) {
        this.coordinatorLayout = coordinatorLayout;
    }

    public void setDate(String date) {
        this.Date = date;
    }

    private final static String KEY = "DRIVE_PREF_KEY";
    private FirebaseAuth mAuth;
    private DatabaseReference mRef;
    private FirebaseUser mUser;

    static Toast toast;

    HashMap<String, ArrayList<Long>> drives;

    @Override
    public boolean onLongClick(View v) {
        final Context context = v.getContext();

        final String TAG = "CardViewDelete";

        //coordinatorLayout = (CoordinatorLayout) v.findViewById(R.id.snackbarPosition);

        //Log.d("RVAdapter", Date);

        final FirebaseUser user = MainActivity.mAuth.getCurrentUser();
        if (user != null) {
            mRef = FirebaseDatabase.getInstance()
                    .getReferenceFromUrl("https://drives-timer.firebaseio.com/")
                    .child(user.getUid())
                    .child("drives");
            mRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    drives = dataSnapshot.getValue(new GenericTypeIndicator<HashMap<String, ArrayList<Long>>>() {
                    });

                    mRef.child(Date).setValue(null).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                        }
                    });
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


        }

        Log.d(TAG, "Toast has been dispatched");
        toast = Toast.makeText(context, "Deleted drive on day: " + Date, Toast.LENGTH_SHORT);
        toast.show();

        return true;
    }
}
