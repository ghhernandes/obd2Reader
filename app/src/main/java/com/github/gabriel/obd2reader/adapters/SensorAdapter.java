package com.github.gabriel.obd2reader.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.github.gabriel.obd2reader.R;
import com.github.gabriel.obd2reader.classes.SensorClass;

import java.util.List;

public class SensorAdapter extends RecyclerView.Adapter {
    private final List<SensorClass> sensores;
    private final Context context;

    public SensorAdapter(List<SensorClass> sensores, Context context) {
        this.sensores = sensores;
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                      int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.listview_sensors, parent, false);

        SensorViewHolder holder = new SensorViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder,
                                 int position) {
        SensorViewHolder holder = (SensorViewHolder) viewHolder;

        SensorClass sensorClass = sensores.get(position);

        holder.name.setText(sensorClass.getName());
        holder.value.setText(sensorClass.getValue());
    }

    @Override
    public int getItemCount() {
        return sensores.size();
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

}
