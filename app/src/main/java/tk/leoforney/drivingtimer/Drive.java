package tk.leoforney.drivingtimer;

/**
 * Created by Dynamic Signals on 11/4/2016.
 */

public class Drive {
    String Date;
    int Hours;
    int Minutes;
    int Seconds;
    public Drive(String Date, int Hours, int Mintues, int Seconds) {
        this.Hours = Hours;
        this.Minutes = Mintues;
        this.Seconds = Seconds;
        this.Date = Date;
    }
}
