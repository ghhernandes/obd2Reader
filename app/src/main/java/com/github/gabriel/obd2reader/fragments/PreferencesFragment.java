package com.github.gabriel.obd2reader.fragments;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;

import com.github.gabriel.obd2reader.R;

import java.util.ArrayList;
import java.util.Set;


public class PreferencesFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final int REQUEST_ENABLE_BT = 100;

    BluetoothAdapter btAdapter;
    Set<BluetoothDevice> pairedDevices;
//    Set<BluetoothDevice> discoveredDevices;
    ListPreference btDevicesList;
    ArrayList<CharSequence> entries;
    ArrayList<CharSequence> entryValues;


    public PreferencesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        btDevicesList = (ListPreference) findPreference("bluetooth_list_preference");

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        getActivity().registerReceiver(btDiscoveryReceiver, filter);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        btAdapter = getBtAdapter();
        findPreference("enable_bluetooth_preference").setEnabled(btAdapter != null);
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
            if (sharedPreferences.getBoolean(s, false) == true) {
                enableBluetooth();
                findPairedDevices();
                discoverDevices();
                btDevicesList.setEnabled(true);
            } else {
                btAdapter.disable();
                btDevicesList.setEnabled(false);
            }
        }
    }

    public BluetoothAdapter getBtAdapter() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // nao suporta bluetooth
            return null;
        }
        else {
            return mBluetoothAdapter;
        }
    }

    public void enableBluetooth() {
        if (!btAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    public void findPairedDevices() {
        pairedDevices = btAdapter.getBondedDevices();
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
        btAdapter.startDiscovery();
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
