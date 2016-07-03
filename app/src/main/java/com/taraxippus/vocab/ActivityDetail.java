package com.taraxippus.vocab;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import com.taraxippus.vocab.fragment.FragmentDetail;

public class ActivityDetail extends AppCompatActivity
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

		Fragment fragment = new FragmentDetail();
		fragment.setArguments(getIntent().getExtras());
		getFragmentManager().beginTransaction().replace(R.id.layout_content, fragment).commit();
	}
}
