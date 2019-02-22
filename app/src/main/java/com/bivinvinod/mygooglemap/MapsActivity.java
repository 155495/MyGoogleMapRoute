package com.bivinvinod.mygooglemap;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;
    private GoogleMap googleMap1;
    ArrayList<LatLng> listpoints1ong;
    ArrayList<LatLng> listpointslat;
    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    FloatingActionButton fab;
    Location loc;
    Location currentLoc;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        startLockTask();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        fab = findViewById(R.id.fab);

        listpoints1ong = new ArrayList<>();
        listpointslat = new ArrayList<>();


        //FAB CLICK EVENT BEGINS
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                AlertDialog.Builder builder1 = new AlertDialog.Builder(MapsActivity.this);
                builder1.setTitle("Report Anomaly");
                builder1.setIcon(R.drawable.ic_launcher_foreground);
                builder1.setMessage("Are you want to report Anomaly At \n \n" +
                        "Long:"+loc.getLongitude()+"Lat:"+loc.getLatitude());
                builder1.setCancelable(true);

                builder1.setPositiveButton(
                        "Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent i= new Intent(MapsActivity.this,ReportAnomalyActivity.class);
                                i.putExtra("LONGT",Double.parseDouble(String.valueOf(loc.getLongitude())));
                                i.putExtra("LAT",Double.parseDouble(String.valueOf(loc.getLatitude())));

                                startActivity(i);
                                dialog.cancel();
                            }
                        });

                builder1.setNegativeButton(
                        "No",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                AlertDialog alert11 = builder1.create();
                alert11.show();


                Log.d("MyLocation",loc.getLatitude()+"");



                //Toast.makeText(getApplicationContext(),"This is working",Toast.LENGTH_SHORT).show();
            }
        });


        //FAB CLICK EVENT ENDS

//***********************************************************************************************************************
        //ANDROID LOCATION PERMISSION CHECK BEGINS


        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,

        };

        if(!hasPermissions(this, PERMISSIONS)){
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }

        displayLocationSettingsRequest(MapsActivity.this);

        //ANDROID LOCATION PERMISSION CHECK ENDS
    }

    //FOR ENABLING LOCATION request BEGINS
    private void displayLocationSettingsRequest(Context context) {
        final int REQUEST_CHECK_SETTINGS = 0x1;
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API).build();
        googleApiClient.connect();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(10000 / 2);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            private static final String TAG = "MAP-LOCATION";

            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        Log.i(TAG, "All location settings are satisfied.");
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.i(TAG, "Location settings are not satisfied. Show the user a dialog to upgrade location settings ");

                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the result
                            // in onActivityResult().
                            status.startResolutionForResult(MapsActivity.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            Log.i(TAG, "PendingIntent unable to execute request.");
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.i(TAG, "Location settings are inadequate, and cannot be fixed here. Dialog not created.");
                        break;
                }
            }
        });
    }

