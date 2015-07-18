package pt.ipg.mcm.app.tests;

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.util.SparseArray;
import android.view.View;
import android.widget.TextView;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLocationManager;
import org.robolectric.shadows.ShadowSensorManager;
import pt.ipg.mcm.app.RobolectricGradleTestRunner;
import pt.ipg.mcm.app.instances.MyActivityTest;
import pt.ipg.mcm.mcmgpsnav.app.activities.MainActivity;
import pt.ipg.mcm.mcmgpsnav.app.R;
import pt.ipg.mcm.mcmgpsnav.app.Utils;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(RobolectricGradleTestRunner.class)
@Config(shadows = MyActivityTest.class)
public class ApplicationTest {

    private MyActivityTest myActivityTest;

    @Before
    public void setUp() {
        myActivityTest = (MyActivityTest) shadowOf(Robolectric.buildActivity(MainActivity.class).create().get());
    }

    @Test
    @TargetApi(Build.VERSION_CODES.CUPCAKE)
    public void confirmBearing() {

        LocationManager locationManager = (LocationManager)
            Robolectric.application.getSystemService(Context.LOCATION_SERVICE);
        ShadowLocationManager shadowLocationManager = shadowOf(locationManager);
        SensorManager sensorManager = (SensorManager)
            Robolectric.application.getSystemService(Context.SENSOR_SERVICE);
        ShadowSensorManager shadowSensorManager = shadowOf(sensorManager);

        Location location1 = location(LocationManager.GPS_PROVIDER, 40.542101, -7.281412);
        Location location2 = location(LocationManager.GPS_PROVIDER, 40.542017, -7.281506);
        Location location3 = location(LocationManager.GPS_PROVIDER, 40.541926, -7.281450);

        shadowLocationManager.simulateLocation(location1);

        Sensor sensor = Robolectric.newInstanceOf(Sensor.class);
        shadowSensorManager.addSensor(Sensor.TYPE_GYROSCOPE, sensor);

        SensorEvent sensorEvent = Robolectric.newInstance(SensorEvent.class, new Class[]{int.class}, new Integer[]{2});
        sensorEvent.values[0]=200;

        SparseArray<View> sparse = new SparseArray<View>();
        TextView tvLocation = mock(TextView.class);
        doNothing().when(tvLocation).setText(any(CharSequence.class));

        sparse.put(R.id.tvLocation,tvLocation);
        myActivityTest.setResources(sparse);

        myActivityTest.getActivity(MainActivity.class).onSensorChanged(sensorEvent);


        float angle1 = location1.bearingTo(location2);
        shadowLocationManager.simulateLocation(location2);


        float angle2 = location2.bearingTo(location3);


        Assert.assertEquals(20, Utils.normalizeAngle(sensorEvent.values[0], angle1), 10);
    }

    private Location location(String provider, double latitude, double longitude) {
        Location location = new Location(provider);
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        location.setTime(System.currentTimeMillis());
        return location;
    }

    @Test
    public void myGpsTest() {

    }
}


