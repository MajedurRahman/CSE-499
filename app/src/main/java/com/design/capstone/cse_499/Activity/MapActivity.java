package com.design.capstone.cse_499.Activity;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.design.capstone.cse_499.Model.Online;
import com.design.capstone.cse_499.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.onesignal.OneSignal;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by Majedur Rahman on 8/1/2017.
 */

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final int MAP_PERMISSION_KEY = 111;
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private final String API_KEY = "AIzaSyDKk3qlFlAipHQ9Y3YGoGZSL-S67IGssN4";
    double longitude;
    double latitude;
    private LocationManager locationManager;
    private Switch signout, onlineofflineSwitch;
    private Button requestButton;
    private String userID;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();

    private DatabaseReference userOnlineRef = database.getReference("isOnline");
    private DatabaseReference userBusyRef = database.getReference("isBusy");
    private DatabaseReference requestRef = database.getReference("Requests");
    private ArrayList<LatLng> userPositionList;
    private ArrayList<String> onlineUserKeyList;
    private ArrayList<Online> onlineUserList;
    private GoogleMap mMap;
    //Google ApiClient
    private GoogleApiClient googleApiClient;
    private FirebaseAuth mAuth;
    private String value;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        value = String.valueOf(true).toString();

        userPositionList = new ArrayList<>();
        onlineUserList = new ArrayList<>();
        onlineUserKeyList = new ArrayList<>();

        initComponent();
        onClickAction();


        mAuth = FirebaseAuth.getInstance();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Initializing googleapi client
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


        userID = mAuth.getCurrentUser().getPhoneNumber().toString();


        initOneSignalData();

        updateOnlineUserData();

        getDataListner();


    }


    public void initOneSignalData() {

        OneSignal.sendTag("User_ID", userID);
        //  OneSignal.deleteTag("Online");


    }

    public void updateOnlineUserData() {


        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                getOnlineUser();
            }
        });


        Handler handler = new Handler();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                for (int i = 0; i < userPositionList.size(); i++) {
                    addMarkerOnMap(userPositionList.get(i).latitude, userPositionList.get(i).longitude);
                }

            }
        }, 1000);
    }


    public void getOnlineUser() {

        userOnlineRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (!onlineUserList.isEmpty() || !userPositionList.isEmpty()) {

                    onlineUserList.clear();
                    userPositionList.clear();
                    if (mMap != null) {
                        mMap.clear();
                    }

                }

                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    Online tempOnlineUser = data.getValue(Online.class);
                    onlineUserList.add(tempOnlineUser);

                    //Toast.makeText(MapsActivity.this, onlineUserList.size() + "", Toast.LENGTH_SHORT).show();
                    getOnlineUserData();


                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    public void getOnlineUserData() {

        for (int i = 0; i < onlineUserList.size(); i++) {
            LatLng latlong = new LatLng(onlineUserList.get(i).getLatitude(), onlineUserList.get(i).getLongitude());
            userPositionList.add(latlong);
        }


    }


    private void getDataListner() {

        userOnlineRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {


                updateOnlineUserData();


            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.e("Child changed", dataSnapshot.getKey());


            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                updateOnlineUserData();
                Log.e("Child removed ", dataSnapshot.getKey());

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void sendRequestToSpecificMonitorApp(final String userKey) {

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String jsonResponse;

                    URL url = new URL("https://onesignal.com/api/v1/notifications");
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setUseCaches(false);
                    con.setDoOutput(true);
                    con.setDoInput(true);

                    con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    con.setRequestProperty("Authorization", "Basic ODVkNDdjZTMtMThlNy00M2VmLTkwYTItOGI3NTgyNmQ5MDlm");
                    con.setRequestMethod("POST");

                    String strJsonBody = "{"
                            + "\"app_id\": \"9902773d-e28d-4b87-9ed2-b1683306d0bc\","
                            + "\"filters\": [{\"field\": \"tag\", \"key\": \"User_ID\", \"relation\": \"=\", \"value\": \"" + userKey + "\"}],"
                            + "\"data\": {\"tap\":\"tap\",\"requestId\":\"" + userID + "\"},"
                            + "\"buttons\": [{\"id\":\"accept\",\"text\":\"OPEN\",\"icon\":\"\"},{\"id\":\"cancel\",\"text\":\"CANCEL\",\"icon\":\"\"}],"
                            + "\"contents\": {\"en\": \"Tap Here To See Notification\"}"
                            + "}";


                    System.out.println("strJsonBody:\n" + strJsonBody);

                    byte[] sendBytes = strJsonBody.getBytes("UTF-8");
                    con.setFixedLengthStreamingMode(sendBytes.length);

                    OutputStream outputStream = con.getOutputStream();
                    outputStream.write(sendBytes);

                    int httpResponse = con.getResponseCode();
                    System.out.println("httpResponse: " + httpResponse);

                    if (httpResponse >= HttpURLConnection.HTTP_OK
                            && httpResponse < HttpURLConnection.HTTP_BAD_REQUEST) {
                        Scanner scanner = new Scanner(con.getInputStream(), "UTF-8");
                        jsonResponse = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
                        scanner.close();
                    } else {
                        Scanner scanner = new Scanner(con.getErrorStream(), "UTF-8");
                        jsonResponse = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
                        scanner.close();
                    }
                    System.out.println("jsonResponse:\n" + jsonResponse);

                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        });
    }

    private void sendNotificationToOnlineUser() {


        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String jsonResponse;

                    URL url = new URL("https://onesignal.com/api/v1/notifications");
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setUseCaches(false);
                    con.setDoOutput(true);
                    con.setDoInput(true);

                    con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    con.setRequestProperty("Authorization", "Basic ODVkNDdjZTMtMThlNy00M2VmLTkwYTItOGI3NTgyNmQ5MDlm");
                    con.setRequestMethod("POST");

                    String strJsonBody = "{"
                            + "\"app_id\": \"9902773d-e28d-4b87-9ed2-b1683306d0bc\","
                            //  +   "\"included_segments\": [\"All\"],"
                            + "\"filters\": [ {\"field\": \"tag\", \"key\": \"Online\", \"relation\": \"exists\"}],"
                            + "\"data\": {\"tap\":\"tap\"},"
                            + "\"buttons\": [{\"id\":\"explore\",\"text\":\"EXPLORE NOW\",\"icon\":\"\"}],"
                            + "\"contents\": {\"en\": \"Someone is visible on Map\"}"
                            + "}";


                    System.out.println("strJsonBody:\n" + strJsonBody);

                    byte[] sendBytes = strJsonBody.getBytes("UTF-8");
                    con.setFixedLengthStreamingMode(sendBytes.length);

                    OutputStream outputStream = con.getOutputStream();
                    outputStream.write(sendBytes);

                    int httpResponse = con.getResponseCode();
                    System.out.println("httpResponse: " + httpResponse);

                    if (httpResponse >= HttpURLConnection.HTTP_OK
                            && httpResponse < HttpURLConnection.HTTP_BAD_REQUEST) {
                        Scanner scanner = new Scanner(con.getInputStream(), "UTF-8");
                        jsonResponse = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
                        scanner.close();
                    } else {
                        Scanner scanner = new Scanner(con.getErrorStream(), "UTF-8");
                        jsonResponse = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
                        scanner.close();
                    }
                    System.out.println("jsonResponse:\n" + jsonResponse);

                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        });

    }

    private void onClickAction() {

        signout.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {

                if (isChecked == false) {
                    mAuth.signOut();
                    finish();

                }

            }
        });


        onlineofflineSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    sendNotificationToOnlineUser();
                    ///userOnlineRef.child(userID).setValue(true);
                    onlineofflineSwitch.setText("Online");


                    Online online = new Online(userID, latitude, longitude);
                    userOnlineRef.child(userID).setValue(online).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {
                                OneSignal.sendTag("Online", value);
                                Toast.makeText(MapActivity.this, " Complete", Toast.LENGTH_SHORT).show();
                            } else {

                                Toast.makeText(MapActivity.this, "Internet Connection Problem", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });


                } else {
                    userOnlineRef.child(userID).removeValue();
                    onlineofflineSwitch.setText("Offline");
                    //OneSignal.deleteTag("Online");

                }
            }
        });


        requestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                sendRequestToSpecificMonitorApp(userID);
                // sendNotificationToOnlineUser();
                //  Toast.makeText(MapActivity.this, " Request Send ", Toast.LENGTH_SHORT).show();

                requestRef.child(userID).setValue(true);

                final Dialog dialog = new Dialog(MapActivity.this);
                dialog.setTitle("Request Notification ");
                dialog.setContentView(R.layout.custom_layout_watting_dialog);
                dialog.setTitle("Custom Dialog");
                dialog.show();
                dialog.setCancelable(false);


                Button cancel = (Button) dialog.findViewById(R.id.cancel_waiting);
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        dialog.dismiss();
                        requestRef.child(userID).removeValue();
                    }
                });


            }
        });
    }

    private void initComponent() {

        signout = (Switch) findViewById(R.id.signOutSwitch);
        onlineofflineSwitch = (Switch) findViewById(R.id.onlineOfflineSwitch);
        requestButton = (Button) findViewById(R.id.requestBtn);
    }


    //When Map is ready. This method is called 1st after oncreate
    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Log.d("Detection", "on map ready");
        mMap = googleMap;

        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setIndoorLevelPickerEnabled(false);
        mMap.getUiSettings().setTiltGesturesEnabled(false);

        //Moving the camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(latitude, longitude)));
        //Animating the camera
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            checkLocationPermission();
            return;
        }

        //  addMarkerOnMap(latitude , longitude);


    }

    public void addMarkerOnMap(double latitude, double longitude) {
        MarkerOptions markerOption = new MarkerOptions()
                .position(new LatLng(latitude, longitude))//setting position
                .title("My Position")
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.marker4));

        mMap.addMarker(markerOption);

    }

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {


                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    //Permission request result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == MAP_PERMISSION_KEY) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Toast.makeText(this, "Permission accepted ", Toast.LENGTH_SHORT).show();

            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        getCurrentLocation();

    }

    @Override
    public void onConnectionSuspended(int i) {

    }


    @Override
    protected void onStart() {
        googleApiClient.connect();
        super.onStart();


    }

    @Override
    protected void onStop() {
        googleApiClient.disconnect();
        super.onStop();


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        userOnlineRef.child(userID).removeValue();
        onlineofflineSwitch.setText("Offline");
        OneSignal.deleteTag("Online");

    }


    //Getting current location
    private void getCurrentLocation() {
        mMap.clear();
        //    Log.d("Detection", "getcurrentlocation");

        //Check Permission for then android 6.0+
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MAP_PERMISSION_KEY);
            return;
        }
        //collect location from location service
        Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        mMap.setMyLocationEnabled(true); //enable current position floating button


        if (location != null) {
            //Getting longitude and latitude
            longitude = location.getLongitude();
            latitude = location.getLatitude();

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 15));


        }
        addMarkerOnMap(latitude, longitude);


    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        Toast.makeText(this, connectionResult.toString(), Toast.LENGTH_SHORT).show();
    }
}
