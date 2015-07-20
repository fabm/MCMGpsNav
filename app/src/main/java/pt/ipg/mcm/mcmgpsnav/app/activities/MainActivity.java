package pt.ipg.mcm.mcmgpsnav.app.activities;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;
import com.google.android.gms.maps.model.LatLng;
import pt.ipg.mcm.mcmgpsnav.app.GpsNavStatus;
import pt.ipg.mcm.mcmgpsnav.app.R;
import pt.ipg.mcm.mcmgpsnav.app.Utils;
import pt.ipg.mcm.mcmgpsnav.app.db.FluentBD;
import pt.ipg.mcm.mcmgpsnav.app.db.gen.CoordAndCompass;
import pt.ipg.mcm.mcmgpsnav.app.db.gen.DaoSession;

import java.util.ArrayList;
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
    private int count = 0;
    private int state;
    private Timer timer;
    private TimerTask schedule;
    private Handler handler = new Handler();
    private FluentBD fluentBD = new FluentBD();


    @Override
    protected void doOnCreate(Bundle savedInstanceState) {

        TextView lblbearing = (TextView) findViewById(R.id.lblBearing);

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
        switchTimer(tgActivarTimer.isChecked());

        Button btExportar = (Button) findViewById(R.id.btExportar);

        btExportar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                fluentBD.save();


                AsyncTask<Void, Void, Void> at = new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... params) {
                        Intent sendIntent = new Intent();
                        sendIntent.setAction(Intent.ACTION_SEND);
                        sendIntent.putExtra(Intent.EXTRA_TEXT, fluentBD.serializeToJson());
                        sendIntent.setType("text/json");
                        if (sendIntent.resolveActivity(getPackageManager()) != null) {
                            startActivity(sendIntent);
                        }
                        return null;
                    }
                };

                at.execute();


            }
        });

        Button btDefDestino = (Button) findViewById(R.id.btDefDestino);
        btDefDestino.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
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
        if (timer != null) {
            timer.cancel();
        }
    }

    @Override
    protected void doAdkRead(String stringIn) {
        if (stringIn.equals("corrdir")) {
            correctDirection();
        }
    }

    private void correctDirection() {

        if(nextLocation == null){
            //writeAdk("X");
            return;
        }else {
            final float degrees = Utils.normalizeAngle(lastAngle, lastLocation.bearingTo(nextLocation));
            if (degrees - lastLocation.bearingTo(nextLocation) < 10) {
                state = GpsNavStatus.MOVE;
                //writeAdk("" + GpsNavStatus.MOVE + degrees);
            } else {
                state = GpsNavStatus.ROTATE;

                //writeAdk("R" + GpsNavStatus.ROTATE + degrees);
            }
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
        if (nextLocation == null || location.distanceTo(nextLocation) < 8f) {
           // writeAdk("" + GpsNavStatus.STOP);
            state = GpsNavStatus.STOP;
        }
        TextView tvLatitude = (TextView) findViewById(R.id.tvLatitude);
        tvLatitude.setText("" + location.getLatitude());
        TextView bearing = (TextView) findViewById(R.id.lblBearing);

        TextView tvLongitude = (TextView) findViewById(R.id.tvLongitude);
        tvLongitude.setText("" + location.getLongitude());

        TextView lblbearing = (TextView) findViewById(R.id.lblBearing);
        lblbearing.setText("" + location.getBearing());



        //------------------------------------------------

            Location startingLocation = new Location("starting point");
            startingLocation.setLatitude(location.getLatitude());
            startingLocation.setLongitude(location.getLongitude());

        if (nextLocation != null) {
            //Get the target location
            Location endingLocation = new Location("ending point");
            //endingLocation.setLatitude(41.538284);
            //endingLocation.setLongitude(-6.958638);
            // nextLocation.getLatitude(nextLocation.getLatitude());
            endingLocation.setLatitude(nextLocation.getLatitude());
            endingLocation.setLatitude(nextLocation.getLongitude());
            //Find the Bearing from current location to next location
            float targetBearing = startingLocation.bearingTo(endingLocation);
            bearing.setText("" + Utils.normalizeAngle(lastAngle, startingLocation.bearingTo(endingLocation)));
        }


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

        TextView tvDegreesToNext = (TextView) findViewById(R.id.tvMainDegreesToNext);
        if (nextLocation != null) {
            tvDegreesToNext.setText("" + lastLocation.bearingTo(nextLocation));
        }
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
        timer = new Timer();
        schedule = new TimerTask() {
            public void run() {

                //use a handler to run a toast that shows the current timestamp
                handler.post(new Runnable() {

                    public void run() {
                        final DaoSession daoSession = fluentBD.getDaoSession();
                        count++;
                        if (lastLocation != null) {
                            daoSession.runInTx(new Runnable() {
                                @Override
                                public void run() {
                                    insertCoordAndCompass(daoSession);
                                }
                            });
                        }
                        if(count>=30){
                            correctDirection();
                            //writeAdk("M");
                            count = 0;
                        }
                    }
                });
            }
        };
    }

    private void insertCoordAndCompass(DaoSession daoSession) {
        CoordAndCompass coord = new CoordAndCompass();
        coord.setDate(new Date());
        coord.setDegrees(lastAngle);
        coord.setLatitude(lastLocation.getLatitude());
        coord.setLongitude(lastLocation.getLongitude());

        if (nextLocation != null) {
            coord.setDegreesToNext(lastLocation.bearingTo(nextLocation));
            coord.setDistanceToNext(lastLocation.distanceTo(nextLocation));
            coord.setNextLat(nextLocation.getLatitude());
            coord.setNextLon(nextLocation.getLongitude());
            TextView tvMainDistance = (TextView) findViewById(R.id.tvMainDistance);
            tvMainDistance.setText(""+lastLocation.distanceTo(nextLocation));
        }

        daoSession.getCoordAndCompassDao().insert(coord);
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
        ArrayList<Parcelable> parcelables = data.getParcelableArrayListExtra("latLng");
        LatLng latLng = (LatLng) parcelables.get(0);

        nextLocation = new Location(LocationManager.GPS_PROVIDER);
        nextLocation.setLatitude(latLng.latitude);
        nextLocation.setLongitude(latLng.longitude);
        nextLocation.setTime(System.currentTimeMillis());

        TextView tvLatNext = (TextView) findViewById(R.id.tvMainLatNext);
        tvLatNext.setText("" + nextLocation.getLatitude());


        TextView tvLonNext = (TextView) findViewById(R.id.tvMainLonNext);
        tvLonNext.setText("" + nextLocation.getLongitude());

    }
}