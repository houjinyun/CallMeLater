package com.lanwen.callmelater;

import android.net.Uri;
import android.provider.BaseColumns;

public class Reply {
	public static final String AUTHORITY = "com.lanwen.callmelater.Reply";
	public static final String ACTION_VIEW 	 	= "com.lanwen.callmelater.Reply.VIEW";
	public static final String ACTION_INSERT 	= "com.lanwen.callmelater.Reply.INSERT";
	
	private Reply() {
	}
	
    /**
     * Replies table
     */
    public static final class Replies implements BaseColumns {

    	private Replies() {}

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/replies");

        /**
         * The content of the reply
         * <P>Type: TEXT</P>
         */
        public static final String CONTENT = "content";
        
        /**
         * The MIME type of {@link #CONTENT_URI} 
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.lanwen.reply";
        
        /**
         * The MIME type of {@link #CONTENT_URI} 
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.lanwen.reply";
    }	
}
