package com.github.gabriel.obd2reader.activities;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.github.gabriel.obd2reader.fragments.HomeFragment;
import com.github.gabriel.obd2reader.fragments.NotificationsFragment;
import com.github.gabriel.obd2reader.R;
import com.github.gabriel.obd2reader.fragments.PreferencesFragment;
import com.github.gabriel.obd2reader.fragments.TroubleCodesFragment;
import com.github.pires.obd.commands.protocol.EchoOffCommand;
import com.github.pires.obd.commands.protocol.LineFeedOffCommand;
import com.github.pires.obd.commands.protocol.ObdResetCommand;
import com.github.pires.obd.commands.protocol.SelectProtocolCommand;
import com.github.pires.obd.commands.protocol.TimeoutCommand;
import com.github.pires.obd.enums.ObdProtocols;
import com.github.pires.obd.exceptions.NoDataException;

import java.io.IOException;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private final FragmentManager fragmentManager = getFragmentManager();
    private Fragment fragment = null;

    //Bluetooth
    private UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public BluetoothSocket Socket = null;
    public BluetoothAdapter Adapter = null;
    public boolean preRequisites = true; //verifica se possui bluetooth e esta conectado
    public Boolean liveDataActive = false; //thread verifica se esta true para executar
    private String BluetoothDeviceAddress = ""; //bluetooth onde esta conectado
    private boolean bluetoothDefaultEnabled = false; //verifica se o bluetooth estava ativo quando iniciou o apk

    private class ConectarLeitorTask extends AsyncTask<String, Integer, Integer>{
        private MainActivity mainActivity;
        private String deviceAddress;

        public ConectarLeitorTask(MainActivity mainActivity, String deviceAddress) {
            Log.i("ConectarLeitorTask", "onCreate");
            this.mainActivity = mainActivity;
            this.deviceAddress = deviceAddress;
        }

        @Override
        protected Integer doInBackground(String... strings) {
            Log.i("ConectarLeitorTask", "Conectado...");
            BluetoothDevice device = this.mainActivity.Adapter.getRemoteDevice(deviceAddress);
            try {

                this.mainActivity.Socket = device.createInsecureRfcommSocketToServiceRecord(uuid);
                this.mainActivity.Socket.connect();

                executeObdDefaultCommands();

                new SelectProtocolCommand(ObdProtocols.AUTO).run(this.mainActivity.Socket.getInputStream(), this.mainActivity.Socket.getOutputStream());
            } catch (IOException | NoDataException | InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Integer result){
            if (this.mainActivity.deviceIsConnected()) {
                Toast.makeText(this.mainActivity, getString(R.string.status_bluetooth_ok), Toast.LENGTH_SHORT).show();
                mainActivity.liveDataActive = true;
                Log.d("ConectarLeitorTask", "Conectado.");
            }else {
                this.mainActivity.closeConnection();
                Log.d("ConectarLeitorTask", "Desconectado.");
                Toast.makeText(this.mainActivity, R.string.text_bluetooth_error_connecting, Toast.LENGTH_SHORT).show();
            }
        }
    }


    public void executeObdDefaultCommands() {
        try {
            new ObdResetCommand().run(this.Socket.getInputStream(), this.Socket.getOutputStream());

            new EchoOffCommand().run(this.Socket.getInputStream(), this.Socket.getOutputStream());

            new LineFeedOffCommand().run(this.Socket.getInputStream(), this.Socket.getOutputStream());

            new TimeoutCommand(2000).run(this.Socket.getInputStream(), this.Socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            final FragmentTransaction transaction = fragmentManager.beginTransaction();

            switch (item.getItemId()) {
                case R.id.navigation_home:
                    setTitle(R.string.title_home);

//                    if (!(fragment instanceof HomeFragment)) {
                        fragment = new HomeFragment();
                        transaction.replace(R.id.main_content, fragment).commit();
//                    }

                    break;
                case R.id.navigation_options:
                    setTitle(R.string.title_options);
//                    if (!(fragment instanceof PreferencesFragment)) {
                        fragment = new PreferencesFragment();
                        transaction.replace(R.id.main_content, fragment).commit();
//                    }
                    break;
//                case R.id.navigation_notifications:
//                    setTitle(R.string.title_notifications);
////                    if (!(fragment instanceof NotificationsFragment)) {
//                        fragment = new NotificationsFragment();
//                        transaction.replace(R.id.main_content, fragment).commit();
////                    }
//                    break;
                case R.id.navigation_troublecodes:
                    setTitle(R.string.title_trouble_codes);
//                    if (!(fragment instanceof TroubleCodesFragment)) {
                        fragment = new TroubleCodesFragment();
                        transaction.replace(R.id.main_content, fragment).commit();
//                    }
                    break;
            }
            showWelcomeScreen(false);
            return true;
        }

    };

    private void showWelcomeScreen(boolean b){
        FrameLayout subframeLayout = (FrameLayout)findViewById(R.id.main_subcontent);

        if (b)
            subframeLayout.setVisibility(View.VISIBLE);
        else
            subframeLayout.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.BluetoothDeviceAddress = "";
        this.Socket = null;
        this.Adapter = BluetoothAdapter.getDefaultAdapter();
        this.bluetoothDefaultEnabled = this.Adapter != null && this.Adapter.isEnabled();

        this.showWelcomeScreen(true);

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
             if (deviceAddress.equals(""))
                deviceAddress = this.BluetoothDeviceAddress;

            if (!deviceAddress.equals("")) {
                Toast.makeText(this, R.string.status_bluetooth_connecting, Toast.LENGTH_LONG).show();
                ConectarLeitorTask conectarLeitorTask = new ConectarLeitorTask(this, deviceAddress);
                conectarLeitorTask.execute("");
            }
        }
    }

    public void disconnectBluetoothDevice(boolean showToast) {
        this.liveDataActive = false;

        if (this.Socket != null) {
            try {
                this.Socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.Socket = null;
            if (showToast) {
                Toast.makeText(this, getString(R.string.socket_disconnected_error), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startLiveData() {
        if ((!this.liveDataActive) && (!this.BluetoothDeviceAddress.equals("")))
            connectBluetoothDeviceAddress(this.BluetoothDeviceAddress);

    }

    private void stopLiveData() {
        this.liveDataActive = false;
    }

    public void closeConnection(){
        this.liveDataActive = false;

        if (this.Socket != null) {
            try {
                this.Socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        this.Socket = null;
    }

    public boolean deviceIsConnected() {
        return (this.Socket != null);
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
