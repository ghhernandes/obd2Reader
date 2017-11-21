package com.github.gabriel.obd2reader.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.gabriel.obd2reader.R;
import com.github.gabriel.obd2reader.classes.SensorClass;

import java.util.List;

/**
 * Created by gabriel on 15/10/17.
 */




public class MainViewHolder extends RecyclerView.ViewHolder {

    final TextView name;
    final TextView value;



    public MainViewHolder(View view) {
        super(view);
        name = (TextView) view.findViewById(R.id.main_card_name);
        value = (TextView) view.findViewById(R.id.main_card_value);

    }
    

}
