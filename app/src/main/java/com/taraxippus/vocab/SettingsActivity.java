package com.taraxippus.vocab;

import android.os.*;
import android.preference.*;
import android.support.v7.app.*;
import android.support.v7.widget.*;

public class SettingsActivity extends ActionBarActivity 
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);

		setContentView(R.layout.settings_activity);

		Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
		setSupportActionBar(toolbar);

		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
        getFragmentManager().beginTransaction()
			.replace(R.id.content_frame, new SettingsFragment())
			.commit();
    }

	public class SettingsFragment extends PreferenceFragment
	{
		public SettingsFragment()
		{
			super();
		}
		
		@Override
		public void onCreate(Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);

			addPreferencesFromResource(R.xml.preferences);
		}
	}
}
