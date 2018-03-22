package com.soleeklab.yamam_driver;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class HomeActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks
    ,GoogleApiClient.OnConnectionFailedListener, LocationListener{

    private static final String LOG_TAG ="driver-tag" ;
    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LocationRequest mLocationRequest;
    final static String driverChannel = "driverLocation";
    private  final String driverRequest = "driverRequest";
    private com.github.nkzawa.socketio.client.Socket mSocket;
    private Boolean isConnected = false;
    String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwOi8vNTIuMTc0LjIyLjE4OC95YW1hbS9wdWJsaWMvYXBpL2F1dGgvdmVyaWZ5IiwiaWF0IjoxNTIwOTUyNTc1LCJleHAiOjM3NTIwOTUyNTc1LCJuYmYiOjE1MjA5NTI1NzUsImp0aSI6IjEzWHdnMmVaTWhjazQzRjgiLCJzdWIiOjEyLCJwcnYiOiIyNTkwOGUxMDQzYjNlYWUzYmQ1ZTUxNzllMzgwNWExOTBjZjdmOGE1In0.-7uFSkaXfME1DAlu3S4Hv2J_X07Z4buEUpcmMV9uIig";
    double lat = 29.975088;
    double lon =31.279678 ;
    ArrayList<com.soleeklab.yamam_driver.model.Location> driverLocation = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        try {
            IO.Options opts = new IO.Options();
            opts.forceNew = true;
            opts.query = "token=" + token;
            mSocket = IO.socket("http://52.174.22.188:4201", opts);
            mSocket.on(Socket.EVENT_CONNECT,onConnect);
            mSocket.on(Socket.EVENT_DISCONNECT,onDisconnect);
            mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
            mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
            mSocket.on(driverChannel, recieve);
            mSocket.on(driverRequest,requestRecieve);
            mSocket.connect();
        } catch (Exception e) {
            Log.d(LOG_TAG+"error",e.getMessage()+"");
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        buildGoogleApiClient();
        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

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

    public void send(String channel) {
        JSONObject rider = new JSONObject();
        /* rider.put("name", "saif");
         rider.put("lat", lat);
         rider.put("lon", lon);*/
        mSocket.emit(channel, rider);
        isConnected = true;
        //Log.d(LOG_TAG, "connected" + mDefaultLocation.longitude+"");
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
                //tripId = date.getString("tripId");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    @SuppressLint("RestrictedApi")
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        driverLocation.add(new com.soleeklab.yamam_driver.model.Location(location.getLatitude(),location.getLongitude()));
        Log.d(LOG_TAG,location.getLatitude()+"");
        mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(),location.getLongitude())));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(),location.getLongitude())));
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        mSocket.disconnect();
        mSocket.off(Socket.EVENT_CONNECT, onConnect);
        mSocket.off(Socket.EVENT_DISCONNECT, onDisconnect);
        mSocket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.off(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        mSocket.off(driverChannel, recieve);
        mSocket.off(driverRequest);
    }
}
