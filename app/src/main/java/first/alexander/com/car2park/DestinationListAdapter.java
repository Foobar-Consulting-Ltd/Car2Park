package first.alexander.com.car2park;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Alexander Julianto on 11/17/2017.
 */

public class DestinationListAdapter extends ArrayAdapter {


    // Hash Map keys
    private final String KEY_NAME = "Name";
    private final String KEY_LATITUDE = "Lat";
    private final String KEY_LONGITUDE = "Long";
    private final String KEY_ADDRESS = "Address";

    private final Activity context;
    private final ArrayList<HashMap> destination_list;

    public  DestinationListAdapter(Activity context, ArrayList<HashMap> destination_list) {
        super(context, R.layout.destination_list_row, destination_list);
        this.context = context;
        this.destination_list = destination_list;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {

        // Begin: Set up list_row layout as custom item list view row
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.destination_list_row, null, true);

        HashMap destination_info = destination_list.get(position);

        String address = "Address: " + destination_info.get(KEY_ADDRESS).toString();

        TextView tvName = (TextView) rowView.findViewById(R.id.destination_name);
        tvName.setText(destination_info.get(KEY_NAME).toString());
        TextView tvCoordinates = (TextView) rowView.findViewById(R.id.destination_address);
        tvCoordinates.setText(address);
        // End: Set up list_row layout as custom item list view row

        return rowView;
    }

}
