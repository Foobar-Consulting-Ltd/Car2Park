package first.alexander.com.car2park_mvp;

import android.app.ActionBar;
import android.app.Dialog;
import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {


    final String URL = "http://casahydro.ddns.net:8080";

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

        ArrayList itemList = new ArrayList();
        final ParkingListAdapter adapter = new ParkingListAdapter(MainActivity.this, itemList);

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

        final ArrayList product_list = new ArrayList();
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
                            String Name = response.getString("name");
                            product_list.add(Name);

                            /*// Tracing trough the Order array
                            for (int order_index = 0; order_index < OrderArray.length(); order_index++) {

                                // Get an order
                                JSONObject Order = OrderArray.getJSONObject(order_index);

                                // Make sure order is not cancelled
                                if (Order.getString("cancel_reason").equals("null")) {

                                    // Get a line items array from an order
                                    JSONArray line_itemsArray = Order.getJSONArray("line_items");


                                    // Tracing trough the line items array
                                    for (int line_index = 0; line_index < line_itemsArray.length(); line_index++) {

                                        // Get a line item
                                        JSONObject Item = line_itemsArray.getJSONObject(line_index);

                                        // Get the item title
                                        String item_title = Item.getString("title");

                                        // Check for duplicates and add the name into the list
                                        if (!product_list.contains(item_title)) {
                                            product_list.add(item_title);
                                            System.out.println(item_title);
                                        }

                                    }

                                }

                            }

                            // Sort customer by first name
                            Collections.sort(product_list, new Comparator<String>() {
                                @Override
                                public int compare(String s1, String s2) {
                                    return s1.compareToIgnoreCase(s2);
                                }
                            });*/

                            // Clear and add the product list into the adapter
                            final_adapter.clear();
                            final_adapter.addAll(product_list);

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
        ArrayList itemList = new ArrayList();
        final ParkingListAdapter adapter = new  ParkingListAdapter(MainActivity.this, itemList);

        JSONRequestParkingSpots(adapter);

        list = (ListView) findViewById(R.id.parking_listView);
        list.setAdapter(adapter);
        // End: Refresh the item list on swipe down
    }
}
