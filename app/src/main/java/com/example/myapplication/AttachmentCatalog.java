package com.example.myapplication;

import java.util.ArrayList;
import java.util.List;

public class AttachmentCatalog
{
    public static class CatalogItem
    {
        public final int    id;
        public final String displayName;
        public final String type;       // "image" or "text"
        public final int    resourceId; // R.drawable.xxx  or  R.raw.xxx

        public CatalogItem(int id, String displayName, String type, int resourceId)
        {
            this.id          = id;
            this.displayName = displayName;
            this.type        = type;
            this.resourceId  = resourceId;
        }

        public boolean isImage() { return "image".equals(type); }
        public boolean isText()  { return "text".equals(type); }
    }

    private static final List<CatalogItem> ITEMS = new ArrayList<>();

    static {
        // 2 images + 1 text as required by the assignment (initially Files(3) = 2 images + 1 text)
        ITEMS.add(new CatalogItem(1, "sports-project2.fig", "text",  R.raw.attachment_text1));
        ITEMS.add(new CatalogItem(2, "briefing from client.pdf", "text", R.raw.attachment_text2));
        ITEMS.add(new CatalogItem(3, "mount asset",         "image", R.drawable.attach_image1));
        ITEMS.add(new CatalogItem(4, "logo-design.png",    "image", R.drawable.attach_image2));
        ITEMS.add(new CatalogItem(5, "wireframe.sketch",   "text",  R.raw.attachment_text3));
    }

    public static List<CatalogItem> getAll() { return ITEMS; }

    public static CatalogItem getById(int id)
    {
        for (CatalogItem item : ITEMS)
            if (item.id == id) return item;
        return null;
    }

    /** Build the 3 default attachments that the sample project starts with */
    public static ArrayList<AttachmentLink> buildDefaultLinks()
    {
        ArrayList<AttachmentLink> links = new ArrayList<>();
        links.add(new AttachmentLink(1, 1, "sports-project2.fig",     "text",  R.raw.attachment_text1));
        links.add(new AttachmentLink(2, 2, "briefing from client.pdf","text",  R.raw.attachment_text2));
        links.add(new AttachmentLink(3, 3, "mount asset",             "image", R.drawable.attach_image1));
        return links;
    }
}
