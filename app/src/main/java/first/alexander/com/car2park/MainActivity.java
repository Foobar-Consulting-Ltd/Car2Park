package first.alexander.com.car2park;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.nightonke.boommenu.BoomButtons.ButtonPlaceEnum;
import com.nightonke.boommenu.BoomButtons.HamButton;
import com.nightonke.boommenu.BoomButtons.OnBMClickListener;
import com.nightonke.boommenu.BoomMenuButton;
import com.nightonke.boommenu.ButtonEnum;
import com.nightonke.boommenu.Piece.PiecePlaceEnum;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import DirectionFinderPackage.DirectionFinder;
import DirectionFinderPackage.DirectionFinderListener;
import DirectionFinderPackage.Route;
import info.hoang8f.widget.FButton;

import static android.location.GpsStatus.GPS_EVENT_STARTED;
import static android.location.GpsStatus.GPS_EVENT_STOPPED;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener, DirectionFinderListener, GoogleMap.OnMapClickListener {
    private GoogleMap mMap;
    private EditText etDestination;

    private LocationManager locationManager;

    private Double latitude;
    private Double longitude;
    private LatLng latLng;

    private int PARKING_SPOTS_LIMIT = 5;

    // Set JSON Request Connection Timeout (6 seconds)
    final private int JSON_TIME_OUT = 6000;

    private List<Marker> destinationMarkers = new ArrayList<>();
    private List<Marker> parkingMarkers = new ArrayList<>();

    private Marker currentMarker;

    private static final int LOC_PERMISSION_CODE = 102;

    private String Key;

    final Context context = this;

    private static final CharSequence[] MAP_TYPE_ITEMS =
            {"Road Map", "Satellite", "Terrain", "Hybrid"};

    private ProgressDialog progressDialog;

    private Snackbar noGPSMessage;
    private boolean gpsAvailable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        this.Key = prefs.getString("cookie_key", "NULL");

        noGPSMessage = Snackbar.make(findViewById(R.id.linearLayout), "GPS is off", Snackbar.LENGTH_INDEFINITE);
        noGPSMessage.setActionTextColor(getResources().getColor(R.color.colorPrimary));
        noGPSMessage.setAction("Enable GPS", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        });

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        gpsAvailable = setProvider();
        if (!gpsAvailable) {
            noGPSMessage.show();
            checkGPS();
        }

        etDestination = (EditText) findViewById(R.id.etDestination);

        FButton btnFindPath = (FButton) findViewById(R.id.btnSetDestination);
        btnFindPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDestinationFromAddress();
            }
        });

        // Start:Boom Buttons and Menu Implementation
        BoomMenuButton bmb_settings = (BoomMenuButton) findViewById(R.id.bmb_settings);
        bmb_settings.setNormalColor(Color.LTGRAY);
        bmb_settings.setButtonEnum(ButtonEnum.Ham);
        bmb_settings.setPiecePlaceEnum(PiecePlaceEnum.HAM_3);
        bmb_settings.setButtonPlaceEnum(ButtonPlaceEnum.HAM_3);

        HamButton.Builder builder_map = new HamButton.Builder();
        builder_map.normalImageRes(R.drawable.map).normalText("Change Map Type");
        builder_map.normalColor(Color.RED);
        bmb_settings.addBuilder(builder_map);
        builder_map.listener(new OnBMClickListener() {
            @Override
            public void onBoomButtonClick(int index) {
                showMapTypeSelectionDialog();
            }
        });

        HamButton.Builder builder_filter = new HamButton.Builder();
        builder_filter.normalImageRes(R.drawable.filter).normalText("Filter Parking Spots");
        builder_filter.normalColor(Color.BLUE);
        bmb_settings.addBuilder(builder_filter);
        builder_filter.listener(new OnBMClickListener() {
            @Override
            public void onBoomButtonClick(int index) {
                Dialog filterDialog = createFilterSelectDialog();
                filterDialog.show();
            }
        });


        HamButton.Builder builder_saved = new HamButton.Builder();
        builder_saved.normalImageRes(R.drawable.ic_favorite).normalText("Saved User Destinations");
        builder_saved.normalColor(Color.GRAY);
        bmb_settings.addBuilder(builder_saved);
        builder_saved.listener(new OnBMClickListener() {
            @Override
            public void onBoomButtonClick(int index) {
                // Go to Saved Destination List Activity
                Intent intent = new Intent(getBaseContext(), SavedDestinationListActivity.class);
                startActivity(intent);
            }
        });
        // End:Boom Buttons and Menu Implementation

    }


    private void resumeFromSavedDestinationList() {

        if(getIntent().getExtras() != null)
        {

            if (latitude != null && longitude != null && latLng != null) {

                if (destinationMarkers != null) {
                    for (Marker marker : destinationMarkers) {
                        marker.remove();
                    }
                }
            }

            String name_dest = getIntent().getExtras().getString("dest_name");
            double latitude_dest = getIntent().getExtras().getDouble("dest_lat");
            double longitude_dest = getIntent().getExtras().getDouble("dest_long");
            String address_dest = getIntent().getExtras().getString("dest_address");
            LatLng latLng_dest = new LatLng(latitude_dest,longitude_dest);

            currentMarker = mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker))
                    .title(name_dest)
                    .snippet(address_dest)
                    .position(latLng_dest));
            destinationMarkers.add(currentMarker);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng_dest, 13f));

            String server_request_url = "https://dry-shore-37281.herokuapp.com/parkingspots?";
            server_request_url += "lat=" + Double.toString(latitude_dest);
            server_request_url += "&lng=" + Double.toString(longitude_dest);

            JSONRequestParkingSpots(server_request_url);
        }


    }

    @Override
    public void onMapClick(LatLng latLng) {

        if (gpsAvailable && latitude != null && longitude != null && latLng != null) {

            if (destinationMarkers != null) {
                for (Marker marker : destinationMarkers) {
                    marker.remove();
                }
            }

            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> dest_address = null;

            try{
                dest_address = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            }
            catch(Exception e){
                Log.e("GOOGLE MAP", "Geocode Failed");
            }

            String address_name = dest_address.get(0).getAddressLine(0);

            currentMarker = mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker))
                    .title("Picked Location")
                    .snippet(address_name)
                    .position(latLng));
            destinationMarkers.add(currentMarker);
            //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13f));

            String server_request_url = "https://dry-shore-37281.herokuapp.com/parkingspots?";
            server_request_url += "lat=" + Double.toString(latLng.latitude);
            server_request_url += "&lng=" + Double.toString(latLng.longitude);

            JSONRequestParkingSpots(server_request_url);

        }
    }


    private void setDestinationFromAddress() {

        if (latitude != null && longitude != null) {
            String origin = Double.toString(latitude) + "," + Double.toString(longitude);
            String destination = etDestination.getText().toString();

            if (destination.isEmpty()) {
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
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(viewFocus.getWindowToken(), 0);
            }

        }

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        try {
            // Customize the map based on the map_style_json file
            boolean success = mMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.map_style_json));

            if (!success) {
                Log.e("GOOGLE MAP", "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e("GOOGLE MAP", "Can't find style. Error: ", e);
        }


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOC_PERMISSION_CODE);
            return;
        }

        LatLng vancouver = new LatLng(49.2652455,-123.1611889);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(vancouver, 11.2f));

        mMap.setMyLocationEnabled(true);
        mMap.setOnMapClickListener(this);
        mMap.setOnInfoWindowClickListener(this);
    }

    @Override
    public void onInfoWindowClick(Marker marker) {

        if (marker.equals(currentMarker)) {
            Intent intent = new Intent(getBaseContext(), DestinationViewActivity.class);
            intent.putExtra("current_latLng", marker.getPosition());
            intent.putExtra("current_name", marker.getTitle());
            intent.putExtra("current_info", marker.getSnippet());
            startActivity(intent);
        }
        else{
            Intent intent = new Intent(getBaseContext(), StreetViewActivity.class);
            intent.putExtra("current_latLng", marker.getPosition());
            intent.putExtra("current_name", marker.getTitle());
            intent.putExtra("current_info", marker.getSnippet());
            startActivity(intent);
        }

    }


    private void getLocation(String provider) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOC_PERMISSION_CODE);
            return;
        }

        locationManager.requestLocationUpdates(provider, 0, 0, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                // If a previous location did not exist, like when starting the app
                boolean noLocation = latitude == null || longitude == null;

                latitude = location.getLatitude();
                longitude = location.getLongitude();
                latLng = new LatLng(latitude, longitude);

                // Move the camera to the current location
                if (noLocation){
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16.2f));
                    resumeFromSavedDestinationList();
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                if (status == LocationProvider.OUT_OF_SERVICE || status == LocationProvider.TEMPORARILY_UNAVAILABLE) {
                    Toast.makeText(MainActivity.this, "Location provider is unavailable", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onProviderEnabled(String provider) {
                noGPSMessage.dismiss();
                gpsAvailable = true;
            }

            @Override
            public void onProviderDisabled(String provider) {
                noGPSMessage.show();
                gpsAvailable = false;
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
                } else {
                    Toast.makeText(this, "Location permission required to function!", Toast.LENGTH_LONG).show();
                    // App crashes if you press anything lol
                }
                break;
            }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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
        if (routes.isEmpty()) {
            Toast.makeText(this, "Destination not found", Toast.LENGTH_SHORT).show();
            return;
        }

        for (Route route : routes) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(route.endLocation, (float) 12.5));

            currentMarker = mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker))
                    .title("Picked Location")
                    .snippet(route.endAddress)
                    .position(route.endLocation));
            destinationMarkers.add(currentMarker);

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

    public void onDirectionFinderFailed() {
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
                                // Remove from map
                                for (Marker marker : parkingMarkers) {
                                    marker.remove();
                                }
                                // Clear list
                                parkingMarkers.clear();
                            }

                            JSONArray ParkingSpots = response.getJSONArray("parkingSpots");

                            // Tracing trough the ParkingSpots array
                            for (int index = 0; index < ParkingSpots.length() && index < PARKING_SPOTS_LIMIT; index++) {

                                JSONObject location = ParkingSpots.getJSONObject(index);

                                JSONObject location_info = location.getJSONObject("location");

                                JSONObject spot_info = location_info.getJSONObject("spot");

                                String location_name = spot_info.getString("name");
                                String totalCapacity = spot_info.getString("totalCapacity");
                                String usedCapacity = spot_info.getString("usedCapacity");

                                JSONArray array_coordinates = spot_info.getJSONArray("coordinates");

                                double Lat = array_coordinates.getDouble(0);
                                double Long = array_coordinates.getDouble(1);

                                LatLng latLng_parking = new LatLng(Lat, Long);


                                String snippet_info = "Total Capacity: " + totalCapacity + "  " +
                                        "Used Capacity: " + usedCapacity;

                                parkingMarkers.add(mMap.addMarker(new MarkerOptions()
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_parking))
                                        .title(location_name)
                                        .snippet(snippet_info)
                                        .position(latLng_parking)));

                            }

                            progressDialog.dismiss();

                            if (parkingMarkers.isEmpty()) {
                                Toast.makeText(getApplicationContext(), "Cannot Find Any Nearby Parking Spots", Toast.LENGTH_LONG).show();
                            }

                        } catch (Exception e) {
                            progressDialog.dismiss();
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("VOLLEY", "ERROR " + error.getMessage());

                        progressDialog.dismiss();
                        // Handle network related Errors
                        if (error.networkResponse == null) {

                            // Handle network Timeout error
                            if (error.getClass().equals(TimeoutError.class)) {
                                Toast.makeText(getApplicationContext(),
                                        "Request Timeout Error!", Toast.LENGTH_LONG)
                                        .show();
                            } else {
                                // Handle no internet network error
                                Toast.makeText(getApplicationContext(),
                                        "Network Error. No Internet Connection", Toast.LENGTH_LONG)
                                        .show();
                            }
                        }
                    }
                }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                System.out.println("Sending KEY: " + Key);

                params.put("Cookie", "auth=" + Key);
                return params;
            }
        };

        JsonObjectR.setRetryPolicy(new DefaultRetryPolicy(JSON_TIME_OUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Add to JSON request Queue
        JSONVolleyController.getInstance().addToRequestQueue(JsonObjectR);
    }

    private void showMapTypeSelectionDialog() {

        final String fDialogTitle = "Select Map Type";
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(fDialogTitle);
        builder.setIcon(R.drawable.map);

        // Find the current map type to pre-check the item representing the current state.
        int checkItem = mMap.getMapType() - 1;

        builder.setSingleChoiceItems(
                MAP_TYPE_ITEMS,
                checkItem,
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int item) {

                        // Perform an action depending on which item was selected.
                        switch (item) {
                            case 0:
                                try {
                                    // Customize the map based on the map_style_json file
                                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                                    boolean success = mMap.setMapStyle(
                                            MapStyleOptions.loadRawResourceStyle(
                                                    context, R.raw.map_style_json));

                                    if (!success) {
                                        Log.e("GOOGLE MAP", "Style parsing failed.");
                                    }
                                } catch (Resources.NotFoundException e) {
                                    Log.e("GOOGLE MAP", "Can't find style. Error: ", e);
                                }
                                break;
                            case 1:
                                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                                System.out.println("SELECT SATATLIE");
                                break;
                            case 2:
                                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                                break;
                            case 3:
                                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                                break;
                            default:
                                try {
                                    // Customize the map based on the map_style_json file
                                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                                    boolean success = mMap.setMapStyle(
                                            MapStyleOptions.loadRawResourceStyle(
                                                    context, R.raw.map_style_json));

                                    if (!success) {
                                        Log.e("GOOGLE MAP", "Style parsing failed.");
                                    }
                                } catch (Resources.NotFoundException e) {
                                    Log.e("GOOGLE MAP", "Can't find style. Error: ", e);
                                }
                        }
                        dialog.dismiss();
                    }
                }
        );

        // Build the dialog and show it.
        AlertDialog fMapTypeDialog = builder.create();
        fMapTypeDialog.setCanceledOnTouchOutside(true);
        fMapTypeDialog.show();
    }

    private void checkGPS() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOC_PERMISSION_CODE);
            return;
        }

        locationManager.addGpsStatusListener(new android.location.GpsStatus.Listener()
        {
            public void onGpsStatusChanged(int event)
            {
                switch(event)
                {
                    case GPS_EVENT_STARTED:
                        gpsAvailable = setProvider();
                        noGPSMessage.dismiss();
                        break;
                    case GPS_EVENT_STOPPED:
                        gpsAvailable = false;
                        noGPSMessage.show();
                        break;
                }
            }
        });
    }

    /*
     * Returns true if provider set, false if none available
     */
    private boolean setProvider() {
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            getLocation(LocationManager.NETWORK_PROVIDER);
        } else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            getLocation(LocationManager.GPS_PROVIDER);
        } else {
            return false;
        }

        return true;
    }

    private Dialog createFilterSelectDialog() {
        final String[] filterChoices = {"1", "5", "10", "15"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Max number of parking spots")
                .setIcon(R.drawable.filter)
                .setItems(filterChoices, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if(which == 0)
                            PARKING_SPOTS_LIMIT = 1;
                        else
                            PARKING_SPOTS_LIMIT = which * 5;

                        // If a marker exists, refresh parking spots
                        if(currentMarker != null) {
                            LatLng currLoc = currentMarker.getPosition();
                            String server_request_url = "https://dry-shore-37281.herokuapp.com/parkingspots?";
                            server_request_url += "lat=" + Double.toString(currLoc.latitude);
                            server_request_url += "&lng=" + Double.toString(currLoc.longitude);

                            JSONRequestParkingSpots(server_request_url);
                        }
                    }
                });

        return builder.create();
    }
}

