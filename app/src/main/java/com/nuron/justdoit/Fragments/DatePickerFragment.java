package com.nuron.justdoit.Fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

/**
 * Created by nuron on 01/12/15.
 */
public class DatePickerFragment extends DialogFragment {


    public static final String DATE_YEAR_ARG = "year";
    public static final String DATE_MONTH_ARG = "month";
    public static final String DATE_DAY_ARG = "day";

    DatePickerDialog.OnDateSetListener ondateSet;
    private int year, month, day;

    public DatePickerFragment() {
    }

    public void setCallBack(DatePickerDialog.OnDateSetListener ondate) {
        ondateSet = ondate;
    }

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
        year = args.getInt(DATE_YEAR_ARG);
        month = args.getInt(DATE_MONTH_ARG);
        day = args.getInt(DATE_DAY_ARG);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new DatePickerDialog(getActivity(), ondateSet, year, month, day);
    }
}
