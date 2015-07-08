package pt.ipg.mcm.mcmgpsnav.app;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import java.util.List;

/**
 * angulo entre dois vetores @see <a href="http://www.wolframalpha.com/input/?i=vectorangle%5B%7B1%2C+0%7D%2C+%7Ba%2C+b%7D%5D">wolfram</a>
 */
public class MainActivity extends Activity implements LocationListener, SensorEventListener {
    private Location lastLocation;
    private SensorManager sensorManager;
    private GpsNav gpsNav = new GpsNavAtomate();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initLocationListener();
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
    }

    private void initLocationListener() {
        LocationManager locationManager = (LocationManager)
            getSystemService(Context.LOCATION_SERVICE);
        List<String> allProviders = locationManager.getAllProviders();

        for (String provider : allProviders) {
            locationManager.requestLocationUpdates(provider, 0, 0, this);
        }
    }


    @Override
    public void onLocationChanged(Location location) {
        gpsNav.updateLocation(location);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Override
    public void onProviderEnabled(String s) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        gpsNav.updateAngleToNorth(Math.round(event.values[0]));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


}