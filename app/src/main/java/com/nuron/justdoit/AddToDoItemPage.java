package com.nuron.justdoit;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.percent.PercentRelativeLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.DatePicker;
import android.widget.TimePicker;

import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.Calendar;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AddToDoItemPage extends AppCompatActivity {


    @Bind(R.id.todo_item_name)
    MaterialEditText todoItemNameText;

    @Bind(R.id.item_name_layout)
    PercentRelativeLayout toDoLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_to_do_item_page);
        ButterKnife.bind(this);

        int elevation = getResources().getDimensionPixelSize(R.dimen.toolbar_elevation);
        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.appbarlayout);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            appBarLayout.setElevation(0);
            toDoLayout.setElevation(elevation);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Add Item");
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

//        MaterialEditText date = (MaterialEditText) findViewById(R.id.todo_item_name_date);
//        date.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                showDateTimePicker();
//            }
//        });
    }

    @OnClick(R.id.todo_item_name_date)
    public void showDateTimePicker() {

        DatePickerFragment datePickerFragment = new DatePickerFragment();

        Calendar calender = Calendar.getInstance();
        Bundle dateArgs = new Bundle();
        dateArgs.putInt("year", calender.get(Calendar.YEAR));
        dateArgs.putInt("month", calender.get(Calendar.MONTH));
        dateArgs.putInt("day", calender.get(Calendar.DAY_OF_MONTH));
        datePickerFragment.setArguments(dateArgs);


        final TimePickerFragment timePickerFragment = new TimePickerFragment();
        Bundle timeArgs = new Bundle();
        timeArgs.putInt("hour", calender.get(Calendar.HOUR_OF_DAY));
        timeArgs.putInt("minute", calender.get(Calendar.MINUTE));
        timePickerFragment.setArguments(timeArgs);

        timePickerFragment.setCallBack(new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

            }
        });

        datePickerFragment.setCallBack(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                String month, day;

                if (monthOfYear < 10)
                    month = "0" + Integer.toString(monthOfYear + 1);
                else
                    month = Integer.toString(monthOfYear + 1);

                if (dayOfMonth < 10)
                    day = "0" + dayOfMonth;
                else
                    day = Integer.toString(dayOfMonth);

//                String selectedDate = utilities.
//                        getDateFormat(String.valueOf(year) + "-" + month + "-" + day,
//                                Utilities.FROM_DATE_PICKER_TO_EDIT_TEXT);
//                EditText date = (EditText) rootView.findViewById(R.id.add_date);
//                date.setText(selectedDate);

                timePickerFragment.show(getSupportFragmentManager(), "Time Picker");
            }
        });

        datePickerFragment.show(getSupportFragmentManager(), "Date Picker");
    }

    public void setLocation(String location) {

    }
}
