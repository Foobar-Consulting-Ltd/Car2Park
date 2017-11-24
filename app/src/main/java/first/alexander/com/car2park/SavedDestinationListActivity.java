package first.alexander.com.car2park;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.sdsmdg.tastytoast.TastyToast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import info.hoang8f.widget.FButton;

public class SavedDestinationListActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private ListView list;

    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_destination_list);

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // SwipeRefreshLayout to refresh the items list view
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout_destination_list);
        swipeRefreshLayout.setOnRefreshListener(this);

        ArrayList<HashMap> destinationList = new ArrayList();
        final DestinationListAdapter adapter = new DestinationListAdapter(SavedDestinationListActivity.this, destinationList);


        // Start a refresh onCreate and initialize JSON Volley Request for item list view
        swipeRefreshLayout.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        swipeRefreshLayout.setRefreshing(true);
                                        // Set and populate the list adapter with saved destinations
                                        setDestinationList(adapter);
                                    }
                                }
        );



        list = (ListView) findViewById(R.id.destination_listView);
        list.setAdapter(adapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                HashMap destination_info = (HashMap) parent.getItemAtPosition(position);

                Intent intent = new Intent(getBaseContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("dest_name",  destination_info.get("Name").toString());
                intent.putExtra("dest_lat",  (double) destination_info.get("Lat"));
                intent.putExtra("dest_long", (double) destination_info.get("Long"));
                intent.putExtra("dest_address", destination_info.get("Address").toString());
                startActivity(intent);
                finish();
            }
        });


        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                final HashMap destination_info = (HashMap) parent.getItemAtPosition(position);

                // Initialize custom dialog for save menu dialog
                final Dialog dialog = new Dialog(SavedDestinationListActivity.this);
                dialog.setContentView(R.layout.saved_destination_edit_dialog);
                dialog.setTitle("Edit Saved Destination");
                dialog.setCanceledOnTouchOutside(false);
                dialog.setCancelable(false);

                // Get and set current saved destination name
                final EditText etEditName = (EditText) dialog.findViewById(R.id.etEditName);
                etEditName.setText(destination_info.get("Name").toString());

                FButton btnSaveEdit = (FButton) dialog.findViewById(R.id.btnSaveEdit);
                btnSaveEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        InputMethodManager inputManager = (InputMethodManager)
                                getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                                InputMethodManager.HIDE_NOT_ALWAYS);

                        if (etEditName.getText().length() < 1) {
                            etEditName.setError("Please enter a new name for the destination");
                        }
                        else if (prefs.contains("destination_"+etEditName.getText().toString())){
                            etEditName.setError("Destination name already exists");
                        }
                        else {
                            changeDestinationName(destination_info.get("Name").toString(),
                                    etEditName.getText().toString());

                            // Reset and re-populate the list adapter with saved destinations
                            swipeRefreshLayout.setRefreshing(true);
                            setDestinationList(adapter);
                            dialog.dismiss();
                        }
                    }
                });


                FButton btnDeleteSavedDest = (FButton) dialog.findViewById(R.id.btnDeleteSavedDest);
                btnDeleteSavedDest.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        InputMethodManager inputManager = (InputMethodManager)
                                getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                                InputMethodManager.HIDE_NOT_ALWAYS);

                        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface di, int which) {
                                switch (which){
                                    case DialogInterface.BUTTON_POSITIVE:
                                        // Yes, delete the destination
                                        deleteDestination(destination_info.get("Name").toString());

                                        // Reset and re-populate the list adapter with saved destinations
                                        swipeRefreshLayout.setRefreshing(true);
                                        setDestinationList(adapter);
                                        dialog.dismiss();
                                        break;

                                    case DialogInterface.BUTTON_NEGATIVE:
                                        //No, don't delete
                                        break;
                                }
                            }
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(SavedDestinationListActivity.this);
                        builder.setMessage("Delete this destination ?").setPositiveButton("Yes", dialogClickListener)
                                .setNegativeButton("No", dialogClickListener).show();
                    }
                });


                FButton btnCancelEdit = (FButton) dialog.findViewById(R.id.btnCancelEdit);
                btnCancelEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        InputMethodManager inputManager = (InputMethodManager)
                                getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                                InputMethodManager.HIDE_NOT_ALWAYS);
                        dialog.dismiss();
                    }
                });

                dialog.show();

                return true;
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
                    String[] info_dest = entry.getValue().toString().split(";");
                    double Lat = Double.parseDouble(info_dest[0]);
                    double Long = Double.parseDouble(info_dest[1]);
                    String destination_address = "";

                    if(info_dest.length > 2) {
                        destination_address = info_dest[2];
                    }

                    destination_info.put("Name", destination_name);
                    destination_info.put("Lat",Lat);
                    destination_info.put("Long",Long);
                    destination_info.put("Address",destination_address);

                    destination_list.add(destination_info);
                }

            }

        }

        adapter.clear();
        adapter.addAll(destination_list);
        adapter.notifyDataSetChanged();

        swipeRefreshLayout.setRefreshing(false);
    }

    private void changeDestinationName(String OldName, String NewName){

        final ProgressDialog progressDialog = ProgressDialog.show(SavedDestinationListActivity.this,
                "Please wait",
                "Changing Destination Name...", true);
        if(OldName.equals(NewName)){
            progressDialog.dismiss();
            return;
        }

        String key_old = "destination_" + OldName;
        String key_new = "destination_" + NewName;

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final SharedPreferences.Editor editor = prefs.edit();
        Map<String,?> keys = prefs.getAll();

        for(Map.Entry<String,?> entry : keys.entrySet()){

            if((!entry.getKey().isEmpty()) && (entry.getValue() != null)){

                if(entry.getKey().toString().equals(key_old)){
                    String dest_info = entry.getValue().toString();
                    editor.remove(key_old);
                    editor.putString(key_new,dest_info);
                    editor.commit();

                    progressDialog.dismiss();
                    TastyToast.makeText(getApplicationContext(),  "Destination name changed",
                            TastyToast.LENGTH_LONG, TastyToast.DEFAULT);
                    return;
                }

            }

        }

        progressDialog.dismiss();
        TastyToast.makeText(getApplicationContext(),   "Error: Could not find key",
                TastyToast.LENGTH_LONG, TastyToast.ERROR);

    }

    private void deleteDestination(String DestName){
        final ProgressDialog progressDialog = ProgressDialog.show(SavedDestinationListActivity.this,
                "Please wait",
                "Deleting Destination...", true);

        String key = "destination_" + DestName;

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final SharedPreferences.Editor editor = prefs.edit();

        Map<String,?> keys = prefs.getAll();

        for(Map.Entry<String,?> entry : keys.entrySet()){

            if((!entry.getKey().isEmpty()) && (entry.getValue() != null)){

                if(entry.getKey().toString().equals(key)){
                    editor.remove(key);
                    editor.commit();

                    progressDialog.dismiss();
                    TastyToast.makeText(getApplicationContext(),   "Destination deleted",
                            TastyToast.LENGTH_LONG, TastyToast.DEFAULT);
                    return;
                }

            }

        }

        TastyToast.makeText(getApplicationContext(),    "Error: Could not find key",
                TastyToast.LENGTH_LONG, TastyToast.ERROR);
        progressDialog.dismiss();

    }

    @Override
    public void onRefresh() {
        ArrayList<HashMap> destinationList = new ArrayList();
        final DestinationListAdapter adapter = new DestinationListAdapter(SavedDestinationListActivity.this, destinationList);

        setDestinationList(adapter);

        list = (ListView) findViewById(R.id.destination_listView);
        list.setAdapter(adapter);
    }
}
