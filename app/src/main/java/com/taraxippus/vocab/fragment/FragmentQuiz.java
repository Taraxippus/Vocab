package com.taraxippus.vocab.fragment;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.taraxippus.vocab.ActivityLearn;
import com.taraxippus.vocab.ActivityQuiz;
import com.taraxippus.vocab.ActivityStats;
import com.taraxippus.vocab.R;
import com.taraxippus.vocab.util.StringHelper;
import com.taraxippus.vocab.view.GraphView;
import com.taraxippus.vocab.view.LineGraphView;
import com.taraxippus.vocab.vocabulary.DBHelper;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class FragmentQuiz extends Fragment
{
	private DBHelper dbHelper;
	private SharedPreferences preferences;
	
	public FragmentQuiz() {}

	public Fragment setDefaultTransitions(Context context)
	{
		this.setEnterTransition(TransitionInflater.from(context).inflateTransition(android.R.transition.slide_bottom));
		this.setExitTransition(TransitionInflater.from(context).inflateTransition(android.R.transition.explode));

		this.setAllowEnterTransitionOverlap(false);
		this.setAllowReturnTransitionOverlap(false);
		
		return this;
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		dbHelper = new DBHelper(getContext());
		preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
		setHasOptionsMenu(true);
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
        return inflater.inflate(R.layout.fragment_quiz, container, false);
	}

	@Override
	public void onViewCreated(View v, Bundle savedInstanceState)
	{
		int[] newVocabularies = dbHelper.getNewVocabularies();
		char[] newKanji = dbHelper.getNewKanji();
		
		RecyclerView recyclerView = (RecyclerView)v.findViewById(R.id.recycler_new_vocabularies);
		recyclerView.setHasFixedSize(true);
		recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
		recyclerView.setAdapter(new FragmentDetail.VocabularyAdapter(getActivity(), dbHelper, recyclerView, newVocabularies));
		TextView text_new_vocabularies = (TextView) v.findViewById(R.id.text_title_new_vocabularies);
		text_new_vocabularies.setText(newVocabularies.length + (newVocabularies.length == 0 ? " New Vocabulary" : " New Vocabularies"));
		
		if (newVocabularies.length == 0)
		{
			text_new_vocabularies.setVisibility(View.GONE);
			recyclerView.setVisibility(View.GONE);
		}
		else
		{
			text_new_vocabularies.setVisibility(View.VISIBLE);
			recyclerView.setVisibility(View.VISIBLE);
		}
		
		recyclerView = (RecyclerView)v.findViewById(R.id.recycler_new_kanji);
		recyclerView.setHasFixedSize(true);
		recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
		recyclerView.setAdapter(new FragmentDetail.KanjiAdapter(getActivity(), dbHelper, recyclerView, newKanji));
		TextView text_new_kanji = (TextView) v.findViewById(R.id.text_title_new_kanji);
		text_new_kanji.setText(newKanji.length + " New Kanji");

		if (newKanji.length == 0)
		{
			text_new_kanji.setVisibility(View.GONE);
			recyclerView.setVisibility(View.GONE);
		}
		else
		{
			text_new_kanji.setVisibility(View.VISIBLE);
			recyclerView.setVisibility(View.VISIBLE);
		}
		
		v.findViewById(R.id.button_start_quiz).setOnClickListener(new View.OnClickListener()
		{
				@Override
				public void onClick(View p1)
				{
					getContext().startActivity(new Intent(getContext(), ActivityQuiz.class));
				}
		});
		
		v.findViewById(R.id.button_start_quiz_random).setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View p1)
				{
					getContext().startActivity(new Intent(getContext(), ActivityQuiz.class).setAction(ActivityQuiz.ACTION_RANDOM));
				}
			});
			
		v.findViewById(R.id.button_start_quiz_fast).setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View p1)
				{
					getContext().startActivity(new Intent(getContext(), ActivityQuiz.class).setAction(ActivityQuiz.ACTION_FAST));
				}
			});
			
		v.findViewById(R.id.button_stats).setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View p1)
				{
					getContext().startActivity(new Intent(getContext(), ActivityStats.class));
				}
			});
			
		int learned_total = 0, critical = 0;
		int correct_kanji = 0, total_kanji = 0, correct_reading = 0, total_reading = 0,
			correct_meaning = 0, total_meaning = 0;
		long nextReview = 0;
		int[] review = new int[25];
	
		Cursor res = dbHelper.getReadableDatabase().rawQuery("SELECT category, learned, nextReview, timesChecked_kanji, timesChecked_reading, timesChecked_meaning, timesCorrect_kanji, timesCorrect_reading, timesCorrect_meaning FROM vocab", null);
		int count = res.getCount();
		if (count > 0)
		{
			res.moveToFirst();

			int vCategory;
			long vNextReview;

			do
			{
				if (res.getInt(res.getColumnIndex("learned")) == 1)
				{
					vCategory = res.getInt(res.getColumnIndex("category"));
					vNextReview = res.getLong(res.getColumnIndex("nextReview"));

					learned_total++;
					if (vCategory <= 1)
						critical++;
					correct_kanji += res.getInt(res.getColumnIndex("timesCorrect_kanji"));
					total_kanji += res.getInt(res.getColumnIndex("timesChecked_kanji"));
					correct_reading += res.getInt(res.getColumnIndex("timesCorrect_reading"));
					total_reading += res.getInt(res.getColumnIndex("timesChecked_reading"));
					correct_meaning += res.getInt(res.getColumnIndex("timesCorrect_meaning"));
					total_meaning += res.getInt(res.getColumnIndex("timesChecked_meaning"));

					if (nextReview == 0)
						nextReview = vNextReview;
					else
						nextReview = Math.min(nextReview, vNextReview);

					if (vNextReview < System.currentTimeMillis())
						review[0]++;
					else if (vNextReview < System.currentTimeMillis() + 1000 * 60 * 60 * 24)
						review[1 + (int) ((vNextReview - System.currentTimeMillis()) / (1000 * 60 * 60))]++;
				}
			}
			while (res.moveToNext());
		}

		res.close();

		((TextView) v.findViewById(R.id.text_progress_learned)).setText(learned_total + " / " + count);
		ProgressBar progress_learned = (ProgressBar) v.findViewById(R.id.progress_learned);
		progress_learned.setMax(count);
		progress_learned.setProgress(learned_total - critical);
		progress_learned.setSecondaryProgress(learned_total);
		
		learned_total = 0;
		critical = 0;
		res = dbHelper.getReadableDatabase().rawQuery("SELECT category, learned, nextReview, timesChecked_kanji, timesChecked_reading_kun, timesChecked_reading_on, timesChecked_meaning, timesCorrect_kanji, timesCorrect_reading_kun, timesCorrect_reading_on, timesCorrect_meaning FROM kanji", null);
		count = res.getCount();
		if (count > 0)
		{
			res.moveToFirst();

			int vCategory;
			long vNextReview;

			do
			{
				if (res.getInt(res.getColumnIndex("learned")) == 1)
				{
					vCategory = res.getInt(res.getColumnIndex("category"));
					vNextReview = res.getLong(res.getColumnIndex("nextReview"));

					learned_total++;
					if (vCategory <= 1)
						critical++;
					correct_kanji += res.getInt(res.getColumnIndex("timesCorrect_kanji"));
					total_kanji += res.getInt(res.getColumnIndex("timesChecked_kanji"));
					correct_reading += res.getInt(res.getColumnIndex("timesCorrect_reading_kun")) + res.getInt(res.getColumnIndex("timesCorrect_reading_on"));
					total_reading += res.getInt(res.getColumnIndex("timesChecked_reading_kun")) + res.getInt(res.getColumnIndex("timesChecked_reading_on"));
					correct_meaning += res.getInt(res.getColumnIndex("timesCorrect_meaning"));
					total_meaning += res.getInt(res.getColumnIndex("timesChecked_meaning"));

					if (nextReview == 0)
						nextReview = vNextReview;
					else
						nextReview = Math.min(nextReview, vNextReview);

					if (vNextReview < System.currentTimeMillis())
						review[0]++;
					else if (vNextReview < System.currentTimeMillis() + 1000 * 60 * 60 * 24)
						review[1 + (int) ((vNextReview - System.currentTimeMillis()) / (1000 * 60 * 60))]++;
				}
			}
			while (res.moveToNext());
		}

		res.close();
		
		((TextView) v.findViewById(R.id.text_progress_learned_kanji)).setText(learned_total + " / " + count);
		ProgressBar progress_learned_kanji = (ProgressBar) v.findViewById(R.id.progress_learned_kanji);
		progress_learned_kanji.setMax(count);
		progress_learned_kanji.setProgress(learned_total - critical);
		progress_learned_kanji.setSecondaryProgress(learned_total);
		
		int reviews1 = 0;
		for (int i = 0; i < review.length; i++)
			review[i] = (reviews1 += review[i]);

		v.findViewById(R.id.button_start_quiz).setVisibility(review[0] > 0 ? View.VISIBLE : View.GONE);
		
		((TextView) v.findViewById(R.id.text_progress_total)).setText((correct_meaning + correct_reading + correct_kanji) + " / " + (total_meaning + total_reading + total_meaning));
		ProgressBar progress_total = (ProgressBar) v.findViewById(R.id.progress_total);
		progress_total.setMax(total_meaning + total_reading + total_kanji);
		progress_total.setProgress(correct_meaning + correct_reading + correct_kanji);

		((TextView) v.findViewById(R.id.text_progress_kanji)).setText(correct_kanji + " / " + total_kanji);
		ProgressBar progress_kanji = (ProgressBar) v.findViewById(R.id.progress_kanji);
		progress_kanji.setMax(total_kanji);
		progress_kanji.setProgress(correct_kanji);

		((TextView) v.findViewById(R.id.text_progress_reading)).setText(correct_reading + " / " + total_reading);
		ProgressBar progress_reading = (ProgressBar) v.findViewById(R.id.progress_reading);
		progress_reading.setMax(total_reading);
		progress_reading.setProgress(correct_reading);

		((TextView) v.findViewById(R.id.text_progress_meaning)).setText(correct_meaning + " / " + total_meaning);
		ProgressBar progress_meaning = (ProgressBar) v.findViewById(R.id.progress_meaning);
		progress_meaning.setMax(total_meaning);
		progress_meaning.setProgress(correct_meaning);

		((LineGraphView) v.findViewById(R.id.line_graph_review)).setValues(review);

		long lastDate = preferences.getLong("lastReviewDate", 0);
		int[] review1 = StringHelper.toIntArray(preferences.getString("review1", ""));
		int[] review2 = StringHelper.toIntArray(preferences.getString("review2", ""));

		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTimeInMillis(lastDate);
		int days = new GregorianCalendar().get(Calendar.DAY_OF_YEAR) - calendar.get(Calendar.DAY_OF_YEAR);
		if (days < 0)
			days += calendar.isLeapYear(calendar.get(Calendar.YEAR)) ? 366 : 365;
		if (days > 30 || review1.length != 30)
		{
			review1 = new int[30];
			review2 = new int[30];
		}
		else
		{
			System.arraycopy(review1, days, review1, 0, 30 - days);
			System.arraycopy(review2, days, review2, 0, 30 - days);

			for (int i = 29; i >= 30 - days; --i)
			{
				review1[i] = 0;
				review2[i] = 0;
			}
		}
		preferences.edit()
			.putLong("lastReviewDate", System.currentTimeMillis())
			.putString("review1", StringHelper.toString(review1))
			.putString("review2", StringHelper.toString(review2))
			.apply();

		((GraphView) v.findViewById(R.id.graph_reviewed)).setValues(review1, review2);

		calendar = new GregorianCalendar();
		calendar.add(Calendar.DAY_OF_YEAR, -30);
		((TextView) v.findViewById(R.id.text_date)).setText(DateFormat.getDateFormat(getContext()).format(calendar.getTime()));

		((TextView) v.findViewById(R.id.text_next_review_values)).setText(
			review[0] + "\n"
			+ review[1] + "\n"
			+ review[24]
		);
		((TextView) v.findViewById(R.id.date_next_review)).setText("Next Review: " + (nextReview < System.currentTimeMillis() ? "Now" : nextReview == 0 ? "Never" : new SimpleDateFormat().format(new Date(nextReview))));
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.item_learn:
				getContext().startActivity(new Intent(getContext(), ActivityLearn.class));
				return true;
			
			case R.id.item_start_quiz:
				getContext().startActivity(new Intent(getContext(), ActivityQuiz.class));
				return true;
				
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		inflater.inflate(R.menu.fragment_quiz, menu);

		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu)
	{
		menu.findItem(R.id.item_learn).setVisible(dbHelper.getCount("learned = 1 AND lastChecked = 0") > 0);
		menu.findItem(R.id.item_start_quiz).setVisible(dbHelper.getCount("learned = 1 AND nextReview < " + System.currentTimeMillis()) > 0);
		
		super.onPrepareOptionsMenu(menu);
	}
	
	@Override
	public void onResume()
	{
		super.onResume();

		getActivity().setTitle("Quiz");
		onViewCreated(getView(), null);
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();

		dbHelper.close();
	}
}
