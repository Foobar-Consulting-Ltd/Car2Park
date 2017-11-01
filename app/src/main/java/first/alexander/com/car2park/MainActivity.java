package first.alexander.com.car2park;

import android.Manifest;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.OnStreetViewPanoramaReadyCallback;
import com.google.android.gms.maps.StreetViewPanorama;
import com.google.android.gms.maps.StreetViewPanoramaFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import DirectionFinderPackage.DirectionFinder;
import DirectionFinderPackage.DirectionFinderListener;
import DirectionFinderPackage.Route;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback, OnStreetViewPanoramaReadyCallback, GoogleMap.OnInfoWindowClickListener, DirectionFinderListener, GoogleMap.OnMapClickListener{
    private GoogleMap mMap;
    private EditText etDestination;

    private LocationManager locationManager;

    private Double latitude;
    private Double longitude;
    private LatLng latLng;

    private int PARKING_SPOTS_LIMIT = 5;

    private boolean follow;

    // Set JSON Request Connection Timeout (10 seconds)
    final private int JSON_TIME_OUT = 10000;

    private List<Marker> destinationMarkers = new ArrayList<>();
    private List<Marker> parkingMarkers = new ArrayList<>();

    private Marker currentMarker;

    private static final int LOC_PERMISSION_CODE = 102;

    final Context context = this;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        String locProvider;
        if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locProvider  = LocationManager.NETWORK_PROVIDER;
            getLocation(locProvider);
        }
        else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locProvider = LocationManager.GPS_PROVIDER;
            getLocation(locProvider);
        }
        else {
            Toast.makeText(this, "Please enable your GPS provider!", Toast.LENGTH_LONG).show();
        }

        etDestination = (EditText) findViewById(R.id.etDestination);

        Button btnFindPath = (Button) findViewById(R.id.btnSetDestination);
        btnFindPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDestinationFromAddress();
            }
        });


    }


    @Override
    public void onMapClick(LatLng latLng) {

        if(latitude != null && longitude != null && latLng != null) {

            if (destinationMarkers != null) {
                for (Marker marker : destinationMarkers) {
                    marker.remove();
                }
            }
            destinationMarkers.add(mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker))
                    .title("Picked Location")
                    .position(latLng)));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13f));

            String server_request_url = "https://dry-shore-37281.herokuapp.com/parkingspots?";
            server_request_url += "lat=" + Double.toString(latLng.latitude);
            server_request_url += "&lng=" + Double.toString(latLng.longitude);

            JSONRequestParkingSpots(server_request_url);

        }
        else{
            return;
        }
    }



    private void setDestinationFromAddress(){

        if(latitude != null && longitude != null) {
            String origin = Double.toString(latitude) + "," + Double.toString(longitude);
            String destination = etDestination.getText().toString();

            if(destination.isEmpty()){
                Toast.makeText(this, "Please enter destination address!", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                new DirectionFinder(this, origin, destination).execute();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            // Closes keyboard
            View viewFocus = this.getCurrentFocus();
            if (viewFocus != null) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(viewFocus.getWindowToken(), 0);
            }

        }
        else{
            return;
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOC_PERMISSION_CODE);
            return;
        }

        mMap.setMyLocationEnabled(true);
        mMap.setOnMapClickListener(this);
        mMap.setOnInfoWindowClickListener(this);
    }

    @Override
    public void onInfoWindowClick(Marker marker) {

        Intent intent = new Intent(getBaseContext(),StreetViewActivity .class);
        intent.putExtra("current_marker", marker.getPosition());
        startActivity(intent);

        /*final Dialog dialog = new Dialog(context);

        if(dialog.isShowing()){
            dialog.dismiss();
        }

        dialog.setContentView(R.layout.parking_info_dialog);


        // Begin: Set up FragmentManager and get previous fragment (if exist)
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment prevFrag = fragmentManager.findFragmentById(R.id.street_view_fragment_container);
        // End: Set up FragmentManager and get previous fragment (if exist)

        // Need to remove any previous existing fragments
        if(prevFrag!=null) {
            fragmentTransaction.remove(prevFrag);
        }

        // Instantiate new fragment CustomerDetailsFragment
        StreetViewFragment streetFragment = new StreetViewFragment();

        // Adding the new fragment to transaction
        fragmentTransaction.add(R.id.street_view_fragment_container, streetFragment);

        // Set fragment transaction
        fragmentTransaction.commit();*/



      /*  StreetViewPanoramaFragment prevFragment = (StreetViewPanoramaFragment) getFragmentManager().findFragmentById(R.id.streetviewpanorama);

        if (prevFragment != null) {
            // fragment must be added
            System.out.println("PREVIOUS STREET VIEW FRAGMENT IS STILL EXIST");
            getFragmentManager().beginTransaction().remove(prevFragment).commit();
            getSupportFragmentManager().popBackStackImmediate();
            while (getFragmentManager().findFragmentById(R.id.streetviewpanorama) != null){
                System.out.println("NOT DELETED-------------------------------------------------------");
            }
        }

        dialog.setContentView(R.layout.parking_info_dialog);


        StreetViewPanoramaFragment streetViewPanoramaFragment =
                (StreetViewPanoramaFragment) getFragmentManager()
                        .findFragmentById(R.id.streetviewpanorama);
        streetViewPanoramaFragment.getStreetViewPanoramaAsync(this);*/


        /*dialog.setTitle("Parking Info");
        dialog.show();*/
    }

    @Override
    public void onStreetViewPanoramaReady(StreetViewPanorama panorama) {
        LatLng latLng = currentMarker.getPosition();
        panorama.setPosition(latLng);
    }

    private void getLocation(String provider) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOC_PERMISSION_CODE);
            return;
        }

        locationManager.requestLocationUpdates(provider, 0, 0, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                boolean noLocation = false;

                // If a previous location did not exist, like when starting the app
                if (latitude == null || longitude == null)
                    noLocation = true;

                latitude = location.getLatitude();
                longitude = location.getLongitude();
                latLng = new LatLng(latitude, longitude);

                // Move the camera to the current location
                if (follow || noLocation)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16.2f));
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                if(status == LocationProvider.OUT_OF_SERVICE || status == LocationProvider.TEMPORARILY_UNAVAILABLE) {
                    Toast.makeText(MainActivity.this, "Location provider is unavailable", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onProviderEnabled(String provider) {
                Toast.makeText(MainActivity.this, "Location provider enabled", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProviderDisabled(String provider) {
                Toast.makeText(MainActivity.this, "Location provider disabled, please enable!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOC_PERMISSION_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Recreates activity, should probably not do this
                    this.recreate();
                }
                else {
                    Toast.makeText(this, "Location permission required to function!", Toast.LENGTH_LONG).show();
                    // App crashes if you press anything lol
                }
                break;
            }
            default: super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
        }
    }


    @Override
    public void onDirectionFinderStart() {
        progressDialog = ProgressDialog.show(this, "Please wait",
                "Finding destination.", true);

        if (destinationMarkers != null) {
            for (Marker marker : destinationMarkers) {
                marker.remove();
            }
        }

    }

    @Override
    public void onDirectionFinderSuccess(List<Route> routes) {
        progressDialog.dismiss();

        destinationMarkers = new ArrayList<>();

        LatLng destination_location = null;

        // No route was found
        if(routes.isEmpty()) {
            Toast.makeText(this, "Destination not found", Toast.LENGTH_SHORT).show();
            return;
        }

        for (Route route : routes) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(route.endLocation, (float) 12.5));

            destinationMarkers.add(mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker))
                    .title(route.endAddress)
                    .position(route.endLocation)));

            destination_location = route.endLocation;
        }


        String server_request_url = "https://dry-shore-37281.herokuapp.com/parkingspots?";
        server_request_url += "lat=" + Double.toString(destination_location.latitude);
        server_request_url += "&lng=" + Double.toString(destination_location.longitude);

        JSONRequestParkingSpots(server_request_url);

       /* Intent intent = new Intent(getBaseContext(), ParkingLocations.class);
        intent.putExtra("lat", Double.toString(destination_location.latitude));
        intent.putExtra("long", Double.toString(destination_location.longitude));
        startActivity(intent);*/

    }

    public void onDirectionFinderFailed(){
        progressDialog.dismiss();
        Toast.makeText(this, "Cannot Request Destination", Toast.LENGTH_SHORT).show();
    }




    /**
     * JSON Volley Request to get all available nearest parking spots
     * and display it on a list view.
     *
     * @param server_request_url - server_request_url request to the server
     */
    private void JSONRequestParkingSpots(String server_request_url) {

        progressDialog = ProgressDialog.show(this, "Please wait",
                "Displaying All Available Parking Spots.", true);

        JsonObjectRequest JsonObjectR = new JsonObjectRequest
                (Request.Method.GET, server_request_url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        try {

                            // Clear old markers
                            if (parkingMarkers != null) {
                                for (Marker marker : parkingMarkers) {
                                    marker.remove();
                                }
                            }

                            JSONArray ParkingSpots = response.getJSONArray("parkingSpots");

                            // Tracing trough the ParkingSpots array
                            for(int index =0; index < ParkingSpots.length() && index <  PARKING_SPOTS_LIMIT; index++){

                                JSONObject location = ParkingSpots.getJSONObject(index);

                                JSONObject location_info = location.getJSONObject("location");

                                JSONObject spot_info = location_info.getJSONObject("spot");

                                String location_name = spot_info.getString("name");
                                String totalCapacity = spot_info.getString("totalCapacity");
                                String usedCapacity = spot_info.getString("usedCapacity");

                                JSONArray array_coordinates = spot_info.getJSONArray("coordinates");

                                double Lat = array_coordinates.getDouble(0);
                                double Long = array_coordinates.getDouble(1);

                                LatLng latLng_parking = new LatLng(Lat,Long);

                                String snippet_info = "Total Capacity: " + totalCapacity + "  " +
                                                       "Used Capacity: " + usedCapacity;

                                parkingMarkers.add(mMap.addMarker(new MarkerOptions()
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_start_location))
                                        .title(location_name)
                                        .snippet(snippet_info)
                                        .position(latLng_parking)));

                            }

                            progressDialog.dismiss();

                        } catch (Exception e) {
                            progressDialog.dismiss();
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("VOLLEY", "ERROR");

                        // Handle network related Errors
                        if (error.networkResponse == null) {

                            // Handle network Timeout error
                            if (error.getClass().equals(TimeoutError.class)) {
                                progressDialog.dismiss();
                                Toast.makeText(getApplicationContext(),
                                        "Request Timeout Error!", Toast.LENGTH_LONG)
                                        .show();
                            } else {
                                // Handle no internet network error
                                progressDialog.dismiss();
                                Toast.makeText(getApplicationContext(),
                                        "Network Error. No Internet Connection", Toast.LENGTH_LONG)
                                        .show();
                            }
                        }
                    }
                });

        JsonObjectR.setRetryPolicy(new DefaultRetryPolicy(JSON_TIME_OUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Add to JSON request Queue
        JSONVolleyController.getInstance().addToRequestQueue(JsonObjectR);
    }

}

