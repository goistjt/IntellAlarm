package com.jrproject.brown_goist.intellalarm;

import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SimpleCursorAdapter;

public class DisplayAlarmsActivity extends ListActivity {
    private AlarmDataAdapter alarmDataAdapter;
    private SimpleCursorAdapter cursorAdaptor;

    private static final long NO_SELECTED_ID = -1;
    private long selectedId = NO_SELECTED_ID;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.alarm_list_activity);

        alarmDataAdapter = new AlarmDataAdapter(this);
        alarmDataAdapter.open();

        Cursor cursor = alarmDataAdapter.getAlarmsCursor();
        String[] fromColumns = new String[] {AlarmDataAdapter.KEY_NAME, AlarmDataAdapter.KEY_HOUR,
                AlarmDataAdapter.KEY_MINUTE};
        int[] toTextViews = new int[] {R.id.textViewName, R.id.textViewHour, R.id.textViewMinute};
        cursorAdaptor = new SimpleCursorAdapter(this, R.layout.alarm_list_item, cursor,
                fromColumns, toTextViews, 0);

        setListAdapter(cursorAdaptor);

        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //create dialog for editing
            }
        });

        registerForContextMenu(getListView());
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
                showDialog(DIALOG_ID);
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
                showDialog(DIALOG_ID);
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
}
