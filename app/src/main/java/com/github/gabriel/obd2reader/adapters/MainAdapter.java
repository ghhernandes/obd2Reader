package com.github.gabriel.obd2reader.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.gabriel.obd2reader.R;
import com.github.gabriel.obd2reader.classes.SensorClass;

import java.util.ArrayList;
import java.util.List;

public class MainAdapter extends RecyclerView.Adapter {
    private final List<SensorClass> sensores;
    private List<String> sensor_keys;
    private final Context context;

    public MainAdapter(List<SensorClass> sensores, Context context) {
        this.sensores = sensores;
        this.context = context;
        this.sensor_keys = new ArrayList<>();

        //adiciona o ID de cada sensor em outra lista para consulta
        for (SensorClass s: this.sensores) {
            this.sensor_keys.add(s.getId());
        }
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

    public boolean sensor_added(String sensor_id){
        return this.sensor_keys.contains(sensor_id);
    }

    public void add(SensorClass sensorClass) {

        //se encontrar o ID previamente inserido, chama a funcao de update
        if (!sensor_added(sensorClass.getId())) {
            this.sensores.add(sensorClass);
            this.sensor_keys.add(sensorClass.getId());

            notifyItemInserted(getItemCount());
        }else {
            this.update(this.sensor_keys.indexOf(sensorClass.getId()), sensorClass.getValue());
        }
    }

    public void update (int position, String value) {
        SensorClass sensorClass = this.sensores.get(position);
        sensorClass.setValue(value);

        notifyItemChanged(position);
    }

    public void delete (int position) {
        this.sensores.remove(position);
        this.sensor_keys.remove(position);

        notifyItemRemoved(position);
        notifyItemRangeChanged(position, this.sensores.size());
    }

}
