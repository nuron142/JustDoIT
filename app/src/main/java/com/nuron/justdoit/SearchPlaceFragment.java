package com.nuron.justdoit;


import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.jakewharton.rxbinding.widget.TextViewTextChangeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import pl.charmas.android.reactivelocation.ReactiveLocationProvider;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by nuron on 02/12/15.
 */
public class SearchPlaceFragment extends Fragment {

    @Bind(R.id.search_location_edit_text)
    EditText searchLocationText;

    @Bind(R.id.current_location_text)
    TextView currentLocationText;

    private String currentLocation;
    public static final String CURRENT_LOCATION_ARG = "current_location";
    public static final String TAG = SearchPlaceFragment.class.getSimpleName();


    private ReactiveLocationProvider reactiveLocationProvider;
    private CompositeSubscription compositeSubscription;

    public SearchPlaceFragment() {
    }

    public static SearchPlaceFragment newInstance(String currentLocation) {
        SearchPlaceFragment fragment = new SearchPlaceFragment();
        Bundle args = new Bundle();
        args.putString(CURRENT_LOCATION_ARG, currentLocation);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            currentLocation = getArguments().getString(CURRENT_LOCATION_ARG);
        }
        reactiveLocationProvider = new ReactiveLocationProvider(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_search_place, container, false);
        ButterKnife.bind(this, rootView);


        if(currentLocation != null && !currentLocation.isEmpty()) {
            currentLocationText.setText(currentLocation);
        } else {
            currentLocationText.setText("Couldn't Locate you. Tap to try again");
        }

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        compositeSubscription = new CompositeSubscription();

        Observable<TextViewTextChangeEvent> searchTextSubscription =
                RxTextView.textChangeEvents(searchLocationText)
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

        compositeSubscription.
                add(suggestionsObservable.subscribe(new Action1<AutocompletePredictionBuffer>() {
                    @Override
                    public void call(AutocompletePredictionBuffer buffer) {
                        List<AutocompleteInfo> infos = new ArrayList<>();
                        Log.d(TAG, "infos size : " + buffer.getCount());
                        for (AutocompletePrediction prediction : buffer) {
                            infos.add(new AutocompleteInfo(prediction.getDescription(),
                                    prediction.getPlaceId()));
                        }
                        buffer.release();
                    }
                }));
    }


    @Override
    public void onStop() {
        super.onStop();
        if (compositeSubscription != null && !compositeSubscription.isUnsubscribed()) {
            compositeSubscription.unsubscribe();
            compositeSubscription = null;
        }
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
