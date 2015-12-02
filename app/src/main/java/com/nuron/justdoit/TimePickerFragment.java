package com.nuron.justdoit;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;

/**
 * Created by nuron on 02/12/15.
 */
public class TimePickerFragment extends DialogFragment {
    TimePickerDialog.OnTimeSetListener onTimeSetListener;
    private int hour, minute;


    public TimePickerFragment() {

    }

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
        hour = args.getInt("hour");
        minute = args.getInt("minute");
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new TimePickerDialog(getActivity(), onTimeSetListener, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }

    public void setCallBack(TimePickerDialog.OnTimeSetListener onTime) {
        onTimeSetListener = onTime;
    }
}