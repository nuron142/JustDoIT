package com.nuron.justdoit.Activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.nuron.justdoit.Adapters.ToDoRecyclerAdapter;
import com.nuron.justdoit.Model.ToDoItem;
import com.nuron.justdoit.Notification.NotificationPublisher;
import com.nuron.justdoit.R;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.pnikosis.materialishprogress.ProgressWheel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.parse.ParseObservable;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private final static String TAG = HomeActivity.class.getSimpleName();

    @Bind(R.id.recycler_view)
    RecyclerView recyclerView;

    @Bind(R.id.empty_items_layout)
    TextView emptyItemsLayout;

    @Bind(R.id.progress_wheel)
    ProgressWheel progressWheel;

    ToDoRecyclerAdapter toDoRecyclerAdapter;
    CompositeSubscription allSubscriptions;
    Observable loadToDoItemsObservable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        toDoRecyclerAdapter = new ToDoRecyclerAdapter(this);
        recyclerView.setAdapter(toDoRecyclerAdapter);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle =
                new ActionBarDrawerToggle(this, drawer, toolbar,
                        R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View header = navigationView.getHeaderView(0);
        TextView userNameTextView = (TextView) header.findViewById(R.id.userName);
        TextView userEmailTextView = (TextView) header.findViewById(R.id.userEmail);

        ParseUser parseUser = ParseUser.getCurrentUser();
        if (parseUser != null) {
            String userName = parseUser.getString(LoginActivity.USER_ACCOUNT_NAME);
            String userEmail = parseUser.getString(LoginActivity.USER_PERSONAL_EMAIL);
            if (userName != null) {
                userNameTextView.setText(userName);
            }

            if (userEmail != null) {
                userEmailTextView.setText(userEmail);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        allSubscriptions = new CompositeSubscription();
        loadToDoItems();
    }

    @Override
    public void onStop() {
        super.onStop();

        if (allSubscriptions != null && allSubscriptions.isUnsubscribed()) {
            allSubscriptions.unsubscribe();
            allSubscriptions = null;
        }
    }


    private void loadToDoItems() {

        emptyItemsLayout.setVisibility(View.GONE);
        progressWheel.spin();
        toDoRecyclerAdapter.clear();
        ParseQuery<ParseObject> query = ParseQuery.getQuery(ToDoItem.TODO_TABLE_NAME);
        allSubscriptions.add(ParseObservable.find(query)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ParseObject>() {
                    @Override
                    public void onCompleted() {

                        if (toDoRecyclerAdapter.getItemCount() == 0) {
                            emptyItemsLayout.setVisibility(View.VISIBLE);
                        }
                        progressWheel.stopSpinning();
                        toDoRecyclerAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(ParseObject parseObject) {
                        toDoRecyclerAdapter.addData(parseObject);
                        scheduleNotification(parseObject);
                    }
                })
        );
    }

    @OnClick(R.id.fab)
    public void addTodoItem() {
        Intent intent = new Intent(this, AddToDoItemActivity.class);
        startActivity(intent);
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.logout) {

            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);

            logOutUser();
        }
        return true;
    }

    public void logOutUser() {

        allSubscriptions.add(ParseObservable.logOut()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Void>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "Logged out successfully");
                        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "Logout failed : " + e);
                        Toast.makeText(HomeActivity.this, "Logout failed : " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNext(Void aVoid) {

                    }
                })
        );
    }

    public void deleteToDoItem(ParseObject parseObject, final int position) {

        final PendingIntent pendingIntent = getAlarmPendingIntent(parseObject);

        Log.d(TAG, "Delete Notification ID : " + parseObject.getObjectId().hashCode());

        progressWheel.spin();
        allSubscriptions.add(ParseObservable.delete(parseObject)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ParseObject>() {
                    @Override
                    public void onCompleted() {
                        toDoRecyclerAdapter.removeData(position);
                        progressWheel.stopSpinning();
                        toDoRecyclerAdapter.notifyItemRemoved(position);
                        toDoRecyclerAdapter.notifyItemRangeChanged(position,
                                toDoRecyclerAdapter.getItemCount());

                        if (toDoRecyclerAdapter.getItemCount() == 0) {
                            emptyItemsLayout.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(ParseObject parseObject) {

                        AlarmManager alarmManager =
                                (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                        alarmManager.cancel(pendingIntent);

                    }
                })
        );
    }


    private void scheduleNotification(ParseObject parseObject) {

        Log.d(TAG, "Schedule Notification ID : " +
                parseObject.getObjectId().hashCode());

        PendingIntent pendingIntent = getAlarmPendingIntent(parseObject);

        SimpleDateFormat database = new SimpleDateFormat("hh:mm a, dd-MM-yyyy");
        Date date;
        try {
            date = database.parse(parseObject.getString(ToDoItem.TODO_ITEM_DUE_DATE));

            Calendar setAlarmDate = Calendar.getInstance();
            setAlarmDate.setTime(date);

            Calendar currentDate = Calendar.getInstance();

            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

            long delay = setAlarmDate.getTimeInMillis() - currentDate.getTimeInMillis();

            if (delay > 0) {
                alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        SystemClock.elapsedRealtime() + delay, pendingIntent);
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private PendingIntent getAlarmPendingIntent(ParseObject parseObject) {
        Intent notificationIntent = new Intent(this, NotificationPublisher.class);

        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID,
                parseObject.getObjectId().hashCode());

        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_CONTENT_TITLE,
                parseObject.getString(ToDoItem.TODO_ITEM_NAME));

        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_CONTENT_TEXT,
                parseObject.getString(ToDoItem.TODO_ITEM_LOCATION));

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, notificationIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        return pendingIntent;
    }
}
