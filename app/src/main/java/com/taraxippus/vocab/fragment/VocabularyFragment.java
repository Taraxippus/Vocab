package com.taraxippus.vocab.fragment;

import android.app.*;
import android.os.*;
import android.support.v7.widget.*;
import android.transition.*;
import android.view.*;
import android.view.ViewGroup.*;
import android.widget.*;
import com.taraxippus.vocab.*;
import com.taraxippus.vocab.vocabulary.*;
import java.text.*;
import java.util.*;

public class VocabularyFragment extends Fragment implements View.OnTouchListener, GestureDetector.OnGestureListener
{
	MainActivity main;
	Vocabulary vocabulary;
	
	private GestureDetector gestureDetector; 
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		this.main = (MainActivity)getActivity();
		this.vocabulary = main.vocabulary.get(main.vocabulary_selected);
		
		gestureDetector = new GestureDetector(main, this);
		
		View v = inflater.inflate(R.layout.vocabulary, container, false);
		v.setOnTouchListener(this);
		
		int position = main.vocabulary_filtered.indexOf(vocabulary);
		
		v.findViewById(R.id.card_kanji).setTransitionName("card" + position);
		
		TextView kanji = (TextView)v.findViewById(R.id.kanji_text);
		kanji.setTransitionName("kanji" + position);
		kanji.setText(vocabulary.correctAnswer(QuestionType.KANJI));

		TextView reading = (TextView)v.findViewById(R.id.reading_text);
		reading.setTransitionName("reading" + position);
		reading.setText(vocabulary.correctAnswer(QuestionType.READING));
		
		TextView meaning = (TextView)v.findViewById(R.id.meaning_text);
		meaning.setTransitionName("meaning" + position);
		meaning.setText(vocabulary.correctAnswer(QuestionType.MEANING_INFO));
		
		TextView type = (TextView)v.findViewById(R.id.type);
		type.setText(vocabulary.getType());
		
