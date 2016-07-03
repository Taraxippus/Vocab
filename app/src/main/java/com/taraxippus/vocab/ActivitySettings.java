package com.taraxippus.vocab;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import com.taraxippus.vocab.fragment.FragmentActivitySettings;

public class ActivitySettings extends ActionBarActivity 
{
	public static final String ACTION_STROKE_ORDER = "com.taraxippus.vocab.action.ACTION_SETTINGS_STROKE_ORDER";
	
    @Override
    protected void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
		setSupportActionBar(toolbar);

		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
        getFragmentManager().beginTransaction()
			.replace(R.id.layout_content, new FragmentActivitySettings())
			.commit();
    }
}
