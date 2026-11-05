package com.example.myapplication;

import android.os.Parcel;
import android.os.Parcelable;

public class AttachmentLink implements Parcelable
{
    private int    id;
    private int    catalogId;    // references AttachmentCatalog item id
    private String displayName;
    private String type;         // "image" or "text"
    private int    resourceId;   // drawable/raw res id

    public AttachmentLink(int id, int catalogId, String displayName, String type, int resourceId)
    {
        this.id          = id;
        this.catalogId   = catalogId;
        this.displayName = displayName;
        this.type        = type;
        this.resourceId  = resourceId;
    }

    protected AttachmentLink(Parcel in)
    {
        id          = in.readInt();
        catalogId   = in.readInt();
        displayName = in.readString();
        type        = in.readString();
        resourceId  = in.readInt();
    }

    public static final Creator<AttachmentLink> CREATOR = new Creator<AttachmentLink>()
    {
        @Override
        public AttachmentLink createFromParcel(Parcel in) { return new AttachmentLink(in); }

        @Override
        public AttachmentLink[] newArray(int size) { return new AttachmentLink[size]; }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeInt(id);
        dest.writeInt(catalogId);
        dest.writeString(displayName);
        dest.writeString(type);
        dest.writeInt(resourceId);
    }

    @Override
    public int describeContents() { return 0; }

    public int getId()            { return id; }
    public int getCatalogId()     { return catalogId; }
    public String getDisplayName(){ return displayName; }
    public String getType()       { return type; }
    public int getResourceId()    { return resourceId; }

    public boolean isImage() { return "image".equals(type); }
    public boolean isText()  { return "text".equals(type); }
}
