package com.example.myapplication;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.databinding.ItemFileRowBinding;

import java.util.List;

/**
 * AttachmentAdapter – RecyclerView.Adapter for the Files tab.
 * Replaces the old FileListAdapter (ArrayAdapter).
 *
 * Each row shows: thumbnail/icon, display name, type, Preview button, remove icon (🗑).
 * Swipe-RIGHT = remove with name-confirmation dialog (handled in FilesFragment via ItemTouchHelper).
 * The remove icon (🗑) triggers the same dialog as the swipe.
 */
public class AttachmentAdapter extends RecyclerView.Adapter<AttachmentAdapter.FileViewHolder>
{
    // ---- Callback ---------------------------------------------------------

    public interface FileActionListener
    {
        void onPreview(AttachmentLink link);
        void onRemove(AttachmentLink link, int adapterPosition);
    }

    // ---- Fields -----------------------------------------------------------

    private final List<AttachmentLink> files;
    private final FileActionListener   listener;

    // ---- Constructor ------------------------------------------------------

    public AttachmentAdapter(List<AttachmentLink> files, FileActionListener listener)
    {
        this.files    = files;
        this.listener = listener;
    }

    // ---- RecyclerView.Adapter overrides -----------------------------------

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        ItemFileRowBinding binding = ItemFileRowBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new FileViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position)
    {
        holder.bind(files.get(position));
    }

    @Override
    public int getItemCount() { return files.size(); }

    // ---- ViewHolder -------------------------------------------------------

    class FileViewHolder extends RecyclerView.ViewHolder
    {
        private final ItemFileRowBinding binding;

        FileViewHolder(ItemFileRowBinding binding)
        {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(AttachmentLink link)
        {
            binding.tvFileName.setText(link.getDisplayName());
            binding.tvFileType.setText(link.isImage() ? "Image" : "Text");

            // Thumbnail
            if (link.isImage())
            {
                try
                {
                    Drawable d = ContextCompat.getDrawable(
                            binding.getRoot().getContext(), link.getResourceId());
                    binding.ivFileThumb.setImageDrawable(d);
                }
                catch (Exception e)
                {
                    binding.ivFileThumb.setImageResource(R.drawable.attach_image1);
                }
            }
            else
            {
                binding.ivFileThumb.setImageResource(android.R.drawable.ic_menu_agenda);
            }

            binding.btnPreview.setOnClickListener(v ->
            {
                if (listener != null) listener.onPreview(link);
            });

            binding.tvRemoveFile.setOnClickListener(v ->
            {
                int pos = getBindingAdapterPosition();
                if (pos != RecyclerView.NO_ID && listener != null)
                    listener.onRemove(link, pos);
            });
        }
    }
}
