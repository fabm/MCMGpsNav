package pt.ipg.mcm.mcmgpsnav.app.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import android.widget.Button;
import android.widget.Toast;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import pt.ipg.mcm.mcmgpsnav.app.R;
import pt.ipg.mcm.mcmgpsnav.app.utils.Reference;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private ArrayList<LatLng> latLngList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        init();
    }

    private void init() {
        setUpMapIfNeeded();
        latLngList = new ArrayList<LatLng>();
    }


    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        mMap.setMyLocationEnabled(true);

        final Reference<LatLng> latLngReference = new Reference<LatLng>();


        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                latLngReference.setRef(latLng);
                popupChoise(latLng);
            }
        });
    }

    private void popupChoise(final LatLng latLng) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
            .setTitle("Opção")
            .setMessage("Adicionar novo botão")
            .setPositiveButton("Adicionar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    addLatLng(latLng);
                }
            })
            .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    return;
                }
            });

        if(!latLngList.isEmpty()){
            builder.setNeutralButton("Finalizar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    returnResult();
                }
            });
        }
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void addLatLng(LatLng latLng) {
        mMap.addMarker(new MarkerOptions().position(latLng).title("Marker"));
        latLngList.add(latLng);
    }

    private void returnResult() {
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("latLng", latLngList);

        Intent result = new Intent();
        result.putExtras(bundle);
        setResult(Activity.RESULT_OK, result);
        finish();
    }
}
