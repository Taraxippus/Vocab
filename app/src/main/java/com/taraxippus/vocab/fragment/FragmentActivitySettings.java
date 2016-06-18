package com.taraxippus.vocab.fragment;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import com.taraxippus.vocab.R;

public class FragmentActivitySettings extends PreferenceFragment
{
	public FragmentActivitySettings()
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
