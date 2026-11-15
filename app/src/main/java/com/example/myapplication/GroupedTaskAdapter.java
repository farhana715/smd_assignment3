package com.example.myapplication;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.databinding.ItemTaskHeaderBinding;
import com.example.myapplication.databinding.ItemTaskRowBinding;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * GroupedTaskAdapter – RecyclerView.Adapter with 2 view types (HEADER and TASK).
 *
 * Groups in fixed order: Overdue → In progress → To do → Done.
 * "Overdue" is computed (status != Done AND dueDate before today).
 * Only non-empty groups (and their headers) are shown.
 *
 * Header row  : group name + count, e.g. "In progress (2)"
 * Task row    : CheckBox, description (2 lines), status chip, priority chip, due date, overdue badge.
 */
public class GroupedTaskAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
    // ---- Item types -------------------------------------------------------

    public static final int TYPE_HEADER = 0;
    public static final int TYPE_TASK   = 1;

    // ---- Sealed list items ------------------------------------------------

    public static class Item
    {
        public static final int HEADER = 0;
        public static final int TASK   = 1;

        public int    type;
        public String headerTitle; // used when type == HEADER
        public int    taskCount;   // used when type == HEADER
        public Task   task;        // used when type == TASK

        static Item header(String title, int count)
        {
            Item i = new Item();
            i.type        = HEADER;
            i.headerTitle = title;
            i.taskCount   = count;
            return i;
        }

        static Item task(Task t)
        {
            Item i = new Item();
            i.type = TASK;
            i.task = t;
            return i;
        }
    }

    // ---- Callback ---------------------------------------------------------

    public interface OnTaskChangedListener
    {
        void onTaskChanged();
    }

    // ---- Fields -----------------------------------------------------------

    private final List<Item>            items    = new ArrayList<>();
    private       OnTaskChangedListener listener;

    // ---- Constructor ------------------------------------------------------

    public GroupedTaskAdapter(OnTaskChangedListener listener)
    {
        this.listener = listener;
    }

    // ---- Public API -------------------------------------------------------

    /**
     * Rebuild the flat item list from a filtered & sorted task list.
     * Call this whenever the display task list changes.
     */
    public void submitTasks(List<Task> tasks)
    {
        items.clear();

        LocalDate today = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
            today = LocalDate.now();

        // Partition tasks into groups
        List<Task> overdue     = new ArrayList<>();
        List<Task> inProgress  = new ArrayList<>();
        List<Task> todo        = new ArrayList<>();
        List<Task> done        = new ArrayList<>();

        for (Task t : tasks)
        {
            if (isOverdue(t, today))
                overdue.add(t);
            else if (Task.STATUS_IN_PROGRESS.equals(t.getStatus()))
                inProgress.add(t);
            else if (Task.STATUS_DONE.equals(t.getStatus()))
                done.add(t);
            else
                todo.add(t);
        }

        addGroup("Overdue",      overdue);
        addGroup("In progress",  inProgress);
        addGroup("To do",        todo);
        addGroup("Done",         done);

        notifyDataSetChanged();
    }

    /** Returns the flat Item at the given adapter position. */
    public Item getItemAt(int position) { return items.get(position); }

    /** Moves a task item from fromPos to toPos within the flat list. */
    public boolean moveItem(int fromPos, int toPos)
    {
        Item from = items.get(fromPos);
        Item to   = items.get(toPos);

        // Only reorder TASK items within the same status group
        if (from.type != TYPE_TASK || to.type != TYPE_TASK) return false;
        if (!sameGroup(from.task, to.task)) return false;

        items.remove(fromPos);
        items.add(toPos, from);
        notifyItemMoved(fromPos, toPos);
        return true;
    }

    /** Restores a row that was swiped but should NOT be acted on. */
    public void restoreItem(int position)
    {
        notifyItemChanged(position);
    }

    // ---- RecyclerView.Adapter overrides -----------------------------------

    @Override
    public int getItemViewType(int position) { return items.get(position).type; }

    @Override
    public int getItemCount() { return items.size(); }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        LayoutInflater inf = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_HEADER)
        {
            ItemTaskHeaderBinding b = ItemTaskHeaderBinding.inflate(inf, parent, false);
            return new HeaderViewHolder(b);
        }
        else
        {
            ItemTaskRowBinding b = ItemTaskRowBinding.inflate(inf, parent, false);
            return new TaskViewHolder(b);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position)
    {
        Item item = items.get(position);
        if (item.type == TYPE_HEADER)
            ((HeaderViewHolder) holder).bind(item);
        else
            ((TaskViewHolder) holder).bind(item.task);
    }

    // ---- Helper methods ---------------------------------------------------

    private void addGroup(String label, List<Task> tasks)
    {
        if (tasks.isEmpty()) return;
        items.add(Item.header(label, tasks.size()));
        for (Task t : tasks)
            items.add(Item.task(t));
    }

    private boolean isOverdue(Task task, LocalDate today)
    {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.O) return false;
        if (Task.STATUS_DONE.equals(task.getStatus())) return false;
        String due = task.getDueDate();
        if (due == null || due.isEmpty()) return false;
        try { return LocalDate.parse(due).isBefore(today); }
        catch (Exception e) { return false; }
    }

    private boolean sameGroup(Task a, Task b)
    {
        // Determine the display group for each task.
        // Both must belong to the same named group in the flat list.
        // We locate the header that precedes each task's position.
        int posA = -1, posB = -1;
        for (int i = 0; i < items.size(); i++)
        {
            Item it = items.get(i);
            if (it.type == TYPE_TASK)
            {
                if (it.task == a) posA = i;
                if (it.task == b) posB = i;
            }
        }
        if (posA < 0 || posB < 0) return false;

        String headerA = findHeader(posA);
        String headerB = findHeader(posB);
        return headerA != null && headerA.equals(headerB);
    }

    private String findHeader(int taskPos)
    {
        for (int i = taskPos - 1; i >= 0; i--)
        {
            if (items.get(i).type == TYPE_HEADER)
                return items.get(i).headerTitle;
        }
        return null;
    }

    // ---- ViewHolders ------------------------------------------------------

    static class HeaderViewHolder extends RecyclerView.ViewHolder
    {
        private final ItemTaskHeaderBinding binding;

        HeaderViewHolder(ItemTaskHeaderBinding binding)
        {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Item item)
        {
            binding.tvHeaderTitle.setText(item.headerTitle + " (" + item.taskCount + ")");
        }
    }

    class TaskViewHolder extends RecyclerView.ViewHolder
    {
        private final ItemTaskRowBinding binding;

        TaskViewHolder(ItemTaskRowBinding binding)
        {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Task task)
        {
            // Unregister old listener before setting checked state to avoid spurious callbacks
            binding.cbTask.setOnCheckedChangeListener(null);
            binding.cbTask.setChecked(task.isCompleted());

            binding.tvTaskDescription.setText(task.getDescription());

            // Strike-through for Done
            if (task.isCompleted())
                binding.tvTaskDescription.setPaintFlags(
                        binding.tvTaskDescription.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            else
                binding.tvTaskDescription.setPaintFlags(
                        binding.tvTaskDescription.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));

            binding.tvTaskStatus.setText(task.getStatus());
            binding.tvTaskPriority.setText(task.getPriority());

            String due = task.getDueDate();
            binding.tvTaskDue.setText((due == null || due.isEmpty()) ? "No due date" : due);

            boolean overdue = isOverdueNow(task);
            binding.tvOverdueBadge.setVisibility(overdue ? View.VISIBLE : View.GONE);

            // Checkbox: mark as Done (or revert to To do)
            binding.cbTask.setOnCheckedChangeListener((btn, isChecked) ->
            {
                task.setStatus(isChecked ? Task.STATUS_DONE : Task.STATUS_TODO);
                if (isChecked)
                    binding.tvTaskDescription.setPaintFlags(
                            binding.tvTaskDescription.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                else
                    binding.tvTaskDescription.setPaintFlags(
                            binding.tvTaskDescription.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                binding.tvTaskStatus.setText(task.getStatus());
                binding.tvOverdueBadge.setVisibility(isOverdueNow(task) ? View.VISIBLE : View.GONE);
                if (listener != null) listener.onTaskChanged();
            });
        }

        private boolean isOverdueNow(Task task)
        {
            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.O) return false;
            if (Task.STATUS_DONE.equals(task.getStatus())) return false;
            String due = task.getDueDate();
            if (due == null || due.isEmpty()) return false;
            try { return LocalDate.parse(due).isBefore(LocalDate.now()); }
            catch (Exception e) { return false; }
        }
    }
}
