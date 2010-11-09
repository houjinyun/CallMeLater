package com.lanwen.callmelater;

import android.app.AlertDialog;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceClickListener;
import android.view.Window;

public class ReplyPreferenceActivity extends PreferenceActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.reply_preferences);
		
		PreferenceScreen preference = (PreferenceScreen) findPreference("about");
		preference.setOnPreferenceClickListener(new OnPreferenceClickListener(){
			public boolean onPreferenceClick(Preference preference) {
		        AlertDialog.Builder builder = new AlertDialog.Builder(ReplyPreferenceActivity.this);
		        builder.setTitle(R.string.about_title);
		        builder.setIcon(android.R.drawable.ic_dialog_info);
		        builder.setCancelable(true);
		        builder.setMessage(R.string.about_content);
		        AlertDialog dlg = builder.create();
				dlg.setCanceledOnTouchOutside(true);
				dlg.show();
				return false;
			}
		});
	}
}
