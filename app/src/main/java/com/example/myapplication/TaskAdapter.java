package com.example.myapplication;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.databinding.ItemTaskBinding;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    public interface OnTaskStatusChangedListener {
        void onTaskStatusChanged();
    }

    private final List<Task> taskList;
    private final OnTaskStatusChangedListener listener;

    public TaskAdapter(List<Task> taskList, OnTaskStatusChangedListener listener) {
        this.taskList = taskList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTaskBinding binding = ItemTaskBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new TaskViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        holder.bind(taskList.get(position));
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public void addTask(Task task) {
        taskList.add(task);
        notifyItemInserted(taskList.size() - 1);
    }

    public int getCompletedCount() {
        int count = 0;
        for (Task task : taskList) {
            if (task.isCompleted()) count++;
        }
        return count;
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {

        private final ItemTaskBinding binding;

        TaskViewHolder(ItemTaskBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Task task) {
            binding.cbTask.setOnCheckedChangeListener(null);
            binding.cbTask.setChecked(task.isCompleted());

            binding.tvTaskDescription.setText(task.getDescription());



            if (task.isCompleted())
            {
                binding.tvTaskDescription.setPaintFlags(
                        binding.tvTaskDescription.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else
            {
                binding.tvTaskDescription.setPaintFlags(
                        binding.tvTaskDescription.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            }

            binding.cbTask.setOnCheckedChangeListener((buttonView, isChecked) -> {
                task.setCompleted(isChecked);
                if (isChecked)
                {
                    binding.tvTaskDescription.setPaintFlags(
                            binding.tvTaskDescription.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                } else
                {
                    binding.tvTaskDescription.setPaintFlags(
                            binding.tvTaskDescription.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                }
                listener.onTaskStatusChanged();
            });
        }
    }
}
