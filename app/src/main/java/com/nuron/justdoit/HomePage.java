package com.nuron.justdoit;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.google.android.gms.location.LocationRequest;
import com.parse.FindCallback;
import com.parse.LogInCallback;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SignUpCallback;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.json.JSONException;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pl.charmas.android.reactivelocation.ReactiveLocationProvider;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.parse.ParseFacebookObservable;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class HomePage extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private final int LOCATION_TIMEOUT_IN_SECONDS = 10;
    ParseUser parseUser;
    String fbName = null, fbMail = null;
    Subscription locationSub, addressSub,
            parseLoginFacebookSubscription, fullFbDetailsSubscription;

    CompositeSubscription allSubscriptions;

    private final static String TAG = HomePage.class.getSimpleName();

    @Bind(R.id.login_email_text)
    MaterialEditText loginEmail;

    @Bind(R.id.login_pass_text)
    MaterialEditText loginPass;

    @OnClick(R.id.parse_login)
    public void loginWithParseClick() {

        if (loginPass.getText() == null || loginPass.getText().toString().isEmpty()) {
            Toast.makeText(HomePage.this, "Password can't be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if (loginEmail.getText() == null || loginEmail.getText().toString().isEmpty()) {
            Toast.makeText(HomePage.this, "Password can't be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Initiating login process");
        ParseUser.logInInBackground(loginEmail.getText().toString(),
                loginPass.getText().toString(), new LogInCallback() {
                    public void done(ParseUser user, ParseException e) {
                        if (user != null) {
                            Log.d(TAG, "Login successful");
                            Toast.makeText(HomePage.this, "Welcome back " +
                                            ParseUser.getCurrentUser().getUsername(),
                                    Toast.LENGTH_SHORT).show();
                            //saveNewData();
                            retrieveData();
                        } else {
                            Log.d(TAG, "Login failed : " + e);
                            Toast.makeText(HomePage.this, e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        getRxLocation();
    }

    @OnClick(R.id.parse_signup)
    public void signUpWithParseClick() {

        if (loginPass.getText() == null || loginPass.getText().toString().isEmpty()) {
            Toast.makeText(HomePage.this, "Password can't be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if (loginEmail.getText() == null || loginEmail.getText().toString().isEmpty()) {
            Toast.makeText(HomePage.this, "Password can't be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Initiating sign up process");

        ParseUser user = new ParseUser();
        user.setUsername(loginEmail.getText().toString());
        user.setPassword(loginPass.getText().toString());
        user.setEmail(loginEmail.getText().toString());

        user.signUpInBackground(new SignUpCallback() {
            public void done(ParseException e) {
                if (e == null) {
                    Log.d(TAG, "Sign Up successful");
                    Toast.makeText(HomePage.this, "Sign Up successful", Toast.LENGTH_SHORT).show();
                    saveNewData();
                } else {

                    Log.d(TAG, "Exception during SignUp : " + e);
                    Toast.makeText(HomePage.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                }
            }
        });

    }

    @OnClick(R.id.search_places)
    public void launchPlacesActivity() {
        Intent intent = new Intent(this, PlacesActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.add_new_item)
    public void launchAddItemActivity() {
        Intent intent = new Intent(this, AddToDoItemActivity.class);
        startActivity(intent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle =
                new ActionBarDrawerToggle(this, drawer, toolbar,
                        R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @OnClick(R.id.facebook_login)
    public void loginWithFacebookClick() {

        parseLoginFacebookSubscription =
                ParseFacebookObservable.logInWithReadPermissions(HomePage.this,
                        Arrays.asList("public_profile", "email"))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<ParseUser>() {
                            @Override
                            public void onCompleted() {
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.d(TAG, "Something went wrong while logging in through Facebook!");
                            }

                            @Override
                            public void onNext(ParseUser parseUser) {

                                if (parseUser == null) {
                                    Log.d(TAG, "Uh oh. The user cancelled the Facebook login.");
                                } else if (parseUser.isNew()) {
                                    Log.d(TAG, "User signed up and logged in through Facebook!");
                                    getUserDetailsFromFB();
                                } else {
                                    Log.d(TAG, "User logged in through Facebook!");
                                    getUserDetailsFromFB();
                                    saveNewData();
                                    getUserDetailsFromParse();
                                }
                            }
                        });
    }

    private void getUserDetailsFromFB() {

        Observable<GraphResponse> fullDetailsObservable =
                Observable.fromCallable(new Callable<GraphResponse>() {
                    @Override
                    public GraphResponse call() throws Exception {
                        return new GraphRequest(AccessToken.getCurrentAccessToken(), "/me", null,
                                HttpMethod.GET).executeAndWait();
                    }
                });

        fullFbDetailsSubscription = fullDetailsObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<GraphResponse>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(GraphResponse graphResponse) {

                        Log.d(TAG, "graphResponse : " + graphResponse.getRawResponse());

                        try {
                            fbName = graphResponse.getJSONObject().getString("name");
                            fbMail = graphResponse.getJSONObject().getString("email");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        saveNewUser();

                    }
                });
    }

    private void getUserDetailsFromParse() {
        parseUser = ParseUser.getCurrentUser();
        Toast.makeText(HomePage.this, "Welcome back " + parseUser.getUsername(),
                Toast.LENGTH_SHORT).show();
    }

    private void saveNewUser() {
        ParseUser parseUser = ParseUser.getCurrentUser();
        if (!fbName.equals(parseUser.getUsername())) {

            parseUser.setUsername(fbName);
            parseUser.saveInBackground();
        }
    }

    private void saveNewData() {

        ParseObject privateNote = new ParseObject("Note");
        privateNote.put("todo", "Complete the App");
        privateNote.put("time", "10:30PM, 29th Nov");
        privateNote.put("location", "Home");
        privateNote.setACL(new ParseACL(ParseUser.getCurrentUser()));
        privateNote.saveInBackground();

    }

    private void retrieveData() {

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Note");
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> scoreList, ParseException e) {
                if (e == null) {
                    Log.d("score", "Retrieved " + scoreList.size() + " scores");
                } else {
                    Log.d("score", "Error: " + e.getMessage());
                }
            }
        });
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

                        Toast.makeText(HomePage.this, "Couldn't get location",
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
                            Toast.makeText(HomePage.this, "Couldn't lookup address",
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
                        Toast.makeText(HomePage.this, "Couldn't lookup address",
                                Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onNext(List<Address> addresses) {

                        Address addressItem = addresses.get(0);

                        String address = addressItem.getAddressLine(0) + ", " + addressItem.getAddressLine(1);

                        Log.d(TAG, "Address : " + address);

                    }
                });
    }
    //endregion

    @Override
    protected void onStop() {
        super.onStop();
        if (parseLoginFacebookSubscription != null &&
                !parseLoginFacebookSubscription.isUnsubscribed())
            parseLoginFacebookSubscription.unsubscribe();

        if (fullFbDetailsSubscription != null && !fullFbDetailsSubscription.isUnsubscribed())
            fullFbDetailsSubscription.unsubscribe();

        if (locationSub != null && !locationSub.isUnsubscribed())
            locationSub.unsubscribe();

        if (addressSub != null && !addressSub.isUnsubscribed())
            addressSub.unsubscribe();
    }

    //region Activity Related
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home_page, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    //endregion

}
