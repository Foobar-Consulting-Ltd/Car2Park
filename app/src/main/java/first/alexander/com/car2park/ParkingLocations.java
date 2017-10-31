package first.alexander.com.car2park;

import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * Parking Locations Activity of Car2Park
 *
 * This activity currently contains the implementation of the parking spots list view.
 *
 * @author Alexander Julianto (no131614)
 * @version
 * @since API 21
 */
public class ParkingLocations extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    // Set JSON Request Connection Timeout (15 seconds)
    final private int JSON_TIME_OUT = 15000;

    //private String URL = "https://dry-shore-37281.herokuapp.com/parkingspots?lat=49.2624&lng=-123.2433";
    private String URL = "";

    // Hash Map keys
    private final String KEY_NAME = "Name";
    private final String KEY_LATITUDE = "Lat";
    private final String KEY_LONGITUDE = "Long";

    private SwipeRefreshLayout swipeRefreshLayout;

    private ListView list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parking_locations);

        // SwipeRefreshLayout to refresh the items list view
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout_parking_list);
        swipeRefreshLayout.setOnRefreshListener(this);

        ArrayList<HashMap> parkingList = new ArrayList();
        final ParkingListAdapter adapter = new ParkingListAdapter(ParkingLocations.this, parkingList);

        URL = "https://dry-shore-37281.herokuapp.com/parkingspots?";
        URL += "lat=" + getIntent().getStringExtra("lat");
        URL += "&lng=" + getIntent().getStringExtra("long");


        // Start a refresh onCreate and initialize JSON Volley Request for item list view
        swipeRefreshLayout.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        swipeRefreshLayout.setRefreshing(true);
                                        JSONRequestParkingSpots(adapter, URL);
                                    }
                                }
        );

        list = (ListView) findViewById(R.id.parking_listView);
        list.setAdapter(adapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
           public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                 HashMap parking_info = (HashMap) parent.getItemAtPosition(position);

                Intent intent = new Intent(getBaseContext(), MapsActivity.class);
                intent.putExtra("parking_name",  parking_info.get(KEY_NAME).toString());
                intent.putExtra("parking_lat",  parking_info.get(KEY_LATITUDE).toString());
                intent.putExtra("parking_long", parking_info.get(KEY_LONGITUDE).toString());
                startActivity(intent);

             }
         });
    }


    /**
     * JSON Volley Request to get all available nearest parking spots
     * and display it on a list view.
     *
     * @param adapter - Adapter to be displayed on the list view
     */
    private void JSONRequestParkingSpots(ParkingListAdapter adapter, String URL) {

        final ArrayList<HashMap> parking_list = new ArrayList();
        final ParkingListAdapter final_adapter = adapter;

        swipeRefreshLayout.setRefreshing(true);

        JsonObjectRequest JsonObjectR = new JsonObjectRequest
                (Request.Method.GET, URL, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        try {

                            JSONArray ParkingSpots = response.getJSONArray("parkingSpots");

                            // Tracing trough the ParkingSpots array
                            for(int index =0; index < ParkingSpots.length(); index++){

                                JSONObject location = ParkingSpots.getJSONObject(index);

                                JSONObject location_info = location.getJSONObject("location");

                                JSONObject spot_info = location_info.getJSONObject("spot");

                                String location_name = spot_info.getString("name");
                                JSONArray array_coordinates = spot_info.getJSONArray("coordinates");

                                HashMap parking_info = new HashMap<>();

                                double Lat = array_coordinates.getDouble(0);
                                double Long = array_coordinates.getDouble(1);

                                parking_info.put("Name", location_name);
                                parking_info.put("Lat",Lat);
                                parking_info.put("Long",Long);

                                parking_list.add(parking_info);
                            }

                            // Clear and add the parking spots list into the adapter
                            final_adapter.clear();
                            final_adapter.addAll(parking_list);

                            // Notify the adapter to be updated
                            final_adapter.notifyDataSetChanged();

                            swipeRefreshLayout.setRefreshing(false);

                        } catch (Exception e) {
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
                                Toast.makeText(getApplicationContext(),
                                        "Request Timeout Error!", Toast.LENGTH_LONG)
                                        .show();
                                swipeRefreshLayout.setRefreshing(false);
                            } else {
                                // Handle no internet network error
                                Toast.makeText(getApplicationContext(),
                                        "Network Error. No Internet Connection", Toast.LENGTH_LONG)
                                        .show();
                                swipeRefreshLayout.setRefreshing(false);
                            }
                        }
                    }
                });

        JsonObjectR.setRetryPolicy(new DefaultRetryPolicy(JSON_TIME_OUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Add to JSON request Queue
        JSONVolleyController.getInstance().addToRequestQueue(JsonObjectR);
    }


    /**
     * Method inherited from SwipeRefreshLayout.OnRefreshListener. Executed on
     * SwipeRefreshLayout refresh
     */
    @Override
    public void onRefresh() {
        // Begin: Refresh the item list on swipe down
        ArrayList ParkingList = new ArrayList();
        final ParkingListAdapter adapter = new  ParkingListAdapter(ParkingLocations.this, ParkingList);

        JSONRequestParkingSpots(adapter, URL);

        list = (ListView) findViewById(R.id.parking_listView);
        list.setAdapter(adapter);
        // End: Refresh the item list on swipe down
    }
}
