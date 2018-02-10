package com.taraxippus.vocab;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import com.taraxippus.vocab.R;
import com.taraxippus.vocab.fragment.FragmentActivityStats;

public class ActivityStats extends AppCompatActivity
{
	@Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

		Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
		setSupportActionBar(toolbar);

		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		Fragment fragment = new FragmentActivityStats();
		fragment.setArguments(getIntent().getExtras());
		getFragmentManager().beginTransaction().replace(R.id.layout_content, fragment).commit();
	}
}
