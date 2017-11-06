package first.alexander.com.car2park;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.OnStreetViewPanoramaReadyCallback;
import com.google.android.gms.maps.StreetViewPanorama;
import com.google.android.gms.maps.StreetViewPanoramaFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import info.hoang8f.widget.FButton;

public class StreetViewActivity extends AppCompatActivity implements OnStreetViewPanoramaReadyCallback {

    private LatLng current_latLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_street_view);

        current_latLng = getIntent().getExtras().getParcelable("current_latLng");

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

        tvParkingName.append(getIntent().getExtras().getString("current_name"));
        tvParkingInfo.append("\n" + getIntent().getExtras().getString("current_info"));

        streetViewPanorama.setPosition(current_latLng);
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
