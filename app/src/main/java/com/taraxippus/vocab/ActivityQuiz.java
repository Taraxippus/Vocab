package com.taraxippus.vocab;

import android.app.*;
import android.os.*;
import android.support.v7.app.*;
import android.support.v7.widget.*;
import android.transition.*;
import android.view.*;
import com.taraxippus.vocab.view.*;
import com.taraxippus.vocab.fragment.FragmentActivityQuiz;
import com.taraxippus.vocab.fragment.FragmentActivityQuizFast;

public class ActivityQuiz extends AppCompatActivity
{
	public static final String ACTION_RANDOM = "com.taraxippus.vocab.action.ACTION_RANDOM";
	public static final String ACTION_FAST = "com.taraxippus.vocab.action.ACTION_FAST";
	
	@Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
		
        setContentView(R.layout.main);
		
		Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
		setSupportActionBar(toolbar);
		
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		Fragment f;
		
		if (getIntent() != null && ACTION_RANDOM.equals(getIntent().getAction()))
			f = new FragmentActivityQuizFast().setDefaultTransitions(this);
		
		else if (getIntent() != null && ACTION_FAST.equals(getIntent().getAction()))
			f = new FragmentActivityQuizFast().setDefaultTransitions(this);
		
		else
			f = new FragmentActivityQuiz().setDefaultTransitions(this);
			
		getFragmentManager().beginTransaction().replace(R.id.layout_content, f).commit();
	}
}
