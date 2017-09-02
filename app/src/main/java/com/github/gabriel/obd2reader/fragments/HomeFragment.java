package com.github.gabriel.obd2reader.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.github.gabriel.obd2reader.R;
import com.github.gabriel.obd2reader.adapters.SensorAdapter;
import com.github.gabriel.obd2reader.classes.SensorClass;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    public HomeFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        ListView list = (ListView) rootView.findViewById(R.id.list_sensor);

        List<SensorClass> sensores = new ArrayList<SensorClass>(); //passar os sensores

        sensores.add(new SensorClass("velocidade", "Velocidade", "", "104 km/h"));
        sensores.add(new SensorClass("distancia", "Distância Percorrida", "", "200 km"));
        sensores.add(new SensorClass("temperatura", "Temperatura", "", "30 °C"));
        sensores.add(new SensorClass("consumo", "Consumo", "", "10 km/l"));

//        ArrayAdapter<SensorClass> adapter = new ArrayAdapter<SensorClass>(rootView.getContext(),
//                                                                          android.R.layout.simple_list_item_1,
//                                                                          sensores);
        SensorAdapter adapter = new SensorAdapter(sensores, getActivity());
        list.setAdapter(adapter);

        return rootView;
    }

}
