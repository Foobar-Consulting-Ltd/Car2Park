package first.alexander.com.car2park;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar;
import com.google.android.gms.maps.OnStreetViewPanoramaReadyCallback;
import com.google.android.gms.maps.StreetViewPanorama;
import com.google.android.gms.maps.StreetViewPanoramaFragment;
import com.google.android.gms.maps.model.LatLng;

import info.hoang8f.widget.FButton;

public class StreetViewActivity extends AppCompatActivity implements OnStreetViewPanoramaReadyCallback  {

    private LatLng current_latLng;

    private  RoundCornerProgressBar parkingCapacity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_street_view);

        current_latLng = getIntent().getExtras().getParcelable("current_latLng");

        parkingCapacity = (RoundCornerProgressBar) findViewById(R.id.progress_2);

        StreetViewPanoramaFragment streetViewPanoramaFragment =
                (StreetViewPanoramaFragment) getFragmentManager()
                        .findFragmentById(R.id.streetviewpanorama_fragment);
        streetViewPanoramaFragment.getStreetViewPanoramaAsync(this);

        FButton btnSetParkingDestination = (FButton) findViewById(R.id.btnSetParkingDestination);
        btnSetParkingDestination.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDestinationDirection();
            }
        });

    }


    @Override
    public void onStreetViewPanoramaReady(StreetViewPanorama streetViewPanorama) {

        TextView tvParkingName = (TextView) findViewById(R.id.tvParkingName);
        TextView tvParkingInfo = (TextView) findViewById(R.id.tvParkingInfo);

        tvParkingName.setText(null);
        tvParkingInfo.setText(null);

        String parking_info = getIntent().getExtras().getString("current_info");

        // Split parking information
        String[] parts = parking_info.split(",");
        String total = parts[0];
        String used = parts[1];
        String distance = parts[2];

        tvParkingName.append(getIntent().getExtras().getString("current_name"));
        tvParkingInfo.append(distance + "\n\n" + total + used);

        streetViewPanorama.setPosition(current_latLng);

        // Need to extract numbers from info string
        total = total.replaceAll("\\D+","");
        used = used.replaceAll("\\D+","");
        distance = distance.replaceAll("\\D+","");

        int num_total = Integer.parseInt(total);
        int num_used = Integer.parseInt(used);
        int num_distance = Integer.parseInt(distance);

        parkingCapacity.setProgressColor(Color.parseColor("#1abc9c")); // Green
        parkingCapacity.setProgressBackgroundColor(Color.parseColor("#757575")); // Grey
        parkingCapacity.setMax(num_total);
        parkingCapacity.setProgress(num_used);

    }


    private void showDestinationDirection(){

        String Uri_destination = Double.toString(current_latLng.latitude);
        Uri_destination += "," + Double.toString(current_latLng.longitude);

        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + Uri_destination);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }
}
