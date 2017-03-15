package com.coste.syncorg.gui.filter;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.coste.syncorg.R;
import com.coste.syncorg.dao.FilterDao;
import com.coste.syncorg.orgdata.NodeFilter;
import com.coste.syncorg.orgdata.OrgProviderUtils;
import com.coste.syncorg.orgdata.SyncOrgApplication;

import java.util.Set;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FilterActivity extends AppCompatActivity {


    @Inject
    FilterDao filterDao;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.filter_list)
    ListView filterList;

    @BindView(R.id.btnOK)
    Button okButton;

    @BindView(R.id.btnCancel)
    Button cancelButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_filter);
        ButterKnife.bind(this);

        ((SyncOrgApplication) getApplication()).getDiComponent().inject(this);

        setSupportActionBar(toolbar);

        filterList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        filterList.setItemsCanFocus(false);

        final NodeFilter nodeFilter = filterDao.loadFilter(NodeFilter.FilterType.AGENDA);
        Set<Long> selectedIds = nodeFilter.getIncludedNodeIds();

        final FilterAdapter adapter = new FilterAdapter(this, R.layout.filter_item, OrgProviderUtils.getFiles(getContentResolver()), selectedIds);
        filterList.setAdapter(adapter);

        setTitle("Select files");

        filterList.getCheckedItemCount();

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String selString = "Saving Seletion";
                Snackbar.make(view, selString, Snackbar.LENGTH_LONG).show();
                nodeFilter.setIncludedNodeIds(adapter.getSelectedIds());
                filterDao.saveFilter(nodeFilter); // TODO check result and show info
                onBackPressed();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

}
