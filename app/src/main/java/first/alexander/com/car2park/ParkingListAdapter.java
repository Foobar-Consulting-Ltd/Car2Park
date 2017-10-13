package first.alexander.com.car2park;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Alexander Julianto on 10/11/2017.
 */

public class ParkingListAdapter  extends ArrayAdapter {

    private final Activity context;
    private final ArrayList<HashMap> parking_list;

    public ParkingListAdapter(Activity context, ArrayList<HashMap> parking_list) {
        super(context, R.layout.parking_list_row, parking_list);
        this.context = context;
        this.parking_list = parking_list;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {

        // Begin: Set up list_row layout as custom item list view row
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.parking_list_row, null, true);

        HashMap parking_info = parking_list.get(position);

        String coordinates = "Coordinates: " + parking_info.get("Lat").toString() + ", " +  parking_info.get("Long").toString();

        TextView tvName = (TextView) rowView.findViewById(R.id.location_name);
        tvName.setText(parking_info.get("Name").toString());
        TextView tvCoordinates = (TextView) rowView.findViewById(R.id.coordinates);
        tvCoordinates.setText(coordinates);
        // End: Set up list_row layout as custom item list view row

        return rowView;
    }
}
