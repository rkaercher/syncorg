package com.coste.syncorg.gui.outline;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.coste.syncorg.AgendaFragment;
import com.coste.syncorg.OrgNodeDetailActivity;
import com.coste.syncorg.OrgNodeDetailFragment;
import com.coste.syncorg.R;
import com.coste.syncorg.dao.OrgFileDao;
import com.coste.syncorg.gui.theme.DefaultTheme;
import com.coste.syncorg.orgdata.OrgContract;
import com.coste.syncorg.orgdata.OrgNode;
import com.coste.syncorg.orgdata.SyncOrgApplication;
import com.coste.syncorg.orgdata.table.FileEntity;
import com.coste.syncorg.synchronizers.Synchronizer;
import com.coste.syncorg.util.OrgNodeNotFoundException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;


public class MainAdapter extends RecyclerView.Adapter<MainAdapter.OutlineItem> {
    private final AppCompatActivity activity;
    public List<FileEntity> items = new ArrayList<>();
    // Number of added items. Here it is two: Agenda and Todos.
    private int numExtraItems;
    private ActionMode actionMode;
    private boolean mTwoPanes = false;
    private SparseBooleanArray selectedItems;
    private boolean calendarEnabled;
    private int positionTodo;
    private int positionCalendar;


    private DefaultTheme theme;
    private ActionMode.Callback mDeleteMode = new ActionMode.Callback() {
        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            String wordItem;
            int count = getSelectedItemCount();
            if (count == 1) wordItem = activity.getResources().getString(R.string.file);
            else wordItem = activity.getResources().getString(R.string.files);
//            menu.findItem(R.id.action_text).setTitle(count + " " + wordItem);
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {
            MainAdapter.this.clearSelections();
        }

        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            MenuInflater inflater = activity.getMenuInflater();
            inflater.inflate(R.menu.main_context_action_bar, menu);

            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.item_delete:
                    String message;
                    int numSelectedItems = getSelectedItemCount();
                    if (numSelectedItems == 1)
                        message = activity.getResources().getString(R.string.prompt_delete_file);
                    else {
                        message = activity.getResources().getString(R.string.prompt_delete_files);
                        message = message.replace("#", String.valueOf(numSelectedItems));
                    }

                    new AlertDialog.Builder(activity)
                            .setMessage(message)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    deleteSelectedFiles();
                                }
                            })
                            .setNegativeButton(android.R.string.no, null).show();
                    return true;
                case R.id.share_files:

                    Intent emailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
// set the type to 'email'
                    emailIntent.setType("vnd.android.cursor.dir/email");
                    emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    List<Integer> selectedItems = getSelectedItems();
                    ArrayList<Uri> uris = new ArrayList<>();
                    for (Integer num : selectedItems) {
                        num -= numExtraItems;
                        FileEntity file = items.get(num);
                        File f = new File(file.getFilePath());
                        Uri fileUri = FileProvider.getUriForFile(activity, "com.coste.fileprovider", new File(f.getPath()));
                        uris.add(fileUri);
                    }

                    emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);

