package com.nuron.justdoit;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.parse.ParseObservable;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    @Bind(R.id.recycler_view)
    RecyclerView recyclerView;

    ToDoRecyclerAdapter toDoRecyclerAdapter;

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
            String userEmail = parseUser.getEmail();
            if (userName != null) {
                userNameTextView.setText(userName);
            }

            if (userEmail != null) {
                userEmailTextView.setText(userEmail);
            }
        }

        loadToDoItems();
    }

    private void loadToDoItems() {

        ParseQuery<ParseObject> query = ParseQuery.getQuery(ToDoItem.TODO_TABLE_NAME);
        ParseObservable.find(query)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ParseObject>() {
                    @Override
                    public void onCompleted() {
                        toDoRecyclerAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(ParseObject parseObject) {
                        toDoRecyclerAdapter.addData(parseObject);
                    }
                });
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
            ParseUser.logOut();
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            }, 200);
        }
        return true;
    }

    public void deleteToDoItem(ParseObject parseObject, final int position) {
        ParseObservable.delete(parseObject)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ParseObject>() {
                    @Override
                    public void onCompleted() {
                        toDoRecyclerAdapter.removeData(position);
                        toDoRecyclerAdapter.notifyItemRemoved(position);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(ParseObject parseObject) {

                    }
                });

    }

}
