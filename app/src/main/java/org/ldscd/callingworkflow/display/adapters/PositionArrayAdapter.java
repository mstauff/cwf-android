package org.ldscd.callingworkflow.display.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.ldscd.callingworkflow.R;
import org.ldscd.callingworkflow.model.Position;

import java.util.ArrayList;
import java.util.List;

/**
 * Used for listing out positions
 */
public class PositionArrayAdapter<T> extends ArrayAdapter<Position> {
    private static class ViewHolder {
        private TextView itemView;
    }

    public PositionArrayAdapter(Context context, int textViewResourceId, List<Position> items) {
        super(context, textViewResourceId, items);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView = LayoutInflater.from(this.getContext()).inflate(R.layout.position_list_layout, parent, false);
        }
        Position item = getItem(position);
        TextView itemView = (TextView) convertView.findViewById(R.id.create_calling_position_name);
        itemView.setText(item.getName());
        return convertView;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(this.getContext())
                    .inflate(R.layout.position_list_layout, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.itemView = (TextView) convertView.findViewById(R.id.create_calling_position_name);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Position item = getItem(position);
        if (item != null) {
            /* Add the correct text to the place holder */
            viewHolder.itemView.setText(String.format("%s", item.getName()));
        }

        return convertView;
    }
}