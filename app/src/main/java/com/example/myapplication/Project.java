package com.example.myapplication;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class Project implements Parcelable
{
    private int id;
    private String name;
    private String tag;
    private String description;
    private String attachmentName;   // kept for compat; use attachmentLinks for A3
    private int commentCount;
    private ArrayList<Task> tasks;
    private ArrayList<AttachmentLink> attachmentLinks;

    public Project(int id, String name, String tag, String description,
                   String attachmentName, int commentCount, ArrayList<Task> tasks)
    {
        this.id             = id;
        this.name           = name;
        this.tag            = tag;
        this.description    = description;
        this.attachmentName = attachmentName;
        this.commentCount   = commentCount;
        this.tasks          = tasks != null ? tasks : new ArrayList<>();
        this.attachmentLinks = new ArrayList<>();
    }

    public Project(int id, String name, String tag, String description,
                   String attachmentName, int commentCount,
                   ArrayList<Task> tasks, ArrayList<AttachmentLink> attachmentLinks)
    {
        this.id              = id;
        this.name            = name;
        this.tag             = tag;
        this.description     = description;
        this.attachmentName  = attachmentName;
        this.commentCount    = commentCount;
        this.tasks           = tasks != null ? tasks : new ArrayList<>();
        this.attachmentLinks = attachmentLinks != null ? attachmentLinks : new ArrayList<>();
    }

    protected Project(Parcel in)
    {
        id              = in.readInt();
        name            = in.readString();
        tag             = in.readString();
        description     = in.readString();
        attachmentName  = in.readString();
        commentCount    = in.readInt();
        tasks           = in.createTypedArrayList(Task.CREATOR);
        attachmentLinks = in.createTypedArrayList(AttachmentLink.CREATOR);
    }

    public static final Creator<Project> CREATOR = new Creator<Project>()
    {
        @Override
        public Project createFromParcel(Parcel in) { return new Project(in); }

        @Override
        public Project[] newArray(int size) { return new Project[size]; }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(tag);
        dest.writeString(description);
        dest.writeString(attachmentName);
        dest.writeInt(commentCount);
        dest.writeTypedList(tasks);
        dest.writeTypedList(attachmentLinks);
    }

    @Override
    public int describeContents() { return 0; }

    // ---- Getters ----
    public int    getId()            { return id; }
    public String getName()          { return name; }
    public String getTag()           { return tag; }
    public String getDescription()   { return description; }
    public String getAttachmentName(){ return attachmentName; }
    public int    getCommentCount()  { return commentCount; }
    public ArrayList<Task> getTasks(){ return tasks; }
    public ArrayList<AttachmentLink> getAttachmentLinks() { return attachmentLinks; }

    // ---- Setters ----
    public void setName(String name)               { this.name = name; }
    public void setTag(String tag)                 { this.tag = tag; }
    public void setDescription(String description) { this.description = description; }
    public void setTasks(ArrayList<Task> tasks)    { this.tasks = tasks; }
    public void setAttachmentLinks(ArrayList<AttachmentLink> links) { this.attachmentLinks = links; }

    // ---- Computed counts ----
    public int getTaskCount()           { return tasks.size(); }
    public int getAttachmentLinkCount() { return attachmentLinks != null ? attachmentLinks.size() : 0; }

    public int getCompletedTaskCount()
    {
        int count = 0;
        for (Task t : tasks)
            if (t.isCompleted()) count++;
        return count;
    }

    /** Returns 0–100, integer division. */
    public int getProgressPercent()
    {
        if (tasks.isEmpty()) return 0;
        return (int) ((getCompletedTaskCount() * 100L) / tasks.size());
    }

    public int getStatusCount(String status)
    {
        int count = 0;
        for (Task t : tasks)
            if (status.equals(t.getStatus())) count++;
        return count;
    }

    public int getNextTaskId()
    {
        int max = 0;
        for (Task t : tasks) if (t.getId() > max) max = t.getId();
        return max + 1;
    }

    public int getNextAttachmentId()
    {
        if (attachmentLinks == null || attachmentLinks.isEmpty()) return 1;
        int max = 0;
        for (AttachmentLink a : attachmentLinks) if (a.getId() > max) max = a.getId();
        return max + 1;
    }
}
