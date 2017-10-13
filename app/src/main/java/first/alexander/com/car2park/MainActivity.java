package first.alexander.com.car2park;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {


    final String URL = "https://dry-shore-37281.herokuapp.com/parkingspots";

    // Set JSON Request Connection Timeout (15 seconds)
    final int JSON_TIME_OUT = 15000;

    final Context context = this;

    private SwipeRefreshLayout swipeRefreshLayout;

    private ListView list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // SwipeRefreshLayout to refresh the items list view
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout_parking_list);
        swipeRefreshLayout.setOnRefreshListener(this);

        ArrayList<HashMap> parkingList = new ArrayList();
        final ParkingListAdapter adapter = new ParkingListAdapter(MainActivity.this, parkingList);

        // Start a refresh onCreate and initialize JSON Volley Request for item list view
        swipeRefreshLayout.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        swipeRefreshLayout.setRefreshing(true);
                                            JSONRequestParkingSpots(adapter);
                                    }
                                }
        );

        list = (ListView) findViewById(R.id.parking_listView);
        list.setAdapter(adapter);
        /*list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                // Initialize custom dialog for item information
                final Dialog dialog = new Dialog(context);
                dialog.setContentView(R.layout.item_info_dialog);
                dialog.setTitle("Product Item Info");

                // Set the custom dialog text view for item information
                TextView tvItemInfo = (TextView) dialog.findViewById(R.id.item_TextViewDialog);

                // Get the item information to display on the text view
                JSONRequestGetItemInfo(parent.getItemAtPosition(position).toString(), tvItemInfo);

                Button buttonClose = (Button) dialog.findViewById(R.id.buttonClose);
                buttonClose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                dialog.show();
            }
        });*/

    }





    private void JSONRequestParkingSpots(ParkingListAdapter adapter) {

        final ArrayList<HashMap> parking_list = new ArrayList();
        final ParkingListAdapter final_adapter = adapter;

        swipeRefreshLayout.setRefreshing(true);

       /* try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("Title", "VolleyApp Android Demo");
            jsonBody.put("Author", "BNK");
            jsonBody.put("Date", "2015/08/26");
            String requestBody = jsonBody.toString();
        }
        catch(Exception e){
            e.printStackTrace();
        }*/

        JsonObjectRequest JsonObjectR = new JsonObjectRequest
                (Request.Method.GET, URL, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        try {

                            // Get the Order JSON Array
                            JSONArray ParkingSpots = response.getJSONArray("parkingspots");

                            // Tracing trough the ParkingSpots array
                            for(int index =0; index < ParkingSpots.length(); index++){

                                JSONObject info = ParkingSpots.getJSONObject(index);
                                String location_name = info.getString("name");
                                JSONArray array_coordinates = info.getJSONArray("coordinates");

                                HashMap parking_info = new HashMap<>();

                                /*String Lat_Key = location_name + "Lat";
                                String Long_Key = location_name +"Long";*/

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



    @Override
    public void onRefresh() {
        // Begin: Refresh the item list on swipe down
        ArrayList ParkingList = new ArrayList();
        final ParkingListAdapter adapter = new  ParkingListAdapter(MainActivity.this, ParkingList);

        JSONRequestParkingSpots(adapter);

        list = (ListView) findViewById(R.id.parking_listView);
        list.setAdapter(adapter);
        // End: Refresh the item list on swipe down
    }
}
