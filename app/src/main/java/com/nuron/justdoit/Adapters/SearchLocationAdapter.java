package com.nuron.justdoit.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nuron.justdoit.Activities.AddToDoItemActivity;
import com.nuron.justdoit.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by nuron on 04/12/15.
 */
public class SearchLocationAdapter extends RecyclerView.Adapter<SearchLocationAdapter.ViewHolder> {
    List<String> mLocations;
    Context context;
    public SearchLocationAdapter(Context context) {
        super();
        this.context = context;
        mLocations = new ArrayList<>();
    }

    public void addData(String location) {
        mLocations.add(location);
    }

    public void clear() {
        mLocations.clear();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {

        return new ViewHolder(LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.search_location_item, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        final String location = mLocations.get(i);
        viewHolder.latLongView.setText(location);

        viewHolder.latLongView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((AddToDoItemActivity) context).setLocation(location);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mLocations.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.search_location_result)
        public TextView latLongView;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
