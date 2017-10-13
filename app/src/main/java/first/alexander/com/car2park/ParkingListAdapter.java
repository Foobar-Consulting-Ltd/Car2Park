package first.alexander.com.car2park;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Alexander Julianto on 10/11/2017.
 */

public class ParkingListAdapter  extends ArrayAdapter {

    private final Activity context;
    private final ArrayList parking_list;

    public ParkingListAdapter(Activity context, ArrayList parking_list) {
        super(context, R.layout.parking_list_row, parking_list);
        this.context = context;
        this.parking_list = parking_list;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {

        // Begin: Set up list_row layout as custom item list view row
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.parking_list_row, null, true);

        TextView tvTitle = (TextView) rowView.findViewById(R.id.location_name);
        tvTitle.setText(parking_list.get(position).toString());
        // End: Set up list_row layout as custom item list view row

        return rowView;
    }
}
