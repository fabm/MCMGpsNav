package pt.ipg.mcm.mcmgpsnav.app.activities;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import com.google.android.gms.maps.model.LatLng;

import pt.ipg.mcm.mcmgpsnav.app.R;
import pt.ipg.mcm.mcmgpsnav.app.db.FluentBD;
import pt.ipg.mcm.mcmgpsnav.app.db.gen.CoordAndCompass;
import pt.ipg.mcm.mcmgpsnav.app.db.gen.DaoSession;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

/**
 * angulo entre dois vetores @see <a href="http://www.wolframalpha.com/input/?i=vectorangle%5B%7B1%2C+0%7D%2C+%7Ba%2C+b%7D%5D">wolfram</a>
 */
public class MainActivity extends AbstractAdkActivity implements LocationListener, SensorEventListener {

    //BLUETOOTH-----------------------------------------
    private static final int REQUEST_ENABLE_BT = 1;
    BluetoothAdapter bluetoothAdapter;
    private UUID myUUID;
    private String myName;
    ThreadBeConnected myThreadBeConnected;
    ThreadConnected myThreadConnected;

    TextView textInfo;
    public EditText inputField;

    //----------------------------------------------------

    boolean espera = false;
    private float lastAngle;
    private SensorManager sensorManager;
    private int count = 0;
    private Timer timer;
    private TimerTask schedule;
    private Handler handler = new Handler();
    private FluentBD fluentBD = new FluentBD();

    Location pontoA= new Location("PA");;  //PONTO A DA CERCA
    Location pontoB= new Location("PB");;  //PONTO B DA CERCA
    Location lago = new Location("lago"); //PONTO DO LOAGO

    boolean automatico = false; //MODO AUTOMATICO INICIA A FALSE



