package com.lanwen.callmelater;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;

import com.lanwen.callmelater.Reply.Replies;

public class ReplyEditor extends Activity {

    private static final int STATE_EDIT = 0;
    private static final int STATE_INSERT = 1;
    private int mState;
	private Uri mUri;
	private EditText mText;
	private Cursor mCursor;
	private String mOriginalContent;
	
    private static final String[] PROJECTION = new String[] {
        Replies._ID, // 0
        Replies.CONTENT, // 1
    };
	private static final int COLUMN_INDEX_CONTENT = 1;
    private static final String ORIGINAL_CONTENT = "origContent";
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.editor);
		
		final Intent intent = getIntent();
        final String action = intent.getAction();
        if (Reply.ACTION_VIEW.equals(action)) {
            mState = STATE_EDIT;
            mUri = intent.getData();
        } else if (Reply.ACTION_INSERT.equals(action)) {
            mState = STATE_INSERT;
            mUri = getContentResolver().insert(intent.getData(), null);

            if (mUri == null) {
                finish();
                return;
            }
            setResult(RESULT_OK, (new Intent()).setAction(mUri.toString()));
        } else {
            finish();
            return;
        }		
	
        mText = (EditText) findViewById(android.R.id.edit);

        mCursor = managedQuery(mUri, PROJECTION, null, null, null);
	}
	
    @Override
    protected void onResume() {
        super.onResume();

        if (mCursor != null) {
            mCursor.moveToFirst();

            if (mState == STATE_EDIT) {
                setTitle(getText(R.string.reply_edit));
            } else if (mState == STATE_INSERT) {
                setTitle(getText(R.string.reply_create));
            }

            String note = mCursor.getString(COLUMN_INDEX_CONTENT);
            mText.setTextKeepState(note);
            
            if (mOriginalContent == null) {
                mOriginalContent = note;
            }

        } else {
            setTitle(getText(R.string.error_reply));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Save away the original text, so we still have it if the activity
        // needs to be killed while paused.
        outState.putString(ORIGINAL_CONTENT, mOriginalContent);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mCursor != null) {
            String text = mText.getText().toString();
            int length = text.length();

            if (isFinishing() && (length == 0)) {
                setResult(RESULT_CANCELED);
                deleteNote();

            } else {
                ContentValues values = new ContentValues();
                values.put(Replies.CONTENT, text);

                getContentResolver().update(mUri, values, null, null);
            }
        }
    }
    
    private void deleteNote() {
        if (mCursor != null) {
            mCursor.close();
            mCursor = null;
            getContentResolver().delete(mUri, null, null);
            mText.setText("");
        }
    }
}
