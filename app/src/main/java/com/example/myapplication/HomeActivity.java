package com.example.myapplication;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.databinding.ActivityHomeBinding;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity
{
    private ActivityHomeBinding binding;
    private ProjectListAdapter  projectListAdapter;

    /** Master list — NEVER cleared by adapter operations */
    private final List<Project> projectList = new ArrayList<>();
    private int nextProjectId = 1;

    /** Current search query — kept so we can re-filter after updates */
    private String currentQuery = "";

    private final ActivityResultLauncher<Intent> projectLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result ->
                    {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null)
                        {
                            Intent data    = result.getData();
                            Project updated = getParcelableFromIntent(data,
                                    ProjectActivity.EXTRA_UPDATED_PROJECT);

                            if (updated != null)
                            {
                                // Update the master list by project ID (not by position)
                                for (int i = 0; i < projectList.size(); i++)
                                {
                                    if (projectList.get(i).getId() == updated.getId())
                                    {
                                        projectList.set(i, updated);
                                        break;
                                    }
                                }
                                // Rebuild adapter display from master list
                                filterProjects(currentQuery);
                            }
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupListView();
        loadSampleProject();
        setupSearch();

        binding.btnNewProject.setOnClickListener(v -> showAddProjectDialog());
    }

    // ---- ListView setup ----
    private void setupListView()
    {
        // IMPORTANT: pass a NEW empty list — the adapter owns its own display list,
        // separate from projectList (the master list). This prevents clear() from
        // wiping projectList.
        projectListAdapter = new ProjectListAdapter(this, new ArrayList<>());
        binding.lvProjects.setAdapter(projectListAdapter);

        binding.lvProjects.setOnItemClickListener((parent, view, position, id) ->
        {
            Project project = projectListAdapter.getItem(position);
            if (project == null) return;

            // Find position in MASTER list by ID (safe even when filtered)
            int masterPosition = -1;
            for (int i = 0; i < projectList.size(); i++)
            {
                if (projectList.get(i).getId() == project.getId())
                {
                    masterPosition = i;
                    break;
                }
            }

            Intent intent = new Intent(HomeActivity.this, ProjectActivity.class);
            intent.putExtra(ProjectActivity.EXTRA_PROJECT, project);
            intent.putExtra(ProjectActivity.EXTRA_PROJECT_POSITION, masterPosition);
            projectLauncher.launch(intent);
        });
    }

    // ---- Search ----
    private void setupSearch()
    {
        binding.etSearchProjects.addTextChangedListener(new TextWatcher()
        {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c)
            {
                currentQuery = s.toString().trim();
                filterProjects(currentQuery);
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    /**
     * Rebuilds the adapter's display list from projectList (master).
     * projectList is NEVER modified here — only the adapter's internal list is.
     */
    private void filterProjects(String query)
    {
        projectListAdapter.clear();             // clears adapter's OWN list, NOT projectList
        if (query.isEmpty())
        {
            projectListAdapter.addAll(projectList); // copies references into adapter list
        }
        else
        {
            String lower = query.toLowerCase();
            for (Project p : projectList)
            {
                if (p.getName().toLowerCase().contains(lower) ||
                    p.getTag().toLowerCase().contains(lower))
                    projectListAdapter.add(p);
            }
        }
        projectListAdapter.notifyDataSetChanged();
    }

    // ---- Sample data ----
    private void loadSampleProject()
    {
        ArrayList<Task> tasks = new ArrayList<>();
        tasks.add(new Task(1, "Create brand guideline for website design",
                Task.STATUS_DONE, Task.PRIORITY_HIGH, null));
        tasks.add(new Task(2, "Create low fidelity service page website",
                Task.STATUS_IN_PROGRESS, Task.PRIORITY_NORMAL, "2026-11-10"));
        tasks.add(new Task(3, "Design hero banner",
                Task.STATUS_TODO, Task.PRIORITY_NORMAL, "2026-11-15"));
        tasks.add(new Task(4, "Build component library",
                Task.STATUS_TODO, Task.PRIORITY_LOW, null));
        for (int i = 5; i <= 12; i++)
            tasks.add(new Task(i, "Task " + i, Task.STATUS_TODO, Task.PRIORITY_NORMAL, null));

        Project sample = new Project(
                nextProjectId++,
                "Service Page Website",
                "User Interface",
                "Make a page display about services for the website company with blue and red colors",
                "sports-project2.fig",
                3,
                tasks,
                AttachmentCatalog.buildDefaultLinks()
        );

        // Add to master list first, THEN refresh the adapter display
        projectList.add(sample);
        filterProjects(currentQuery);   // <-- do NOT call addAll() directly on adapter
    }

    // ---- Add Project Dialog ----
    private void showAddProjectDialog()
    {
        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_add_project, null);

        EditText etName  = dialogView.findViewById(R.id.et_project_name);
        EditText etTag   = dialogView.findViewById(R.id.et_project_tag);
        EditText etAttach= dialogView.findViewById(R.id.et_attachment);
        EditText etDesc  = dialogView.findViewById(R.id.et_project_description);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.add_project_title))
                .setView(dialogView)
                .setPositiveButton(getString(R.string.add_project), null)
                .setNegativeButton(getString(R.string.cancel), null)
                .create();

        dialog.setOnShowListener(di ->
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v ->
                {
                    String name  = etName.getText().toString().trim();
                    String tag   = etTag.getText().toString().trim();
                    String attach= etAttach.getText().toString().trim();
                    String desc  = etDesc.getText().toString().trim();

                    if (TextUtils.isEmpty(name) || name.length() > 60)
                    { etName.setError("Name required (1–60 chars)"); return; }
                    if (TextUtils.isEmpty(tag) || tag.length() > 20)
                    { etTag.setError("Tag required (1–20 chars)"); return; }
                    if (TextUtils.isEmpty(desc) || desc.length() > 500)
                    { etDesc.setError("Description required (1–500 chars)"); return; }

                    ArrayList<AttachmentLink> links = new ArrayList<>();
                    if (!attach.isEmpty())
                    {
                        for (AttachmentCatalog.CatalogItem item : AttachmentCatalog.getAll())
                        {
                            if (item.displayName.equalsIgnoreCase(attach))
                            {
                                links.add(new AttachmentLink(1, item.id, item.displayName,
                                        item.type, item.resourceId));
                                break;
                            }
                        }
                    }

                    Project newProject = new Project(
                            nextProjectId++, name, tag, desc,
                            attach, 0, new ArrayList<>(), links);

                    // 1. Add to master list
                    projectList.add(newProject);

                    // 2. Refresh adapter display (re-applies current search)
                    filterProjects(currentQuery);

                    Toast.makeText(this,
                            "Project \"" + name + "\" added!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                })
        );
        dialog.show();
    }

    @SuppressWarnings("deprecation")
    private Project getParcelableFromIntent(Intent intent, String key)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            return intent.getParcelableExtra(key, Project.class);
        return intent.getParcelableExtra(key);
    }
}
