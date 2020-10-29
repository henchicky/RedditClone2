package managedbean;

import java.util.Date;
import javax.inject.Named;
import javax.enterprise.context.RequestScoped;

@Named(value = "miscManagedBean")
@RequestScoped
public class MiscManagedBean {

    public MiscManagedBean() {
    }

    public String getElapsedTime(Date startDate) {

        Date currentDateTime = new Date();
        //milliseconds
        long different = currentDateTime.getTime() - startDate.getTime();

        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        long daysInMilli = hoursInMilli * 24;

        long elapsedDays = different / daysInMilli;
        different = different % daysInMilli;

        long elapsedHours = different / hoursInMilli;
        different = different % hoursInMilli;

        long elapsedMinutes = different / minutesInMilli;
        different = different % minutesInMilli;

        long elapsedSeconds = different / secondsInMilli;

        if (elapsedDays != 0) {
            return "" + elapsedDays + " days ago";
        } else if (elapsedHours != 0) {
            return "" + elapsedHours + " hours ago";
        } else if (elapsedMinutes != 0) {
            return "" + elapsedMinutes + " mins ago";
        } else {
            return "" + elapsedSeconds + " secs ago";
        }
    }
}
