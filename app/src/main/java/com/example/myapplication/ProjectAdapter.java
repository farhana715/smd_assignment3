package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.databinding.ItemProjectBinding;

import java.util.List;

public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ProjectViewHolder> {

    public interface OnProjectClickListener {
        void onProjectClick(Project project, int position);
    }

    private final List<Project> projectList;
    private final OnProjectClickListener listener;

    public ProjectAdapter(List<Project> projectList, OnProjectClickListener listener)
    {
        this.projectList = projectList;
        this.listener = listener;
    }

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
        holder.bind(projectList.get(position), position);
    }

    @Override
    public int getItemCount() {
        return projectList.size();
    }

    public void addProject(Project project)
    {
        projectList.add(project);
        notifyItemInserted(projectList.size() - 1);
    }

    public void updateProject(int position, Project updatedProject)
    {
        projectList.set(position, updatedProject);
        notifyItemChanged(position);
    }

    class ProjectViewHolder extends RecyclerView.ViewHolder
    {

        private final ItemProjectBinding binding;

        ProjectViewHolder(ItemProjectBinding binding)
        {
            super(binding.getRoot());

            this.binding = binding;
        }

        void bind(Project project, int position)
        {
            binding.tvProjectTag.setText(project.getTag());
            binding.tvProjectName.setText(project.getName());
            binding.tvProjectDescription.setText(project.getDescription());

            int attachCount = project.getAttachmentName().isEmpty() ? 0 : 1;
            binding.tvAttachmentCount.setText("📎 " + attachCount + " Attachment");

            binding.tvTaskCount.setText(
                    " " + project.getCompletedTaskCount() + "/" + project.getTaskCount());
            binding.tvCommentCount.setText("💬 " + project.getCommentCount() + " Comment");

            binding.getRoot().setOnClickListener(v -> listener.onProjectClick(project, position));

            binding.btnInvite.setEnabled(false);
        }
    }
}
