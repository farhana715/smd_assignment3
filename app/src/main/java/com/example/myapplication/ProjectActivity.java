package com.example.myapplication;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import com.example.myapplication.databinding.ActivityProjectBinding;

import java.util.ArrayList;

public class ProjectActivity extends AppCompatActivity
        implements OverviewFragment.ProjectHost
{
    public static final String EXTRA_PROJECT          = "extra_project";
    public static final String EXTRA_PROJECT_POSITION = "extra_project_position";
    public static final String EXTRA_UPDATED_PROJECT  = "extra_updated_project";

    private ActivityProjectBinding binding;
    private Project project;
    private int     projectPosition;
    private ProjectFragmentAdapter fragmentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        binding = ActivityProjectBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        project         = getProjectFromIntent();
        projectPosition = getIntent().getIntExtra(EXTRA_PROJECT_POSITION, -1);

        if (project == null) { finish(); return; }

        // Ensure lists non-null
        if (project.getAttachmentLinks() == null)
            project.setAttachmentLinks(new ArrayList<>());

        updateHeader();
        setupTabs();

        binding.btnBack.setOnClickListener(v -> returnUpdatedProject());
        binding.btnInviteProject.setEnabled(false);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true)
        {
            @Override public void handleOnBackPressed() { returnUpdatedProject(); }
        });
    }

    // ---- ProjectHost implementation ----
    @Override
    public Project getProject() { return project; }

    @Override
    public void onProjectUpdated()
    {
        updateHeader();
        updateTabBadges();
    }

    @Override
    public void navigateToTab(int tabIndex)
    {
        binding.viewPager.setCurrentItem(tabIndex, true);
    }

    // ---- Header ----
    private void updateHeader()
    {
        binding.tvProjectHeaderName.setText(project.getName());
        binding.tvProjectHeaderTag.setText(project.getTag());
    }

    // ---- Tabs ----
    private void setupTabs()
    {
        fragmentAdapter = new ProjectFragmentAdapter(this, project.getId());
        binding.viewPager.setAdapter(fragmentAdapter);

        new TabLayoutMediator(binding.tabLayout, binding.viewPager,
                (tab, position) ->
                {
                    switch (position)
                    {
                        case ProjectFragmentAdapter.TAB_OVERVIEW:
                            tab.setText(getString(R.string.tab_overview));
                            break;
                        case ProjectFragmentAdapter.TAB_TASKS:
                            tab.setText(getString(R.string.tab_tasks) +
                                    " (" + project.getTaskCount() + ")");
                            break;
                        case ProjectFragmentAdapter.TAB_FILES:
                            tab.setText(getString(R.string.tab_files) +
                                    " (" + project.getAttachmentLinkCount() + ")");
                            break;
                        case ProjectFragmentAdapter.TAB_DISCUSSION:
                            tab.setText(getString(R.string.tab_discussion) +
                                    " (" + project.getCommentCount() + ")");
                            break;
                    }
                }
        ).attach();

        // Refresh tab labels when page changes
        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback()
        {
            @Override
            public void onPageSelected(int position)
            {
                super.onPageSelected(position);
                updateTabBadges();
            }
        });
    }

    private void updateTabBadges()
    {
        TabLayout.Tab tasks      = binding.tabLayout.getTabAt(ProjectFragmentAdapter.TAB_TASKS);
        TabLayout.Tab files      = binding.tabLayout.getTabAt(ProjectFragmentAdapter.TAB_FILES);
        TabLayout.Tab discussion = binding.tabLayout.getTabAt(ProjectFragmentAdapter.TAB_DISCUSSION);

        if (tasks      != null) tasks.setText(getString(R.string.tab_tasks) + " (" + project.getTaskCount() + ")");
        if (files      != null) files.setText(getString(R.string.tab_files) + " (" + project.getAttachmentLinkCount() + ")");
        if (discussion != null) discussion.setText(getString(R.string.tab_discussion) + " (" + project.getCommentCount() + ")");
    }

    // ---- Return result ----
    private void returnUpdatedProject()
    {
        Intent resultIntent = new Intent();
        resultIntent.putExtra(EXTRA_UPDATED_PROJECT, project);
        resultIntent.putExtra(EXTRA_PROJECT_POSITION, projectPosition);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    @SuppressWarnings("deprecation")
    private Project getProjectFromIntent()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            return getIntent().getParcelableExtra(EXTRA_PROJECT, Project.class);
        return getIntent().getParcelableExtra(EXTRA_PROJECT);
    }
}
