package com.github.gabriel.obd2reader.fragments;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.github.gabriel.obd2reader.R;
import com.github.gabriel.obd2reader.activities.MainActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;


public class PreferencesFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final int REQUEST_ENABLE_BT = 100;
    Set<BluetoothDevice> pairedDevices;
    ListPreference btDevicesList;
    ArrayList<CharSequence> entries;
    ArrayList<CharSequence> entryValues;


    public PreferencesFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        btDevicesList = (ListPreference) findPreference("bluetooth_list_preference");
        btDevicesList.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                try {
                    ((MainActivity)getActivity()).setSelectedBluetoothDevice(btDevicesList.getValue());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ((MainActivity)getActivity()).startLiveData();
                return true;
            }
        });


        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        getActivity().registerReceiver(btDiscoveryReceiver, filter);

        //Protocolos
        ArrayList<CharSequence> protocolEntries = new ArrayList<CharSequence>();
        ArrayList<CharSequence> protocolValues = new ArrayList<CharSequence>();
        protocolEntries.add("AUTO");
        protocolValues.add("AUTO");
        ListPreference obdProtocolsList = (ListPreference) findPreference("obd_protocols_preference");
        obdProtocolsList.setEntries(listToArray(protocolEntries));
        obdProtocolsList.setEntryValues(listToArray(protocolValues));
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        findPreference("enable_bluetooth_preference").setEnabled(((MainActivity)getActivity()).Adapter != null);

        CheckBoxPreference cbxBluetooth = (CheckBoxPreference) findPreference("enable_bluetooth_preference");
        cbxBluetooth.setChecked(((MainActivity)getActivity()).Adapter.isEnabled());

        this.onBluetoothCheckBoxChanged(cbxBluetooth.isChecked());
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(btDiscoveryReceiver);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (s.equals("enable_bluetooth_preference")) {
            this.onBluetoothCheckBoxChanged(sharedPreferences.getBoolean(s, false) == true);
        }
    }

    private void onBluetoothCheckBoxChanged(boolean Active) {
        if (Active) {
            enableBluetooth();
            findPairedDevices();
            discoverDevices();
            btDevicesList.setEnabled(true);
        } else {
            ((MainActivity)getActivity()).Adapter.disable();
            btDevicesList.setEnabled(false);
        }
    }

    public void enableBluetooth() {
        if (!((MainActivity)getActivity()).Adapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    public void findPairedDevices() {
        pairedDevices = ((MainActivity)getActivity()).Adapter.getBondedDevices();
        entries = new ArrayList<CharSequence>();
        entryValues = new ArrayList<CharSequence>();
        for (BluetoothDevice d : pairedDevices) {
            entries.add(d.getName());
            entryValues.add(d.getAddress());
        }
        btDevicesList.setEntries(listToArray(entries));
        btDevicesList.setEntryValues(listToArray(entryValues));
    }

    public void discoverDevices() {
        ((MainActivity)getActivity()).Adapter.startDiscovery();
    }

    public CharSequence[] listToArray(ArrayList<CharSequence> list) {
        CharSequence[] sequence = new CharSequence[list.size()];
        for (int i = 0; i < list.size(); i++) {
            sequence[i] = list.get(i);
        }
        return sequence;
    }

    private final BroadcastReceiver btDiscoveryReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // quando descobrir um dispositivo
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {

                // obtem o objeto do intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //discoveredDevices.add(device);

                entries.add(device.getName());
                btDevicesList.setEntries(listToArray(entries));

                entryValues.add(device.getAddress());
                btDevicesList.setEntryValues(listToArray(entryValues));

            }
        }
    };
}
