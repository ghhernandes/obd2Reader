package com.github.gabriel.obd2reader.fragments;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import com.github.gabriel.obd2reader.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;


public class OptionsFragment extends Fragment {
    private View rootView;
    private Button btnConectar;
    private static final int REQUEST_ENABLE_BT = 178;
    
    public OptionsFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_options, container, false);

        btnConectar = (Button) rootView.findViewById(R.id.btnConectar);
        btnConectar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                connectBluetooth();
            }
        });

        return rootView;
    }

    private void connectBluetooth() {
        ArrayList deviceStrs = new ArrayList();
        final ArrayList devices = new ArrayList();

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(rootView.getContext(), R.string.bluetooth_unsupported, Toast.LENGTH_SHORT).show();
        }else if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        if (bluetoothAdapter.isEnabled()){
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    deviceStrs.add(device.getName() + "\n" + device.getAddress());
                    devices.add(device.getAddress());
                }
            }

            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());

            ArrayAdapter adapter = new ArrayAdapter(getActivity(), android.R.layout.select_dialog_singlechoice,
                                                    deviceStrs.toArray(new String[deviceStrs.size()]));

            alertDialog.setSingleChoiceItems(adapter, -1, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    int position = ((AlertDialog) dialogInterface).getListView().getCheckedItemPosition();
                    String deviceAddress = devices.get(position).toString();

                    try {
                        saveBluetoothSelectedDevice(deviceAddress);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

            alertDialog.setTitle(R.string.bluetooth_choose).show();
        }
    }

    private void saveBluetoothSelectedDevice(String deviceAddress) throws IOException {
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();

        BluetoothDevice device = btAdapter.getRemoteDevice(deviceAddress);

        UUID uuid = UUID.fromString("a1b7ea45-14be-4ce0-ac26-856d613397e0");

        BluetoothSocket socket = (BluetoothSocket) device.createInsecureRfcommSocketToServiceRecord(uuid);

        socket.connect();

    }

}
