package com.nuron.justdoit.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nuron.justdoit.Activities.HomeActivity;
import com.nuron.justdoit.Model.ToDoItem;
import com.nuron.justdoit.R;
import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by nuron on 05/12/15.
 */

public class ToDoRecyclerAdapter extends RecyclerView.Adapter<ToDoRecyclerAdapter.ViewHolder> {
    List<ParseObject> parseObjectList;
    Context context;

    public ToDoRecyclerAdapter(Context context) {
        super();
        this.context = context;
        parseObjectList = new ArrayList<>();
    }

    public void addData(ParseObject parseObject) {
        parseObjectList.add(parseObject);
    }

    public void removeData(int position) {
        parseObjectList.remove(position);
    }

    public void clear() {
        if (parseObjectList != null) {
            parseObjectList.clear();
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.todo_item_layout, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        final ParseObject parseObject = parseObjectList.get(position);

        viewHolder.todoItemName.setText("Name : " +
                parseObject.getString(ToDoItem.TODO_ITEM_NAME));
        viewHolder.todoItemDate.setText("Date : " +
                parseObject.getString(ToDoItem.TODO_ITEM_DATE));
        viewHolder.todoItemDueDate.setText("Due : " +
                parseObject.getString(ToDoItem.TODO_ITEM_DUE_DATE));
        viewHolder.todoItemLocation.setText("Location : " +
                parseObject.getString(ToDoItem.TODO_ITEM_LOCATION));
        viewHolder.todoItemDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((HomeActivity) context).deleteToDoItem(parseObject, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return parseObjectList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.todo_item_name)
        TextView todoItemName;
        @Bind(R.id.todo_item_date)
        TextView todoItemDate;
        @Bind(R.id.todo_item_due_date)
        TextView todoItemDueDate;
        @Bind(R.id.todo_item_location)
        TextView todoItemLocation;
        @Bind(R.id.todo_item_delete)
        View todoItemDelete;


        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}