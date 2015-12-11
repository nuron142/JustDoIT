package com.nuron.justdoit.Fragments;


import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.nuron.justdoit.Activities.AddToDoItemActivity;
import com.nuron.justdoit.Adapters.SearchLocationAdapter;
import com.nuron.justdoit.R;

import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pl.charmas.android.reactivelocation.ReactiveLocationProvider;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
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

    @Bind(R.id.activity_search_recent_query_list)
    RecyclerView searchRecyclerview;

    @Bind(R.id.card_current_location)
    CardView cardCurrentLocation;

    @Bind(R.id.card_search_list)
    CardView cardSearchList;

    @OnClick(R.id.search_clear_button)
    public void clearSearch() {
        searchLocationText.setText("");
    }

    @OnClick(R.id.home_back_button)
    public void closeSearch() {
        ((AddToDoItemActivity) getActivity()).handleBackPressed();
    }

    private String currentLocation;
    public static final String CURRENT_LOCATION_ARG = "current_location";
    public static final String TAG = SearchPlaceFragment.class.getSimpleName();


    private ReactiveLocationProvider reactiveLocationProvider;
    private CompositeSubscription compositeSubscription;
    private SearchLocationAdapter searchLocationAdapter;
    private Context context;

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

        cardCurrentLocation.setVisibility(View.VISIBLE);
        cardSearchList.setVisibility(View.GONE);

        if (currentLocation != null && !currentLocation.isEmpty()) {
            currentLocationText.setText(currentLocation);
        } else {
            currentLocationText.setText("Sorry couldn't locate you");
        }

        searchRecyclerview.setHasFixedSize(true);
        searchRecyclerview.setLayoutManager(new LinearLayoutManager(getActivity()));

        searchLocationAdapter = new SearchLocationAdapter(getActivity());
        searchRecyclerview.setAdapter(searchLocationAdapter);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        compositeSubscription = new CompositeSubscription();

        Observable<TextViewTextChangeEvent> searchTextSubscription =
                RxTextView.textChangeEvents(searchLocationText)
                        .debounce(100, TimeUnit.MILLISECONDS);

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
                        if (q == null || q.location == null) return Observable.empty();

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
                add(suggestionsObservable
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<AutocompletePredictionBuffer>() {
                            @Override
                            public void onCompleted() {

                            }

                            @Override
                            public void onError(Throwable e) {

                            }

                            @Override
                            public void onNext(AutocompletePredictionBuffer buffer) {

                                if (buffer.getCount() == 0) {
                                    cardCurrentLocation.setVisibility(View.VISIBLE);
                                    cardSearchList.setVisibility(View.GONE);
                                } else {
                                    cardCurrentLocation.setVisibility(View.GONE);
                                    cardSearchList.setVisibility(View.VISIBLE);
                                }

                                searchLocationAdapter.clear();
                                for (AutocompletePrediction prediction : buffer) {
                                    searchLocationAdapter.addData(prediction.getDescription());
                                }
                                searchLocationAdapter.notifyDataSetChanged();
                                buffer.release();
                            }

                        })
                );
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
}
