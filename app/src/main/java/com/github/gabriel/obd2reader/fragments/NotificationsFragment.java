package com.github.gabriel.obd2reader.fragments;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.gabriel.obd2reader.R;

public class NotificationsFragment extends Fragment {

    private View rootView;

    public NotificationsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
   }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.rootView = inflater.inflate(R.layout.fragment_notifications, container, false);

        return this.rootView;
    }

}
