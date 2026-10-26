package com.example.myapplication;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.util.List;

public class FileListAdapter extends ArrayAdapter<AttachmentLink>
{
    public interface FileActionListener
    {
        void onPreview(AttachmentLink link);
        void onRemove(AttachmentLink link, int position);
    }

    private final LayoutInflater     inflater;
    private final FileActionListener listener;

    public FileListAdapter(Context context, List<AttachmentLink> files, FileActionListener listener)
    {
        super(context, 0, files);
        this.inflater  = LayoutInflater.from(context);
        this.listener  = listener;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        ViewHolder holder;
        if (convertView == null)
        {
            convertView = inflater.inflate(R.layout.item_file_row, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        }
        else
        {
            holder = (ViewHolder) convertView.getTag();
        }

        AttachmentLink link = getItem(position);
        if (link == null) return convertView;

        holder.tvName.setText(link.getDisplayName());
        holder.tvType.setText(link.isImage() ? "Image" : "Text");

        // Thumbnail: show the drawable (image) or a document icon (text)
        if (link.isImage())
        {
            try
            {
                Drawable d = ContextCompat.getDrawable(getContext(), link.getResourceId());
                holder.ivThumb.setImageDrawable(d);
            }
            catch (Exception e)
            {
                holder.ivThumb.setImageResource(R.drawable.attach_image1);
            }
        }
        else
        {
            holder.ivThumb.setImageResource(android.R.drawable.ic_menu_agenda);
        }

        holder.btnPreview.setOnClickListener(v -> { if (listener != null) listener.onPreview(link); });
        holder.tvRemove.setOnClickListener(v -> { if (listener != null) listener.onRemove(link, position); });

        return convertView;
    }

    static class ViewHolder
    {
        ImageView ivThumb;
        TextView  tvName, tvType, tvRemove;
        Button    btnPreview;

        ViewHolder(View v)
        {
            ivThumb    = v.findViewById(R.id.iv_file_thumb);
            tvName     = v.findViewById(R.id.tv_file_name);
            tvType     = v.findViewById(R.id.tv_file_type);
            btnPreview = v.findViewById(R.id.btn_preview);
            tvRemove   = v.findViewById(R.id.tv_remove_file);
        }
    }
}