    @Override
    protected void doOnCreate(Bundle savedInstanceState) {

        //BLUETOOTH-----------------------------------------
        myUUID = UUID.fromString("ec79da00-853f-11e4-b4a9-0800200c9a66");
        myName = myUUID.toString();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            Toast.makeText(this,
                    "Bluetooth is not supported on this hardware platform",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)){
            Toast.makeText(this,
                    "FEATURE_BLUETOOTH NOT support",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        textInfo = (TextView)findViewById(R.id.info);
        inputField = (EditText)findViewById(R.id.input);
        String stInfo = bluetoothAdapter.getName() + "\n" +
                bluetoothAdapter.getAddress();

        //------------------------------------------------------------

        //DAR VALORES AOS PONTOS DA CERCA
        pontoA.setLatitude(40.541316);
        pontoA.setLongitude(-7.283097);

        pontoB.setLatitude(40.541118);
        pontoB.setLongitude(-7.282898);

        //DAR VALORES AO PONTO LAGO
        lago.setLatitude( 40.541232);
        lago.setLongitude(-7.282982);



        setContentView(R.layout.activity_main);
        initLocationListener();
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        // for the system's orientation sensor registered listeners
        fluentBD.init(this);


        //BOTOES PARA COMADAR ROBOT-----------------------------------------------------------------------------
        //BOTÃO UP
        final Button up = (Button) findViewById(R.id.btnUp);
        up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!automatico) {
                    writeAdk("F");
                }
            }
        });

        //BOTAO BACK
        final Button back = (Button) findViewById(R.id.btnBack);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!automatico) {
                writeAdk("B");
                }
            }
        });

        //BOTAO LEFT
        final Button left = (Button) findViewById(R.id.btnLeft);
        left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!automatico) {
                    writeAdk("E");
                }
            }
        });

        //BOTAO RIGHT
        final Button right = (Button) findViewById(R.id.btnRight);
        right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!automatico) {
                    writeAdk("D");
                }
            }
        });

        //BOTAO PARAR
        final Button stop = (Button) findViewById(R.id.btnParar);
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!automatico) {
                    writeAdk("S");
                }
            }
        });
        //-----------------------------------------------------------------------------------------------

        //TOGGLE GPS MANUAL------------------------------------------------------------------------------
        final ToggleButton modo = (ToggleButton) findViewById(R.id.btnModo);

        modo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (modo.isChecked()) {
                    automatico = true;
                    writeAdk("G");
                } else {

                    automatico = false;
                    writeAdk("Z");
                }
            }
        });

        //----------------------------------------------------------------------------------------------






        final ToggleButton tgActivarTimer = (ToggleButton) findViewById(R.id.tgTimer);


        tgActivarTimer.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                switchTimer(isChecked);
            }
        });
        switchTimer(tgActivarTimer.isChecked());

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
        TextView estadoA = (TextView) findViewById(R.id.lblEstado);

        //SE ESTÁ NO LAGO
        if (location.distanceTo(lago) < 2f) {
            estadoA.setText("CHEGOU AO LAGO");
            if (automatico) {
                if (!espera) {
                    writeAdk("L");
                    espera = true;
                }
            }

        }

        //SE SAIU FORA DA CERCA
        if (location.getLatitude() > pontoA.getLatitude() || location.getLongitude() < pontoA.getLongitude()){
            estadoA.setText("FORA DA CERCA");
            if (automatico) {
                if (!espera) {
                    writeAdk("V");
                    espera = true;
                    TextView es;
                    es = (TextView) findViewById(R.id.lblEspera);
                    es.setText("ESPERA");
                }

            }
            if (myThreadConnected != null) {
                byte[] bytesToSend = "DOFESTA FORA DA CERCA".getBytes();
                myThreadConnected.write(bytesToSend);
            }
        }else  if (location.getLatitude() < pontoB.getLatitude() || location.getLongitude() > pontoB.getLongitude()){
            estadoA.setText("FORA DA CERCA");
            if (automatico) {
                if (!espera) {
                    writeAdk("V");
                    espera = true;
                    TextView es;
                    es = (TextView) findViewById(R.id.lblEspera);
                    es.setText("ESPERA");
                }
            }
            if (myThreadConnected != null) {
                byte[] bytesToSend = "DOFESTA FORA DA CERCA".getBytes();
                myThreadConnected.write(bytesToSend);
            }
        }
        else{
            estadoA.setText("DENTRO DA CERCA");


            if (myThreadConnected != null) {
                byte[] bytesToSend = "DOFESTA DENTRO DA CERCA".getBytes();
                myThreadConnected.write(bytesToSend);
            }
        }

        TextView tvLatitude = (TextView) findViewById(R.id.tvLatitude);
        tvLatitude.setText("" + location.getLatitude());

        TextView tvLongitude = (TextView) findViewById(R.id.tvLongitude);
        tvLongitude.setText("" + location.getLongitude());

        //MEDIR DISTANCIA AO LAGO
        TextView lagodist = (TextView) findViewById(R.id.lblDistLago);
        lagodist.setText("" + location.distanceTo(lago));

        //ESCREVER O PONTO DO LAGO
        TextView tvLatNext = (TextView) findViewById(R.id.lblLatLago);
        tvLatNext.setText("" + lago.getLatitude());
        TextView tvLonNext = (TextView) findViewById(R.id.lblLongLago);
        tvLonNext.setText("" + lago.getLongitude());


        String lat;
        lat = "" + location.getLatitude();
        lat = lat.substring(0, 9);

        String lon;
        lon = "" + location.getLongitude();
        lon= lon.substring(0, 9);

        String latL;
        latL = "" + lago.getLatitude();
        latL= latL.substring(0, 9);

        String lonL;
        lonL = "" + lago.getLongitude();
        lonL= lonL.substring(0, 9);

        String finall = lat+lon+latL+lonL;
        if (myThreadConnected != null) {
            byte[] bytesToSend = finall.getBytes();
            myThreadConnected.write(bytesToSend);
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

                        if (espera){
                        count++;

                        if(count>=10){
                           espera = false;
                            count = 0;
                        }
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
    protected void onStart() {
        super.onStart();

        //Turn ON BlueTooth if it is OFF
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }

        setup();
    }

    private void setup() {

        myThreadBeConnected = new ThreadBeConnected();
        myThreadBeConnected.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {


    }


    //BLUETOOTH--------------------------------------------------------------------------------------------------------

    private void startThreadConnected(BluetoothSocket socket){

        myThreadConnected = new ThreadConnected(socket);
        myThreadConnected.start();
    }


    private class ThreadBeConnected extends Thread {

        private BluetoothServerSocket bluetoothServerSocket = null;

        public ThreadBeConnected() {
            try {
                bluetoothServerSocket =
                        bluetoothAdapter.listenUsingRfcommWithServiceRecord(myName, myUUID);

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            BluetoothSocket bluetoothSocket = null;

            if(bluetoothServerSocket!=null){
                try {
                    bluetoothSocket = bluetoothServerSocket.accept();

                    BluetoothDevice remoteDevice = bluetoothSocket.getRemoteDevice();

                    final String strConnected = "Connected:\n" +
                            remoteDevice.getName() + "\n" +
                            remoteDevice.getAddress();

                    //connected
                    runOnUiThread(new Runnable(){

                        @Override
                        public void run() {

                        }});

                    startThreadConnected(bluetoothSocket);

                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();

                    final String eMessage = e.getMessage();
                    runOnUiThread(new Runnable(){

                        @Override
                        public void run() {
                           // textStatus.setText("something wrong: \n" + eMessage);
                        }});
                }
            }else{
                runOnUiThread(new Runnable(){

                    @Override
                    public void run() {
                        //textStatus.setText("bluetoothServerSocket == null");
                    }});
            }
        }

        public void cancel() {

            Toast.makeText(getApplicationContext(),
                    "close bluetoothServerSocket",
                    Toast.LENGTH_LONG).show();

            try {
                bluetoothServerSocket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }




    private class ThreadConnected extends Thread {
        private final BluetoothSocket connectedBluetoothSocket;
        private final InputStream connectedInputStream;
        private final OutputStream connectedOutputStream;

        public ThreadConnected(BluetoothSocket socket) {
            connectedBluetoothSocket = socket;
            InputStream in = null;
            OutputStream out = null;

            try {
                in = socket.getInputStream();
                out = socket.getOutputStream();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            connectedInputStream = in;
            connectedOutputStream = out;
        }

        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            while (true) {
                try {
                    bytes = connectedInputStream.read(buffer);

                    final String strReceived = new String(buffer, 0, bytes);
                    final String msgReceived = String.valueOf(bytes);

                    runOnUiThread(new Runnable(){

                        @Override
                        public void run() {
                            EditText inputField;
                            inputField = (EditText)findViewById(R.id.input);
                            inputField.setText(strReceived);
                            writeAdk(strReceived);

                            if (strReceived.equals("G")){
                                if (!automatico){
                                    automatico = true;
                                }
                            }

                            if (strReceived.equals("Z")){
                                if (automatico){
                                    automatico = false;
                                }
                            }

                        }});

                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();

                    final String msgConnectionLost = "Connection lost:\n"
                            + e.getMessage();
                    runOnUiThread(new Runnable(){

                        @Override
                        public void run() {
                            //textStatus.setText(msgConnectionLost);
                        }});
                }
            }
        }

        public void write(byte[] buffer) {
            try {
                connectedOutputStream.write(buffer);
                connectedOutputStream.flush();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        public void cancel() {
            try {
                connectedBluetoothSocket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }
}