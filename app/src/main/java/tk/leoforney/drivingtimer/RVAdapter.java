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

    ArrayList<Drive> drives;

    String TimeLength;

    CoordinatorLayout coordinatorLayout;

    RVAdapter(ArrayList<Drive> drives, CoordinatorLayout coordinatorLayout) {
        this.drives = drives;
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
        Drive drive = drives.get(i);
        driveViewHolder.DateTextView.setText(drive.Date);
        String HoursString = Long.toString(drive.Hours);
        String MinutesString = Long.toString(drive.Minutes);
        String SecondsString = Long.toString(drive.Seconds);

        TimeLength = HoursString + " h, " + MinutesString + " m, " + SecondsString + " s";
        Log.d("RVAdapter", TimeLength);

        driveViewHolder.LengthTextView.setText(TimeLength);

        CardViewDelete cardviewDelete = new CardViewDelete(coordinatorLayout);
        cardviewDelete.setDate(drive.Date);

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