		if (this.vocabulary.sameReading.isEmpty())
		{
			v.findViewById(R.id.card_same_reading).setVisibility(View.INVISIBLE);
			v.findViewById(R.id.card_same_reading).setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, 0));
		}
		
		if (this.vocabulary.sameMeaning.isEmpty())
		{
			v.findViewById(R.id.card_same_meaning).setVisibility(View.INVISIBLE);
			v.findViewById(R.id.card_same_meaning).setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, 0));
		}
			
		RecyclerView recyclerView = (RecyclerView)v.findViewById(R.id.same_reading_recycler_view);
		recyclerView.setHasFixedSize(true);

		LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
		recyclerView.setLayoutManager(layoutManager);

		SynonymAdapter mAdapter = new SynonymAdapter((MainActivity)getActivity(), recyclerView, true);
		recyclerView.setAdapter(mAdapter);

		recyclerView = (RecyclerView)v.findViewById(R.id.same_meaning_recycler_view);
		recyclerView.setHasFixedSize(true);

		layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
		recyclerView.setLayoutManager(layoutManager);

		mAdapter = new SynonymAdapter((MainActivity)getActivity(), recyclerView, false);
		recyclerView.setAdapter(mAdapter);

		TextView kanji_succes = (TextView)v.findViewById(R.id.kanji_progress_number);
		kanji_succes.setText(vocabulary.timesCorrect_kanji + " / " + vocabulary.timesChecked_kanji);
		
		ProgressBar kanji_progress = (ProgressBar)v.findViewById(R.id.kanji_progress);
		kanji_progress.setMax(vocabulary.timesChecked_kanji);
		kanji_progress.setProgress(vocabulary.timesCorrect_kanji);
		
		TextView reading_succes = (TextView)v.findViewById(R.id.reading_progress_number);
		reading_succes.setText(vocabulary.timesCorrect_reading + " / " + vocabulary.timesChecked_reading);
		
		ProgressBar reading_progress = (ProgressBar)v.findViewById(R.id.reading_progress);
		reading_progress.setMax(vocabulary.timesChecked_reading);
		reading_progress.setProgress(vocabulary.timesCorrect_reading);
		
		TextView meaning_succes = (TextView)v.findViewById(R.id.meaning_progress_number);
		meaning_succes.setText(vocabulary.timesCorrect_meaning + " / " + vocabulary.timesChecked_meaning);
		
		ProgressBar meaning_progress = (ProgressBar)v.findViewById(R.id.meaning_progress);
		meaning_progress.setMax(vocabulary.timesChecked_meaning);
		meaning_progress.setProgress(vocabulary.timesCorrect_meaning);
		
		TextView kanji_streak = (TextView)v.findViewById(R.id.kanji_streak);
		kanji_streak.setText("Kanji streak: " + vocabulary.streak_kanji + ", longest streak: " + vocabulary.streak_kanji_best);

		TextView reading_streak = (TextView)v.findViewById(R.id.reading_streak);
		reading_streak.setText("Reading streak: " + vocabulary.streak_reading + ", longest streak: " + vocabulary.streak_reading_best);
		
		TextView meaning_streak = (TextView)v.findViewById(R.id.meaning_streak);
		meaning_streak.setText("Meaning streak: " + vocabulary.streak_meaning + ", longest streak: " + vocabulary.streak_meaning_best);
		
		TextView next_review = (TextView)v.findViewById(R.id.next_review);
		next_review.setText("Next Review: " + (vocabulary.lastChecked + vocabulary.getNextReview() < System.currentTimeMillis() ? "Now" : new SimpleDateFormat().format(new Date(vocabulary.lastChecked + vocabulary.getNextReview()))));

		if (!vocabulary.learned)
			next_review.setVisibility(View.INVISIBLE);
		
		TextView last_checked = (TextView)v.findViewById(R.id.last_checked);
		last_checked.setText("Last Checked: " + (vocabulary.lastChecked == 0 ? "Never" : new SimpleDateFormat().format(new Date(vocabulary.lastChecked))));
		
		TextView added = (TextView)v.findViewById(R.id.added);
		added.setText("Added: " + (new SimpleDateFormat().format(new Date(vocabulary.added))));
		
		TextView category = (TextView)v.findViewById(R.id.category);
		category.setText("Category: " + vocabulary.category);
		
		if (vocabulary.reading.length == 0)
		{
			v.findViewById(R.id.layout_reading).setVisibility(View.INVISIBLE);
			v.findViewById(R.id.layout_reading).setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, 0));
			
			reading_streak.setVisibility(View.INVISIBLE);
			reading_streak.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, 0));
		}
		

		if (this.vocabulary.reading.length > 1 || this.vocabulary.meaning.length > 1)
		{
			v.findViewById(R.id.reading_meaning_usage).setVisibility(View.VISIBLE);
			v.findViewById(R.id.reading_meaning_usage).setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.MATCH_PARENT));
		
			StringBuilder builder = new StringBuilder();
			
			if (this.vocabulary.reading.length > 1)
			{
				builder.append("Reading entered:\n");
				
				float sum = 0;
				
				for (int i = 0; i < vocabulary.reading.length; ++i)
					sum += vocabulary.reading_used[i];
				
				for (int i = 0; i < vocabulary.reading.length; ++i)
				{
					builder.append("- ");
					builder.append(vocabulary.reading[i]);
					builder.append(": ");
					builder.append((int) (vocabulary.reading_used[i] / sum * 100));
					builder.append("℅\n");
				}
				
				if (this.vocabulary.meaning.length > 1)
				{
					builder.append("\n");
				}
			}
			
			if (this.vocabulary.meaning.length > 1)
			{
				builder.append("Meaning entered:\n");
				
				float sum = 0;

				for (int i = 0; i < vocabulary.meaning.length; ++i)
					sum += vocabulary.meaning_used[i];

				for (int i = 0; i < vocabulary.meaning.length; ++i)
				{
					builder.append("- ");
					builder.append(vocabulary.meaning[i]);
					builder.append(": ");
					builder.append((int) (vocabulary.meaning_used[i] / sum * 100));
					builder.append("℅\n");
				}
			}
			
			((TextView)v.findViewById(R.id.reading_meaning_usage_text)).setText(builder.toString());
		}
		
		
		return v;
	}

	@Override
	public void onResume()
	{
		super.onResume();

		((MainActivity)getActivity()).setTap(this);
	}
	

	@Override
	public boolean onTouch(View p1, MotionEvent p2)
	{
		return gestureDetector.onTouchEvent(p2);
	}

	@Override
	public boolean onDown(MotionEvent p1)
	{
		return false;
	}

	@Override
	public void onShowPress(MotionEvent p1)
	{
	}

	@Override
	public boolean onSingleTapUp(MotionEvent p1)
	{
		return false;
	}

	@Override
	public boolean onScroll(MotionEvent p1, MotionEvent p2, float p3, float p4)
	{
		return false;
	}

	@Override
	public void onLongPress(MotionEvent p1)
	{
	}

	@Override
	public boolean onFling(MotionEvent p1, MotionEvent p2, float p3, float p4)
	{
		if (p3 > 5000 || p3 < -5000)
		{
			int index = main.vocabulary_filtered.indexOf(main.vocabulary.get(main.vocabulary_selected));
			if (index == -1)
				return false;

			Fragment fragment = new VocabularyFragment();
			fragment.setSharedElementEnterTransition(TransitionInflater.from(main).inflateTransition(R.transition.change_image_transform));
			fragment.setSharedElementReturnTransition(TransitionInflater.from(main).inflateTransition(R.transition.change_image_transform));

			if (p3 < 0)
			{
				main.vocabulary_selected = main.vocabulary.indexOf(main.vocabulary_filtered.get((index + 1) % main.vocabulary_filtered.size()));
				fragment.setEnterTransition(TransitionInflater.from(main).inflateTransition(android.R.transition.slide_right));
				fragment.setReturnTransition(TransitionInflater.from(main).inflateTransition(android.R.transition.fade));

			}
			else
			{
				main.vocabulary_selected = main.vocabulary.indexOf(main.vocabulary_filtered.get((index + main.vocabulary_filtered.size() - 1) % main.vocabulary_filtered.size()));
				fragment.setEnterTransition(TransitionInflater.from(main).inflateTransition(android.R.transition.slide_left));
				fragment.setReturnTransition(TransitionInflater.from(main).inflateTransition(android.R.transition.fade));

			}

			main.changeFragment(fragment, "detail_swipe");

            return true;
		}
		return false;
	}
	
}
