package com.nuron.justdoit;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.location.Address;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.percent.PercentRelativeLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.location.LocationRequest;
import com.parse.ParseACL;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.pnikosis.materialishprogress.ProgressWheel;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pl.charmas.android.reactivelocation.ReactiveLocationProvider;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.parse.ParseObservable;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class AddToDoItemActivity extends AppCompatActivity {


    @Bind(R.id.todo_item_name)
    MaterialEditText todoItemNameText;

    @Bind(R.id.todo_item_name_location)
    MaterialEditText todoItemLocationText;

    @Bind(R.id.todo_item_name_date)
    MaterialEditText todoDateText;

    @Bind(R.id.item_name_layout)
    PercentRelativeLayout toDoLayout;

    @Bind(R.id.add_tem_layout)
    PercentRelativeLayout addItemLayout;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.fab)
    FloatingActionButton floatingActionButton;

    @Bind(R.id.progress_wheel)
    ProgressWheel progressWheel;

    String currentLocation;
    Subscription locationSub, addressSub;

    CompositeSubscription allSubscriptions;
    private final int LOCATION_TIMEOUT_IN_SECONDS = 10;

    private final static String TAG = AddToDoItemActivity.class.getSimpleName();

    String dateString, timeString;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_to_do_item_page);
        ButterKnife.bind(this);

        context = this;
        progressWheel.stopSpinning();
        getRxLocation();

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
        final Bundle dateArgs = new Bundle();
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

                if (hourOfDay < 12) {
                    timeString = "0" + Integer.toString(hourOfDay) + ":" + minute + " AM";
                } else if (hourOfDay == 12) {
                    timeString = Integer.toString(hourOfDay) + ":" + minute + " PM";
                } else {
                    timeString = Integer.toString(hourOfDay - 12) + ":" + minute + " PM";
                }

                todoDateText.setText(timeString + " , " + dateString);
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

                dateString = day + "-" + month + "-" + Integer.toString(year);

                timePickerFragment.show(getSupportFragmentManager(), "Time Picker");
            }
        });

        datePickerFragment.show(getSupportFragmentManager(), "Date Picker");
    }

    @OnClick(R.id.todo_search_location)
    public void searchLocation() {


        SearchPlaceFragment searchPlaceFragment = new SearchPlaceFragment();
        Bundle transactionBundle = new Bundle();
        transactionBundle.putString(SearchPlaceFragment.CURRENT_LOCATION_ARG, currentLocation);
        searchPlaceFragment.setArguments(transactionBundle);

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


    @OnClick(R.id.fab)
    public void saveItemToDo() {

        if (todoItemNameText.getText().length() < 1) {
            Toast.makeText(this, "To Do can't be blank", Toast.LENGTH_SHORT).show();
            return;
        }

        if (todoDateText.getText().length() < 1) {
            Toast.makeText(this, "Date can't be blank", Toast.LENGTH_SHORT).show();
            return;
        }

        progressWheel.spin();

        ParseObject privateNote = new ParseObject(ToDoItem.TODO_TABLE_NAME);
        privateNote.put(ToDoItem.TODO_TABLE_NAME, todoItemNameText.getText().toString());
        privateNote.put(ToDoItem.TODO_ITEM_DATE, todoDateText.getText().toString());
        privateNote.put(ToDoItem.TODO_ITEM_DUE_DATE, todoDateText.getText().toString());
        privateNote.put(ToDoItem.TODO_ITEM_LOCATION, todoItemLocationText.getText().toString());
        privateNote.setACL(new ParseACL(ParseUser.getCurrentUser()));

        ParseObservable.save(privateNote)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ParseObject>() {
                    @Override
                    public void onCompleted() {
                        progressWheel.stopSpinning();
                        Toast.makeText(getApplicationContext(),
                                "Successfully saved", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(context,
                                "Couldn't save. Please try again", Toast.LENGTH_SHORT).show();
                        progressWheel.stopSpinning();
                    }

                    @Override
                    public void onNext(ParseObject parseObject) {

                    }
                });

    }

    public void setLocation(String location) {
        currentLocation = location;
        todoItemLocationText.setText(location);
        onBackPressed();
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

    //region Rx Calls for Location
    public boolean isNetConnected() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    private void getRxLocation() {
        Log.d(TAG, "Getting Location");
        ReactiveLocationProvider locationProvider = new ReactiveLocationProvider(this);

        LocationRequest req = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setExpirationDuration(TimeUnit.SECONDS.toMillis(LOCATION_TIMEOUT_IN_SECONDS))
                .setInterval(5);


        locationSub = locationProvider.getUpdatedLocation(req)
                .timeout(LOCATION_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)
                .first()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Location>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                        Toast.makeText(AddToDoItemActivity.this, "Couldn't get location",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNext(Location location) {

                        DecimalFormat formatter = new DecimalFormat("##.##", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
                        formatter.setRoundingMode(RoundingMode.HALF_UP);

                        Log.d(TAG, "Lat = " +
                                formatter.format(location.getLatitude()) + " Long = "
                                + formatter.format(location.getLongitude()));

                        if (isNetConnected())
                            getRxAddress(location);
                        else {
                            Toast.makeText(AddToDoItemActivity.this, "Couldn't lookup address",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void getRxAddress(Location location) {

        ReactiveLocationProvider locationProvider = new ReactiveLocationProvider(this);
        Observable<List<Address>> reverseGeocodeObservable = locationProvider
                .getReverseGeocodeObservable(location.getLatitude(), location.getLongitude(), 1);

        addressSub = reverseGeocodeObservable
                .timeout(LOCATION_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<Address>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(AddToDoItemActivity.this, "Couldn't lookup address",
                                Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onNext(List<Address> addresses) {

                        Address addressItem = addresses.get(0);

                        String address = addressItem.getAddressLine(1);

                        currentLocation = address;
                        todoItemLocationText.setText(address);

                    }
                });
    }
    //endregion
}