// the mail subject
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject");
                    activity.startActivity(Intent.createChooser(emailIntent, "Send email..."));

            }
            return false;
        }
    };

    @Inject
    Synchronizer synchronizer;


    @Inject
    OrgFileDao fileDao;

    public MainAdapter(AppCompatActivity activity) {
        super();
        this.activity = activity;

        ((SyncOrgApplication) this.activity.getApplication()).getDiComponent().inject(this);

        this.theme = DefaultTheme.getTheme(activity);
        selectedItems = new SparseBooleanArray();
        refresh();
    }

    public void refresh() {
        clear();

        for (FileEntity file : fileDao.getFiles()) {
            add(file);
        }


        // Display calendar if enabled in Settings
        SharedPreferences appPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
        calendarEnabled = appPreferences.getBoolean("calendarEnabled", false);
        positionTodo = 0;
        positionCalendar = calendarEnabled ? 1 : -1;
        numExtraItems = calendarEnabled ? 2 : 1;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return items.size() + numExtraItems;
    }

    @Override
    public OutlineItem onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.outline_item, parent, false);
        return new OutlineItem(v);
    }

    @Override
    public void onBindViewHolder(final OutlineItem holder, final int position) {
        final int positionInItems = position - numExtraItems;
        FileEntity file = null;
        try {
            file = items.get(positionInItems);
        } catch (ArrayIndexOutOfBoundsException ignored) {
        }
        final boolean conflict = false; //// TODO: 29.03.17 re add  //(file != null && file.getState() == OrgFileOld.State.kConflict);
        String title;
        if (position == 0) {
            title = activity.getResources().getString(R.string.menu_todos);
        } else if (position == positionCalendar) {
            title = activity.getResources().getString(R.string.menu_agenda);
        } else {
            title = items.get(positionInItems).getDisplayName();
        }

        holder.titleView.setText(title);

        TextView comment = (TextView) holder.mView.findViewById(R.id.comment);

        if (conflict) {
            comment.setText(R.string.conflict);
            comment.setVisibility(View.VISIBLE);
        } else {
            comment.setVisibility(View.GONE);
        }

        holder.mView.setActivated(selectedItems.get(position, false));

        final long itemId = getItemId(position);
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = v.getContext();

                if (getSelectedItemCount() > 0) {
                    if (!isSelectableItem(position)) return;
                    toggleSelection(position);
                } else {
                    if (mTwoPanes) {
                        Bundle arguments = new Bundle();
                        Intent intent;
                        // Special activity for conflicted file
                        if (conflict) {
                            intent = new Intent(context, ConflictResolverActivity.class);
                            context.startActivity(intent);
                            return;
                        }

                        Fragment fragment;
                        if (position == 0) {
                            arguments.putSerializable(OrgNodeDetailFragment.ARGUMENT_LIST_TYPE, OrgNodeDetailFragment.NodeListType.TODO);
                            fragment = new OrgNodeDetailFragment();
                        } else if (position == positionCalendar) {
                            arguments.putSerializable(OrgNodeDetailFragment.ARGUMENT_LIST_TYPE, OrgNodeDetailFragment.NodeListType.AGENDA);
                            fragment = new AgendaFragment();
                        } else {
                            arguments.putSerializable(OrgNodeDetailFragment.ARGUMENT_LIST_TYPE, OrgNodeDetailFragment.NodeListType.FILE);
                            arguments.putLong(OrgNodeDetailFragment.ARGUMENT_NODE_OR_FILE_ID, itemId);
                            fragment = new OrgNodeDetailFragment();
                        }

                        AppCompatActivity activity = (AppCompatActivity) v.getContext();
                        activity.getSupportFragmentManager().beginTransaction()
                                .replace(R.id.orgnode_detail_container, fragment)
                                .commit();

                        ActionBar actionBar = ((AppCompatActivity) context).getSupportActionBar();
                        if (actionBar != null) {
                            if (arguments.getLong(OrgContract.NODE_ID) == OrgContract.TODO_ID)
                                actionBar.setTitle(context.getResources().getString(R.string.menu_todos));
                            else if (arguments.getLong(OrgContract.NODE_ID) == OrgContract.AGENDA_ID) {
                                actionBar.setTitle(context.getResources().getString(R.string.menu_agenda));
                            } else {
                                try {
                                    OrgNode node = new OrgNode(arguments.getLong(OrgContract.NODE_ID), context.getContentResolver());
                                    actionBar.setTitle(node.getDisplayName());
                                } catch (OrgNodeNotFoundException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    } else {
                        Intent intent;

                        // Special activity for conflicted file
                        if (conflict) {
                            intent = new Intent(context, ConflictResolverActivity.class);
                        } else {
                            intent = new Intent(context, OrgNodeDetailActivity.class);
                        }


                        if (position == 0) {
                            intent.putExtra(OrgNodeDetailFragment.ARGUMENT_LIST_TYPE, OrgNodeDetailFragment.NodeListType.TODO);
                        } else if (position == positionCalendar) {
                            intent.putExtra(OrgNodeDetailFragment.ARGUMENT_LIST_TYPE, OrgNodeDetailFragment.NodeListType.AGENDA);
                        } else {
                            intent.putExtra(OrgNodeDetailFragment.ARGUMENT_LIST_TYPE, OrgNodeDetailFragment.NodeListType.FILE);
                            intent.putExtra(OrgNodeDetailFragment.ARGUMENT_NODE_OR_FILE_ID, itemId);
                        }

                        context.startActivity(intent);
                    }
                }
            }
        });


        holder.mView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // Do nothing when user select Agenda.org or Todos.org

                if (!isSelectableItem(position)) return true;
                toggleSelection(position);
                return true;
            }
        });

    }

    private boolean isSelectableItem(int position) {
        if (position == positionCalendar || position == positionTodo) {
            String text = activity.getResources().getString(R.string.unselectable_item);
            Toast.makeText(activity, text, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    public void clear() {
        this.items.clear();
    }

    public void add(FileEntity file) {
        this.items.add(file);
    }

    @Override
    public long getItemId(int position) {
        if (position < numExtraItems) return -1;
        FileEntity file = items.get(position - numExtraItems);
        return file.getId(); // // TODO: 29.03.17 was nodeId
    }

    private void toggleSelection(int pos) {
        int countBefore = getSelectedItemCount();
        if (selectedItems.get(pos, false)) {
            selectedItems.delete(pos);
        } else {
            selectedItems.put(pos, true);
        }

        notifyItemChanged(pos);
        int countAfter = getSelectedItemCount();
        if (countBefore == 0 && countAfter > 0)
            actionMode = activity.startSupportActionMode(mDeleteMode);
        if (countAfter == 0 && actionMode != null)
            actionMode.finish();
        if (countAfter > 0 && actionMode != null) {
            actionMode.invalidate();
        }
    }

    private void clearSelections() {
        selectedItems.clear();
        notifyDataSetChanged();
    }

    private int getSelectedItemCount() {
        return selectedItems.size();
    }

    private List<Integer> getSelectedItems() {
        List<Integer> items =
                new ArrayList<>(selectedItems.size());
        for (int i = 0; i < selectedItems.size(); i++) {
            items.add(selectedItems.keyAt(i));
        }
        return items;
    }

    private void deleteSelectedFiles() {
        List<Integer> selectedItems = getSelectedItems();
        for (Integer num : selectedItems) {
            num -= numExtraItems;
            FileEntity file = items.get(num);
            //    file.removeFile(activity, true); //// TODO: 29.03.17 reove with  dao
        }
        synchronizer.runSynchronize();
        refresh();
        actionMode.finish();
    }

    public void setHasTwoPanes(boolean _hasTwoPanes) {
        mTwoPanes = _hasTwoPanes;
    }

    class OutlineItem extends RecyclerView.ViewHolder {
        final View mView;
        TextView titleView;

        OutlineItem(View view) {
            super(view);
            mView = view;
            titleView = (TextView) view.findViewById(R.id.title);

        }
    }

}
