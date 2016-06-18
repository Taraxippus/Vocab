package com.taraxippus.vocab.fragment;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.taraxippus.vocab.MainActivity;
import com.taraxippus.vocab.R;
import com.taraxippus.vocab.dialog.LearnNextDialog;
import com.taraxippus.vocab.util.StringHelper;
import com.taraxippus.vocab.view.GraphView;
import com.taraxippus.vocab.view.LineGraphView;
import com.taraxippus.vocab.view.PercentageView;
import com.taraxippus.vocab.vocabulary.DBHelper;
import com.taraxippus.vocab.vocabulary.Vocabulary;
import java.util.Calendar;

public class FragmentActivityQuizFinish extends Fragment implements View.OnClickListener
{
	DBHelper dbHelper;
	SharedPreferences preferences;	
	
	public FragmentActivityQuizFinish() {}

	public Fragment setDefaultTransitions(Context context)
	{
		this.setEnterTransition(TransitionInflater.from(context).inflateTransition(android.R.transition.slide_top));
		this.setReturnTransition(TransitionInflater.from(context).inflateTransition(android.R.transition.fade));
		
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
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View v = inflater.inflate(R.layout.activity_quiz_finish, container, false);

		final PercentageView percentage_total = (PercentageView) v.findViewById(R.id.percentage_total);
		final PercentageView percentage_kanji = (PercentageView) v.findViewById(R.id.percentage_kanji);
		final PercentageView percentage_reading = (PercentageView) v.findViewById(R.id.percentage_reading);
		final PercentageView percentage_meaning = (PercentageView) v.findViewById(R.id.percentage_meaning);

		percentage_total.setValue((getArguments().getInt("timesCorrect_kanji") + getArguments().getInt("timesCorrect_reading") + getArguments().getInt("timesCorrect_meaning")) / (float) (getArguments().getInt("timesChecked_kanji") + getArguments().getInt("timesChecked_reading") + getArguments().getInt("timesChecked_meaning")));
		percentage_total.startAnimation(3000);
		
		((TextView) v.findViewById(R.id.text_progress_kanji)).setText(getArguments().getInt("timesCorrect_kanji") + " / " + getArguments().getInt("timesChecked_kanji"));
		percentage_kanji.setValue(getArguments().getInt("timesCorrect_kanji") / (float) getArguments().getInt("timesChecked_kanji"));
		percentage_kanji.startAnimation(2000);
		
		((TextView) v.findViewById(R.id.text_progress_reading)).setText(getArguments().getInt("timesCorrect_reading") + " / " + getArguments().getInt("timesChecked_reading"));
		percentage_reading.setValue(getArguments().getInt("timesCorrect_reading") / (float) getArguments().getInt("timesChecked_reading"));
		percentage_reading.startAnimation(2250);
		
		((TextView) v.findViewById(R.id.text_progress_meaning)).setText(getArguments().getInt("timesCorrect_meaning") + " / " + getArguments().getInt("timesChecked_meaning"));
		percentage_meaning.setValue(getArguments().getInt("timesCorrect_meaning") / (float) getArguments().getInt("timesChecked_meaning"));
		percentage_meaning.startAnimation(2500);

		v.findViewById(R.id.button_home).setOnClickListener(this);

		((LineGraphView) v.findViewById(R.id.line_graph_quiz)).setValues(" %", getArguments().getFloatArray("history_quiz"));
		
		int[] review = new int[25];
	
		Cursor res = dbHelper.getReadableDatabase().rawQuery("SELECT category, lastChecked FROM vocab WHERE learned = 1", null);
		res.moveToFirst();

		long vNextReview;

		do
		{
			vNextReview = res.getLong(res.getColumnIndex("lastChecked")) + Vocabulary.getNextReview(res.getInt(res.getColumnIndex("category")));

			if (vNextReview < System.currentTimeMillis())
				review[0]++;
			else if (vNextReview < System.currentTimeMillis() + 1000 * 60 * 60 * 24)
				review[1 + (int) ((vNextReview - System.currentTimeMillis()) / (1000 * 60 * 60))]++;
		}
		while (res.moveToNext());

		res.close();

		int reviews1 = 0;
		for (int i = 0; i < review.length; i++)
			review[i] = (reviews1 += review[i]);
			
		((LineGraphView) v.findViewById(R.id.line_graph_review)).setValues(review);
		
		long lastDate = preferences.getLong("lastReviewDate", 0);
		int[] review1 = StringHelper.toIntArray(preferences.getString("review1", ""));
		int[] review2 = StringHelper.toIntArray(preferences.getString("review2", ""));

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(lastDate);
		int days = Calendar.getInstance().get(Calendar.DATE) - calendar.get(Calendar.DATE);
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
		

		calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_YEAR, -30);
		((TextView) v.findViewById(R.id.text_date)).setText(DateFormat.getDateFormat(getContext()).format(calendar.getTime()));
		
		return v;
	}

	@Override
	public void onClick(View v)
	{
		if (v.getId() == R.id.button_home)
			startActivity(new Intent(getContext(), MainActivity.class));
			
		else
			new LearnNextDialog().show(getFragmentManager(), "learn");
	}

	@Override
	public void onResume()
	{
		super.onResume();
	}
}
