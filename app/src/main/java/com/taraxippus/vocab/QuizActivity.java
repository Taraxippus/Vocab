package com.taraxippus.vocab;

import android.app.*;
import android.os.*;
import android.support.v7.app.*;
import android.support.v7.widget.*;
import android.transition.*;
import android.view.*;
import com.taraxippus.vocab.view.*;

public class QuizActivity extends AppCompatActivity
{
	public MainActivity main;
	
	@Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.quiz_activity);
		
		Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
		setSupportActionBar(toolbar);
		
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		Fragment quiz_finish = new FinishQuizFragment();
		quiz_finish.setEnterTransition(TransitionInflater.from(this).inflateTransition(android.R.transition.slide_top));
		quiz_finish.setReturnTransition(TransitionInflater.from(this).inflateTransition(android.R.transition.fade));
		quiz_finish.setAllowEnterTransitionOverlap(false);
		quiz_finish.setAllowReturnTransitionOverlap(false);
		
		getFragmentManager().beginTransaction().replace(R.id.content_frame, quiz_finish).commit();
	}
	
	public class FinishQuizFragment extends Fragment
	{
		public FinishQuizFragment()
		{
			super();
		}
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
		{
			View v = inflater.inflate(R.layout.quiz_finish, container, false);
			
			final PercentageView percentage_total = (PercentageView) v.findViewById(R.id.percentage_total);
			final PercentageView percentage_kanji = (PercentageView) v.findViewById(R.id.percentage_kanji);
			final PercentageView percentage_reading = (PercentageView) v.findViewById(R.id.percentage_reading);
			final PercentageView percentage_meaning = (PercentageView) v.findViewById(R.id.percentage_meaning);
			
			percentage_total.startAnimation(3000);
			percentage_kanji.startAnimation(2000);
			percentage_reading.startAnimation(2000);
			percentage_meaning.startAnimation(2000);
			
			return v;
		}
		
	}
}
