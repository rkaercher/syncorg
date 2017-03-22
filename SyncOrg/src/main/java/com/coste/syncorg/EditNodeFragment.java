package com.coste.syncorg;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TimePicker;

import com.coste.syncorg.orgdata.OrgContract;
import com.coste.syncorg.orgdata.OrgFileOld;
import com.coste.syncorg.orgdata.OrgNode;
import com.coste.syncorg.orgdata.OrgNodeTimeDate;
import com.coste.syncorg.util.FileUtils;
import com.coste.syncorg.util.OrgNodeNotFoundException;
import com.coste.syncorg.util.TodoDialog;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import co.lujun.androidtagview.TagContainerLayout;

public class EditNodeFragment extends Fragment {
    private static final String SSTATE_SCHEDULED_DATE = "scheduled_date";
    private static final String SSTATE_SCHEDULED_TIME = "scheduled_time";
    private static final String SSTATE_DEADLINE_DATE = "deadline_date";
    private static final String SSTATE_DEADLINE_TIME = "deadline_time";

    private Unbinder unbinder;

    static String NODE_ID = "node_id";
    static String PARENT_ID = "parent_id";
    static long nodeId = -1, parentId = -1;

    @BindView(R.id.scheduled_date)
    Button schedule_date;

    @BindView(R.id.deadline_date)
    Button deadline_date;

    @BindView(R.id.scheduled_time)
    Button schedule_time;

    @BindView(R.id.deadline_time)
    Button deadline_time;

    private OrgNode node;


    @BindView(R.id.tcTags)
    TagContainerLayout tagContainerLayout;

    Context context;
    private int position = 0;


    @BindView(R.id.todo)
    Button btnTodo;
    @BindView(R.id.priority)
    Button btnPriority;

    @BindView(R.id.title)
    EditText title;

    @BindView(R.id.content)
    EditText content;

    private void setupTimeStampButtons() {
        OrgNodeTimeDate scheduled = node.getScheduled();
        OrgNodeTimeDate deadline = node.getDeadline();
        if (scheduled.getDate() != null) schedule_date.setText(scheduled.getDateString());
        if (deadline.getDate() != null) deadline_date.setText(deadline.getDateString());
        if (scheduled.getTime() != null) schedule_time.setText(scheduled.getTimeString());
        if (deadline.getTime() != null) deadline_time.setText(deadline.getTimeString());
    }

    static public void createEditNodeFragment(int id, int parentId, int siblingPosition, Context context) {
        Bundle args = new Bundle();
        args.putLong(OrgContract.NODE_ID, id);
        args.putLong(OrgContract.PARENT_ID, parentId);
        args.putInt(OrgContract.OrgData.POSITION, siblingPosition);

        Intent intent = new Intent(context, EditNodeActivity.class);
        intent.putExtras(args);
        context.startActivity(intent);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.edit_node_entry, container, false);
        unbinder = ButterKnife.bind(this, rootView);


        context = getContext();

        Bundle bundle = getArguments();
        if (bundle != null) {
            nodeId = bundle.getLong(NODE_ID, -1);
            parentId = bundle.getLong(PARENT_ID, -1);
            position = bundle.getInt(OrgContract.OrgData.POSITION, 0);
        }

        ContentResolver resolver = getActivity().getContentResolver();

