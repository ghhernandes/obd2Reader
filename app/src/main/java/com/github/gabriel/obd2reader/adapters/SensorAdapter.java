package com.github.gabriel.obd2reader.adapters;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.github.gabriel.obd2reader.R;
import com.github.gabriel.obd2reader.classes.SensorClass;

import java.util.List;

public class SensorAdapter extends BaseAdapter {
    private final List<SensorClass> sensores;
    private final Activity act;

    public SensorAdapter(List<SensorClass> sensores, Activity act) {
        this.sensores = sensores;
        this.act = act;
    }

    @Override
    public int getCount() {
        return sensores.size();
    }

    public Object getItem(int position) {
        return sensores.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    //metodo responsavel pela criacao de cada view
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = act.getLayoutInflater().inflate(R.layout.listview_sensors, parent, false);

        SensorClass sensor = sensores.get(position);

        TextView name = (TextView) view.findViewById(R.id.list_sensor_name);
        TextView value = (TextView) view.findViewById(R.id.list_sensor_value);

        name.setText(sensor.getName());
        value.setText(sensor.getValue());
        return view;
    }
}
