package com.lanwen.callmelater;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import com.lanwen.callmelater.Reply.Replies;

public class ReplyProvider extends ContentProvider {
	
    private static final String DATABASE_NAME = "replies.db";
    private static final int DATABASE_VERSION = 1;
    private static final String REPLIES_TABLE_NAME = "replies";

    private DatabaseHelper mOpenHelper;
    private static final UriMatcher sUriMatcher;
    private static final int REPLIES = 1;
    private static final int REPLY_ID = 2;
    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(Reply.AUTHORITY, "replies", REPLIES);
        sUriMatcher.addURI(Reply.AUTHORITY, "replies/#", REPLY_ID);
    }
    
    private static class DatabaseHelper extends SQLiteOpenHelper {
    	private Context mContext;
    	
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            mContext = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + REPLIES_TABLE_NAME + " ("
                    + Replies._ID + " INTEGER PRIMARY KEY,"
                    + Replies.CONTENT + " TEXT);");
            ContentValues values = new ContentValues();
            values.put(Replies.CONTENT, mContext.getString(R.string.meeting));
            db.insert(REPLIES_TABLE_NAME, Replies.CONTENT, values);  
            
            values = new ContentValues();
            values.put(Replies.CONTENT, mContext.getString(R.string.driving));
            db.insert(REPLIES_TABLE_NAME, Replies.CONTENT, values);  
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS notes");
            onCreate(db);
        }
    }
    
    

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
        case REPLIES:
            count = db.delete(REPLIES_TABLE_NAME, selection, selectionArgs);
            break;

        case REPLY_ID:
            String replyId = uri.getPathSegments().get(1);
            count = db.delete(REPLIES_TABLE_NAME, Replies._ID + "=" + replyId
                    + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
	}

	@Override
	public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
        case REPLIES:
            return Replies.CONTENT_TYPE;

        case REPLY_ID:
            return Replies.CONTENT_ITEM_TYPE;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
        // Validate the requested uri
        if (sUriMatcher.match(uri) != REPLIES) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        ContentValues values;
        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }

        if (values.containsKey(Replies.CONTENT) == false) {
            values.put(Replies.CONTENT, "");
        }

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        long rowId = db.insert(REPLIES_TABLE_NAME, Replies.CONTENT, values);
        if (rowId > 0) {
            Uri noteUri = ContentUris.withAppendedId(Replies.CONTENT_URI, rowId);
            getContext().getContentResolver().notifyChange(noteUri, null);
            return noteUri;
        }

        throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public boolean onCreate() {
        mOpenHelper = new DatabaseHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(REPLIES_TABLE_NAME);

        switch (sUriMatcher.match(uri)) {
        case REPLIES:
            break;

        case REPLY_ID:
            qb.appendWhere(Replies._ID + "=" + uri.getPathSegments().get(1));
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        // Get the database and run the query
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, null);

        // Tell the cursor what uri to watch, so it knows when its source data changes
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;		
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
        case REPLIES:
            count = db.update(REPLIES_TABLE_NAME, values, selection, selectionArgs);
            break;

        case REPLY_ID:
            String replyId = uri.getPathSegments().get(1);
            count = db.update(REPLIES_TABLE_NAME, values, Replies._ID + "=" + replyId
                    + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;		
	}

}
