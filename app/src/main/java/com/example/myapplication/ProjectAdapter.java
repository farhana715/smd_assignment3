package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.databinding.ItemProjectBinding;

/**
 * ProjectAdapter – ListAdapter<Project, ProjectViewHolder> using DiffUtil.
 * Replaces the old ProjectListAdapter (ArrayAdapter) for the HomeActivity RecyclerView.
 * Never calls notifyDataSetChanged(); uses submitList() instead.
 */
public class ProjectAdapter extends ListAdapter<Project, ProjectAdapter.ProjectViewHolder>
{
    // ---- DiffUtil callback -----------------------------------------------

    private static final DiffUtil.ItemCallback<Project> DIFF =
            new DiffUtil.ItemCallback<Project>()
            {
                @Override
                public boolean areItemsTheSame(@NonNull Project a, @NonNull Project b)
                {
                    return a.getId() == b.getId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull Project a, @NonNull Project b)
                {
                    return a.getName().equals(b.getName())
                            && a.getTag().equals(b.getTag())
                            && a.getDescription().equals(b.getDescription())
                            && a.getProgressPercent() == b.getProgressPercent()
                            && a.getTaskCount()       == b.getTaskCount()
                            && a.getCompletedTaskCount() == b.getCompletedTaskCount()
                            && a.getAttachmentLinkCount() == b.getAttachmentLinkCount()
                            && a.getCommentCount()    == b.getCommentCount();
                }
            };

    // ---- Click listener interface -----------------------------------------

    public interface OnProjectClickListener
    {
        void onProjectClick(Project project);
    }

    // ---- Fields -----------------------------------------------------------

    private final OnProjectClickListener listener;

    // ---- Constructor ------------------------------------------------------

    public ProjectAdapter(OnProjectClickListener listener)
    {
        super(DIFF);
        this.listener = listener;
    }

    // ---- RecyclerView.Adapter overrides ------------------------------------

    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        ItemProjectBinding binding = ItemProjectBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ProjectViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position)
    {
        holder.bind(getItem(position));
    }

    // ---- ViewHolder -------------------------------------------------------

    class ProjectViewHolder extends RecyclerView.ViewHolder
    {
        private final ItemProjectBinding binding;

        ProjectViewHolder(ItemProjectBinding binding)
        {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Project project)
        {
            binding.tvProjectTag.setText(project.getTag());
            binding.tvProjectName.setText(project.getName());
            binding.tvProjectDescription.setText(project.getDescription());

            int progress = project.getProgressPercent();
            binding.pbProject.setProgress(progress);
            binding.tvProgressPercent.setText(progress + "%");

            binding.tvTaskCount.setText(
                    project.getCompletedTaskCount() + "/" + project.getTaskCount());

            int attachCount = project.getAttachmentLinkCount();
            binding.tvAttachmentCount.setText("📎 " + attachCount);
            binding.tvCommentCount.setText("💬 " + project.getCommentCount());

            binding.btnInvite.setEnabled(false);

            binding.getRoot().setOnClickListener(v ->
            {
                if (listener != null) listener.onProjectClick(project);
            });
        }
    }
}