//FOR ENABLING LOCATION ENDS

    // ENABLE RUNTIME PERMISSION FOR LOCATION

    private boolean hasPermissions(Context context, String[] permissions) {

        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;

    }

    /*ENABLE RUNTIME PERMISSION FOR LOCATION ENDS*/

    @Override
    protected void onPause() {
        super.onPause();

        ActivityManager activityManager = (ActivityManager) getApplicationContext()
                .getSystemService(Context.ACTIVITY_SERVICE);

        activityManager.moveTaskToFront(getTaskId(), 0);
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



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //loc=mMap.getMyLocation();
        googleMap1 = googleMap;

        //ADDING CORDINATE VALUES TO ARRAYLIST


        listpoints1ong.add(new LatLng(10.006970, 76.373052));
        listpoints1ong.add(new LatLng(9.99724, 76.30201));
        listpoints1ong.add(new LatLng(9.99728, 76.302));
        listpoints1ong.add(new LatLng(9.99734, 76.30198));
        listpoints1ong.add(new LatLng(9.99756, 76.3019));
        listpoints1ong.add(new LatLng(9.99761, 76.30188));
        listpoints1ong.add(new LatLng(9.99775, 76.30183));
        listpoints1ong.add(new LatLng(9.99788, 76.30177));
        listpoints1ong.add(new LatLng(9.99797, 76.30173));
        listpoints1ong.add(new LatLng(9.99802, 76.30171));
        listpoints1ong.add(new LatLng(9.99806, 76.30168));
        listpoints1ong.add(new LatLng(9.99811, 76.30164));
        listpoints1ong.add(new LatLng(9.99815, 76.3016));
        listpoints1ong.add(new LatLng(9.99818, 76.30157));
        listpoints1ong.add(new LatLng(9.99822, 76.30161));
        listpoints1ong.add(new LatLng(9.99824, 76.30163));
        listpoints1ong.add(new LatLng(9.99825, 76.30163));
        listpoints1ong.add(new LatLng(9.99827, 76.30164));
        listpoints1ong.add(new LatLng(9.99829, 76.30163));
        listpoints1ong.add(new LatLng(9.99831, 76.30163));
        listpoints1ong.add(new LatLng(9.99883, 76.30145));
        listpoints1ong.add(new LatLng(9.99906, 76.30135));
        listpoints1ong.add(new LatLng(9.99937, 76.30124));
        listpoints1ong.add(new LatLng(9.99951, 76.3012));
        listpoints1ong.add(new LatLng(9.99963, 76.30112));
        listpoints1ong.add(new LatLng(9.99975, 76.30105));
        listpoints1ong.add(new LatLng(9.99984, 76.301));
        listpoints1ong.add(new LatLng(9.99991, 76.30097));
        listpoints1ong.add(new LatLng(9.99999, 76.30095));
        listpoints1ong.add(new LatLng(10.0001, 76.30093));
        listpoints1ong.add(new LatLng(10.00013, 76.30106));
        listpoints1ong.add(new LatLng(10.00014, 76.30113));
        listpoints1ong.add(new LatLng(10.00016, 76.30119));
        listpoints1ong.add(new LatLng(10.00018, 76.30124));
        listpoints1ong.add(new LatLng(10.00022, 76.30129));
        listpoints1ong.add(new LatLng(10.00025, 76.30131));
        listpoints1ong.add(new LatLng(10.00029, 76.30131));
        listpoints1ong.add(new LatLng(10.00035, 76.3013));
        listpoints1ong.add(new LatLng(10.00043, 76.30129));
        listpoints1ong.add(new LatLng(10.00049, 76.3013));
        listpoints1ong.add(new LatLng(10.00056, 76.3013));
        listpoints1ong.add(new LatLng(10.00065, 76.30131));
        listpoints1ong.add(new LatLng(10.00077, 76.3013));
        listpoints1ong.add(new LatLng(10.00124, 76.30122));
        listpoints1ong.add(new LatLng(10.00127, 76.30143));
        listpoints1ong.add(new LatLng(10.00138, 76.30205));
        listpoints1ong.add(new LatLng(10.00142, 76.30231));
        listpoints1ong.add(new LatLng(10.00151, 76.30264));
        listpoints1ong.add(new LatLng(10.00157, 76.30283));
        listpoints1ong.add(new LatLng(10.00162, 76.303));
        listpoints1ong.add(new LatLng(10.00165, 76.3031));
        listpoints1ong.add(new LatLng(10.00169, 76.30323));
        listpoints1ong.add(new LatLng(10.00174, 76.30335));
        listpoints1ong.add(new LatLng(10.00182, 76.3035));
        listpoints1ong.add(new LatLng(10.00181, 76.30352));
        listpoints1ong.add(new LatLng(10.0018, 76.30353));
        listpoints1ong.add(new LatLng(10.0018, 76.30354));
        listpoints1ong.add(new LatLng(10.0018, 76.30356));
        listpoints1ong.add(new LatLng(10.0018, 76.30357));
        listpoints1ong.add(new LatLng(10.00181, 76.30363));
        listpoints1ong.add(new LatLng(10.00185, 76.3038));
        listpoints1ong.add(new LatLng(10.00188, 76.3039));
        listpoints1ong.add(new LatLng(10.00195, 76.30442));
        listpoints1ong.add(new LatLng(10.00201, 76.30478));
        listpoints1ong.add(new LatLng(10.00206, 76.30519));
        listpoints1ong.add(new LatLng(10.00218, 76.30565));
        listpoints1ong.add(new LatLng(10.00225, 76.3059));
        listpoints1ong.add(new LatLng(10.00229, 76.30599));
        listpoints1ong.add(new LatLng(10.00236, 76.30616));
        listpoints1ong.add(new LatLng(10.00235, 76.30617));
        listpoints1ong.add(new LatLng(10.00235, 76.30618));
        listpoints1ong.add(new LatLng(10.00235, 76.30619));
        listpoints1ong.add(new LatLng(10.00235, 76.3062));
        listpoints1ong.add(new LatLng(10.00235, 76.30621));
        listpoints1ong.add(new LatLng(10.00236, 76.30622));
        listpoints1ong.add(new LatLng(10.00236, 76.30623));
        listpoints1ong.add(new LatLng(10.00237, 76.30624));
        listpoints1ong.add(new LatLng(10.00237, 76.30625));
        listpoints1ong.add(new LatLng(10.00238, 76.30626));
        listpoints1ong.add(new LatLng(10.00239, 76.30627));
        listpoints1ong.add(new LatLng(10.0024, 76.30627));
        listpoints1ong.add(new LatLng(10.0024, 76.30628));
        listpoints1ong.add(new LatLng(10.00241, 76.30628));
        listpoints1ong.add(new LatLng(10.00242, 76.30628));
        listpoints1ong.add(new LatLng(10.00242, 76.30629));
        listpoints1ong.add(new LatLng(10.00243, 76.30629));
        listpoints1ong.add(new LatLng(10.00244, 76.30629));
        listpoints1ong.add(new LatLng(10.00245, 76.30629));
        listpoints1ong.add(new LatLng(10.00246, 76.30629));
        listpoints1ong.add(new LatLng(10.00247, 76.30629));
        listpoints1ong.add(new LatLng(10.00248, 76.30629));
        listpoints1ong.add(new LatLng(10.00248, 76.30628));
        listpoints1ong.add(new LatLng(10.0025, 76.30628));
        listpoints1ong.add(new LatLng(10.0027, 76.30659));
        listpoints1ong.add(new LatLng(10.00294, 76.30704));
        listpoints1ong.add(new LatLng(10.00301, 76.30717));
        listpoints1ong.add(new LatLng(10.00306, 76.30724));
        listpoints1ong.add(new LatLng(10.00315, 76.30736));
        listpoints1ong.add(new LatLng(10.00323, 76.30748));
        listpoints1ong.add(new LatLng(10.0033, 76.30759));
        listpoints1ong.add(new LatLng(10.00339, 76.30772));
        listpoints1ong.add(new LatLng(10.00345, 76.30784));
        listpoints1ong.add(new LatLng(10.0035, 76.30796));
        listpoints1ong.add(new LatLng(10.00355, 76.30808));
        listpoints1ong.add(new LatLng(10.00357, 76.30815));
        listpoints1ong.add(new LatLng(10.00362, 76.30828));
        listpoints1ong.add(new LatLng(10.00383, 76.30881));
        listpoints1ong.add(new LatLng(10.00391, 76.30909));
        listpoints1ong.add(new LatLng(10.00393, 76.30915));
        listpoints1ong.add(new LatLng(10.004, 76.30924));
        listpoints1ong.add(new LatLng(10.00421, 76.30968));
        listpoints1ong.add(new LatLng(10.00453, 76.31048));
        listpoints1ong.add(new LatLng(10.00459, 76.31069));
        listpoints1ong.add(new LatLng(10.00467, 76.31101));
        listpoints1ong.add(new LatLng(10.00474, 76.31131));
        listpoints1ong.add(new LatLng(10.00484, 76.31168));
        listpoints1ong.add(new LatLng(10.00504, 76.3118));
        listpoints1ong.add(new LatLng(10.00529, 76.31193));
        listpoints1ong.add(new LatLng(10.00611, 76.31236));
        listpoints1ong.add(new LatLng(10.00662, 76.3126));
        listpoints1ong.add(new LatLng(10.00756, 76.31247));
        listpoints1ong.add(new LatLng(10.00853, 76.31229));
        listpoints1ong.add(new LatLng(10.00853, 76.31236));
        listpoints1ong.add(new LatLng(10.0108, 76.31199));
        listpoints1ong.add(new LatLng(10.01157, 76.31189));
        listpoints1ong.add(new LatLng(10.01225, 76.31178));
        listpoints1ong.add(new LatLng(10.01377, 76.31154));
        listpoints1ong.add(new LatLng(10.01408, 76.31148));
        listpoints1ong.add(new LatLng(10.01457, 76.31141));
        listpoints1ong.add(new LatLng(10.01492, 76.31135));
        listpoints1ong.add(new LatLng(10.01566, 76.31124));
        listpoints1ong.add(new LatLng(10.01665, 76.31108));
        listpoints1ong.add(new LatLng(10.01795, 76.31088));
        listpoints1ong.add(new LatLng(10.0184, 76.31082));
        listpoints1ong.add(new LatLng(10.01886, 76.31075));
        listpoints1ong.add(new LatLng(10.01906, 76.31072));
        listpoints1ong.add(new LatLng(10.01923, 76.31069));
        listpoints1ong.add(new LatLng(10.01924, 76.31081));
        listpoints1ong.add(new LatLng(10.01938, 76.31079));
        listpoints1ong.add(new LatLng(10.01974, 76.31073));
        listpoints1ong.add(new LatLng(10.02000, 76.31068));
        listpoints1ong.add(new LatLng(10.02023, 76.31063));
        listpoints1ong.add(new LatLng(10.02051, 76.31056));
        listpoints1ong.add(new LatLng(10.02077, 76.31048));
        listpoints1ong.add(new LatLng(10.02106, 76.31038));
        listpoints1ong.add(new LatLng(10.02137, 76.31028));
        listpoints1ong.add(new LatLng(10.02151, 76.31021));
        listpoints1ong.add(new LatLng(10.02167, 76.31013));
        listpoints1ong.add(new LatLng(10.02208, 76.30992));
        listpoints1ong.add(new LatLng(10.02236, 76.30977));
        listpoints1ong.add(new LatLng(10.0226, 76.30961));
        listpoints1ong.add(new LatLng(10.02263, 76.3096));
        listpoints1ong.add(new LatLng(10.02267, 76.30967));
        listpoints1ong.add(new LatLng(10.02285, 76.31007));
        listpoints1ong.add(new LatLng(10.02295, 76.31027));
        listpoints1ong.add(new LatLng(10.023, 76.31039));
        listpoints1ong.add(new LatLng(10.02305, 76.31054));
        listpoints1ong.add(new LatLng(10.02313, 76.31088));
        listpoints1ong.add(new LatLng(10.02315, 76.31098));
        listpoints1ong.add(new LatLng(10.0232, 76.31122));
        listpoints1ong.add(new LatLng(10.02322, 76.31136));
        listpoints1ong.add(new LatLng(10.02344, 76.31131));
        listpoints1ong.add(new LatLng(10.02361, 76.31128));



        //*********************************************************************
        //* CREATE MARKER ANOMALIES TO PLOT A RANDOM POINT IN MAP
        MarkerOptions markerOptions = new MarkerOptions();

        // Setting the position for the marker
        markerOptions.position(listpoints1ong.get(10));
        markerOptions.position(listpoints1ong.get(20));

        // Setting the title for the marker.
        // This will be displayed on taping the marker
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        markerOptions.title("Anomalies");
        mMap.addMarker(markerOptions);

        //END MARKER ANOMALIES
        //*********************************************************************


        //****************************************************************************************************

        // DRAW LINES IN MAP BEGINS HERE


        PolylineOptions options = new PolylineOptions().width(25).color(Color.BLUE).geodesic(true);
        for (int z = 0; z < listpoints1ong.size(); z++) {
            LatLng point = listpoints1ong.get(z);
            options.add(point);
        }
        Polyline line = mMap.addPolyline(options);


        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(listpoints1ong.get(0), 16.5f));
        mMap.getUiSettings().setZoomControlsEnabled(true);

        //DRAW LINES ENDS HERE

        //**********************************************************************************************


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        googleMap1.setMyLocationEnabled(true);
        //****************************************************************************************************
        // MAP LONG PRESS TO ADD MARKER BEGINS

        googleMap1.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {

            @Override
            public void onMapLongClick(LatLng latLng) {

                // Creating a marker
                MarkerOptions markerOptions = new MarkerOptions();

                // Setting the position for the marker
                markerOptions.position(latLng);

                // Setting the title for the marker.
                // This will be displayed on taping the marker
                markerOptions.title(latLng.latitude + " : " + latLng.longitude);

                // Clears the previously touched position
                //googleMap1.clear();

                // Animating to the touched position
                googleMap1.animateCamera(CameraUpdateFactory.newLatLng(latLng));

                // Placing a marker on the touched position
                googleMap1.addMarker(markerOptions);
            }
        });


        // MAP ON CLICK ENDS
