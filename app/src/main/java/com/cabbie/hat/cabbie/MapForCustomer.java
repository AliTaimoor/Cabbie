package com.cabbie.hat.cabbie;


import android.Manifest;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
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
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MapForCustomer extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;
    GoogleApiClient googleApiClient;
    Location lastLocation;
    private LatLng pickUpLocation;
    private LatLng destinationLatLng;
    LocationRequest locationRequest;
    private Button logout, request, settings, mHistory;
    private int radius = 1;
    private boolean driverFound = false;
    private String driverFoundId = null;
    private String destination, requestedService;
    private Marker driverMarker;
    private Marker pickUpMarker;

    long startTime;

    private RatingBar mRatingBar;

    SupportMapFragment mapFragment;

    private RadioGroup radioGroup;

    private LinearLayout driverInfo;
    private ImageView driverProfileImage;
    private TextView driverName, driverPhone, driverCar;

    final int LOCATION_REQUEST_CODE = 1;


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

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        destinationLatLng = new LatLng(0.0,0.0);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MapForCustomer.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);

        }

        else{
            mapFragment.getMapAsync(this);
        }

        driverInfo = (LinearLayout) findViewById(R.id.driverInfo);
        driverProfileImage = (ImageView) findViewById(R.id.driverProfileImage);
        driverName = (TextView) findViewById(R.id.driverName);
        driverPhone = (TextView) findViewById(R.id.driverPhone);
        driverCar = (TextView) findViewById(R.id.driverCar);

        mRatingBar = (RatingBar) findViewById(R.id.ratingBar);

        logout = (Button) findViewById(R.id.logout);
        request = (Button) findViewById(R.id.request);
        settings = (Button) findViewById(R.id.settings);
        mHistory = (Button) findViewById(R.id.history);

        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        radioGroup.check(R.id.faisalMovers);

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();

                startActivity(new Intent(MapForCustomer.this, StartUpActivity.class));
                finish();
                return;

            }
        });

        mHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapForCustomer.this, HistoryActivity.class);
                intent.putExtra("customerOrDriver", "Customers");
                startActivity(intent);
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

        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                destination = place.getName().toString();
                destinationLatLng = place.getLatLng();
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
            }
        });

        request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (requestBol){

                    endRide();

                }
                else{

                    int selectedButtonId = radioGroup.getCheckedRadioButtonId();

                    final RadioButton radioButton = (RadioButton) findViewById(selectedButtonId);

                    if(radioButton.getText() == null) return;

                    requestedService = radioButton.getText().toString();

                    radioGroup.setVisibility(View.GONE);

                    requestBol=true;

                    startTime = System.nanoTime();

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
        );

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
                    DatabaseReference customerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(key);

                    customerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                                Map<String, Object> driverMap = (Map<String, Object>) dataSnapshot.getValue();

                                if(driverFound) return;

                                if(driverMap.get("service").equals(requestedService)){

                                    driverFound = true;
                                    driverFoundId = dataSnapshot.getKey();


                                    DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference("Users").child("Drivers")
                                            .child(driverFoundId).child("customerRequest");
                                    String customerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                    HashMap map = new HashMap();
                                    map.put("customerRideId", customerId);
                                    map.put("destination", destination);
                                    map.put("destinationLat", destinationLatLng.latitude);
                                    map.put("destinationLng", destinationLatLng.longitude);
                                    driverRef.updateChildren(map);
                                    getHasRideEnded();
                                    getDriverInfo();
                                    getDriverLocation();

                                }
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });



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

                    if(TimeUnit.NANOSECONDS.toMinutes(System.nanoTime() - startTime) >= 1) {
                        endRide();
                        Toast.makeText(getApplicationContext(), "Sorry! Cabbie service is currently unavailable at your location", Toast.LENGTH_LONG).show();
                        return;
                    }

                    radius++;
                    getClosestDriver();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });

    }

    private void getDriverInfo() {

        driverInfo.setVisibility(View.VISIBLE);

        DatabaseReference customerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverFoundId);
        customerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0){

                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

                    if(map.get("name") != null){
                        driverName.setText(map.get("name").toString());
                    }
                    if(map.get("phoneNo") != null){
                        driverPhone.setText(map.get("phoneNo").toString());
                    }
                    if(map.get("car") != null){
                        driverCar.setText(map.get("car").toString());
                    }

                    if(map.get("profileImageUrl") != null){
                        Glide.with(getApplication()).load(map.get("profileImageUrl").toString()).into(driverProfileImage);
                    }

                    int ratingSum = 0;
                    float ratingsTotal = 0;
                    float ratingsAvg = 0;
                    for (DataSnapshot child : dataSnapshot.child("rating").getChildren()) {
                        ratingSum = ratingSum + Integer.valueOf(child.getValue().toString());
                        ratingsTotal++;
                    }

                    if(ratingsTotal!= 0){
                        ratingsAvg = ratingSum/ratingsTotal;
                        mRatingBar.setRating(ratingsAvg);
                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private DatabaseReference driveHasEndedRef;
    private ValueEventListener driveHasEndedRefListener;

    private void getHasRideEnded(){
        String driverId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        driveHasEndedRef = FirebaseDatabase.getInstance().getReference().child("Users")
                .child("Drivers").child(driverFoundId).child("customerRequest").child("customerRideId");
        driveHasEndedRefListener = driveHasEndedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()) {
                    endRide();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void endRide() {

        requestBol=false;
        geoQuery.removeAllListeners();

        if (driverFoundId != null){
            driverLocationRef.removeEventListener(driverLocationRefListener);
            driveHasEndedRef.removeEventListener(driverLocationRefListener);

            DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users").child("Drivers").child(driverFoundId).child("customerRequest");
            ref.removeValue();

            driverFoundId = null;
        }

        driverFound = false;
        radius = 1;
        userID=FirebaseAuth.getInstance().getCurrentUser().getUid();
        geoFire.removeLocation(userID, new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {

            }

        });


        if (driverMarker != null){
            driverMarker.remove();
        }

        if(pickUpMarker != null) pickUpMarker.remove();

        driverInfo.setVisibility(View.GONE);
        driverName.setText("");
        driverPhone.setText("");
        driverCar.setText("Ferrari la Ferrari");
        driverProfileImage.setImageResource(R.drawable.profile);

        radioGroup.setVisibility(View.VISIBLE);

        request.setText("Call Cabbie");

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

            ActivityCompat.requestPermissions(MapForCustomer.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);

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

            ActivityCompat.requestPermissions(MapForCustomer.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);

        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch(requestCode){

            case LOCATION_REQUEST_CODE:
                if(grantResults.length > 0  && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    mapFragment.getMapAsync(this);
                }
                else{
                    Toast.makeText(getApplicationContext(), "Please provide the permission!", Toast.LENGTH_LONG).show();
                }
                break;

        }
    }

    /*@Override
    protected void onStop() {
        super.onStop();
    }*/

}