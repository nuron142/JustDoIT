package com.nuron.justdoit;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by nuron on 02/12/15.
 */
public class SearchPlaceFragment extends Fragment {

    private String currentLocation;
    private static final String CURRENT_LOCATION_ARG = "current_location";
    public static final String TAG = SearchPlaceFragment.class.getSimpleName();

    public SearchPlaceFragment() {
    }

    public static SearchPlaceFragment newInstance(String currentLocation) {
        SearchPlaceFragment fragment = new SearchPlaceFragment();
        Bundle args = new Bundle();
        args.putString("current_location", currentLocation);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            currentLocation = getArguments().getString(CURRENT_LOCATION_ARG);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search_place, container, false);
    }

}
