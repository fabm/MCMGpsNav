package pt.ipg.mcm.mcmgpsnav.app.activities;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import com.google.android.gms.maps.model.LatLng;
import org.json.JSONException;
import pt.ipg.mcm.mcmgpsnav.app.GpsNavStatus;
import pt.ipg.mcm.mcmgpsnav.app.R;
import pt.ipg.mcm.mcmgpsnav.app.Utils;
import pt.ipg.mcm.mcmgpsnav.app.db.FluentBD;
import pt.ipg.mcm.mcmgpsnav.app.db.gen.CoordAndCompass;
import pt.ipg.mcm.mcmgpsnav.app.db.gen.DaoSession;

import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * angulo entre dois vetores @see <a href="http://www.wolframalpha.com/input/?i=vectorangle%5B%7B1%2C+0%7D%2C+%7Ba%2C+b%7D%5D">wolfram</a>
 */
public class MainActivity extends AbstractAdkActivity implements LocationListener, SensorEventListener {
    private Location lastLocation;
    private Location nextLocation;
    private float lastAngle;
    private SensorManager sensorManager;
    private int state;
    private Timer timer;
    private TimerTask schedule;
    private Handler handler = new Handler();
    private FluentBD fluentBD = new FluentBD();


    @Override
    protected void doOnCreate(Bundle savedInstanceState) {

        nextLocation = new Location(LocationManager.GPS_PROVIDER);
        nextLocation.setLatitude(40.542017);
        nextLocation.setLongitude(-7.281506);
        nextLocation.setTime(System.currentTimeMillis());

        setContentView(R.layout.activity_main);
        initLocationListener();
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        // for the system's orientation sensor registered listeners
        fluentBD.init(this);

        final Button btLimparBd = (Button) findViewById(R.id.btLimparBd);

        btLimparBd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fluentBD.deleteAllInTx(CoordAndCompass.class);
            }
        });


        Button btCoordsAndCompass = (Button) findViewById(R.id.btCoordsAndCompass);

        btCoordsAndCompass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DataBaseListActivity.class);
                startActivity(intent);
            }
        });

        final ToggleButton tgActivarTimer = (ToggleButton) findViewById(R.id.tgTimer);


        tgActivarTimer.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                switchTimer(isChecked);
            }
        });
        timer = new Timer();
        switchTimer(tgActivarTimer.isChecked());

        Button btExportar = (Button) findViewById(R.id.btExportar);

        btExportar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                try {
                    sendIntent.putExtra(Intent.EXTRA_TEXT, fluentBD.serilizeJson());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                sendIntent.setType("text/json");
                if (sendIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(sendIntent);
                }
            }
        });

        Button btDefDestino = (Button) findViewById(R.id.btDefDestino);
        btDefDestino.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,MapsActivity.class);
                startActivityForResult(intent, 2);
            }
        });
    }

    protected void switchTimer(boolean activated) {
        if (activated) {
            startTimer();
        } else {
            stopTimer();
        }
    }

    protected void stopTimer() {
        timer.cancel();
    }

    @Override
    protected void doAdkRead(String stringIn) {
        if (stringIn.equals("corrdir")) {
            correctDirection();
        }
    }

    private void correctDirection() {
        final float degrees = Utils.normalizeAngle(lastAngle, lastLocation.bearingTo(nextLocation));
        if (degrees < 2) {
            state = GpsNavStatus.MOVE;
            writeAdk("" + GpsNavStatus.MOVE + degrees);
        } else {
            state = GpsNavStatus.ROTATE;
            writeAdk("" + GpsNavStatus.ROTATE + degrees);
        }
    }


    private void initLocationListener() {
        LocationManager locationManager = (LocationManager)
            getSystemService(LOCATION_SERVICE);
        List<String> allProviders = locationManager.getAllProviders();

        for (String provider : allProviders) {
            locationManager.requestLocationUpdates(provider, 0, 0, this);
        }
    }


    @Override
    public void onLocationChanged(Location location) {
        lastLocation = location;
        if (location.distanceTo(nextLocation) < 1f) {
            writeAdk("" + GpsNavStatus.STOP);
            state = GpsNavStatus.STOP;
        }
        TextView tvLatitude = (TextView) findViewById(R.id.tvLatitude);
        tvLatitude.setText("" + location.getLatitude());
        TextView tvLongitude = (TextView) findViewById(R.id.tvLongitude);
        tvLongitude.setText("" + location.getLongitude());
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
        // get the angle around the z-axis rotated
        lastAngle = Math.round(event.values[0]);
        TextView tvLatitude = (TextView) findViewById(R.id.tvCurrentDegrees);
        tvLatitude.setText("" + event.values[0]);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // for the system's orientation sensor registered listeners
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
            SensorManager.SENSOR_DELAY_GAME);
    }

    public void startTimer() {
        initTimerTask();
        timer.schedule(schedule, 1000, 1000);
    }

    private void initTimerTask() {
        schedule = new TimerTask() {
            public void run() {

                //use a handler to run a toast that shows the current timestamp
                handler.post(new Runnable() {
                    public void run() {
                        final DaoSession daoSession = fluentBD.getDaoSession();
                        if (lastLocation != null) {

                            daoSession.runInTx(new Runnable() {
                                @Override
                                public void run() {
                                    CoordAndCompass coord = new CoordAndCompass();
                                    coord.setDate(new Date());
                                    coord.setDegrees(lastAngle);
                                    coord.setLatitude(lastLocation.getLatitude());
                                    coord.setLongitude(lastLocation.getLongitude());
                                    daoSession.getCoordAndCompassDao().insert(coord);
                                }
                            });
                        }
                    }
                });
            }
        };
    }

    @Override
    protected void onPause() {
        super.onPause();
        // to stop the listener and save battery
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //not used
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        fluentBD.close();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        LatLng latLng = data.getParcelableExtra("latLng");
        Toast.makeText(this,"lat:"+latLng.latitude+":long:"+latLng.longitude,Toast.LENGTH_LONG).show();
    }
}