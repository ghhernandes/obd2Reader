package com.github.gabriel.obd2reader.activities;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.SharedPreferences;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.widget.Toast;

import com.github.gabriel.obd2reader.R;
import com.github.gabriel.obd2reader.config.ObdConfig;
import com.github.pires.obd.commands.ObdCommand;
import com.github.pires.obd.enums.ObdProtocols;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Set;

public class PreferencesActivity extends PreferenceActivity implements Preference.OnPreferenceChangeListener{

    public static final String BLUETOOTH_LIST_KEY = "bluetooth_list_preference";
    public static final String OBD_UPDATE_PERIOD_KEY = "obd_update_period_preference";
    public static final String VEHICLE_ID_KEY = "vehicle_id_preference";
    public static final String ENGINE_DISPLACEMENT_KEY = "engine_displacement_preference";
    public static final String VOLUMETRIC_EFFICIENCY_KEY = "volumetric_efficiency_preference";
    public static final String IMPERIAL_UNITS_KEY = "imperial_units_preference";
    public static final String COMMANDS_SCREEN_KEY = "obd_commands_screen";
    public static final String PROTOCOLS_LIST_KEY = "obd_protocols_preference";
    public static final String ENABLE_BT_KEY = "enable_bluetooth_preference";
    public static final String MAX_FUEL_ECON_KEY = "max_fuel_econ_preference";
    public static final String CONFIG_READER_KEY = "reader_config_preference";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //carrega o xml de preferencias
        addPreferencesFromResource(R.xml.preferences);

        ArrayList<CharSequence> pairedDeviceStrings = new ArrayList<>();
        ArrayList<CharSequence> vals = new ArrayList<>();
        ArrayList<CharSequence> protocolStrings = new ArrayList<>();

        ListPreference listBtDevices = (ListPreference) getPreferenceScreen()
                .findPreference(BLUETOOTH_LIST_KEY);

        ListPreference listProtocols = (ListPreference) getPreferenceScreen()
                .findPreference(PROTOCOLS_LIST_KEY);

        String[] prefKeys = new String[]{ENGINE_DISPLACEMENT_KEY,
                                         VOLUMETRIC_EFFICIENCY_KEY,
                                         OBD_UPDATE_PERIOD_KEY,
                                         MAX_FUEL_ECON_KEY};

        for (String prefKey: prefKeys){
            EditTextPreference txtPref = (EditTextPreference) getPreferenceScreen()
                    .findPreference(prefKey);
            txtPref.setOnPreferenceChangeListener(this);
        }


    /*
     * Comandos OBD disponíveis
     *
     */
        ArrayList<ObdCommand> cmds = ObdConfig.getCommands();
        PreferenceScreen cmdScr = (PreferenceScreen) getPreferenceScreen()
                .findPreference(COMMANDS_SCREEN_KEY);

        for (ObdCommand cmd : cmds) {
            CheckBoxPreference cpref = new CheckBoxPreference(this);
            cpref.setTitle(cmd.getName());
            cpref.setKey(cmd.getName());
            cpref.setChecked(true);
            cmdScr.addPreference(cpref);
        }
    /*
     * Protocolos OBD disponíveis
     *
     */

        for (ObdProtocols protocol : ObdProtocols.values()) {
            protocolStrings.add(protocol.name());
        }
        listProtocols.setEntries(protocolStrings.toArray(new CharSequence[0]));
        listProtocols.setEntryValues(protocolStrings.toArray(new CharSequence[0]));



        final BluetoothAdapter mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter == null) {
            listBtDevices
                    .setEntries(pairedDeviceStrings.toArray(new CharSequence[0]));
            listBtDevices.setEntryValues(vals.toArray(new CharSequence[0]));

            Toast.makeText(this, "Este dispositivo não suporta Bluetooth.",
                    Toast.LENGTH_LONG).show();

            return;
        }

        final Activity thisActivity = this;
        listBtDevices.setEntries(new CharSequence[1]);
        listBtDevices.setEntryValues(new CharSequence[1]);
        listBtDevices.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                if (mBtAdapter == null || !mBtAdapter.isEnabled()) {
                    Toast.makeText(thisActivity,
                            "Este dispositivo não suporta Bluetooth ou está desabilitado.",
                            Toast.LENGTH_LONG).show();
                    return false;
                }
                return true;
            }
        });

    /*
     * Get paired devices and populate preference list.
     */
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                pairedDeviceStrings.add(device.getName() + "\n" + device.getAddress());
                vals.add(device.getAddress());
            }
        }
        listBtDevices.setEntries(pairedDeviceStrings.toArray(new CharSequence[0]));
        listBtDevices.setEntryValues(vals.toArray(new CharSequence[0]));
    }

    public static int getObdUpdatePeriod(SharedPreferences prefs) {
        String periodString = prefs.
                getString(PreferencesActivity.OBD_UPDATE_PERIOD_KEY, "4"); // 4 as in seconds
        int period = 4000; // by default 4000ms

        try {
            period = (int) (Double.parseDouble(periodString) * 1000);
        } catch (Exception e) {
        }

        if (period <= 0) {
            period = 4000;
        }

        return period;
    }

    public static double getVolumetricEfficieny(SharedPreferences prefs) {
        String veString = prefs.getString(PreferencesActivity.VOLUMETRIC_EFFICIENCY_KEY, ".85");
        double ve = 0.85;
        try {
            ve = Double.parseDouble(veString);
        } catch (Exception e) {
        }
        return ve;
    }

    public static double getEngineDisplacement(SharedPreferences prefs) {
        String edString = prefs.getString(PreferencesActivity.ENGINE_DISPLACEMENT_KEY, "1.6");
        double ed = 1.6;
        try {
            ed = Double.parseDouble(edString);
        } catch (Exception e) {
        }
        return ed;
    }

    public static ArrayList<ObdCommand> getObdCommands(SharedPreferences prefs) {
        ArrayList<ObdCommand> cmds = ObdConfig.getCommands();
        ArrayList<ObdCommand> ucmds = new ArrayList<>();
        for (int i = 0; i < cmds.size(); i++) {
            ObdCommand cmd = cmds.get(i);
            boolean selected = prefs.getBoolean(cmd.getName(), true);
            if (selected)
                ucmds.add(cmd);
        }
        return ucmds;
    }

    public static double getMaxFuelEconomy(SharedPreferences prefs) {
        String maxStr = prefs.getString(PreferencesActivity.MAX_FUEL_ECON_KEY, "70");
        double max = 70;
        try {
            max = Double.parseDouble(maxStr);
        } catch (Exception e) {
        }
        return max;
    }

    public static String[] getReaderConfigCommands(SharedPreferences prefs) {
        String cmdsStr = prefs.getString(CONFIG_READER_KEY, "atsp0\natz");
        String[] cmds = cmdsStr.split("\n");
        return cmds;
    }


    @Override
    public boolean onPreferenceChange(Preference preference, Object o) {
        if (OBD_UPDATE_PERIOD_KEY.equals(preference.getKey())
                || VOLUMETRIC_EFFICIENCY_KEY.equals(preference.getKey())
                || ENGINE_DISPLACEMENT_KEY.equals(preference.getKey())
                || MAX_FUEL_ECON_KEY.equals(preference.getKey())) {
            try {
                Double.parseDouble(o.toString().replace(",", "."));
                return true;
            } catch (Exception e) {
                Toast.makeText(this,
                        "Couldn't parse '" + o.toString() + "' as a number.", Toast.LENGTH_LONG).show();
            }
        }
        return false;
    }
}
