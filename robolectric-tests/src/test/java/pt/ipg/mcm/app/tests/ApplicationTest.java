package pt.ipg.mcm.app.tests;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLocationManager;
import pt.ipg.mcm.app.RobolectricGradleTestRunner;
import pt.ipg.mcm.app.instances.MyActivityTest;
import pt.ipg.mcm.mcmgpsnav.app.MainActivity;
import pt.ipg.mcm.mcmgpsnav.app.Utils;

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
    public void confirmBearing() {



        LocationManager locationManager = (LocationManager)
            Robolectric.application.getSystemService(Context.LOCATION_SERVICE);
        ShadowLocationManager shadowLocationManager = shadowOf(locationManager);

        Location location1 = location(LocationManager.GPS_PROVIDER, 40.542452, -7.283403);
        Location location2 = location(LocationManager.GPS_PROVIDER, 40.541784, -7.284202);
        Location location3 = location(LocationManager.GPS_PROVIDER, 40.541294, -7.283459);

        shadowLocationManager.simulateLocation(location1);

        float angle1 = location1.bearingTo(location2);
        shadowLocationManager.simulateLocation(location2);

        float angle2 = location2.bearingTo(location3);

        float dif = angle1 - angle2;

        //correct to minor angle
        if (dif > 180) {
            dif = 360 - dif;
        }else if(dif<-180){
            dif = 360 + dif;
        }

        Assert.assertEquals(90,dif,5);
    }

    @Test
    public void angleNormalized(){
        float angle = 190f;

        Utils.normalizeAngle(angle);
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


