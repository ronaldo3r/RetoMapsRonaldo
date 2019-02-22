package com.example.retomapsronaldo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final int REQUEST_CODE = 11 ;
    private GoogleMap mapa;
    private LocationManager manager;
    private ArrayList<Marker> marcadores;
    private Marker inicio;
    public Dialog dialog_agregar;
    public TextView txt_dialog;
    public EditText edit_nombre;
    public Button btn_cancelar;
    public Button btn_agregar;
    public LatLng latLongitud;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapa);
        mapFragment.getMapAsync(this);

        manager = (LocationManager) getSystemService(LOCATION_SERVICE);

        dialog_agregar= new Dialog(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mapa = googleMap;

        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        }, REQUEST_CODE);


//        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(4, -72);
//        mapa.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mapa.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        //Agregar el listener de ubicacion
        manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.e(">>>","LAT: "+location.getLatitude()+ " , LONG: "+location.getLongitude());

                if(inicio != null){
                    inicio.remove();
                }
                inicio = mapa.addMarker(new MarkerOptions()
                        .position(new LatLng(location.getLatitude(), location.getLongitude()))
                        .title("Mi posición actual")
                );
                mapa.moveCamera(CameraUpdateFactory
                        .newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 10));
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }

        });

        mapa.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                latLongitud=latLng;
                agregarMarcador();
            }
        });

    }

    public void agregarMarcador(){

        dialog_agregar.setContentView(R.layout.dialogo_agregar);
        txt_dialog = findViewById(R.id.txt_dialog);
        edit_nombre = findViewById(R.id.edit_name);
        btn_cancelar = findViewById(R.id.btn_cancel);
        btn_agregar = findViewById(R.id.btn_add);

        btn_cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog_agregar.dismiss();
            }
        });

        btn_agregar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Marker actual = mapa.addMarker(new MarkerOptions()
                        .position(latLongitud)
                        .title(edit_nombre.getText().toString()));
                marcadores.add(actual);
                dialog_agregar.dismiss();
            }
        });


        dialog_agregar.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog_agregar.show();
    }
}
