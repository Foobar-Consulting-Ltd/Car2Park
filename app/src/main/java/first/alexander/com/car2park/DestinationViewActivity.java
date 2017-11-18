package first.alexander.com.car2park;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.OnStreetViewPanoramaReadyCallback;
import com.google.android.gms.maps.StreetViewPanorama;
import com.google.android.gms.maps.StreetViewPanoramaFragment;
import com.google.android.gms.maps.model.LatLng;

import info.hoang8f.widget.FButton;

public class DestinationViewActivity extends AppCompatActivity implements OnStreetViewPanoramaReadyCallback {

    private LatLng current_latLng;

    private EditText etDestinationName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_destination_view);

        current_latLng = getIntent().getExtras().getParcelable("current_latLng");

        StreetViewPanoramaFragment streetViewPanoramaFragment =
                (StreetViewPanoramaFragment) getFragmentManager()
                        .findFragmentById(R.id.streetviewpanorama_fragment);
        streetViewPanoramaFragment.getStreetViewPanoramaAsync(this);

        FButton btnSaveDestination = (FButton) findViewById(R.id.btnSaveDestination);
        btnSaveDestination.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSaveDestinationDialog();
            }
        });

    }

    @Override
    public void onStreetViewPanoramaReady(StreetViewPanorama streetViewPanorama) {

        TextView tvDestinationInfo = (TextView) findViewById(R.id.tvDestinationInfo);
        tvDestinationInfo.setText(null);
        tvDestinationInfo.append("\n" + getIntent().getExtras().getString("current_info"));

        streetViewPanorama.setPosition(current_latLng);
    }

    private void showSaveDestinationDialog(){

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final SharedPreferences.Editor editor = prefs.edit();

        // Initialize custom dialog for save menu dialog
        final Dialog dialog = new Dialog(DestinationViewActivity.this);
        dialog.setContentView(R.layout.save_destination_dialog);
        dialog.setTitle("Save Destination");
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);

        // Set the custom dialog text view for destination coordinate info
       /* TextView tvDestCoordinate = (TextView) dialog.findViewById(R.id.tvDestCoordinate);
        tvDestCoordinate.setText("\n" + getIntent().getExtras().getString("current_info"));*/

        etDestinationName = (EditText) dialog.findViewById(R.id.etDestinationName);


        FButton btnSave = (FButton) dialog.findViewById(R.id.btnSave);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager inputManager = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);

                if (etDestinationName.getText().length() < 1) {
                    etDestinationName.setError("Please enter a name for the destination");
                }
                else if (prefs.contains("destination_"+etDestinationName.getText().toString())){
                    etDestinationName.setError("Destination name already exists");
                }
                else {
                    etDestinationName.setError(null);
                    // Set Key as user Key
                    final ProgressDialog progressDialog = ProgressDialog.show(DestinationViewActivity.this, "Please wait",
                            "Saving Destination...", true);
                    editor.putString("destination_" + etDestinationName.getText().toString()
                            ,current_latLng.latitude + "," + current_latLng.longitude);
                    editor.commit();
                    progressDialog.dismiss();
                    Toast.makeText(getApplicationContext(),
                            "Destination " + etDestinationName.getText().toString() + " Saved",
                            Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                }

            }
        });


        FButton btnCancelSave = (FButton) dialog.findViewById(R.id.btnCancelSave);
        btnCancelSave.setOnClickListener(new View.OnClickListener() {
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
    }
}
