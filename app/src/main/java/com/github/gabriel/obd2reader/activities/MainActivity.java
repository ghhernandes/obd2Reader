package com.github.gabriel.obd2reader.activities;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.github.gabriel.obd2reader.config.ObdConfig;
import com.github.gabriel.obd2reader.fragments.HomeFragment;
import com.github.gabriel.obd2reader.fragments.NotificationsFragment;
import com.github.gabriel.obd2reader.R;
import com.github.gabriel.obd2reader.fragments.PreferencesFragment;
import com.github.pires.obd.commands.ObdCommand;
import com.github.pires.obd.commands.protocol.EchoOffCommand;
import com.github.pires.obd.commands.protocol.LineFeedOffCommand;
import com.github.pires.obd.commands.protocol.SelectProtocolCommand;
import com.github.pires.obd.commands.protocol.TimeoutCommand;
import com.github.pires.obd.enums.ObdProtocols;


import java.io.IOException;
import java.util.ArrayList;

import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    //Bluetooth
    public BluetoothSocket Socket = null;
    public BluetoothAdapter Adapter = null;
    public boolean preRequisites = true; //verifica se possui bluetooth e esta conectado
    public Boolean liveDataActive = false; //thread verifica se esta true para executar
    private String BluetoothDeviceAddress = ""; //bluetooth onde esta conectado
    private boolean bluetoothDefaultEnabled = false; //verifica se o bluetooth estava ativo quando iniciou o apk

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();

            FrameLayout frameLayout = (FrameLayout) findViewById(R.id.main_content);
//            frameLayout.setBackgroundColor(getResources().getColor(R.color.colorBackground));

            switch (item.getItemId()) {
                case R.id.navigation_home:
                    setTitle(R.string.title_home);
                    transaction.replace(R.id.main_content, new HomeFragment()).commit();
//                    frameLayout.setBackgroundColor(getResources().getColor(R.color.colorCardBackground));
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
            FrameLayout subframeLayout = (FrameLayout)findViewById(R.id.main_subcontent);
            subframeLayout.setVisibility(View.INVISIBLE);
            return true;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.BluetoothDeviceAddress = "";
        this.Socket = null;
        this.Adapter = BluetoothAdapter.getDefaultAdapter();
        this.bluetoothDefaultEnabled = this.Adapter != null && this.Adapter.isEnabled();

        final BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        Button button_connect = (Button) findViewById(R.id.main_connect_button);

        button_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navigation.findViewById(R.id.navigation_options).performClick();

                FrameLayout frameLayout = (FrameLayout)findViewById(R.id.main_subcontent);
                frameLayout.setVisibility(View.INVISIBLE);
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();

        preRequisites = this.Adapter != null && this.Adapter.isEnabled();

        if (!preRequisites) {
            Toast.makeText(this, getString(R.string.text_bluetooth_disabled), Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.stopLiveData();

        if (this.Adapter != null && this.Adapter.isEnabled() && !this.bluetoothDefaultEnabled)
            this.Adapter.disable();
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.stopLiveData();
    }

    public void connectBluetoothDeviceAddress(String deviceAddress) {
        if ((this.Socket == null) || (!this.Socket.isConnected())) {
            BluetoothDevice device = this.Adapter.getRemoteDevice(deviceAddress);
            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
            try {
                this.Socket = device.createInsecureRfcommSocketToServiceRecord(uuid);
                this.Socket.connect();

                new EchoOffCommand().run(this.Socket.getInputStream(), this.Socket.getOutputStream());

                new LineFeedOffCommand().run(this.Socket.getInputStream(), this.Socket.getOutputStream());

                new TimeoutCommand(2000).run(this.Socket.getInputStream(), this.Socket.getOutputStream());

                new SelectProtocolCommand(ObdProtocols.AUTO).run(this.Socket.getInputStream(), this.Socket.getOutputStream());

               Toast.makeText(this, getString(R.string.status_bluetooth_ok), Toast.LENGTH_SHORT).show();
               this.liveDataActive = true;
            } catch (IOException e) {
                this.liveDataActive = false;
                this.Socket = null;
                Toast.makeText(this, R.string.text_bluetooth_error_connecting, Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void startLiveData() {
        if ((!this.liveDataActive) && (!this.BluetoothDeviceAddress.equals("")))
            connectBluetoothDeviceAddress(this.BluetoothDeviceAddress);

    }

    private void stopLiveData() {
        this.liveDataActive = false;
    }

    public void closeConnection() throws IOException{
        this.liveDataActive = false;

        if (this.Socket != null)
            this.Socket.close();

        this.Socket = null;
    }

    public boolean deviceIsConnected() {
        return (this.Socket != null) && (this.Socket.isConnected());
    }

    public void setSelectedBluetoothDevice(String device) throws IOException {
        this.BluetoothDeviceAddress = device;
        if (this.deviceIsConnected()){
            this.closeConnection();
            this.startLiveData();
        }else if (this.Socket != null){
            this.liveDataActive = false;
            this.Socket = null;
            this.startLiveData();
        }else{
            this.startLiveData();
        }
    }

}
