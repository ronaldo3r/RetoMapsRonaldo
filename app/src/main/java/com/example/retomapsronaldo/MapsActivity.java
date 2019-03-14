package com.example.retomapsronaldo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final int REQUEST_CODE = 11;
    private GoogleMap mapa;
    private LocationManager manager;
    private ArrayList<Marker> marcadores;

    //Diálogo
    private Dialog dialog_agregar;
    private TextView txt_dialog;
    private EditText edit_nombre;
    private Button btn_cancelar;
    private Button btn_agregar;


    private LatLng latLongitud;
    private boolean first;

    //Más cercano
    private Marker inicio;
    private Marker masCercano;
    private TextView lugarCercano;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapa);
        mapFragment.getMapAsync(this);

        manager = (LocationManager) getSystemService(LOCATION_SERVICE);

        marcadores = new ArrayList<Marker>();

        //Dialogo
        dialog_agregar = new Dialog(this);
        dialog_agregar.setContentView(R.layout.dialogo_agregar);
        txt_dialog = dialog_agregar.findViewById(R.id.txt_dialog);
        edit_nombre = dialog_agregar.findViewById(R.id.edit_name);
        btn_cancelar = dialog_agregar.findViewById(R.id.btn_cancel);
        btn_agregar = dialog_agregar.findViewById(R.id.btn_add);

        //Mas cercano
        lugarCercano = findViewById(R.id.txt_cercano);
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

        first=false;
        //Agregar el listener de ubicacion
        manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.e(">>>", "LAT: " + location.getLatitude() + " , LONG: " + location.getLongitude());

                if (first==false) {
                    inicio = mapa.addMarker(new MarkerOptions()
                            .position(new LatLng(location.getLatitude(), location.getLongitude()))
                            .title("Mi posición actual").icon(BitmapDescriptorFactory.fromResource(R.drawable.iconperson))
                    );
                    mapa.moveCamera(CameraUpdateFactory
                            .newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15));
                    first=true;
                }else{

                    inicio.remove();
                    inicio = mapa.addMarker(new MarkerOptions()
                            .position(new LatLng(location.getLatitude(), location.getLongitude()))
                            .title("Mi posición actual").icon(BitmapDescriptorFactory.fromResource(R.drawable.iconperson))
                    );
                    mapa.moveCamera(CameraUpdateFactory
                            .newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15));

                    Toast.makeText(getApplicationContext(), "Ubicación actual actualizada", Toast.LENGTH_LONG).show();

                }


                if (!marcadores.isEmpty()) {
                    String mens = estaCerca();
                    lugarCercano.setText(mens);
                }

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

                latLongitud = latLng;
                agregarMarcador();

            }
        });

        mapa.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                if (marker.equals(inicio)) {

                    try {
                        Geocoder geo = new Geocoder(MapsActivity.this.getApplicationContext(), Locale.getDefault());
                        List<Address> addresses = geo.getFromLocation(inicio.getPosition().latitude, marker.getPosition().longitude, 1);
                        if (addresses.isEmpty()) {
                            Toast.makeText(getApplicationContext(), "Esperando por la dirección...", Toast.LENGTH_LONG).show();
                        } else {
                            if (addresses.size() > 0) {
                                marker.setSnippet("Usted se encuentra en " + addresses.get(0).getAddressLine(0) + addresses.get(0).getAdminArea());
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace(); // getFromLocation() may sometimes fail
                    }


                } else {

                    int distancia = calcularDistanciaMarcadores(marker);

                    marker.setSnippet("Para llegar a este lugar faltan " + distancia + " metros");
                }
                return false;
            }
        });

    }

    //Método para agregar marcador
    public void agregarMarcador() {

        btn_cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog_agregar.dismiss();
            }
        });

        btn_agregar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((edit_nombre.getText().toString() != null) && !(edit_nombre.getText().toString().equals(""))&&!(edit_nombre.getText().toString().trim().equals(""))) {
                    Marker actual = mapa.addMarker(new MarkerOptions()
                            .position(latLongitud)
                            .title(edit_nombre.getText().toString()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));
                    marcadores.add(actual);
                    String mens = estaCerca();
                    lugarCercano.setText(mens);
                    edit_nombre.setText("");
                    dialog_agregar.dismiss();
                }
            }
        });

        dialog_agregar.show();
    }

    //Calcular marcador mas cercano a tu posición actual
    public Marker calcularMasCercano() {

        Marker marMenor = null;
        double disMenor = 0;
        Location miPos = new Location("Mi posición");
        miPos.setLatitude(inicio.getPosition().latitude);
        miPos.setLongitude(inicio.getPosition().longitude);


        for (int i = 0; i < marcadores.size(); i++) {

            Marker temporal = marcadores.get(i);

            Location pos = new Location("Otra posición");
            pos.setLatitude(temporal.getPosition().latitude);
            pos.setLongitude(temporal.getPosition().longitude);

            double disTemporal = miPos.distanceTo(pos);

            if (i == 0) {
                disMenor = miPos.distanceTo(pos);
                marMenor = temporal;
            }

            if (disMenor > disTemporal) {
                disMenor = disTemporal;
                marMenor = temporal;
            }

        }

        return marMenor;
    }

    //Calcular distancia entre posición actual y otro marcador
    public int calcularDistanciaMarcadores(Marker uno) {

        double distancia = 0;
        int dist = 0;

        Location miPos = new Location("Otra posición");
        miPos.setLatitude(inicio.getPosition().latitude);
        miPos.setLongitude(inicio.getPosition().longitude);

        Location pos = new Location("Otra posición");
        pos.setLatitude(uno.getPosition().latitude);
        pos.setLongitude(uno.getPosition().longitude);

        distancia = miPos.distanceTo(pos);
        dist = (int) distancia;

        return dist;
    }

    public String estaCerca() {

        String mensaje = "";

        masCercano = calcularMasCercano();

        int distancia = calcularDistanciaMarcadores(masCercano);

        if (distancia <= 500) {

            mensaje = "Para llegar a " + masCercano.getTitle() + " faltan " + distancia + " metros";
        }
        else if(distancia<=100){
            mensaje="Usted se encuentra en "+masCercano.getTitle();
        }
        else {

            mensaje = "El lugar más cercano es:" + masCercano.getTitle();
        }

        return mensaje;

    }
}
