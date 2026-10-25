package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class ProjectListAdapter extends ArrayAdapter<Project>
{
    private final LayoutInflater inflater;

    public ProjectListAdapter(Context context, List<Project> projects)
    {
        super(context, 0, projects);
        inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        ViewHolder holder;
        if (convertView == null)
        {
            convertView = inflater.inflate(R.layout.item_project, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        }
        else
        {
            holder = (ViewHolder) convertView.getTag();
        }

        Project project = getItem(position);
        if (project == null) return convertView;

        holder.tvTag.setText(project.getTag());
        holder.tvName.setText(project.getName());
        holder.tvDescription.setText(project.getDescription());

        int percent = project.getProgressPercent();
        holder.pbProject.setProgress(percent);
        holder.tvPercent.setText(percent + "%");

        holder.tvTaskCount.setText("✅ " + project.getCompletedTaskCount() + "/" + project.getTaskCount());
        holder.tvAttachmentCount.setText("📎 " + project.getAttachmentLinkCount());
        holder.tvCommentCount.setText("💬 " + project.getCommentCount());

        // Invite btn is UI-only
        holder.btnInvite.setEnabled(false);

        return convertView;
    }

    static class ViewHolder
    {
        TextView    tvTag, tvName, tvDescription, tvPercent;
        TextView    tvTaskCount, tvAttachmentCount, tvCommentCount;
        ProgressBar pbProject;
        Button      btnInvite;

        ViewHolder(View v)
        {
            tvTag             = v.findViewById(R.id.tv_project_tag);
            tvName            = v.findViewById(R.id.tv_project_name);
            tvDescription     = v.findViewById(R.id.tv_project_description);
            tvPercent         = v.findViewById(R.id.tv_progress_percent);
            pbProject         = v.findViewById(R.id.pb_project);
            tvTaskCount       = v.findViewById(R.id.tv_task_count);
            tvAttachmentCount = v.findViewById(R.id.tv_attachment_count);
            tvCommentCount    = v.findViewById(R.id.tv_comment_count);
            btnInvite         = v.findViewById(R.id.btn_invite);
        }
    }
}
