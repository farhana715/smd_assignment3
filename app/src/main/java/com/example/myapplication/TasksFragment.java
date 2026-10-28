package com.example.myapplication;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.myapplication.databinding.FragmentTasksBinding;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TasksFragment extends Fragment
{
    private static final String ARG_PROJECT_ID = "project_id";

    private FragmentTasksBinding binding;
    private OverviewFragment.ProjectHost host;
    private TaskListAdapter taskListAdapter;
    private final List<Task> displayedTasks = new ArrayList<>();

    // Filter state (preserved during visit)
    private String  searchQuery  = "";
    private String  filterStatus = "All statuses";
    private String  filterPriority = "All priorities";
    private String  filterDate   = "All dates";
    private String  sortOrder    = "Due date ascending";

    public static TasksFragment newInstance(int projectId)
    {
        TasksFragment f = new TasksFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PROJECT_ID, projectId);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onAttach(@NonNull android.content.Context context)
    {
        super.onAttach(context);
        if (context instanceof OverviewFragment.ProjectHost)
            host = (OverviewFragment.ProjectHost) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        binding = FragmentTasksBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        setupSpinners();
        setupSearch();
        setupList();
        applyFilters();

        binding.tvResetFilters.setOnClickListener(v -> resetFilters());
        binding.btnAddTask.setOnClickListener(v -> showAddTaskDialog());
        binding.btnEmptyAddTask.setOnClickListener(v -> showAddTaskDialog());
    }

    @Override
    public void onResume()
    {
        super.onResume();
        applyFilters();
    }

    private void setupSpinners()
    {
        String[] statuses   = {"All statuses", Task.STATUS_TODO, Task.STATUS_IN_PROGRESS, Task.STATUS_DONE};
        String[] priorities = {"All priorities", Task.PRIORITY_HIGH, Task.PRIORITY_NORMAL, Task.PRIORITY_LOW};
        String[] dates      = {"All dates", "Today", "Overdue", "No due date"};
        String[] sorts      = {"Due date ascending", "Due date descending", "Priority", "Created"};

        setSpinner(binding.spinnerStatus,   statuses,   0, sel -> { filterStatus = sel;   applyFilters(); });
        setSpinner(binding.spinnerPriority, priorities, 0, sel -> { filterPriority = sel; applyFilters(); });
        setSpinner(binding.spinnerDate,     dates,      0, sel -> { filterDate = sel;     applyFilters(); });
        setSpinner(binding.spinnerSort,     sorts,      0, sel -> { sortOrder = sel;      applyFilters(); });
    }

    private void setSpinner(Spinner spinner, String[] items, int defaultSel,
                             java.util.function.Consumer<String> onChange)
    {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(defaultSel);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id)
            { onChange.accept(items[pos]); }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });
    }

    private void setupSearch()
    {
        binding.etTaskSearch.addTextChangedListener(new TextWatcher()
        {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c)
            { searchQuery = s.toString().trim(); applyFilters(); }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void setupList()
    {
        taskListAdapter = new TaskListAdapter(requireContext(), displayedTasks,
                () ->
                {
                    if (host != null) host.onProjectUpdated();
                    applyFilters();
                });
        binding.lvTasks.setAdapter(taskListAdapter);
    }

    public void applyFilters()
    {
        if (host == null || binding == null) return;
        Project p = host.getProject();
        List<Task> all = p.getTasks();

        List<Task> filtered = new ArrayList<>();
        LocalDate today = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O
                ? LocalDate.now() : null;

        for (Task t : all)
        {
            // Search
            if (!searchQuery.isEmpty() &&
                !t.getDescription().toLowerCase().contains(searchQuery.toLowerCase()))
                continue;

            // Status filter
            if (!"All statuses".equals(filterStatus) && !filterStatus.equals(t.getStatus()))
                continue;

            // Priority filter
            if (!"All priorities".equals(filterPriority) && !filterPriority.equals(t.getPriority()))
                continue;

            // Date filter
            if (!"All dates".equals(filterDate) && today != null)
            {
                String due = t.getDueDate();
                switch (filterDate)
                {
                    case "No due date":
                        if (due != null && !due.isEmpty()) continue;
                        break;
                    case "Today":
                        if (due == null || due.isEmpty()) continue;
                        try { if (!LocalDate.parse(due, DateTimeFormatter.ISO_LOCAL_DATE).isEqual(today)) continue; }
                        catch (Exception e) { continue; }
                        break;
                    case "Overdue":
                        if (Task.STATUS_DONE.equals(t.getStatus())) continue;
                        if (due == null || due.isEmpty()) continue;
                        try { if (!LocalDate.parse(due, DateTimeFormatter.ISO_LOCAL_DATE).isBefore(today)) continue; }
                        catch (Exception e) { continue; }
                        break;
                }
            }
            filtered.add(t);
        }

        // Sort
        sortTasks(filtered, today);

        displayedTasks.clear();
        displayedTasks.addAll(filtered);
        taskListAdapter.notifyDataSetChanged();

        int total = all.size();
        int shown = filtered.size();
        binding.tvShowingCount.setText("Showing " + shown + " of " + total + " tasks");

        // Empty states
        if (total == 0)
        {
            binding.llEmptyNoTasks.setVisibility(View.VISIBLE);
            binding.tvEmptyNoMatch.setVisibility(View.GONE);
            binding.lvTasks.setVisibility(View.GONE);
        }
        else if (shown == 0)
        {
            binding.llEmptyNoTasks.setVisibility(View.GONE);
            binding.tvEmptyNoMatch.setVisibility(View.VISIBLE);
            binding.lvTasks.setVisibility(View.GONE);
        }
        else
        {
            binding.llEmptyNoTasks.setVisibility(View.GONE);
            binding.tvEmptyNoMatch.setVisibility(View.GONE);
            binding.lvTasks.setVisibility(View.VISIBLE);
        }
    }

    private void sortTasks(List<Task> tasks, LocalDate today)
    {
        Comparator<Task> cmp;
        switch (sortOrder)
        {
            case "Due date descending":
                cmp = (a, b) -> compareDates(b.getDueDate(), a.getDueDate());
                break;
            case "Priority":
                cmp = (a, b) -> priorityOrder(a.getPriority()) - priorityOrder(b.getPriority());
                break;
            case "Created":
                cmp = Comparator.comparingLong(Task::getCreatedAt);
                break;
            default: // Due date ascending
                cmp = (a, b) -> compareDates(a.getDueDate(), b.getDueDate());
        }
        Collections.sort(tasks, cmp);
    }

    private int compareDates(String a, String b)
    {
        boolean aNull = (a == null || a.isEmpty());
        boolean bNull = (b == null || b.isEmpty());
        if (aNull && bNull) return 0;
        if (aNull) return 1;
        if (bNull) return -1;
        return a.compareTo(b);
    }

    private int priorityOrder(String p)
    {
        switch (p)
        {
            case Task.PRIORITY_HIGH:   return 0;
            case Task.PRIORITY_NORMAL: return 1;
            case Task.PRIORITY_LOW:    return 2;
            default: return 3;
        }
    }

    private void resetFilters()
    {
        searchQuery    = "";
        filterStatus   = "All statuses";
        filterPriority = "All priorities";
        filterDate     = "All dates";
        sortOrder      = "Due date ascending";

        binding.etTaskSearch.setText("");
        binding.spinnerStatus.setSelection(0);
        binding.spinnerPriority.setSelection(0);
        binding.spinnerDate.setSelection(0);
        binding.spinnerSort.setSelection(0);
        applyFilters();
    }

    private void showAddTaskDialog()
    {
        if (host == null) return;
        Project p = host.getProject();

        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_task, null);

        EditText etDesc      = dialogView.findViewById(R.id.et_task_description);
        TextView tvCharCount = dialogView.findViewById(R.id.tv_char_count);
        Spinner  spPriority  = dialogView.findViewById(R.id.spinner_task_priority);
        Spinner  spStatus    = dialogView.findViewById(R.id.spinner_task_status);
        TextView tvDueDate   = dialogView.findViewById(R.id.tv_due_date);
        View     btnPickDate = dialogView.findViewById(R.id.btn_pick_date);
        View     btnClearDate= dialogView.findViewById(R.id.btn_clear_date);

        final String[] selectedDate = {null};

        ArrayAdapter<String> priAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item,
                new String[]{Task.PRIORITY_NORMAL, Task.PRIORITY_HIGH, Task.PRIORITY_LOW});
        priAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPriority.setAdapter(priAdapter);

        ArrayAdapter<String> stAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item,
                new String[]{Task.STATUS_TODO, Task.STATUS_IN_PROGRESS, Task.STATUS_DONE});
        stAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spStatus.setAdapter(stAdapter);

        etDesc.addTextChangedListener(new TextWatcher()
        {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c)
            { tvCharCount.setText(s.length() + "/500"); }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnPickDate.setOnClickListener(v ->
        {
            Calendar cal = Calendar.getInstance();
            new DatePickerDialog(requireContext(), (dp, y, m, d) ->
            {
                selectedDate[0] = String.format("%04d-%02d-%02d", y, m + 1, d);
                tvDueDate.setText(selectedDate[0]);
                tvDueDate.setTextColor(0xFF1A1A1A);
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        });
        btnClearDate.setOnClickListener(v ->
        {
            selectedDate[0] = null;
            tvDueDate.setText("No due date");
            tvDueDate.setTextColor(0xFFBDBDBD);
        });

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.add_task_title))
                .setView(dialogView)
                .setPositiveButton(getString(R.string.add_task), null)
                .setNegativeButton(getString(R.string.cancel), null)
                .create();

        dialog.setOnShowListener(di ->
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v ->
                {
                    String desc = etDesc.getText().toString().trim();
                    if (desc.isEmpty() || desc.length() > 500)
                    {
                        etDesc.setError("Description required (1–500 chars)");
                        return;
                    }
                    String priority = (String) spPriority.getSelectedItem();
                    String status   = (String) spStatus.getSelectedItem();

                    Task newTask = new Task(p.getNextTaskId(), desc, status, priority, selectedDate[0]);
                    p.getTasks().add(newTask);
                    host.onProjectUpdated();
                    applyFilters();
                    dialog.dismiss();
                    Toast.makeText(requireContext(), "Task added!", Toast.LENGTH_SHORT).show();
                })
        );
        dialog.show();
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        binding = null;
    }
}
