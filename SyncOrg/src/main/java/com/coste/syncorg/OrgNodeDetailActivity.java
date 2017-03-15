package com.coste.syncorg;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.coste.syncorg.gui.filter.FilterActivity;
import com.coste.syncorg.orgdata.OrgContract;
import com.coste.syncorg.orgdata.OrgNode;
import com.coste.syncorg.util.OrgNodeNotFoundException;

/**
 * An activity representing a single OrgNode detail screen. This
 * activity is only used narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link MainActivity}.
 */
public class OrgNodeDetailActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orgnode_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            Long nodeId = getNodeId();

            arguments.putLong(OrgContract.NODE_ID, nodeId);
            arguments.putLong(OrgContract.POSITION, getIntent().getLongExtra(OrgContract.POSITION, -1));
            Fragment fragment;

            if (isAgendaSpecialNode(nodeId)) fragment = new AgendaFragment();
            else fragment = new OrgNodeDetailFragment();

            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.orgnode_detail_container, fragment, "detail_fragment")
                    .commit();

            if (actionBar != null) {
                if (nodeId == OrgContract.TODO_ID)
                    actionBar.setTitle(getResources().getString(R.string.menu_todos));
                else if (isAgendaSpecialNode(nodeId)) {
                    actionBar.setTitle(getResources().getString(R.string.menu_agenda));
                } else {
                    try {
                        OrgNode node = new OrgNode(nodeId, getContentResolver());
                        actionBar.setTitle(node.name);
                    } catch (OrgNodeNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private boolean isAgendaSpecialNode(Long nodeId) {
        return nodeId == OrgContract.AGENDA_ID;
    }

    private long getNodeId() {
        return getIntent().getLongExtra(OrgContract.NODE_ID, -1);
    }

    @Override
    public void onBackPressed() {
        // Writing changes currently done in the fragment EditNodeFragment if any
        EditNodeFragment fragment = (EditNodeFragment) getSupportFragmentManager().findFragmentByTag("edit_node_fragment");
        if (fragment != null) {
            fragment.onOKPressed();
        }

        // code here to show dialog
        super.onBackPressed();  // optional depending on your needs
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (isAgendaSpecialNode(getNodeId())) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.agenda_list, menu);
            return true;
        }

        return super.onCreateOptionsMenu(menu);
    }

    private void showUpgradePopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Magic happened!");
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // This ID represents the Home or Up button. In the case of this
                // activity, the Up button is shown. Use NavUtils to allow users
                // to navigate up one level in the application structure. For
                // more details, see the Navigation pattern on Android Design:
                //
                // http://developer.android.com/design/patterns/navigation.html#up-vs-back
                //
                NavUtils.navigateUpTo(this, new Intent(this, MainActivity.class));
                return true;
            case R.id.menu_filter:
                //showUpgradePopup();
                Intent intent = new Intent(this, FilterActivity.class);
                startActivity(intent);
                return true;

//            case R.id.delete_node_button:
//                OrgNodeDetailFragment fragment = (OrgNodeDetailFragment) getSupportFragmentManager().findFragmentByTag("detail_fragment");
//                if(fragment != null){
//                }
//                return true;
        }
//        return super.onOptionsItemSelected(item);
        return false;
    }


}
