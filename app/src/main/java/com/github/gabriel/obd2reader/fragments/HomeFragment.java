package com.github.gabriel.obd2reader.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
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
import com.github.pires.obd.exceptions.NoDataException;
import com.github.pires.obd.exceptions.NonNumericResponseException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private MainAdapter mAdapter = null;
    private MainActivity act = null;
    private List<SensorClass> sensores = null;
    private LiveDataThread dataThread = null;

    public List<SensorClass> getSensores() {
        return this.sensores;
    }


    public HomeFragment() {
    }

    private class LiveDataThread extends Thread {
        @Override
        public void run(){
            while (!this.isInterrupted()){
                for (final SensorClass sensorClass: sensores) {
                    try {
                        if (act.liveDataActive) {
                            if (sensorClass.getCmd() != null) {
                                try {
                                    sensorClass.getCmd().run(act.Socket.getInputStream(), act.Socket.getOutputStream());
                                } catch (NonNumericResponseException | NoDataException | IndexOutOfBoundsException e) {
                                    e.printStackTrace();
                                }
                            }

                            final String value = sensorClass.getCmd().getFormattedResult();
                            if (!value.equals(sensorClass.getValue())){
                                act.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mAdapter.update(sensores.indexOf(sensorClass), value);
                                    }
                                });
                            }
                        }
                    } catch (Exception e) {
                        if (e.getMessage().toLowerCase().contains("broken pipe"))
                            act.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        act.disconnectBluetoothDevice();
                                        interrupt();
                                    } catch (IOException e1) {
                                        Log.i("LiveDataThread", "Erro ao desconectar. "+e1.getMessage());
                                        e1.printStackTrace();
                                    }
                                }
                            });
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.act = ((MainActivity)getActivity());
        this.sensores = new ArrayList<>();
    }

    @Override
    public void onPause() {
        super.onPause();
        ((MainActivity)getActivity()).liveDataActive = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (this.dataThread != null) {
            this.dataThread.interrupt();
            this.dataThread = null;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        RecyclerView recyclerView = rootView.findViewById(R.id.recycler);

        // aumenta performance se as alteracoes nao afetarem o tamanho do layout
        recyclerView.setHasFixedSize(true);

//        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity(),
//                LinearLayoutManager.VERTICAL, false);

        //grid escalavel
        StaggeredGridLayoutManager layoutManager =
                new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);

//        GridLayoutManager layoutManager = new GridLayoutManager(act, 2);

        recyclerView.setLayoutManager(layoutManager);

        ((MainActivity)getActivity()).liveDataActive = ((MainActivity)getActivity()).deviceIsConnected();

        this.insertDefaultSensors();

        this.mAdapter = new MainAdapter(sensores, act);
        recyclerView.setAdapter(this.mAdapter);

        if ((this.dataThread == null) && ((MainActivity)getActivity()).liveDataActive) {
            this.dataThread = new LiveDataThread();
            this.dataThread.start();
        }

        return rootView;
    }

    private void insertDefaultSensors() {
        this.sensores.clear();
        for (ObdCommand command: ObdConfig.getCommands()) {
            try {
                this.sensores.add(new SensorClass(command.getCommandPID(), command.getName(), "", "N/A", command));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
