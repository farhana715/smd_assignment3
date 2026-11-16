package com.example.myapplication;

import android.content.res.Resources;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.databinding.FragmentFilesBinding;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class FilesFragment extends Fragment
{
    private static final String ARG_PROJECT_ID = "project_id";

    private FragmentFilesBinding binding;
    private OverviewFragment.ProjectHost host;
    private AttachmentAdapter attachmentAdapter;
    private final List<AttachmentLink> displayedFiles = new ArrayList<>();

    private String searchQuery = "";
    private String typeFilter  = "All"; // All / Images / Text

    public static FilesFragment newInstance(int projectId)
    {
        FilesFragment f = new FilesFragment();
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
        binding = FragmentFilesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        setupSearch();
        setupChips();
        applyFilters();

        binding.btnAddAttachment.setOnClickListener(v -> showAddAttachmentDialog());
        binding.btnEmptyAddAttachment.setOnClickListener(v -> showAddAttachmentDialog());
    }

    @Override
    public void onResume()
    {
        super.onResume();
        applyFilters();
    }

    private void setupRecyclerView()
    {
        attachmentAdapter = new AttachmentAdapter(displayedFiles, new AttachmentAdapter.FileActionListener()
        {
            @Override
            public void onPreview(AttachmentLink link)
            {
                FilesFragment.this.onPreview(link);
            }

            @Override
            public void onRemove(AttachmentLink link, int adapterPosition)
            {
                FilesFragment.this.showRemoveDialog(link, adapterPosition);
            }
        });

        binding.rvFiles.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvFiles.setAdapter(attachmentAdapter);

        // ItemTouchHelper: Swipe RIGHT = remove with name-confirmation
        ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(
                0, ItemTouchHelper.RIGHT)
        {
            @Override
            public boolean onMove(@NonNull RecyclerView rv,
                                  @NonNull RecyclerView.ViewHolder from,
                                  @NonNull RecyclerView.ViewHolder to) { return false; }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder vh, int direction)
            {
                int position = vh.getBindingAdapterPosition();
                if (position < 0 || position >= displayedFiles.size())
                {
                    attachmentAdapter.notifyItemChanged(position);
                    return;
                }
                AttachmentLink link = displayedFiles.get(position);
                showRemoveDialog(link, position);
            }
        };

        new ItemTouchHelper(callback).attachToRecyclerView(binding.rvFiles);
    }

    private void setupSearch()
    {
        binding.etFileSearch.addTextChangedListener(new TextWatcher()
        {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c)
            { searchQuery = s.toString().trim(); applyFilters(); }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void setupChips()
    {
        binding.chipAll.setOnClickListener(v -> setTypeFilter("All"));
        binding.chipImages.setOnClickListener(v -> setTypeFilter("Images"));
        binding.chipText.setOnClickListener(v -> setTypeFilter("Text"));
    }

    private void setTypeFilter(String filter)
    {
        typeFilter = filter;
        updateChipStyles();
        applyFilters();
    }

    private void updateChipStyles()
    {
        // Active chip = teal, inactive = bg_chip
        boolean all    = "All".equals(typeFilter);
        boolean images = "Images".equals(typeFilter);
        boolean text   = "Text".equals(typeFilter);

        binding.chipAll.setBackgroundResource(all    ? R.drawable.bg_button_teal : R.drawable.bg_chip);
        binding.chipAll.setTextColor(ContextCompat.getColor(requireContext(),
                all    ? R.color.text_white : R.color.chip_text));

        binding.chipImages.setBackgroundResource(images ? R.drawable.bg_button_teal : R.drawable.bg_chip);
        binding.chipImages.setTextColor(ContextCompat.getColor(requireContext(),
                images ? R.color.text_white : R.color.chip_text));

        binding.chipText.setBackgroundResource(text  ? R.drawable.bg_button_teal : R.drawable.bg_chip);
        binding.chipText.setTextColor(ContextCompat.getColor(requireContext(),
                text   ? R.color.text_white : R.color.chip_text));
    }

    public void applyFilters()
    {
        if (host == null || binding == null) return;
        Project p = host.getProject();
        List<AttachmentLink> all = p.getAttachmentLinks();

        List<AttachmentLink> filtered = new ArrayList<>();
        for (AttachmentLink a : all)
        {
            if (!searchQuery.isEmpty() &&
                !a.getDisplayName().toLowerCase().contains(searchQuery.toLowerCase()))
                continue;
            if ("Images".equals(typeFilter) && !a.isImage()) continue;
            if ("Text".equals(typeFilter)   && !a.isText())  continue;
            filtered.add(a);
        }

        displayedFiles.clear();
        displayedFiles.addAll(filtered);
        attachmentAdapter.notifyDataSetChanged();

        // Update chip labels with total counts
        int totalAll    = all.size();
        int totalImages = 0, totalText = 0;
        for (AttachmentLink a : all)
        {
            if (a.isImage()) totalImages++;
            else             totalText++;
        }
        binding.chipAll.setText("All (" + totalAll + ")");
        binding.chipImages.setText("Images (" + totalImages + ")");
        binding.chipText.setText("Text (" + totalText + ")");
        updateChipStyles();

        // Empty states
        if (all.isEmpty())
        {
            binding.llEmptyNoFiles.setVisibility(View.VISIBLE);
            binding.tvEmptyNoFileMatch.setVisibility(View.GONE);
            binding.rvFiles.setVisibility(View.GONE);
        }
        else if (filtered.isEmpty())
        {
            binding.llEmptyNoFiles.setVisibility(View.GONE);
            binding.tvEmptyNoFileMatch.setVisibility(View.VISIBLE);
            binding.rvFiles.setVisibility(View.GONE);
        }
        else
        {
            binding.llEmptyNoFiles.setVisibility(View.GONE);
            binding.tvEmptyNoFileMatch.setVisibility(View.GONE);
            binding.rvFiles.setVisibility(View.VISIBLE);
        }
    }

    // ---- Preview ----
    private void onPreview(AttachmentLink link)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext())
                .setTitle(link.getDisplayName())
                .setPositiveButton("Close", null);

        if (link.isImage())
        {
            ImageView iv = new ImageView(requireContext());
            try
            {
                iv.setImageResource(link.getResourceId());
            }
            catch (Exception e)
            {
                iv.setImageResource(R.drawable.attach_image1);
            }
            iv.setAdjustViewBounds(true);
            iv.setPadding(16, 16, 16, 16);
            builder.setView(iv);
        }
        else
        {
            TextView tv = new TextView(requireContext());
            tv.setPadding(32, 32, 32, 32);
            tv.setTextSize(13);
            try
            {
                Resources res = requireContext().getResources();
                InputStream is = res.openRawResource(link.getResourceId());
                byte[] bytes = new byte[is.available()];
                //noinspection ResultOfMethodCallIgnored
                is.read(bytes);
                is.close();
                tv.setText(new String(bytes));
            }
            catch (Exception e)
            {
                tv.setText("Could not load file content.");
            }
            builder.setView(tv);
        }
        builder.show();
    }

    // ---- Remove (name-confirmation dialog) ----
    private void showRemoveDialog(AttachmentLink link, int adapterPosition)
    {
        EditText etConfirm = new EditText(requireContext());
        etConfirm.setHint("Type \"" + link.getDisplayName() + "\" to confirm");

        new AlertDialog.Builder(requireContext())
                .setTitle("Remove Attachment")
                .setMessage("Type the attachment name to confirm removal:")
                .setView(etConfirm)
                .setPositiveButton("Remove", (d, w) ->
                {
                    String typed = etConfirm.getText().toString().trim();
                    if (!typed.equals(link.getDisplayName()))
                    {
                        Toast.makeText(requireContext(), "Name doesn't match", Toast.LENGTH_SHORT).show();
                        attachmentAdapter.notifyItemChanged(adapterPosition);
                        return;
                    }
                    host.getProject().getAttachmentLinks().remove(link);
                    host.onProjectUpdated();
                    applyFilters();
                    Toast.makeText(requireContext(), "Attachment removed", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", (d, w) ->
                        attachmentAdapter.notifyItemChanged(adapterPosition))
                .setOnCancelListener(d ->
                        attachmentAdapter.notifyItemChanged(adapterPosition))
                .show();
    }

    // ---- Add Attachment ----
    private void showAddAttachmentDialog()
    {
        if (host == null) return;
        Project p = host.getProject();
        List<Integer> linkedCatalogIds = new ArrayList<>();
        for (AttachmentLink a : p.getAttachmentLinks())
            linkedCatalogIds.add(a.getCatalogId());

        List<AttachmentCatalog.CatalogItem> unlinked = new ArrayList<>();
        for (AttachmentCatalog.CatalogItem item : AttachmentCatalog.getAll())
            if (!linkedCatalogIds.contains(item.id))
                unlinked.add(item);

        if (unlinked.isEmpty())
        {
            Toast.makeText(requireContext(), "All catalog items are already linked!", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] names = new String[unlinked.size()];
        for (int i = 0; i < unlinked.size(); i++) names[i] = unlinked.get(i).displayName;

        final int[] selectedIndex = {-1};

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Add Attachment")
                .setSingleChoiceItems(names, -1, (d, which) -> selectedIndex[0] = which)
                .setPositiveButton("Add", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(di ->
        {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            dialog.getListView().setOnItemClickListener((parent, v, pos, id) ->
            {
                selectedIndex[0] = pos;
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
            });
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v ->
            {
                if (selectedIndex[0] < 0) return;
                AttachmentCatalog.CatalogItem item = unlinked.get(selectedIndex[0]);
                AttachmentLink link = new AttachmentLink(
                        p.getNextAttachmentId(), item.id, item.displayName,
                        item.type, item.resourceId);
                p.getAttachmentLinks().add(link);
                host.onProjectUpdated();
                applyFilters();
                dialog.dismiss();
                Toast.makeText(requireContext(), "Attachment added", Toast.LENGTH_SHORT).show();
            });
        });
        dialog.show();
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        binding = null;
    }
}
