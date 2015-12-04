package com.nuron.justdoit;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.parse.FindCallback;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SignUpCallback;
import com.pnikosis.materialishprogress.ProgressWheel;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.json.JSONException;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.parse.ParseFacebookObservable;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class LoginActivity extends AppCompatActivity {

    private final int LOCATION_TIMEOUT_IN_SECONDS = 10;
    ParseUser parseUser;
    String fbName = null, fbMail = null;
    Subscription locationSub, addressSub,
            parseLoginFacebookSubscription, fullFbDetailsSubscription;

    CompositeSubscription allSubscriptions;

    private final static String TAG = LoginActivity.class.getSimpleName();

    @Bind(R.id.login_email_text)
    MaterialEditText loginEmail;

    @Bind(R.id.login_pass_text)
    MaterialEditText loginPass;

    @Bind(R.id.progress_wheel)
    ProgressWheel progressWheel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        progressWheel.stopSpinning();

        try{
            ParseUser currentUser = ParseUser.getCurrentUser();
            if (currentUser != null) {
                launchHomeActivity();
            }
        } catch (Exception e){

        }

    }


    @OnClick(R.id.parse_login)
    public void loginWithParseClick() {

        if (loginPass.getText() == null || loginPass.getText().toString().isEmpty()) {
            Toast.makeText(LoginActivity.this, "Password can't be blank", Toast.LENGTH_SHORT).show();
            return;
        }

        if (loginEmail.getText() == null || loginEmail.getText().toString().isEmpty()) {
            Toast.makeText(LoginActivity.this, "Email can't be blank", Toast.LENGTH_SHORT).show();
            return;
        }

        progressWheel.spin();
        Log.d(TAG, "Initiating login process");
        ParseUser.logInInBackground(loginEmail.getText().toString(),
                loginPass.getText().toString(), new LogInCallback() {
                    public void done(ParseUser user, ParseException e) {
                        if (user != null) {
                            Log.d(TAG, "Login successful");
                            Toast.makeText(getApplicationContext(), "Welcome back " +
                                            ParseUser.getCurrentUser().getUsername(),
                                    Toast.LENGTH_SHORT).show();
                            launchHomeActivity();
                        } else {
                            Log.d(TAG, "Login failed : " + e);
                            Toast.makeText(LoginActivity.this, e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @OnClick(R.id.facebook_login)
    public void loginWithFacebookClick() {

        progressWheel.spin();
        parseLoginFacebookSubscription =
                ParseFacebookObservable.logInWithReadPermissions(LoginActivity.this,
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
                                }
                            }
                        });
    }

    @OnClick(R.id.parse_signup)
    public void signUpWithParseClick() {

        if (loginPass.getText() == null || loginPass.getText().toString().isEmpty()) {
            Toast.makeText(LoginActivity.this, "Password can't be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if (loginEmail.getText() == null || loginEmail.getText().toString().isEmpty()) {
            Toast.makeText(LoginActivity.this, "Password can't be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        progressWheel.spin();

        Log.d(TAG, "Initiating sign up process");

        ParseUser user = new ParseUser();
        user.setUsername(loginEmail.getText().toString());
        user.setPassword(loginPass.getText().toString());
        user.setEmail(loginEmail.getText().toString());

        user.signUpInBackground(new SignUpCallback() {
            public void done(ParseException e) {
                if (e == null) {
                    Log.d(TAG, "Sign Up successful");
                    progressWheel.stopSpinning();
                    Toast.makeText(getApplicationContext(),
                            "Sign Up successful", Toast.LENGTH_SHORT).show();
                    launchHomeActivity();

                } else {

                    Log.d(TAG, "Exception during SignUp : " + e);
                    Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                }
            }
        });

    }


    private void launchHomeActivity() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    private void getUserDetailsFromParse() {
        parseUser = ParseUser.getCurrentUser();
        Toast.makeText(LoginActivity.this, "Welcome back " + parseUser.getUsername(),
                Toast.LENGTH_SHORT).show();
    }

    private void saveNewUser() {
        ParseUser parseUser = ParseUser.getCurrentUser();
        if (!fbName.equals(parseUser.getUsername())) {

            parseUser.setUsername(fbName);
            parseUser.saveInBackground();
        }

        progressWheel.stopSpinning();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

}
