package com.nuron.justdoit;

import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.jakewharton.rxbinding.widget.TextViewTextChangeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import pl.charmas.android.reactivelocation.ReactiveLocationProvider;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by nuron on 30/11/15.
 */
public class PlacesActivity extends AppCompatActivity {

    private TextView currentPlaceView;
    private EditText queryView;
    private ListView placeSuggestionsList;
    private ReactiveLocationProvider reactiveLocationProvider;
    private CompositeSubscription compositeSubscription;

    private final static String TAG = PlacesActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places);
        currentPlaceView = (TextView) findViewById(R.id.current_place_view);
        queryView = (EditText) findViewById(R.id.place_query_view);
        placeSuggestionsList = (ListView) findViewById(R.id.place_suggestions_list);
        reactiveLocationProvider = new ReactiveLocationProvider(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        compositeSubscription = new CompositeSubscription();

        reactiveLocationProvider.getCurrentPlace(null)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Subscriber<PlaceLikelihoodBuffer>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(PlaceLikelihoodBuffer placeLikelihoods) {

                        for (PlaceLikelihood placeLikelihood : placeLikelihoods) {
                            Log.d(TAG, String.format("Place '%s' has likelihood: %g",
                                    placeLikelihood.getPlace().getName(),
                                    placeLikelihood.getLikelihood()));
                        }

                        placeLikelihoods.release();
                    }
                });


        Observable<TextViewTextChangeEvent> searchTextSubscription =
                RxTextView.textChangeEvents(queryView)
                        .debounce(100, TimeUnit.MILLISECONDS)
                        .filter(new Func1<TextViewTextChangeEvent, Boolean>() {
                            @Override
                            public Boolean call(TextViewTextChangeEvent textViewTextChangeEvent) {
                                return !TextUtils.isEmpty(
                                        textViewTextChangeEvent.text().toString());
                            }
                        });

        Observable<Location> lastKnownLocationObservable =
                reactiveLocationProvider.getLastKnownLocation();
        Observable<AutocompletePredictionBuffer> suggestionsObservable = Observable
                .combineLatest(searchTextSubscription, lastKnownLocationObservable,
                        new Func2<TextViewTextChangeEvent, Location, QueryWithCurrentLocation>() {
                            @Override
                            public QueryWithCurrentLocation call(
                                    TextViewTextChangeEvent textViewTextChangeEvent,
                                    Location currentLocation) {

                                return new QueryWithCurrentLocation(
                                        textViewTextChangeEvent.text().toString(),
                                        currentLocation);
                            }
                        })
                .flatMap(new Func1<QueryWithCurrentLocation,
                        Observable<AutocompletePredictionBuffer>>() {
                    @Override
                    public Observable<AutocompletePredictionBuffer> call(
                            QueryWithCurrentLocation q) {
                        if (q.location == null) return Observable.empty();

                        double latitude = q.location.getLatitude();
                        double longitude = q.location.getLongitude();
                        LatLngBounds bounds = new LatLngBounds(
                                new LatLng(latitude - 0.05, longitude - 0.05),
                                new LatLng(latitude + 0.05, longitude + 0.05)
                        );
                        return reactiveLocationProvider.
                                getPlaceAutocompletePredictions(q.query, bounds, null);
                    }
                });

        suggestionsObservable.subscribe(new Action1<AutocompletePredictionBuffer>() {
            @Override
            public void call(AutocompletePredictionBuffer buffer) {
                List<AutocompleteInfo> infos = new ArrayList<>();
                Log.d(TAG, "infos size : " + buffer.getCount());
                for (AutocompletePrediction prediction : buffer) {
                    infos.add(new AutocompleteInfo(prediction.getDescription(),
                            prediction.getPlaceId()));
                }
                buffer.release();
                placeSuggestionsList.setAdapter(new ArrayAdapter<>(PlacesActivity.this,
                        android.R.layout.simple_list_item_1, infos));
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        compositeSubscription.unsubscribe();
        compositeSubscription = null;
    }

    private static class QueryWithCurrentLocation {
        public final String query;
        public final Location location;

        private QueryWithCurrentLocation(String query, Location location) {
            this.query = query;
            this.location = location;
        }
    }

    private static class AutocompleteInfo {
        private final String description;
        private final String id;

        private AutocompleteInfo(String description, String id) {
            this.description = description;
            this.id = id;
        }

        @Override
        public String toString() {
            return description;
        }
    }
}
