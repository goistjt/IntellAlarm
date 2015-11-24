package com.jrproject.brown_goist.intellalarm;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.GregorianCalendar;

public class DisplayAlarmsActivity extends ListActivity {
    private AlarmDataAdapter alarmDataAdapter;
    private SimpleCursorAdapter cursorAdaptor;

    private static final long NO_SELECTED_ID = -1;
    private long selectedId = NO_SELECTED_ID;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        alarmDataAdapter = new AlarmDataAdapter(this);
        alarmDataAdapter.open();

        Cursor cursor = alarmDataAdapter.getAlarmsCursor();
        String[] fromColumns = new String[]{AlarmDataAdapter.KEY_NAME, AlarmDataAdapter.KEY_HOUR,
                AlarmDataAdapter.KEY_MINUTE, AlarmDataAdapter.KEY_STATUS};
        int[] toTextViews = new int[]{R.id.alarm_list_name_text_view,
                R.id.alarm_list_time_hour_text_view, R.id.alarm_list_time_minute_text_view,
                R.id.alarm_on_switch};
        cursorAdaptor = new SimpleCursorAdapter(this, R.layout.alarm_list_item, cursor,
                fromColumns, toTextViews, 0);



        setListAdapter(cursorAdaptor);

        getListView().setClickable(true);

        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                //create dialog for editing
                makeToast(position);
                showAlarmDialog("Edit", id);
                return true;
            }
        });
    }

    private boolean showAlarmDialog(final String type, final long... id) {
        DialogFragment df = new DialogFragment() {
            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(type + " Alarm");
                LayoutInflater inflater = getActivity().getLayoutInflater();
                final View v = inflater.inflate(R.layout.dialog_add_alarm, null);
                builder.setView(v);
                builder.setNegativeButton(android.R.string.cancel, new DialogInterface
                        .OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                });
                builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name = ((EditText) v.findViewById(R.id.dialog_alarm_name)).getText
                                ().toString();
                        TimePicker tp = (TimePicker) v.findViewById(R.id.dialog_alarm_time_picker);
                        GregorianCalendar time = new GregorianCalendar(0, 0, 0, tp.getCurrentHour(),
                                tp.getCurrentMinute());
                        if (type.equals("Add")) {
                            Alarm a = new Alarm(name, time);
                            addAlarm(a);
                        } else {
                            Alarm a = getAlarm(id[0]);
                            a.setName(name);
                            a.setAlarmTime(time);
                            editAlarm(a);
                        }
                    }
                });
                return builder.create();
            }
        };
        df.show(getFragmentManager(), "");
        return true;
    }

    /**
     * ListActivity sets up the onItemClick listener for the list view
     * automatically via this function
     */
    @Override
    protected void onListItemClick(ListView listView, View selectedView,
                                   int position, long id) {
        super.onListItemClick(listView, selectedView, position, id);
        selectedId = id;
        showAlarmDialog("Edit", id);
    }

    /**
     * Standard menu. Only has one item CONSIDER: Could add an edit and/or
     * remove option when an item is selected
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    /**
     * Standard listener for the option menu item selections
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add_alarm:
                selectedId = NO_SELECTED_ID;
                showAlarmDialog("Add");
                return true;
            default:
                return false;
        }
    }

    /**
     * Create a context menu for the list view Secretly surprised ListActivity
     * doesn't provide a special magic feature here too. :)
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        if (v == getListView()) {
            inflater.inflate(R.menu.alarm_list_view_context_menu, menu);
        }
    }

    /**
     * Standard listener for the context menu item selections
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
                .getMenuInfo();
        switch (item.getItemId()) {
            case R.id.menu_item_list_view_delete:
                removeAlarm(info.id);
                return true;
            case R.id.menu_item_list_view_edit:
                selectedId = info.id;
                showAlarmDialog("Edit", selectedId);
                return true;
        }
        return super.onContextItemSelected(item);
    }

    /**
     * Called when the activity is removed from memory (placeholder for later)
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    // ======================================================================
    // Data CRUD mechanisms (Create, read, update, and delete)
    // ======================================================================

    /**
     * Create: Add a new alarm to the data storage mechanism
     *
     * @param a New alarm to add
     */
    private void addAlarm(Alarm a) {
        alarmDataAdapter.addAlarm(a);
        Cursor cursor = alarmDataAdapter.getAlarmsCursor();
        cursorAdaptor.changeCursor(cursor);
    }

    /**
     * Read: Get an alarm for the data storage mechanism
     *
     * @param id Index of the alarm in the data storage mechanism
     */
    public Alarm getAlarm(long id) {
        return alarmDataAdapter.getAlarm(id);
    }

    /**
     * Update: Edit an alarm in the data storage mechanism Uses the values in the
     * pass Alarm to update the alarm at the mSelectedId location
     *
     * @param a Container for the new values to use in the update
     */
    private void editAlarm(Alarm a) {
        if (selectedId == NO_SELECTED_ID) {
            Log.e("DisplayAlarmsActivity", "Attempt to update with no score selected.");
        }
        a.setId(selectedId);
        alarmDataAdapter.updateAlarm(a);
        cursorAdaptor.changeCursor(alarmDataAdapter.getAlarmsCursor());
    }

    /**
     * Delete: Remove an alarm from the data storage mechanism
     *
     * @param id Index of the alarm in the data storage mechanism
     */
    private void removeAlarm(long id) {
        alarmDataAdapter.removeAlarm(id);
        cursorAdaptor.changeCursor(alarmDataAdapter.getAlarmsCursor());
    }

    private void makeToast(int pos) {
        Toast.makeText(this, "Selected item " + pos, Toast.LENGTH_SHORT).show();
    }
}
