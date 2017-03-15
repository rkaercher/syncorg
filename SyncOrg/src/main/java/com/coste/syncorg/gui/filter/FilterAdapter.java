package com.coste.syncorg.gui.filter;

import android.app.Activity;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.coste.syncorg.R;
import com.coste.syncorg.orgdata.OrgFile;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


class FilterAdapter extends ArrayAdapter<OrgFile> implements CompoundButton.OnCheckedChangeListener {

    private Activity context;
    private Set<Long> selectedIds = new HashSet<>();

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (b) {
            selectedIds.add((Long) compoundButton.getTag());
        } else {
            selectedIds.remove(compoundButton.getTag());
        }
       // context.setTitle("changed" + compoundButton.getTag());
    }

    private static class ViewHolder {
        CheckBox checkBox;
    }

    FilterAdapter(@NonNull Activity context, @LayoutRes int resource, @NonNull List<OrgFile> objects, Set<Long> selectedIds) {
        super(context, resource, objects);
        this.context = context;
        this.selectedIds = selectedIds;
    }

    Set<Long> getSelectedIds() {
        return selectedIds;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View rowView = createOrReuseRowView(convertView);
        ViewHolder holder = (ViewHolder) rowView.getTag();
        OrgFile file = getItem(position);
        if (file != null) {
            holder.checkBox.setText(file.name);
            holder.checkBox.setTag(file.id);
            holder.checkBox.setChecked(selectedIds.contains(file.id));
        }
        return rowView;
    }

    private View createOrReuseRowView(@Nullable View convertView) {
        View rowView = convertView;
        if (rowView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            rowView = inflater.inflate(R.layout.filter_item, null);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.checkBox = (CheckBox) rowView.findViewById(R.id.checkBox);
            rowView.setTag(viewHolder);
            viewHolder.checkBox.setOnCheckedChangeListener(this);
        }
        return rowView;
    }
}
