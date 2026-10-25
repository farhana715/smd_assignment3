package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ProjectFragmentAdapter extends FragmentStateAdapter
{
    public static final int TAB_OVERVIEW    = 0;
    public static final int TAB_TASKS       = 1;
    public static final int TAB_FILES       = 2;
    public static final int TAB_DISCUSSION  = 3;

    private final int projectId;

    public ProjectFragmentAdapter(@NonNull FragmentActivity fragmentActivity, int projectId)
    {
        super(fragmentActivity);
        this.projectId = projectId;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position)
    {
        switch (position)
        {
            case TAB_OVERVIEW:   return OverviewFragment.newInstance(projectId);
            case TAB_TASKS:      return TasksFragment.newInstance(projectId);
            case TAB_FILES:      return FilesFragment.newInstance(projectId);
            case TAB_DISCUSSION: return DiscussionFragment.newInstance();
            default:             return DiscussionFragment.newInstance();
        }
    }

    @Override
    public int getItemCount() { return 4; }
}