        if (nodeId > -1) {
            // Editing already existing node
            try {
                node = new OrgNode(nodeId, resolver);
            } catch (OrgNodeNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            createNewNode(resolver);
        }


        /**
         * Save user changes (scheduled and time values) if a configuration change occured
         * (like screen rotation or call)
         */
        if (savedInstanceState != null) {
            node.getScheduled().setDate((LocalDate) savedInstanceState.getSerializable(SSTATE_SCHEDULED_DATE));
            node.getScheduled().setTime((LocalTime) savedInstanceState.getSerializable(SSTATE_SCHEDULED_TIME));
            node.getDeadline().setDate((LocalDate) savedInstanceState.getSerializable(SSTATE_DEADLINE_DATE));
            node.getDeadline().setTime((LocalTime) savedInstanceState.getSerializable(SSTATE_DEADLINE_TIME));

            node.todo = savedInstanceState.getString("btnTodo");
            node.priority = savedInstanceState.getString("btnPriority");
        }

        TodoDialog.setupTodoButton(getContext(), node, btnTodo, false);


        btnTodo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TodoDialog(getContext(), node, btnTodo, false);
            }
        });

        btnPriority.setText(node.priority);
        btnPriority.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPriorityDialog();
            }
        });

        title.setText(node.name);
        String payload = node.getCleanedPayload();
        if (payload.length() > 0) {
            content.setText(payload);
        }

        title.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                title.setFocusable(true);
                title.requestFocus();
                return false;
            }
        });

        final LinearLayout layout = (LinearLayout) rootView.findViewById(R.id.view_fragment_layout);
        rootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                layout.requestFocus();
                return true;
            }
        });

        if (!node.getTags().isEmpty()) {
            tagContainerLayout.setTags(node.getTags());
        }

        setupTimeStampButtons();

        getActivity().invalidateOptionsMenu();
        return rootView;
    }

    @OnClick({R.id.scheduled_time, R.id.deadline_time})
    public void onTimeClick(View v) {
        boolean isScheduled = (v.getId() == R.id.scheduled_time);
        final OrgNodeTimeDate time;
        if (isScheduled) {
            time = node.getScheduled();
        } else {
            time = node.getDeadline();
        }
        setupTimeDialog(time.getTime(), new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                time.setTime(new LocalTime(hourOfDay, minute));
                setupTimeStampButtons();
            }
        });
    }

    @OnClick({R.id.scheduled_date, R.id.deadline_date})
    public void onDateClick(View view) {
        boolean isScheduled = (view.getId() == R.id.scheduled_date);
        final OrgNodeTimeDate date = isScheduled ? node.getScheduled() : node.getDeadline();
        setupDateDialog(date.getDate(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                date.setDate(new LocalDate(year, month + 1, dayOfMonth));
                setupTimeStampButtons();
            }
        });
    }


    private void createNewNode(ContentResolver resolver) {
        // Creating new node
        node = new OrgNode();
        node.parentId = parentId;
        node.position = position;
        try {
            OrgNode parentNode = new OrgNode(parentId, resolver);
            node.level = parentNode.level + 1;
            node.fileId = parentNode.fileId;
        } catch (OrgNodeNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable(SSTATE_SCHEDULED_DATE, node.getScheduled().getDate());
        outState.putSerializable(SSTATE_SCHEDULED_TIME, node.getScheduled().getTime());

        outState.putSerializable(SSTATE_DEADLINE_DATE, node.getDeadline().getDate());
        outState.putSerializable(SSTATE_DEADLINE_TIME, node.getDeadline().getTime());

        outState.putString("btnTodo", node.todo);
        outState.putString("btnPriority", node.priority);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    /**
     * Called by EditNodeActivity when the OK button from the menu bar is pressed
     * Triggers the update mechanism
     * First the new node is written to the DB
     * Then the file is written to disk
     *
     * @return : whether or not, the fragment must finish
     */
    public boolean onOKPressed() {
        String payload = "";
        String padding = "";
        long paddingLevel;
        String previousPayload = node.getPayload();
        if (previousPayload != null && !previousPayload.trim().equals("")) {
            // Use the padding level from the former payload
            paddingLevel = FileUtils.getMinimumPadding(previousPayload);
        } else {
            paddingLevel = node.level + 1;
        }

        for (int i = 0; i < paddingLevel; i++) padding += ' ';
        for (String line : content.getText().toString().split("\\r?\\n")) {
            payload += padding + line + "\n";
        }

        node.name = title.getText().toString();

        node.setPayload(payload);

        if (nodeId < 0) node.shiftNextSiblingNodes(context);

        node.write(getContext());
        OrgFileOld.updateFile(node, context);
        return true;
    }

    /**
     * Called by EditNodeActivity when the Cancel button from the menu bar is pressed
     */
    public void onCancelPressed() {
    }

    private void setupDateDialog(LocalDate date, DatePickerDialog.OnDateSetListener onDateSetListener) {
        DialogFragment newFragment = new DatePickerFragment(date, onDateSetListener);
        newFragment.show(getActivity().getSupportFragmentManager(), "datePicker");
    }

    private void setupTimeDialog(LocalTime time, TimePickerDialog.OnTimeSetListener onTimeSetListener) {
        DialogFragment newFragment = new TimePickerFragment(time, onTimeSetListener);
        newFragment.show(getActivity().getSupportFragmentManager(), "timePicker");
    }

    private void showPriorityDialog() {
        final ArrayList<String> priorityList = new ArrayList<>();
        priorityList.add("A");
        priorityList.add("B");
        priorityList.add("C");
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.priority)
                .setItems(priorityList.toArray(new CharSequence[priorityList.size()]),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                String selectedPriority = priorityList.get(which);
                                node.priority = selectedPriority;
//                                setupTodoButton(context,node,button, false);
                                btnPriority.setText(selectedPriority);
                            }
                        });
        builder.create().show();
    }


    public static class TimePickerFragment extends DialogFragment {
        private final LocalTime time;
        private final TimePickerDialog.OnTimeSetListener onTimeSetListener;

        public TimePickerFragment(LocalTime time, TimePickerDialog.OnTimeSetListener onTimeSetListener) {
            this.time = time == null ? LocalTime.now() : time;
            this.onTimeSetListener = onTimeSetListener;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new TimePickerDialog(getActivity(), this.onTimeSetListener, time.getHourOfDay(), time.getMinuteOfHour(),
                    DateFormat.is24HourFormat(getActivity()));
        }
    }

    public static class DatePickerFragment extends DialogFragment {
        private LocalDate date;
        private DatePickerDialog.OnDateSetListener listener;

        public DatePickerFragment(LocalDate date, DatePickerDialog.OnDateSetListener listener) {
            this.date = date == null ? LocalDate.now() : date;
            this.listener = listener;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int year = date.getYear();
            int month = date.getMonthOfYear() - 1;
            int day = date.getDayOfMonth();

            return new DatePickerDialog(getActivity(), this.listener, year, month, day);
        }

    }
}
