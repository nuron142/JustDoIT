package com.nuron.justdoit;

import android.app.DatePickerDialog;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;

import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.Calendar;

import butterknife.Bind;

public class AddToDoItemPage extends AppCompatActivity {


    @Bind(R.id.todo_item_name)
    MaterialEditText todoItemNameText;

    //@OnClick(R.id.todo_item_name_date)
    public void showDatePicker() {

        Log.d("1", "Date clicked");

        DatePickerFragment date = new DatePickerFragment();

        Calendar calender = Calendar.getInstance();
        Bundle args = new Bundle();
        args.putInt("year", calender.get(Calendar.YEAR));
        args.putInt("month", calender.get(Calendar.MONTH));
        args.putInt("day", calender.get(Calendar.DAY_OF_MONTH));
        date.setArguments(args);
        date.setCallBack(new DatePickerDialog.OnDateSetListener() {
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
            }
        });

        date.show(this.getSupportFragmentManager(), "Date Picker");
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_to_do_item_page);

        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.appbarlayout);
        appBarLayout.setTargetElevation(0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            appBarLayout.setElevation(0);
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Add Item");
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }


        MaterialEditText date = (MaterialEditText) findViewById(R.id.todo_item_name_date);
        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Log.d("1", "Date clicked 1");

                showDatePicker();
            }
        });
    }
}
