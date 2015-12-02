package com.nuron.justdoit;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.percent.PercentRelativeLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
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

    @Bind(R.id.add_tem_layout)
    PercentRelativeLayout addItemLayout;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.fab)
    FloatingActionButton floatingActionButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_to_do_item_page);
        ButterKnife.bind(this);

        int elevation = getResources().getDimensionPixelSize(R.dimen.toolbar_elevation);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            toDoLayout.setElevation(elevation);
        }

        toolbar.setTitle("Add Item");
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @OnClick(R.id.todo_item_name_date)
    public void showDateTimePicker() {

        final DatePickerFragment datePickerFragment = new DatePickerFragment();

        Calendar calender = Calendar.getInstance();
        Bundle dateArgs = new Bundle();
        dateArgs.putInt(DatePickerFragment.DATE_YEAR_ARG, calender.get(Calendar.YEAR));
        dateArgs.putInt(DatePickerFragment.DATE_MONTH_ARG, calender.get(Calendar.MONTH));
        dateArgs.putInt(DatePickerFragment.DATE_DAY_ARG, calender.get(Calendar.DAY_OF_MONTH));
        datePickerFragment.setArguments(dateArgs);


        final TimePickerFragment timePickerFragment = new TimePickerFragment();
        Bundle timeArgs = new Bundle();
        timeArgs.putInt(TimePickerFragment.TIME_HOUR_ARG, calender.get(Calendar.HOUR_OF_DAY));
        timeArgs.putInt(TimePickerFragment.TIME_MINUTE_ARG, calender.get(Calendar.MINUTE));
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

    @OnClick(R.id.todo_item_name_location)
    public void searchLocation() {


        SearchPlaceFragment searchPlaceFragment = new SearchPlaceFragment();
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_up, R.anim.slide_out_down)
                .replace(R.id.fragment_container, searchPlaceFragment, SearchPlaceFragment.TAG)
                .commit();

        addItemLayout.setVisibility(View.GONE);
        floatingActionButton.hide();

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        getFragmentManager().executePendingTransactions();
    }


    public void setLocation(String location) {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {

        if (menuItem.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onBackPressed() {

        if (!handleBackPressed()) {
            super.onBackPressed();
        }
    }

    public boolean handleBackPressed() {

        SearchPlaceFragment searchPlaceFragment =
                (SearchPlaceFragment) getSupportFragmentManager().
                        findFragmentByTag(SearchPlaceFragment.TAG);

        if (searchPlaceFragment == null) {
            return false;
        } else {

            if (getSupportActionBar() != null) {
                getSupportActionBar().show();
            }
            addItemLayout.setVisibility(View.VISIBLE);
            toolbar.setTitle("Add Item");

            getSupportFragmentManager().beginTransaction().remove(searchPlaceFragment).commit();
            getSupportFragmentManager().executePendingTransactions();

            floatingActionButton.show();
            return true;
        }
    }
}