//*********************************************************************************************

// MAP LOCATION CHANGE LISTENER ..... MAKE CAMEREA MOVEMENT IN LOCATION CHANGING; BEGINS;

        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                loc=location;

                Location targetLocation = new Location("");//provider name is unnecessary
                targetLocation.setLatitude(listpoints1ong.get(0).latitude);//your coords of course
                targetLocation.setLongitude(listpoints1ong.get(0).longitude);


                Location targetLocation1 = new Location("");//provider name is unnecessary
                targetLocation.setLatitude(listpoints1ong.get(1).latitude);//your coords of course
                targetLocation.setLongitude(listpoints1ong.get(1).longitude);



                float bea=targetLocation.bearingTo(targetLocation1);
                Toast.makeText(getApplicationContext(),bea+"",Toast.LENGTH_SHORT).show();

                //mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));

                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(new LatLng(location.getLatitude(),location.getLongitude()))
                        .bearing(location.getBearing()-96)
                        .tilt(30)
                        // .bearing(bea)
                        .zoom(mMap.getCameraPosition().zoom)
                        .build();
                //location.bearingTo();
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition),null);
            }

        });


// MAP LOCATION CHANGE LISTENER ..... MAKE CAMEREA MOVEMENT IN LOCATION CHANGING; ENDS;
//***********************************************************************************************************************



    }



    @Override

    public boolean onKeyDown(int keyCode, KeyEvent event) {

        boolean result;
        switch( event.getKeyCode() ) {

            case KeyEvent.KEYCODE_MENU:
                result = true;
                break;

            case KeyEvent.KEYCODE_VOLUME_UP:
                result = true;
                break;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                result = true;
                break;
            case KeyEvent.KEYCODE_BACK:
                result = true;
                break;
            default:
                result= super.dispatchKeyEvent(event);
                break;
        }

        return result;
    }



    @Override
    public void onLocationChanged(final Location location) {

        //mLocationRequest = LocationRequest.create() .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY) .setInterval(10 * 1000) .setFastestInterval(1 * 1000);
        mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));
        /// googleMap1.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));

        /*CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(location.getLatitude(),location.getLongitude()))
                .bearing(location.getBearing())
                .tilt(30)
                .zoom(mMap.getCameraPosition().zoom)
                .build();
                //location.bearingTo();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition),null);
*/
        loc=location;



    }



    private Location getLocation(Location location) {
        return location;
    }






}

