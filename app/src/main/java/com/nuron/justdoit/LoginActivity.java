package com.nuron.justdoit;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;
import com.pnikosis.materialishprogress.ProgressWheel;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.json.JSONException;

import java.util.Arrays;
import java.util.concurrent.Callable;

import bolts.Task;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.parse.ParseFacebookObservable;
import rx.parse.ParseObservable;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class LoginActivity extends AppCompatActivity {

    CompositeSubscription allSubscriptions;

    private final static String TAG = LoginActivity.class.getSimpleName();
    public final static String USER_ACCOUNT_NAME = "account_name";

    @Bind(R.id.login_email_text)
    MaterialEditText loginEmail;

    @Bind(R.id.login_name_text)
    MaterialEditText loginName;

    @Bind(R.id.login_pass_text)
    MaterialEditText loginPass;

    @Bind(R.id.progress_wheel)
    ProgressWheel progressWheel;

    @Bind(R.id.sign_in_layout)
    View signInLayout;

    @Bind(R.id.sign_up_layout)
    View signUpLayout;

    @Bind(R.id.parse_show_signup)
    View showSignUpLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        allSubscriptions = new CompositeSubscription();
        progressWheel.stopSpinning();

        try {
            ParseUser currentUser = ParseUser.getCurrentUser();
            if (currentUser != null) {
                Log.d(TAG, "User is logged in");
                launchHomeActivity();
            }
        } catch (Exception e) {
            e.printStackTrace();
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

        allSubscriptions.add(
                ParseObservable.logIn(loginEmail.getText().toString(),
                        loginPass.getText().toString())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<ParseUser>() {
                            @Override
                            public void onCompleted() {
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.d(TAG, "Login failed : " + e);
                                progressWheel.stopSpinning();
                                Toast.makeText(LoginActivity.this, e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onNext(ParseUser parseUser) {
                                if (parseUser != null) {
                                    Log.d(TAG, "Login successful");
                                    progressWheel.stopSpinning();
                                    Toast.makeText(getApplicationContext(), "Welcome back " +
                                                    ParseUser.getCurrentUser().getUsername(),
                                            Toast.LENGTH_SHORT).show();
                                    launchHomeActivity();
                                }
                            }
                        }));
    }

    @OnClick(R.id.parse_show_signup)
    public void showSignUpLayout() {
        signInLayout.setVisibility(View.INVISIBLE);
        signUpLayout.setVisibility(View.VISIBLE);
        showSignUpLayout.setVisibility(View.GONE);

    }

    @OnClick(R.id.parse_signup)
    public void signUpWithParseClick() {

        if (loginPass.getText() == null || loginPass.getText().toString().isEmpty()) {
            Toast.makeText(LoginActivity.this,
                    "Password can't be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if (loginEmail.getText() == null || loginEmail.getText().toString().isEmpty()) {
            Toast.makeText(LoginActivity.this,
                    "Email can't be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if (loginName.getText() == null || loginName.getText().toString().isEmpty()) {
            Toast.makeText(LoginActivity.this,
                    "Name can't be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        progressWheel.spin();

        Log.d(TAG, "Initiating sign up process");

        final ParseUser user = new ParseUser();
        user.setUsername(loginEmail.getText().toString());
        user.setPassword(loginPass.getText().toString());
        user.setEmail(loginEmail.getText().toString());
        user.put(USER_ACCOUNT_NAME, loginName.getText().toString());

        allSubscriptions.add(
                Observable.
                        fromCallable(new Callable<Task<Void>>() {
                            @Override
                            public Task<Void> call() throws Exception {
                                return user.signUpInBackground();
                            }
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<Task<Void>>() {
                            @Override
                            public void onCompleted() {
                                Log.d(TAG, "Sign Up successful");
                                progressWheel.stopSpinning();
                                ParseUser currentUser = ParseUser.getCurrentUser();
                                if (currentUser != null) {
                                    Toast.makeText(getApplicationContext(),
                                            "Welcome " + currentUser.get(USER_ACCOUNT_NAME),
                                            Toast.LENGTH_SHORT).show();
                                    launchHomeActivity();
                                }
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.d(TAG, "Exception during SignUp : " + e);
                                progressWheel.stopSpinning();
                                Toast.makeText(LoginActivity.this,
                                        "Error : " + e.getMessage(), Toast.LENGTH_SHORT).show();

                            }

                            @Override
                            public void onNext(Task<Void> voidTask) {

                            }
                        })
        );
    }


    private void launchHomeActivity() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    @OnClick(R.id.facebook_login)
    public void loginWithFacebookClick() {

        progressWheel.spin();

        allSubscriptions.add(
                ParseFacebookObservable.logInWithReadPermissions(LoginActivity.this,
                        Arrays.asList("public_profile", "email"))
                        .flatMap(new Func1<ParseUser, Observable<GraphResponse>>() {
                            @Override
                            public Observable<GraphResponse> call(ParseUser parseUser) {

                                return Observable.fromCallable(new Callable<GraphResponse>() {
                                    @Override
                                    public GraphResponse call() throws Exception {
                                        Log.d(TAG, "Returning GraphRequest");
                                        return new GraphRequest(
                                                AccessToken.getCurrentAccessToken(),
                                                "/me", null, HttpMethod.GET).executeAndWait();
                                    }
                                });
                            }
                        })
                        .flatMap(new Func1<GraphResponse, Observable<ParseUser>>() {
                            @Override
                            public Observable<ParseUser> call(GraphResponse graphResponse) {

                                Log.d(TAG, "graphResponse : " + graphResponse.getRawResponse());

                                FbUser fbUser = new FbUser();
                                try {
                                    fbUser.setFbName(
                                            graphResponse.getJSONObject().getString("name"));
                                    fbUser.setFbMail(
                                            graphResponse.getJSONObject().getString("email"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                ParseUser parseUser = ParseUser.getCurrentUser();
                                if (parseUser.get(USER_ACCOUNT_NAME) == null ||
                                        !fbUser.getFbName().equals(parseUser.get(USER_ACCOUNT_NAME))) {
                                    parseUser.put(USER_ACCOUNT_NAME, fbUser.getFbName());
                                    return ParseObservable.save(parseUser);
                                }
                                return Observable.empty();
                            }
                        })
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<ParseUser>() {
                            @Override
                            public void onCompleted() {
                                progressWheel.stopSpinning();
                                try {
                                    ParseUser currentUser = ParseUser.getCurrentUser();
                                    if (currentUser != null) {
                                        Toast.makeText(getApplicationContext(),
                                                "Welcome " + currentUser.get(USER_ACCOUNT_NAME),
                                                Toast.LENGTH_SHORT).show();
                                        launchHomeActivity();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                Log.d(TAG, "Successfully completed");
                            }

                            @Override
                            public void onError(Throwable e) {
                                progressWheel.stopSpinning();
                                Log.d(TAG, "Exception occured : " + e);
                            }

                            @Override
                            public void onNext(ParseUser parseUser) {
                            }
                        }));
    }

    @Override
    protected void onStop() {
        super.onStop();

        allSubscriptions.unsubscribe();
        allSubscriptions = null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {

        if (progressWheel.isSpinning()) {
            Log.d(TAG, "progressWheel.isSpinning() is true");
            progressWheel.stopSpinning();
            allSubscriptions.unsubscribe();
            allSubscriptions = new CompositeSubscription();

        } else if (showSignUpLayout.getVisibility() == View.GONE) {

            signInLayout.setVisibility(View.VISIBLE);
            signUpLayout.setVisibility(View.INVISIBLE);
            showSignUpLayout.setVisibility(View.VISIBLE);

        } else {
            super.onBackPressed();
        }
    }

}
