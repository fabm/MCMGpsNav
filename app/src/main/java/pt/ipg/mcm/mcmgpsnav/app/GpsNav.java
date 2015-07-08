package pt.ipg.mcm.mcmgpsnav.app;

import android.location.Location;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public interface GpsNav {

    int STOPPED = 0;
    int MOVING = 1;
    int TURNING = 2;

    @IntDef({STOPPED, MOVING, TURNING})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Status {

    }

    void stop(Location location);

    void gotoPos(Location location);

    double angleToNorth();

    void turnTo(Location location);

    Location getLocation();

    void updateLocation(Location location);
    void updateAngleToNorth(float angle);
}
