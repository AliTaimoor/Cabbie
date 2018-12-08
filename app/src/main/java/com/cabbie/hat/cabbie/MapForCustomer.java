package com.cabbie.hat.cabbie;


import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;

public class MapForCustomer extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;
    GoogleApiClient googleApiClient;
    Location lastLocation;
    LatLng pickUpLocation;
    LocationRequest locationRequest;
    private Button logout, request, settings;
    private int radius = 1;
    private boolean driverFound = false;
    private String driverFoundId;
    private Marker driverMarker;
    private Marker pickUpMarker;


    private boolean requestBol=false;
    GeoQuery geoQuery;
    DatabaseReference driverLocationRef;
    ValueEventListener driverLocationRefListener;

    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Customer Requests");
    GeoFire geoFire = new GeoFire(ref);
    String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_customer);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        logout = (Button) findViewById(R.id.logout);
        request = (Button) findViewById(R.id.request);
        settings = (Button) findViewById(R.id.settings);

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();

                startActivity(new Intent(MapForCustomer.this, StartUpActivity.class));
                finish();
                return;

            }
        });

        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MapForCustomer.this, CustomerSettings.class));
                return;
            }
        });

        request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (requestBol){
                    requestBol=false;
                    geoQuery.removeAllListeners();
                    driverLocationRef.removeEventListener(driverLocationRefListener);

                    if (driverFoundId != null){
                        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users").child("Drivers").child(driverFoundId)
                                .child("customerRideId");
                        ref.setValue(null);
<<<<<<< HEAD
                        driverFoundId = null;
                    }

                    driverFound = false;
                    radius = 1;
                    userID=FirebaseAuth.getInstance().getCurrentUser().getUid();
                    geoFire.removeLocation(userID, new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {
=======
                    }


                    userID=FirebaseAuth.getInstance().getCurrentUser().getUid();
                    geoFire.removeLocation(userID);

                    if (driverMarker !=null){
                        driverMarker.remove();
                    }
                    request.setText("Call Cabbie");

                }else{
                    requestBol=true;
                userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
>>>>>>> 1d89a066530664f964a1a81a1fb9e656554ccd6b

                        }
                    });

                    if (driverMarker !=null){
                        driverMarker.remove();
                    }

                    if(pickUpMarker != null) pickUpMarker.remove();

                    request.setText("Call Cabbie");

                }else{
                    requestBol=true;
                    userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

                    geoFire.setLocation(userID, new GeoLocation(lastLocation.getLatitude(), lastLocation.getLongitude()), new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {

                        }
                    });

                    pickUpLocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                    pickUpMarker = mMap.addMarker(new MarkerOptions().position(pickUpLocation).title("Pick Up").icon(BitmapDescriptorFactory.fromResource(R.drawable.wait)));
                    request.setText("Finding Driver...");

                    getClosestDriver();

                }
            }
            }
        });

    }

    private void getClosestDriver() {

        DatabaseReference driversAvailable = FirebaseDatabase.getInstance().getReference().child("Available Drivers");

        GeoFire requestsDatabase = new GeoFire(driversAvailable);

        geoQuery = requestsDatabase.queryAtLocation(new GeoLocation(pickUpLocation.latitude, pickUpLocation.longitude), radius);

        geoQuery.removeAllListeners();



        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if(!driverFound && requestBol){
                    driverFound = true;
                    driverFoundId = key;

<<<<<<< HEAD
                    DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference("Users").child("Drivers")
                            .child(driverFoundId);
                    String customerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    HashMap map = new HashMap();
                    map.put("customerRideId", customerId);
                    driverRef.updateChildren(map);
=======
                   DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference("Users").child("Drivers")
                           .child(driverFoundId);
                   String customerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                   HashMap map = new HashMap();
                   map.put("customerRideId", customerId);
                   driverRef.updateChildren(map);
>>>>>>> 1d89a066530664f964a1a81a1fb9e656554ccd6b

                    getDriverLocation();

                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if(!driverFound){
                    radius++;
                    getClosestDriver();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });

    }

    private void getDriverLocation() {

        driverLocationRef = FirebaseDatabase.getInstance().getReference().child("Working Drivers").child(driverFoundId).child("l");
        driverLocationRefListener = driverLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && requestBol){
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;

                    request.setText("Driver Found!");

                    if(map.get(0) != null){
                        locationLat = Double.parseDouble(map.get(0).toString());
                    }
                    if(map.get(1) != null){
                        locationLng = Double.parseDouble(map.get(1).toString());
                    }

                    LatLng driverLatLng = new LatLng(locationLat, locationLng);

                    if(driverMarker != null) driverMarker.remove();

                    Location loc1 = new Location("");
                    Location loc2 = new Location("");
                    loc1.setLatitude(pickUpLocation.latitude);
                    loc1.setLongitude(pickUpLocation.longitude);
                    loc2.setLatitude(driverLatLng.latitude);
                    loc2.setLongitude(driverLatLng.longitude);

                    float distance = loc1.distanceTo(loc2);

                    if(distance < 100) request.setText("Driver has arrived!");

                    else request.setText("Driver Found: " + String.valueOf(distance));

                    driverMarker = mMap.addMarker(new MarkerOptions().position(driverLatLng).title("Your Driver").icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

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

        buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);
    }

    protected synchronized void buildGoogleApiClient() {

        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();

    }

    @Override
    public void onLocationChanged(Location location) {

        if(getApplicationContext() != null) {

            lastLocation = location;

            LatLng latlng = new LatLng(location.getLatitude(), location.getLongitude());

            mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(16f));


        }

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


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
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    /*@Override
    protected void onStop() {
        super.onStop();
    }*/

}