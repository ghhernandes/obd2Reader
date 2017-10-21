package com.github.gabriel.obd2reader.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.gabriel.obd2reader.R;
import com.github.gabriel.obd2reader.classes.SensorClass;

import java.util.List;

public class MainAdapter extends RecyclerView.Adapter {
    private final List<SensorClass> sensores;
    private final Context context;

    public MainAdapter(List<SensorClass> sensores, Context context) {
        this.sensores = sensores;
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                      int viewType) {
//        Método que deverá retornar layout criado pelo ViewHolder já inflado em uma view.

        View view = LayoutInflater.from(context)
                .inflate(R.layout.main_card_view, parent, false);

        MainViewHolder holder = new MainViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder,
                                 int position) {

//        Método que recebe o ViewHolder e a posição da lista.
//        Aqui é recuperado o objeto da lista de Objetos pela posição e associado à ViewHolder.

        MainViewHolder holder = (MainViewHolder) viewHolder;

        SensorClass sensorClass = sensores.get(position);

        holder.name.setText(sensorClass.getName());
        holder.value.setText(sensorClass.getValue());
    }

    @Override
    public int getItemCount() {
        return sensores != null ? sensores.size() : 0;
    }

    public void add(SensorClass sensorClass) {
        this.sensores.add(sensorClass);
        notifyItemInserted(getItemCount());
    }

    public void update (int position, String value) {
        SensorClass sensorClass = this.sensores.get(position);
        sensorClass.setValue(value);
        notifyItemChanged(position);
    }

    public void delete (int position) {
        this.sensores.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, this.sensores.size());
    }

}
