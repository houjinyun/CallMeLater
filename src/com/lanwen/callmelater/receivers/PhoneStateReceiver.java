package com.lanwen.callmelater.receivers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.PixelFormat;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.telephony.PhoneStateListener;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.android.internal.telephony.ITelephony;
import com.flurry.android.FlurryAgent;
import com.lanwen.callmelater.R;
import com.lanwen.callmelater.Reply.Replies;

public class PhoneStateReceiver extends BroadcastReceiver {

	private static final String MESSAGE_SENT_ACTION = "com.lanwen.callmelater.SENT";
	private ITelephony iTelephony = null;
	
	// TODO put these stuffs into a service....
	@Override
	public void onReceive(final Context context, Intent intent) {
		FlurryAgent.onStartSession(context, context.getString(R.string.flurry));
		createTelephony(context);
		
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		boolean mPopupEnable = preferences.getBoolean("popup_enable", false);
		
		final TelephonyManager telephonyManager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		
		PhoneStateListener listener = new PhoneStateListener() {

			WindowManager mWM = (WindowManager) context
					.getSystemService(Context.WINDOW_SERVICE);
			LayoutInflater inflate = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View mView;

			@Override
			public void onCallStateChanged(int state, String incomingNumber) {

				switch (state) {
				case TelephonyManager.CALL_STATE_IDLE:
				case TelephonyManager.CALL_STATE_OFFHOOK: {
					if (mView != null && mView.getParent() != null)
						mWM.removeView(mView);
					mView = null;
					// unregister the listener
					telephonyManager.listen(this,
							PhoneStateListener.LISTEN_NONE);
					FlurryAgent.onEndSession(context);
					break;
				}
				case TelephonyManager.CALL_STATE_RINGING: {
					populatView(incomingNumber);
					break;
				}
				}
			}
			
			private void sendMessage(final String number,
					final PendingIntent sentIntent, String message) {
				// send message
				SmsManager smsManager = SmsManager.getDefault();
				smsManager.sendTextMessage(number, null, message, sentIntent, null);
			}			

			private void populatView(final String number) {
				mView = inflate.inflate(R.layout.panel, null);
				WindowManager.LayoutParams mParams = new WindowManager.LayoutParams(
						WindowManager.LayoutParams.WRAP_CONTENT,
						WindowManager.LayoutParams.WRAP_CONTENT,
						WindowManager.LayoutParams.TYPE_PRIORITY_PHONE,
						WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
						PixelFormat.TRANSLUCENT);
				mParams.gravity = Gravity.TOP | Gravity.LEFT;
				mParams.x = 0;
				mParams.y = 0;
				
				final ListView list = (ListView) mView.findViewById(android.R.id.list);
				final Button directMessageButton = (Button)mView.findViewById(R.id.direct_message_button);
				Cursor cursor = context.getContentResolver().query(Replies.CONTENT_URI, null, null, null,null);
		        SimpleCursorAdapter adapter = new SimpleCursorAdapter(context, R.layout.reply_item, cursor,
		                new String[] { Replies.CONTENT }, new int[] { android.R.id.text1 });
		        if (cursor.getCount() == 0){
		        	list.setVisibility(View.GONE);
		        	mView.findViewById(android.R.id.empty).setVisibility(View.VISIBLE);
		        }
		        
		        final EditText mMessagetext = (EditText)mView.findViewById(R.id.message_text); 
		        
				final PendingIntent sentIntent = PendingIntent.getBroadcast(context, 0,
		                  new Intent(MESSAGE_SENT_ACTION), 0);
						        
				list.setOnItemClickListener(new OnItemClickListener(){
					public void onItemClick(AdapterView<?> parent, View v,
							int position, long id) {
						list.setEnabled(false);
						directMessageButton.setEnabled(false);
						Toast.makeText(context, R.string.message_sending, Toast.LENGTH_SHORT).show();
						hangup();
						Cursor cursor = (Cursor)parent.getAdapter().getItem(position);
						String message = cursor.getString(1);
						sendMessage(number, sentIntent, message);
						FlurryAgent.onEvent("send reply", null);
					}

				});				
				
				directMessageButton.setOnClickListener(new OnClickListener(){
					public void onClick(View v) {
						String message = mMessagetext.getText().toString().trim();
						if(message.length() > 0){
							list.setEnabled(false);
							directMessageButton.setEnabled(false);
							Toast.makeText(context, R.string.message_sending, Toast.LENGTH_SHORT).show();
							hangup();
							sendMessage(number, sentIntent, message);
							FlurryAgent.onEvent("send direct message", null);
						}
					}
				});
				
				list.setAdapter(adapter);
				
				if (mView.getParent() != null) {
					mWM.removeView(mView);
				}
				mWM.addView(mView, mParams);
			}
		};

		if (mPopupEnable)
			telephonyManager.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
		else
			telephonyManager.listen(listener, PhoneStateListener.LISTEN_NONE);
	}
	
	private void createTelephony(Context context) {
		TelephonyManager telMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

		Class<TelephonyManager> c = TelephonyManager.class;
		Method getITelephonyMethod = null;
		try {
			getITelephonyMethod = c.getDeclaredMethod("getITelephony",
					(Class[]) null);
			getITelephonyMethod.setAccessible(true);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}

		try {
			iTelephony = (ITelephony) getITelephonyMethod.invoke(
					telMgr, (Object[]) null);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}
	
	private void hangup() {
		if(iTelephony != null)
			try {
				iTelephony.endCall();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
	}
}
