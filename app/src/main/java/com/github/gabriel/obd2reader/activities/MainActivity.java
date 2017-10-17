package com.github.gabriel.obd2reader.activities;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.github.gabriel.obd2reader.config.ObdConfig;
import com.github.gabriel.obd2reader.fragments.HomeFragment;
import com.github.gabriel.obd2reader.fragments.NotificationsFragment;
import com.github.gabriel.obd2reader.R;
import com.github.gabriel.obd2reader.fragments.PreferencesFragment;
import com.github.pires.obd.commands.ObdCommand;

import java.io.IOException;
import java.util.ArrayList;

import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private static boolean bluetoothDefaultIsEnable = false;
    private boolean preRequisites = true;
    private String BluetoothDeviceAddress = "";
    private BluetoothSocket SocketConn = null;
    private LiveDataThread liveDataThread = null;

    class LiveDataThread extends Thread {
        @Override
        public void run() {
            for (ObdCommand command: ObdConfig.getCommands()) {
                try {
                    command.run(SocketConn.getInputStream(), SocketConn.getOutputStream());
                    Log.d("OBDCommand", command.getFormattedResult());

                    sleep(100);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();

            switch (item.getItemId()) {
                case R.id.navigation_home:
                    setTitle(R.string.title_home);
                    transaction.replace(R.id.main_content, new HomeFragment()).commit();
                    break;
                case R.id.navigation_options:
                    setTitle(R.string.title_options);
                    transaction.replace(R.id.main_content, new PreferencesFragment()).commit();
                    break;
                case R.id.navigation_notifications:
                    setTitle(R.string.title_notifications);
                    transaction.replace(R.id.main_content, new NotificationsFragment()).commit();
                    break;
            }

            return true;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.BluetoothDeviceAddress = "";
        this.SocketConn = null;

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        final BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter != null)
            bluetoothDefaultIsEnable = btAdapter.isEnabled();

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();

        final BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();

        preRequisites = btAdapter != null && btAdapter.isEnabled();

//        if (!preRequisites) {
//            preRequisites = btAdapter.enable();
//        }

        if (!preRequisites) {
            Toast.makeText(this, getString(R.string.text_bluetooth_disabled), Toast.LENGTH_SHORT).show();
        }else{
            this.startLiveData();
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.stopLiveData();
        final BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter != null && btAdapter.isEnabled() && !bluetoothDefaultIsEnable)
            btAdapter.disable();
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.stopLiveData();
    }

    public void getBluetoothDevices(ArrayList aDeviceStrs, final ArrayList devices) {
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
        if (pairedDevices.size() > 0)
        {
            for (BluetoothDevice device : pairedDevices)
            {
                aDeviceStrs.add(device.getName());
                devices.add(device.getAddress());
            }
        }
    };

    public void showBluetoothDevicesDialog (ArrayList aDeviceStrs, final ArrayList devices) {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.select_dialog_singlechoice,
                aDeviceStrs.toArray(new String[aDeviceStrs.size()]));

        alertDialog.setSingleChoiceItems(adapter, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
                int position = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                BluetoothDeviceAddress = (String) devices.get(position);
                connectBluetoothDeviceAddress(BluetoothDeviceAddress);
            }
        });

        alertDialog.setTitle(R.string.bluetooth_choose);
        alertDialog.show();
    }

    public void connectBluetoothDeviceAddress(String deviceAddress) {
        if ((this.SocketConn == null) || (!this.SocketConn.isConnected())) {
            BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
            BluetoothDevice device = btAdapter.getRemoteDevice(deviceAddress);
            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
            try {
                this.SocketConn = device.createInsecureRfcommSocketToServiceRecord(uuid);
                this.SocketConn.connect();
                Toast.makeText(this, getString(R.string.status_bluetooth_ok), Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                this.SocketConn = null;
                Toast.makeText(this, R.string.text_bluetooth_error_connecting, Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    private void startLiveData() {
        ArrayList deviceStrs = new ArrayList();
        final ArrayList devices = new ArrayList();

        if (this.BluetoothDeviceAddress == "") {
            this.getBluetoothDevices(deviceStrs, devices);
            this.showBluetoothDevicesDialog(deviceStrs, devices);
        } else {
            connectBluetoothDeviceAddress(this.BluetoothDeviceAddress);
        }

        if ((preRequisites) && (this.SocketConn != null) && (this.SocketConn.isConnected())) {
            LiveDataThread liveDataThread = new LiveDataThread();
            new Thread(liveDataThread).start();
        }
    }

    private void stopLiveData() {
        if ((this.liveDataThread != null) && (!this.liveDataThread.isInterrupted())) {
            this.liveDataThread.interrupt();
            this.liveDataThread = null;
        }
    }


}
