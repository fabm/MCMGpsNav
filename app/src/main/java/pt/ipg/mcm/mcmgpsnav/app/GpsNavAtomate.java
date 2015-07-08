package pt.ipg.mcm.mcmgpsnav.app;

import android.location.Location;

public class GpsNavAtomate implements GpsNav {
    @GpsNav.Status
    private int state;
    private Location currentPos;
    private float currentAngle;


    @Override
    public void stop(Location location) {
        state = STOPPED;
    }

    @Override
    public void gotoPos(Location location) {
        turnTo(location);
    }

    @Override
    public double angleToNorth() {
        return currentAngle;
    }

    @Override
    public void turnTo(Location location) {
        currentPos.bearingTo(location);
    }

    @Override
    public Location getLocation() {
        return currentPos;
    }

    @Override
    public void updateLocation(Location location) {
        this.currentPos = location;
    }

    @Override
    public void updateAngleToNorth(float angle) {
        this.currentAngle = angle;
    }

}
