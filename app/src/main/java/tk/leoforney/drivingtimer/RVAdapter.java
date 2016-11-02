package tk.leoforney.drivingtimer;

import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Leo on 6/22/2016.
 */
public class RVAdapter extends RecyclerView.Adapter<RVAdapter.DriveViewHolder> implements View.OnDragListener {

    @Override
    public boolean onDrag(View v, DragEvent event) {
        return false;
    }

    public static class DriveViewHolder extends RecyclerView.ViewHolder {

        TextView DateTextView;
        TextView LengthTextView;

        CardView cardView;



        DriveViewHolder(View itemView) {
            super(itemView);
            cardView = (CardView) itemView.findViewById(R.id.cardview);
            DateTextView = (TextView) itemView.findViewById(R.id.DateTextView);
            LengthTextView = (TextView) itemView.findViewById(R.id.LengthTextView);
            //cardView.setOnLongClickListener(new CardViewDelete());
        }
    }

    //List<Drive> drives;

    ArrayList<ArrayList<Long>> drives;
    String[] DateArray;

    String TimeLength;

    CoordinatorLayout coordinatorLayout;

    RVAdapter(ArrayList<ArrayList<Long>> drives, String[] dates, CoordinatorLayout coordinatorLayout) {
        this.drives = drives;
        this.DateArray = dates;
        this.coordinatorLayout = coordinatorLayout;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public DriveViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.drive_item, viewGroup, false);
        DriveViewHolder pvh = new DriveViewHolder(v);
        return pvh;
    }

    @Override
    public void onBindViewHolder(DriveViewHolder driveViewHolder, int i) {
        //String Date = drives.get(i).Date;
        String Date = DateArray[i];
        driveViewHolder.DateTextView.setText(Date);
        //Drive drive = drives.get(i);
        List<Long> drive = drives.get(i);
        long Hours = drive.get(0);
        String HoursString = Long.toString(Hours);
        long Minutes = drive.get(1);
        String MinutesString = Long.toString(Minutes);
        long Seconds = drive.get(2);
        String SecondsString = Long.toString(Seconds);

        TimeLength = HoursString + " h, " + MinutesString + " m, " + SecondsString + " s";
        Log.d("RVAdapter", TimeLength);

        driveViewHolder.LengthTextView.setText(TimeLength);

        CardViewDelete cardviewDelete = new CardViewDelete(coordinatorLayout);
        cardviewDelete.setDate(Date);

        driveViewHolder.cardView.setOnLongClickListener(cardviewDelete);

    }

    @Override
    public int getItemCount() {
        if (drives == null) {
            return 0;
        } else {
            return drives.size();
        }
    }
}
