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
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.github.gabriel.obd2reader.R;
import com.github.gabriel.obd2reader.activities.MainActivity;
import com.github.gabriel.obd2reader.classes.ModifiedResetTroubleCodesCommand;
import com.github.gabriel.obd2reader.classes.ModifiedTroubleCodesObdCommand;
import com.github.pires.obd.commands.control.TroubleCodesCommand;
import com.github.pires.obd.commands.protocol.ResetTroubleCodesCommand;
import com.github.pires.obd.exceptions.MisunderstoodCommandException;
import com.github.pires.obd.exceptions.NoDataException;
import com.github.pires.obd.exceptions.NonNumericResponseException;
import com.github.pires.obd.exceptions.UnableToConnectException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

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
                    if (isAdded())
                        makeToast(getString(R.string.text_bluetooth_nodevice));
                    break;
                case CANNOT_CONNECT_TO_DEVICE:
                    if (isAdded())
                        makeToast(getString(R.string.text_bluetooth_error_connecting));
                    break;
                case OBD_COMMAND_FAILURE:
                    if (isAdded())
                        makeToast(getString(R.string.text_obd_command_failure));
                    break;
                case OBD_COMMAND_FAILURE_IO:
                    if (isAdded())
                        makeToast(getString(R.string.text_obd_command_failure) + " IO");
                    break;
                case OBD_COMMAND_FAILURE_IE:
                    if (isAdded())
                        makeToast(getString(R.string.text_obd_command_failure) + " IE");
                    break;
                case OBD_COMMAND_FAILURE_MIS:
                    if (isAdded())
                        makeToast(getString(R.string.text_obd_command_failure) + " MIS");
                    break;
                case OBD_COMMAND_FAILURE_UTC:
                    if (isAdded())
                        makeToast(getString(R.string.text_obd_command_failure) + " UTC");
                    break;
                case OBD_COMMAND_FAILURE_NODATA:
                    if (isAdded())
                        makeToastLong(getString(R.string.text_noerrors));
                    break;
                case NO_DATA:
                    if (isAdded())
                        makeToast(getString(R.string.text_dtc_no_data));
                    break;
                case DATA_OK:
                    if (isAdded())
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

                    if (((MainActivity)getActivity()).deviceIsConnected()) {
                        ModifiedTroubleCodesObdCommand tcoc = new ModifiedTroubleCodesObdCommand();
                        tcoc.run(((MainActivity) getActivity()).Socket.getInputStream(), ((MainActivity) getActivity()).Socket.getOutputStream());
                        result = tcoc.getFormattedResult();
                    }

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
            if (isAdded()) {
                mHandler.obtainMessage(DATA_OK, result).sendToTarget();
            }
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
    public void onStop() {
        super.onStop();
        if (this.gtct != null && this.gtct.getStatus() == AsyncTask.Status.RUNNING)
            this.gtct.cancel(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.rootView = inflater.inflate(R.layout.fragment_trouble_codes, container, false);


        Button btnClearDTC = this.rootView.findViewById(R.id.cleardtc_button);
        btnClearDTC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ModifiedResetTroubleCodesCommand rtcc = new ModifiedResetTroubleCodesCommand();
                try {
                    rtcc.run(((MainActivity) getActivity()).Socket.getInputStream(), ((MainActivity) getActivity()).Socket.getOutputStream());
                    Toast.makeText(getActivity(), getString(R.string.clear_dtc_sucess), Toast.LENGTH_SHORT).show();
                    dataOk(null);
                } catch (IOException e) {
                    Toast.makeText(getActivity(), getString(R.string.text_obd_command_failure), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    Toast.makeText(getActivity(), getString(R.string.text_obd_command_failure), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                } catch (NonNumericResponseException e) {
                    if (e.getMessage().toLowerCase().contains("ok")) {
                        dataOk(null);
                        Toast.makeText(getActivity(), getString(R.string.clear_dtc_sucess), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                gtct = new GetTroubleCodesTask();
                gtct.execute("");
            }
        }, 1000);
        return this.rootView;
    }

    public void makeToast(String text) {
        if (isAdded()) {
            Toast toast = Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT);
            toast.show();
        }
    }
    public void makeToastLong(String text) {
        if (isAdded()) {
            Toast toast = Toast.makeText(getActivity(), text, Toast.LENGTH_LONG);
            toast.show();
        }
    }

    private void dataOk(String res) {
        ListView lv = (ListView) this.rootView.findViewById(R.id.listView);
        Map<String, String> dtcVals = getDict(R.array.dtc_keys, R.array.dtc_values);

        ArrayList<String> dtcCodes = new ArrayList<>();
        //int i =1;
        if ((res != null) && (!res.equals(""))) {
            for (String dtcCode : res.split("\n")) {
                if (!dtcCode.equals("")) {
                    dtcCodes.add(dtcCode + " : " + dtcVals.get(dtcCode));
                    Log.d("TEST", dtcCode + " : " + dtcVals.get(dtcCode));
                }
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

}
