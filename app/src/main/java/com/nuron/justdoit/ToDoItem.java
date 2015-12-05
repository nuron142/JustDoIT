package com.nuron.justdoit;

/**
 * Created by nuron on 01/12/15.
 */
public class ToDoItem {

    public final static String TODO_TABLE_NAME = "ToDo";
    public final static String TODO_ITEM_NAME = "toDoName";
    public final static String TODO_ITEM_DATE = "toDoDate";
    public final static String TODO_ITEM_DUE_DATE = "toDoDueDate";
    public final static String TODO_ITEM_LOCATION = "toDoLocation";

    private String itemName;
    private String location;
    private String date;
    private String dueTime;
    private String parseId;


    public String getParseId() {
        return parseId;
    }

    public void setParseId(String parseId) {
        this.parseId = parseId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDueTime() {
        return dueTime;
    }

    public void setDueTime(String dueTime) {
        this.dueTime = dueTime;
    }

}
