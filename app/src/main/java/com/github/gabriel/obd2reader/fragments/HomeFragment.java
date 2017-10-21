package com.github.gabriel.obd2reader.fragments;


import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.gabriel.obd2reader.R;
import com.github.gabriel.obd2reader.activities.MainActivity;
import com.github.gabriel.obd2reader.adapters.MainAdapter;
import com.github.gabriel.obd2reader.classes.SensorClass;
import com.github.gabriel.obd2reader.config.ObdConfig;
import com.github.pires.obd.commands.ObdCommand;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {

    private MainAdapter mAdapter = null;
    private MainActivity act = null;
    private List<SensorClass> sensores = null;

    public HomeFragment() {
    }

    private final Runnable liveDataThread = new Runnable() {
        @Override
        public void run() {
            int position = 0;
            for (ObdCommand command: ObdConfig.getCommands()) {
                try {
                    if ((act.liveDataActive) && (act.Socket != null) && (act.Socket.isConnected())) {
                        command.run(act.Socket.getInputStream(), act.Socket.getOutputStream());

                        mAdapter.update(position, command.getFormattedResult());
                    }

                    Log.d("liveDataThread", command.getFormattedResult());
                    position += 1;

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            new Handler().postDelayed(liveDataThread, 1000);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.act = ((MainActivity)getActivity());
        this.sensores = new ArrayList<SensorClass>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler);

        // aumenta performance se as alteracoes nao afetarem o tamanho do layout
        recyclerView.setHasFixedSize(true);

//        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity(),
//                LinearLayoutManager.VERTICAL, false);

        //grid escalavel
//        StaggeredGridLayoutManager layoutManager =
//                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);

        GridLayoutManager layoutManager = new GridLayoutManager(act, 2);

        recyclerView.setLayoutManager(layoutManager);

        this.insertDefaultSensors();

        this.mAdapter = new MainAdapter(sensores, act);
        recyclerView.setAdapter(this.mAdapter);

        return rootView;
    }

    private void insertDefaultSensors() {
        for (ObdCommand command: ObdConfig.getCommands()) {
            try {
                this.sensores.add(new SensorClass(command.getCommandPID(), command.getName(), "", "N/A"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
