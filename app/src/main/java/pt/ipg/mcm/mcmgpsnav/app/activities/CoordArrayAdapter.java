package pt.ipg.mcm.mcmgpsnav.app.activities;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import pt.ipg.mcm.mcmgpsnav.app.R;
import pt.ipg.mcm.mcmgpsnav.app.db.gen.CoordAndCompass;

import java.util.List;

public class CoordArrayAdapter extends ArrayAdapter<CoordAndCompass> {
    private final Context context;
    private final List<CoordAndCompass> values;

    public CoordArrayAdapter(Context context, List<CoordAndCompass> values) {
        super(context, -1, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.coord_row_layout, parent, false);

        TextView tvDegrees = (TextView) rowView.findViewById(R.id.tvDegrees);
        tvDegrees.setText("" + values.get(position).getDegrees());
        TextView tvLat = (TextView) rowView.findViewById(R.id.tvLongitude);
        tvLat.setText("" + values.get(position).getLatitude());
        TextView tvLong = (TextView) rowView.findViewById(R.id.tvLatitude);
        tvLong.setText("" + values.get(position).getLongitude());
        TextView tvDateTime = (TextView) rowView.findViewById(R.id.tvDateTime);
        tvDateTime.setText(values.get(position).getDate().toString());

        TextView tvLatNext = (TextView) rowView.findViewById(R.id.tvLatNext);
        tvLatNext.setText(""+values.get(position).getNextLat());
        TextView tvLonNext = (TextView) rowView.findViewById(R.id.tvLonNext);
        tvLonNext.setText(""+values.get(position).getNextLon());
        TextView tvDegreesToNext = (TextView) rowView.findViewById(R.id.tvDegreesToNext);
        tvDegreesToNext.setText(""+values.get(position).getDegreesToNext());
        TextView tvDistanceNext = (TextView) rowView.findViewById(R.id.tvDistanceNext);
        tvDistanceNext.setText(""+values.get(position).getDistanceToNext());


        return rowView;
    }
}
