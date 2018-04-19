package com.soleeklab.yamam_driver;

import android.*;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.soleeklab.yamam_driver.model.Trip;
import com.soleeklab.yamam_driver.utils.PermissionsUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class HomeActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks
        , GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final String LOG_TAG = "driver-tag";
    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    private boolean mLocationPermissionGranted;
    LocationRequest mLocationRequest;
    final static String driverChannel = "driverLocation";
    private final String driverRequest = "driverRequest";
    private com.github.nkzawa.socketio.client.Socket mSocket;
    private Boolean isConnected = false;
    CountDownTimer countDownTimer;
    String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwOi8vNTIuMTc0LjIyLjE4OC95YW1hbS9wdWJsaWMvYXBpL2F1dGgvdmVyaWZ5IiwiaWF0IjoxNTIwOTUyNTc1LCJleHAiOjM3NTIwOTUyNTc1LCJuYmYiOjE1MjA5NTI1NzUsImp0aSI6IjEzWHdnMmVaTWhjazQzRjgiLCJzdWIiOjEyLCJwcnYiOiIyNTkwOGUxMDQzYjNlYWUzYmQ1ZTUxNzllMzgwNWExOTBjZjdmOGE1In0.-7uFSkaXfME1DAlu3S4Hv2J_X07Z4buEUpcmMV9uIig";

    private final int INTERVAL_TIME = 10*1000;
    //ArrayList<com.soleeklab.yamam_driver.model.Location> driverLocation = new ArrayList<>();
    JSONArray driverLocations=new JSONArray();
    String name ="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        name = getIntent().getExtras().getString("name");
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        PermissionsUtils.showNoConnectionDialog(this);
        PermissionsUtils.turnGPSOn(this);
        //add_locations();
        try {
            IO.Options opts = new IO.Options();
            opts.forceNew = true;
            opts.query = "token=" + token;
            mSocket = IO.socket("http://52.174.22.188:4201", opts);
            mSocket.on(Socket.EVENT_CONNECT, onConnect);
            mSocket.on(Socket.EVENT_DISCONNECT, onDisconnect);
            mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
            mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
            mSocket.on(driverChannel, recieve);
            mSocket.on(driverRequest, requestRecieve);
            mSocket.connect();
        } catch (Exception e) {
            Log.d(LOG_TAG + "error", e.getMessage() + "");
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        buildGoogleApiClient();
        getLocationPermission();

        // Add a marker in Sydney and move the camera
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
        Log.d(LOG_TAG, "map connecting");
    }
    protected synchronized void buildGoogleApiClient(){
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    private Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i(LOG_TAG, "diconnected");
                    isConnected = false;
                    Toast.makeText(getApplicationContext(),
                            "disconnect", Toast.LENGTH_LONG).show();
                }
            });
        }
    };

    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.e(LOG_TAG, "Error connecting");
                    Toast.makeText(getApplicationContext(),
                            "error_connect", Toast.LENGTH_LONG).show();
                }
            });
        }
    };

    public void send(final String channel) {
        countDownTimer = new CountDownTimer(INTERVAL_TIME,1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                final JSONObject rider = new JSONObject();
                try {
                    rider.put("name", name);
                    rider.put("state", 0);
                    rider.put("locations",driverLocations);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (driverLocations.length()>0){
                    mSocket.emit(channel, rider);
                }
                isConnected = true;
                Log.d(LOG_TAG, "connected" + driverLocations.toString()+"");
                driverLocations = new JSONArray(new ArrayList<String>());
                send(driverChannel);
            }
        }.start();

    }
    private Emitter.Listener recieve = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            //Log.d(LOG_TAG + "response", args[0].toString() + "");
        }
    };
    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            // Log.d(TAG,"connected"+args[0].toString());

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (!isConnected) {
                        send(driverChannel);
                        Toast.makeText(getApplicationContext(),
                                "connect", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    };
    private Emitter.Listener requestRecieve = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d(LOG_TAG + "response1", args[0].toString() + "");
            try {
                JSONObject date = new JSONObject(args[0].toString());
                final Trip trip = Trip.fromJson(date.toString());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showDialog(trip);
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
                Log.d(LOG_TAG+"error",e.getMessage());
            }
        }
    };

    @SuppressLint("RestrictedApi")
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        //Log.d(LOG_TAG, "onMap connected"+mMap.getMyLocation().getLongitude());
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(LOG_TAG,"suspend");

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(LOG_TAG,"failed");
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        JSONObject currrentlocation = new JSONObject();
        try {
            currrentlocation.put("lat",location.getLatitude());
            currrentlocation.put("lon",location.getLongitude());
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d(LOG_TAG+"location",e.getMessage());
        }
        driverLocations.put(currrentlocation);
        //driverLocation.add(new com.soleeklab.yamam_driver.model.Location(location.getLatitude(),location.getLongitude()));
        Log.d(LOG_TAG,location.getLatitude()+"");
        //mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(),location.getLongitude())));
        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(),location.getLongitude()),15));
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        mSocket.off(Socket.EVENT_CONNECT, onConnect);
        mSocket.off(Socket.EVENT_DISCONNECT, onDisconnect);
        mSocket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.off(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        mSocket.off(driverChannel, recieve);
        mSocket.off(driverRequest);
        mSocket.disconnect();
    }

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                         //mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                        mMap.setMyLocationEnabled(true);
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Please provide the permission", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //PermissionsUtils.showNoConnectionDialog(this);
        //PermissionsUtils.turnGPSOn(this);
    }

    private void showDialog(final Trip trip) {
        mMap.addMarker(new MarkerOptions()
                .position(new LatLng(trip.getTrip().getPickUp().getLat(),trip.getTrip().getPickUp().getLon()))
                .title(trip.getTrip().getRiderName()));
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View subView = inflater.inflate(R.layout.dialoge_receive_request, null);
        TextView userNameTv = subView.findViewById(R.id.txt_rider_name);
        userNameTv.setText(trip.getTrip().getRiderName()+" ");
        Button cancelBtn = subView.findViewById(R.id.btn_cancel);
        Button okBtn = subView.findViewById(R.id.btn_accept);
        builder.setView(subView);
        final AlertDialog alertDialog = builder.show();
        alertDialog.setCancelable(false);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                accept(trip,0);
                alertDialog.dismiss();
            }
        });
        okBtn.setOnClickListener(   new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                countDownTimer.cancel();
                accept(trip,1);
                alertDialog.dismiss();
            }
        });
    }
    public void  accept(Trip trip,int state){

        final JSONObject rider = new JSONObject();
        try {
            JSONObject pickUpLocation = new JSONObject();
            pickUpLocation.put("lat",trip.getTrip().getPickUp().getLat());
            pickUpLocation.put("lon",trip.getTrip().getPickUp().getLon());
            rider.put("accept", state);
            rider.put("riderId",trip.getTrip().getRiderId());
            rider.put("id", trip.getTrip().getTripId());
            rider.put("name",trip.getTrip().getRiderName());
            rider.put("locations",driverLocations);
            rider.put("pick_up",pickUpLocation);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d(LOG_TAG+"",trip.getTrip().getTripId());
        mSocket.emit(driverRequest, rider);
    }
    public void cancel (Trip trip){

    }
}
