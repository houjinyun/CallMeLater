package com.lanwen.callmelater;

import android.app.ListActivity;
import android.content.ContentUris;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.lanwen.callmelater.Reply.Replies;

public class CallMeLater extends ListActivity implements OnClickListener {
	
	private Button mAddButton;
	private Button mSettingsButton;
    public static final int MENU_ITEM_DELETE = Menu.FIRST;
    
    private static final String[] PROJECTION = new String[] {
    	Replies._ID, // 0
    	Replies.CONTENT, // 1
    };
    
    private static final int COLUMN_INDEX_CONTENT = 1;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
        Intent intent = getIntent();
        if (intent.getData() == null) {
            intent.setData(Replies.CONTENT_URI);
        }
		
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		preferences.edit().putBoolean("popup_enable", true).commit();
		
        Cursor cursor = managedQuery(intent.getData(), PROJECTION, null, null,null);

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, cursor,
                new String[] { Replies.CONTENT }, new int[] { android.R.id.text1 });
        setListAdapter(adapter);		
        getListView().setOnCreateContextMenuListener(mItemListener);
        
        mAddButton = (Button)findViewById(android.R.id.button1);
        mAddButton.setOnClickListener(this);
        mSettingsButton = (Button)findViewById(android.R.id.button2);
        mSettingsButton.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if (v == mAddButton) {
			if(getListAdapter().getCount() >= 5){
				Toast.makeText(this, R.string.items_limitation, Toast.LENGTH_SHORT).show();
				return;
			}
			startActivity(new Intent(Reply.ACTION_INSERT, getIntent().getData()));
		}
		
		if(v == mSettingsButton){
			startActivity(new Intent(this, ReplyPreferenceActivity.class));
		}
	}
	
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Uri uri = ContentUris.withAppendedId(getIntent().getData(), id);
        
        // Launch activity to view/edit the currently selected item
        startActivity(new Intent(Reply.ACTION_VIEW, uri));
    }	
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info;
        try {
             info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        } catch (ClassCastException e) {
            return false;
        }

        switch (item.getItemId()) {
            case MENU_ITEM_DELETE: {
                // Delete the note that the context menu is for
                Uri noteUri = ContentUris.withAppendedId(getIntent().getData(), info.id);
                getContentResolver().delete(noteUri, null, null);
                return true;
            }
        }
        return false;
    }    
    
	private OnCreateContextMenuListener mItemListener = new OnCreateContextMenuListener(){

		@Override
		public void onCreateContextMenu(ContextMenu menu, View v,
				ContextMenuInfo menuInfo) {
	        AdapterView.AdapterContextMenuInfo info;
	        try {
	             info = (AdapterView.AdapterContextMenuInfo) menuInfo;
	        } catch (ClassCastException e) {
	            return;
	        }

	        Cursor cursor = (Cursor) getListAdapter().getItem(info.position);
	        if (cursor == null) {
	            // For some reason the requested item isn't available, do nothing
	            return;
	        }

	        menu.setHeaderTitle(cursor.getString(COLUMN_INDEX_CONTENT));

	        // Add a menu item to delete the note
	        menu.add(0, MENU_ITEM_DELETE, 0, R.string.menu_delete);			
		}
		
	};    
}