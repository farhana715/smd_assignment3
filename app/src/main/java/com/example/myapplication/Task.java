package com.example.myapplication;

import android.os.Parcel;
import android.os.Parcelable;

public class Task implements Parcelable
{
    public static final String STATUS_TODO        = "To do";
    public static final String STATUS_IN_PROGRESS = "In progress";
    public static final String STATUS_DONE        = "Done";

    public static final String PRIORITY_HIGH   = "High";
    public static final String PRIORITY_NORMAL = "Normal";
    public static final String PRIORITY_LOW    = "Low";

    private int id;
    private String description;
    private boolean isCompleted;   // kept for backward compat; mirrors status==Done
    private String status;         // To do / In progress / Done
    private String priority;       // High / Normal / Low
    private String dueDate;        // ISO date "yyyy-MM-dd" or null
    private long createdAt;        // System.currentTimeMillis()

    public Task(int id, String description, boolean isCompleted)
    {
        this.id = id;
        this.description = description;
        this.isCompleted = isCompleted;
        this.status    = isCompleted ? STATUS_DONE : STATUS_TODO;
        this.priority  = PRIORITY_NORMAL;
        this.dueDate   = null;
        this.createdAt = System.currentTimeMillis();
    }

    public Task(int id, String description, String status, String priority, String dueDate)
    {
        this.id = id;
        this.description = description;
        this.status    = status;
        this.priority  = priority;
        this.dueDate   = dueDate;
        this.isCompleted = STATUS_DONE.equals(status);
        this.createdAt = System.currentTimeMillis();
    }

    protected Task(Parcel in)
    {
        id          = in.readInt();
        description = in.readString();
        isCompleted = in.readByte() != 0;
        status      = in.readString();
        priority    = in.readString();
        dueDate     = in.readString();
        createdAt   = in.readLong();
    }

    public static final Creator<Task> CREATOR = new Creator<Task>()
    {
        @Override
        public Task createFromParcel(Parcel in) { return new Task(in); }

        @Override
        public Task[] newArray(int size) { return new Task[size]; }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeInt(id);
        dest.writeString(description);
        dest.writeByte((byte) (isCompleted ? 1 : 0));
        dest.writeString(status);
        dest.writeString(priority);
        dest.writeString(dueDate);
        dest.writeLong(createdAt);
    }

    @Override
    public int describeContents() { return 0; }

    // Getters
    public int getId()             { return id; }
    public String getDescription() { return description; }
    public boolean isCompleted()   { return isCompleted; }
    public String getStatus()      { return status != null ? status : STATUS_TODO; }
    public String getPriority()    { return priority != null ? priority : PRIORITY_NORMAL; }
    public String getDueDate()     { return dueDate; }
    public long getCreatedAt()     { return createdAt; }

    // Setters
    public void setCompleted(boolean completed)
    {
        isCompleted = completed;
        status = completed ? STATUS_DONE : STATUS_TODO;
    }

    public void setStatus(String status)
    {
        this.status = status;
        this.isCompleted = STATUS_DONE.equals(status);
    }

    public void setPriority(String priority) { this.priority = priority; }
    public void setDueDate(String dueDate)   { this.dueDate = dueDate; }
    public void setDescription(String d)     { this.description = d; }
}
