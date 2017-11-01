package first.alexander.com.car2park;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.gms.maps.OnStreetViewPanoramaReadyCallback;
import com.google.android.gms.maps.StreetViewPanorama;
import com.google.android.gms.maps.StreetViewPanoramaFragment;
import com.google.android.gms.maps.model.LatLng;

public class StreetViewActivity extends AppCompatActivity implements OnStreetViewPanoramaReadyCallback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_street_view);

        StreetViewPanoramaFragment streetViewPanoramaFragment =
                (StreetViewPanoramaFragment) getFragmentManager()
                        .findFragmentById(R.id.streetviewpanorama_fragment);
        streetViewPanoramaFragment.getStreetViewPanoramaAsync(this);

    }


    @Override
    public void onStreetViewPanoramaReady(StreetViewPanorama streetViewPanorama) {

        TextView tvParkingInfo = (TextView) findViewById(R.id.tvParkingInfo);

        tvParkingInfo.setText(null);
        tvParkingInfo.append(" \n Location: " + getIntent().getExtras().getString("current_name"));
        tvParkingInfo.append(" \n  \n" + getIntent().getExtras().getString("current_info"));

        LatLng latLng =getIntent().getExtras().getParcelable("current_marker");
        streetViewPanorama.setPosition(latLng);
    }
}
