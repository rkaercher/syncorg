package com.coste.syncorg.gui.outline;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;

import com.coste.syncorg.MainActivity;
import com.coste.syncorg.R;
import com.coste.syncorg.orgdata.OrgContract;
import com.coste.syncorg.orgdata.OrgFile;
import com.coste.syncorg.orgdata.SyncOrgApplication;
import com.coste.syncorg.synchronizers.JGitWrapper;
import com.coste.syncorg.synchronizers.Synchronizer;
import com.coste.syncorg.util.OrgFileNotFoundException;
import com.coste.syncorg.util.OrgUtils;

import javax.inject.Inject;

public class ConflictResolverActivity extends AppCompatActivity {

    EditText editText;
    String filename;
    Long nodeId;

    @Inject
    Synchronizer synchronizer;

    @Inject
    JGitWrapper gitWrapper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((SyncOrgApplication)getApplication()).getDiComponent().inject(this);
        setContentView(R.layout.activity_conflict_resolver);
        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();


        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            nodeId = getIntent().getLongExtra(OrgContract.NODE_ID, -1);

            editText = (EditText) findViewById(R.id.conflict_resolver_text);
            try {
                OrgFile file = new OrgFile(nodeId, getContentResolver());
                if (actionBar != null) {
                    actionBar.setTitle(file.name);
                }

                String dir = synchronizer.getAbsoluteFilesDir();
                this.filename = dir + "/" + file.filename;
                editText.setText(OrgUtils.readAll(this.filename));

            } catch (OrgFileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.edit_node_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit_menu_cancel:
                NavUtils.navigateUpTo(this, new Intent(this, MainActivity.class));
                return true;
            case R.id.edit_menu_ok:
                if (this.filename != null && !this.filename.equals("")) {
                    OrgUtils.writeToFile(this.filename, editText.getText().toString());
                    gitWrapper.createMergeTask(this, this.filename, synchronizer.getAbsoluteFilesDir()).execute();
                    OrgFile f = null;
                    try {
                        f = new OrgFile(nodeId, this.getContentResolver());
                        ContentValues values = new ContentValues();
                        values.put("comment", "");
                        f.updateFileInDB(this.getContentResolver(), values);
                    } catch (OrgFileNotFoundException e) {
                        e.printStackTrace();
                    }

                }
                NavUtils.navigateUpTo(this, new Intent(this, MainActivity.class));
                return true;
        }
        return false;
    }


}
