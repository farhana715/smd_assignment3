package com.example.myapplication;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.myapplication.databinding.FragmentOverviewBinding;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;

public class OverviewFragment extends Fragment
{
    private static final String ARG_PROJECT_ID = "project_id";

    private FragmentOverviewBinding binding;
    private ProjectHost host;

    public static OverviewFragment newInstance(int projectId)
    {
        OverviewFragment f = new OverviewFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PROJECT_ID, projectId);
        f.setArguments(args);
        return f;
    }

    /** Host activity must implement this */
    public interface ProjectHost
    {
        Project getProject();
        void onProjectUpdated();
        void navigateToTab(int tabIndex);
    }

    @Override
    public void onAttach(@NonNull android.content.Context context)
    {
        super.onAttach(context);
        if (context instanceof ProjectHost)
            host = (ProjectHost) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        binding = FragmentOverviewBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        refreshUI();
        setupClickListeners();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        refreshUI();
    }

    public void refreshUI()
    {
        if (host == null || binding == null) return;
        Project p = host.getProject();

        binding.tvOverviewDescription.setText(p.getDescription());

        int completed = p.getCompletedTaskCount();
        int total     = p.getTaskCount();
        int percent   = p.getProgressPercent();

        binding.tvOverviewTasksCount.setText(completed + "/" + total + " completed");
        binding.tvOverviewPercent.setText(percent + "%");
        binding.pbOverview.setProgress(percent);

        // Task breakdown
        int todo       = p.getStatusCount(Task.STATUS_TODO);
        int inProgress = p.getStatusCount(Task.STATUS_IN_PROGRESS);
        int done       = p.getStatusCount(Task.STATUS_DONE);
        int overdue    = countOverdue(p);

        binding.tvCountTodo.setText(String.valueOf(todo));
        binding.tvCountInProgress.setText(String.valueOf(inProgress));
        binding.tvCountDone.setText(String.valueOf(done));
        binding.tvCountOverdue.setText(String.valueOf(overdue));
    }

    private int countOverdue(Project p)
    {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.O) return 0;
        int count = 0;
        LocalDate today = LocalDate.now();
        for (Task t : p.getTasks())
        {
            if (Task.STATUS_DONE.equals(t.getStatus())) continue;
            String due = t.getDueDate();
            if (due == null || due.isEmpty()) continue;
            try
            {
                if (LocalDate.parse(due, DateTimeFormatter.ISO_LOCAL_DATE).isBefore(today))
                    count++;
            }
            catch (Exception ignored) {}
        }
        return count;
    }

    private void setupClickListeners()
    {
        binding.btnEditProject.setOnClickListener(v -> showEditProjectDialog());
        binding.btnGotoFiles.setOnClickListener(v ->
        {
            if (host != null) host.navigateToTab(ProjectFragmentAdapter.TAB_FILES);
        });
        binding.btnGotoDiscussion.setOnClickListener(v ->
        {
            if (host != null) host.navigateToTab(ProjectFragmentAdapter.TAB_DISCUSSION);
        });

        // Task breakdown clicks
        binding.llTodo.setOnClickListener(v ->
        {
            if (host != null) host.navigateToTab(ProjectFragmentAdapter.TAB_TASKS);
        });
        binding.llInProgress.setOnClickListener(v ->
        {
            if (host != null) host.navigateToTab(ProjectFragmentAdapter.TAB_TASKS);
        });
        binding.llDone.setOnClickListener(v ->
        {
            if (host != null) host.navigateToTab(ProjectFragmentAdapter.TAB_TASKS);
        });
        binding.llOverdue.setOnClickListener(v ->
        {
            if (host != null) host.navigateToTab(ProjectFragmentAdapter.TAB_TASKS);
        });

        binding.btnOverviewAddTask.setOnClickListener(v -> showAddTaskDialog());
    }

    // ---- Edit Project Dialog ----
    private void showEditProjectDialog()
    {
        if (host == null) return;
        Project p = host.getProject();

        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_project, null);

        EditText etName  = dialogView.findViewById(R.id.et_project_name);
        EditText etTag   = dialogView.findViewById(R.id.et_project_tag);
        EditText etDesc  = dialogView.findViewById(R.id.et_project_description);
        EditText etAttach = dialogView.findViewById(R.id.et_attachment);

        etName.setText(p.getName());
        etTag.setText(p.getTag());
        etDesc.setText(p.getDescription());

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.edit_project_title))
                .setView(dialogView)
                .setPositiveButton(getString(R.string.save), null)
                .setNegativeButton(getString(R.string.cancel), null)
                .create();

        dialog.setOnShowListener(di ->
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v ->
                {
                    String name = etName.getText().toString().trim();
                    String tag  = etTag.getText().toString().trim();
                    String desc = etDesc.getText().toString().trim();

                    if (TextUtils.isEmpty(name) || name.length() > 60)
                    {
                        etName.setError("Name required (1–60 chars)");
                        return;
                    }
                    if (TextUtils.isEmpty(tag) || tag.length() > 20)
                    {
                        etTag.setError("Tag required (1–20 chars)");
                        return;
                    }
                    if (TextUtils.isEmpty(desc) || desc.length() > 500)
                    {
                        etDesc.setError("Description required (1–500 chars)");
                        return;
                    }

                    p.setName(name);
                    p.setTag(tag);
                    p.setDescription(desc);

                    host.onProjectUpdated();
                    refreshUI();
                    dialog.dismiss();
                    Toast.makeText(requireContext(), "Project updated", Toast.LENGTH_SHORT).show();
                })
        );
        dialog.show();
    }

    // ---- Add Task Dialog ----
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

        ArrayAdapter<String> priorityAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item,
                new String[]{Task.PRIORITY_NORMAL, Task.PRIORITY_HIGH, Task.PRIORITY_LOW});
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPriority.setAdapter(priorityAdapter);

        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item,
                new String[]{Task.STATUS_TODO, Task.STATUS_IN_PROGRESS, Task.STATUS_DONE});
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spStatus.setAdapter(statusAdapter);

        etDesc.addTextChangedListener(new TextWatcher()
        {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count)
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
                    refreshUI();
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
