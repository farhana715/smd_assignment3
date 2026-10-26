package com.example.myapplication;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TaskListAdapter extends ArrayAdapter<Task>
{
    public interface OnTaskChangedListener
    {
        void onTaskChanged();
    }

    private final LayoutInflater      inflater;
    private final OnTaskChangedListener listener;

    public TaskListAdapter(Context context, List<Task> tasks, OnTaskChangedListener listener)
    {
        super(context, 0, tasks);
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
            convertView = inflater.inflate(R.layout.item_task_row, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        }
        else
        {
            holder = (ViewHolder) convertView.getTag();
        }

        Task task = getItem(position);
        if (task == null) return convertView;

        // Unregister old listener before setting checked state
        holder.cbTask.setOnCheckedChangeListener(null);
        holder.cbTask.setChecked(task.isCompleted());

        holder.tvDescription.setText(task.getDescription());

        // Strike-through for Done tasks
        if (task.isCompleted())
            holder.tvDescription.setPaintFlags(holder.tvDescription.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        else
            holder.tvDescription.setPaintFlags(holder.tvDescription.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));

        holder.tvStatus.setText(task.getStatus());
        holder.tvPriority.setText(task.getPriority());

        // Due date
        String dueDate = task.getDueDate();
        if (dueDate == null || dueDate.isEmpty())
        {
            holder.tvDue.setText("No due date");
        }
        else
        {
            holder.tvDue.setText(dueDate);
        }

        // Overdue check
        boolean overdue = isOverdue(task);
        holder.tvOverdueBadge.setVisibility(overdue ? View.VISIBLE : View.GONE);

        // Checkbox listener
        holder.cbTask.setOnCheckedChangeListener((btn, isChecked) ->
        {
            task.setStatus(isChecked ? Task.STATUS_DONE : Task.STATUS_TODO);
            if (isChecked)
                holder.tvDescription.setPaintFlags(holder.tvDescription.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            else
                holder.tvDescription.setPaintFlags(holder.tvDescription.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.tvStatus.setText(task.getStatus());
            holder.tvOverdueBadge.setVisibility(isOverdue(task) ? View.VISIBLE : View.GONE);
            if (listener != null) listener.onTaskChanged();
        });

        return convertView;
    }

    private boolean isOverdue(Task task)
    {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.O) return false;
        if (Task.STATUS_DONE.equals(task.getStatus())) return false;
        String due = task.getDueDate();
        if (due == null || due.isEmpty()) return false;
        try
        {
            LocalDate dueDate = LocalDate.parse(due, DateTimeFormatter.ISO_LOCAL_DATE);
            return dueDate.isBefore(LocalDate.now());
        }
        catch (Exception e) { return false; }
    }

    static class ViewHolder
    {
        CheckBox cbTask;
        TextView tvDescription, tvStatus, tvPriority, tvDue, tvOverdueBadge;

        ViewHolder(View v)
        {
            cbTask          = v.findViewById(R.id.cb_task);
            tvDescription   = v.findViewById(R.id.tv_task_description);
            tvStatus        = v.findViewById(R.id.tv_task_status);
            tvPriority      = v.findViewById(R.id.tv_task_priority);
            tvDue           = v.findViewById(R.id.tv_task_due);
            tvOverdueBadge  = v.findViewById(R.id.tv_overdue_badge);
        }
    }
}
