package first.alexander.com.car2park;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SavedDestinationListActivity extends AppCompatActivity {

    private ListView list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_destination_list);



        // SwipeRefreshLayout to refresh the items list view
        /*swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout_parking_list);
        swipeRefreshLayout.setOnRefreshListener(this);*/

        ArrayList<HashMap> destinationList = new ArrayList();
        final DestinationListAdapter adapter = new DestinationListAdapter(SavedDestinationListActivity.this, destinationList);


        // Start a refresh onCreate and initialize JSON Volley Request for item list view
    /*    swipeRefreshLayout.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        swipeRefreshLayout.setRefreshing(true);
                                        JSONRequestParkingSpots(adapter, URL);
                                    }
                                }
        );*/

        // Set and populate the list adapter with saved destinations
        setDestinationList(adapter);

        list = (ListView) findViewById(R.id.destination_listView);
        list.setAdapter(adapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                HashMap destination_info = (HashMap) parent.getItemAtPosition(position);

                //Intent intent = new Intent(getBaseContext(), MainActivity.class);
                /*intent.putExtra("parking_name",  destination_info.get(KEY_NAME).toString());
                intent.putExtra("parking_lat",  destination_info.get(KEY_LATITUDE).toString());
                intent.putExtra("parking_long", destination_info.get(KEY_LONGITUDE).toString());*/
                //startActivity(intent);

            }
        });

    }


    private void setDestinationList(DestinationListAdapter adapter){

        final ArrayList<HashMap> destination_list = new ArrayList();
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        Map<String,?> keys = prefs.getAll();

        for(Map.Entry<String,?> entry : keys.entrySet()){

            if((!entry.getKey().isEmpty()) && (entry.getValue() != null)){

                if(entry.getKey().toString().contains("destination_")){

                    HashMap destination_info = new HashMap<>();
                    String destination_name = entry.getKey().toString().replaceFirst("^destination_","");
                    String[] Lat_Long = entry.getValue().toString().split(",");
                    double Lat = Double.parseDouble(Lat_Long[0]);
                    double Long = Double.parseDouble(Lat_Long[1]);

                    destination_info.put("Name", destination_name);
                    destination_info.put("Lat",Lat);
                    destination_info.put("Long",Long);

                    destination_list.add(destination_info);
                }

            }

        }

        adapter.addAll(destination_list);
    }

}
