package com.github.gabriel.obd2reader.fragments;


import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.github.gabriel.obd2reader.R;
import com.github.gabriel.obd2reader.activities.MainActivity;
import com.github.pires.obd.commands.control.TroubleCodesCommand;
import com.github.pires.obd.commands.protocol.ResetTroubleCodesCommand;
import com.github.pires.obd.exceptions.MisunderstoodCommandException;
import com.github.pires.obd.exceptions.NoDataException;
import com.github.pires.obd.exceptions.UnableToConnectException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TroubleCodesFragment extends Fragment {
    private static final String TAG = TroubleCodesFragment.class.getName();
    private static final int NO_BLUETOOTH_DEVICE_SELECTED = 0;
    private static final int CANNOT_CONNECT_TO_DEVICE = 1;
    private static final int NO_DATA = 3;
    private static final int DATA_OK = 4;
    private static final int CLEAR_DTC = 5;
    private static final int OBD_COMMAND_FAILURE = 10;
    private static final int OBD_COMMAND_FAILURE_IO = 11;
    private static final int OBD_COMMAND_FAILURE_UTC = 12;
    private static final int OBD_COMMAND_FAILURE_IE = 13;
    private static final int OBD_COMMAND_FAILURE_MIS = 14;
    private static final int OBD_COMMAND_FAILURE_NODATA = 15;

    private GetTroubleCodesTask gtct;
    private View rootView;

    private Handler mHandler = new Handler(new Handler.Callback() {
        public boolean handleMessage(Message msg) {
            Log.d(TAG, "Message received on handler");
            switch (msg.what) {
                case NO_BLUETOOTH_DEVICE_SELECTED:
                    makeToast(getString(R.string.text_bluetooth_nodevice));
                    break;
                case CANNOT_CONNECT_TO_DEVICE:
                    makeToast(getString(R.string.text_bluetooth_error_connecting));
                    break;
                case OBD_COMMAND_FAILURE:
                    makeToast(getString(R.string.text_obd_command_failure));
                    break;
                case OBD_COMMAND_FAILURE_IO:
                    makeToast(getString(R.string.text_obd_command_failure) + " IO");
                    break;
                case OBD_COMMAND_FAILURE_IE:
                    makeToast(getString(R.string.text_obd_command_failure) + " IE");
                    break;
                case OBD_COMMAND_FAILURE_MIS:
                    makeToast(getString(R.string.text_obd_command_failure) + " MIS");
                    break;
                case OBD_COMMAND_FAILURE_UTC:
                    makeToast(getString(R.string.text_obd_command_failure) + " UTC");
                    break;
                case OBD_COMMAND_FAILURE_NODATA:
                    makeToastLong(getString(R.string.text_noerrors));
                    break;
                case NO_DATA:
                    makeToast(getString(R.string.text_dtc_no_data));
                    break;
                case DATA_OK:
                    dataOk((String) msg.obj);
                    break;

            }
            return false;
        }
    });

    private class GetTroubleCodesTask extends AsyncTask<String, Integer, String> {

        @Override
        protected void onPreExecute() {
            Log.d(TAG, "onPreExecute");
        }

        @Override
        protected String doInBackground(String... params) {
            String result = "";
            synchronized (this) {
                Log.d(TAG, "doInBackground");
                try {
                    ((MainActivity) getActivity()).connectBluetoothDeviceAddress("");
                } catch (Exception e){
                    Log.e(TAG, "Erro ao estabeler conexão. -> "+e.getMessage());
                    mHandler.obtainMessage(CANNOT_CONNECT_TO_DEVICE).sendToTarget();
                }

                try {

                    ModifiedTroubleCodesObdCommand tcoc = new ModifiedTroubleCodesObdCommand();
                    tcoc.run(((MainActivity)getActivity()).Socket.getInputStream(), ((MainActivity)getActivity()).Socket.getOutputStream());
                    result = tcoc.getFormattedResult();

                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("DTCERR", e.getMessage());
                    mHandler.obtainMessage(OBD_COMMAND_FAILURE_IO).sendToTarget();

                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Log.e("DTCERR", e.getMessage());
                    mHandler.obtainMessage(OBD_COMMAND_FAILURE_IE).sendToTarget();

                } catch (UnableToConnectException e) {
                    e.printStackTrace();
                    Log.e("DTCERR", e.getMessage());
                    mHandler.obtainMessage(OBD_COMMAND_FAILURE_UTC).sendToTarget();

                } catch (MisunderstoodCommandException e) {
                    e.printStackTrace();
                    Log.e("DTCERR", e.getMessage());
                    mHandler.obtainMessage(OBD_COMMAND_FAILURE_MIS).sendToTarget();

                } catch (NoDataException e) {
                    Log.e("DTCERR", e.getMessage());
                    mHandler.obtainMessage(OBD_COMMAND_FAILURE_NODATA).sendToTarget();

                } catch (Exception e) {
                    Log.e("DTCERR", e.getMessage());
                    mHandler.obtainMessage(OBD_COMMAND_FAILURE).sendToTarget();

                }

            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            mHandler.obtainMessage(DATA_OK, result).sendToTarget();
            //getActivity().setContentView(R.layout.fragment_trouble_codes);
        }
    }


    public TroubleCodesFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.rootView = inflater.inflate(R.layout.fragment_trouble_codes, container, false);


        this.gtct = new GetTroubleCodesTask();
        this.gtct.execute("");
        return this.rootView;
    }

    public void makeToast(String text) {
        Toast toast = Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT);
        toast.show();
    }
    public void makeToastLong(String text) {
        Toast toast = Toast.makeText(getActivity(), text, Toast.LENGTH_LONG);
        toast.show();
    }

    private void dataOk(String res) {
        ListView lv = (ListView) this.rootView.findViewById(R.id.listView);
        Map<String, String> dtcVals = getDict(R.array.dtc_keys, R.array.dtc_values);

        ArrayList<String> dtcCodes = new ArrayList<>();
        //int i =1;
        if (res != null) {
            for (String dtcCode : res.split("\n")) {
                dtcCodes.add(dtcCode + " : " + dtcVals.get(dtcCode));
                Log.d("TEST", dtcCode + " : " + dtcVals.get(dtcCode));
            }
        } else {
            dtcCodes.add(getString(R.string.text_noerrors));
        }
        ArrayAdapter<String> myarrayAdapter =
                new ArrayAdapter<>(this.rootView.getContext(), android.R.layout.simple_list_item_1,
                        dtcCodes);
        lv.setAdapter(myarrayAdapter);
        lv.setTextFilterEnabled(true);
    }

    Map<String, String> getDict(int keyId, int valId) {
        String[] keys = getResources().getStringArray(keyId);
        String[] vals = getResources().getStringArray(valId);

        Map<String, String> dict = new HashMap<String, String>();
        for (int i = 0, l = keys.length; i < l; i++) {
            dict.put(keys[i], vals[i]);
        }

        return dict;
    }

    public class ModifiedTroubleCodesObdCommand extends TroubleCodesCommand {
        @Override
        public String getResult() {
            return rawData.replace("SEARCHING...", "").replace("NODATA", "");
        }
    }

    public class ClearDTC extends ResetTroubleCodesCommand {
        @Override
        public String getResult() {
            return rawData;
        }
    }
}
