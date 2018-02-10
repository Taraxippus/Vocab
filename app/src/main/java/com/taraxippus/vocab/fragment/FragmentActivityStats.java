package com.taraxippus.vocab.fragment;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.taraxippus.vocab.R;
import com.taraxippus.vocab.util.StringHelper;
import com.taraxippus.vocab.view.GraphView;
import com.taraxippus.vocab.view.LineGraphView;
import com.taraxippus.vocab.vocabulary.DBHelper;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;

public class FragmentActivityStats extends Fragment
{
	public DBHelper dbHelper;

	public FragmentActivityStats() {}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		dbHelper = new DBHelper(getContext());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.activity_stats, container, false);
	}

	@Override
	public void onViewCreated(View v, Bundle savedInstanceState)
	{
		super.onViewCreated(v, savedInstanceState);

		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
		
		int learned_total = 0, critical = 0;
		int correct_kanji = 0, total_kanji = 0, correct_reading = 0, total_reading = 0,
			correct_meaning = 0, total_meaning = 0;
		long nextReview = 0;
		int[] review = new int[25 + 14];
		int maxReviews = 0;
		int maxHomo = 0, maxSyno = 0;
		int kanaCount = 0;
		final HashSet<Character> kanjiSet = new HashSet<>();
		
		Cursor res = dbHelper.getReadableDatabase().rawQuery("SELECT kanji, reading, sameReading, sameMeaning, category, learned, nextReview, timesChecked_kanji, timesChecked_reading, timesChecked_meaning, timesCorrect_kanji, timesCorrect_reading, timesCorrect_meaning FROM vocab", null);
		int count = res.getCount();
		if (count > 0)
		{
			res.moveToFirst();

			int vCategory, vck, vcr, vcm;
			long vNextReview;
			int i;
			
			do
			{
				
				String kanji = res.getString(0);

				for (i = 0; i < kanji.length(); ++i)
					if (StringHelper.isKanji(kanji.charAt(i)))
						kanjiSet.add(kanji.charAt(i));
					
				if (StringHelper.lengthOfArray(res.getString(1)) == 0)
					kanaCount++;
				
				maxHomo = Math.max(maxHomo, StringHelper.lengthOfArray(res.getString(2)));
				maxSyno = Math.max(maxSyno, StringHelper.lengthOfArray(res.getString(3)));
					
				if (res.getInt(res.getColumnIndex("learned")) == 1)
				{
					vCategory = res.getInt(res.getColumnIndex("category"));
					vNextReview = res.getLong(res.getColumnIndex("nextReview"));

					learned_total++;
					if (vCategory <= 1)
						critical++;
					correct_kanji += res.getInt(res.getColumnIndex("timesCorrect_kanji"));
					total_kanji += vck = res.getInt(res.getColumnIndex("timesChecked_kanji"));
					correct_reading += res.getInt(res.getColumnIndex("timesCorrect_reading"));
					total_reading += vcr = res.getInt(res.getColumnIndex("timesChecked_reading"));
					correct_meaning += res.getInt(res.getColumnIndex("timesCorrect_meaning"));
					total_meaning += vcm = res.getInt(res.getColumnIndex("timesChecked_meaning"));

					maxReviews = Math.max(maxReviews, vck + vcr + vcm);
					
					if (nextReview == 0)
						nextReview = vNextReview;
					else
						nextReview = Math.min(nextReview, vNextReview);

					if (vNextReview < System.currentTimeMillis())
						review[0]++;
					else if (vNextReview < System.currentTimeMillis() + 1000 * 60 * 60 * 24)
						review[1 + (int) ((vNextReview - System.currentTimeMillis()) / (1000 * 60 * 60))]++;
					else if (vNextReview < System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 7)
						review[25 + (int) ((vNextReview - System.currentTimeMillis()) / (1000 * 60 * 60 * 12))]++;
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
					else if (vNextReview < System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 7)
						review[25 + (int) ((vNextReview - System.currentTimeMillis()) / (1000 * 60 * 60 * 12))]++;
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

		((LineGraphView) v.findViewById(R.id.line_graph_review)).setValues("", 0, 25, review);
		((LineGraphView) v.findViewById(R.id.line_graph_review2)).setValues("", 26, 13, review);
		
		long lastDate = preferences.getLong("lastReviewDate", 0);
		int[] review1 = StringHelper.toIntArray(preferences.getString("review1", ""));
		int[] review2 = StringHelper.toIntArray(preferences.getString("review2", ""));
		int[] learned1 = StringHelper.toIntArray(preferences.getString("learned1", ""));
		int[] learned2 = StringHelper.toIntArray(preferences.getString("learned2", ""));
		
		if (learned1.length != 30)
		{
			learned1 = new int[30];
			learned2 = new int[30];
		}
		
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTimeInMillis(lastDate);
		int days = new GregorianCalendar().get(Calendar.DAY_OF_YEAR) - calendar.get(Calendar.DAY_OF_YEAR);
		if (days < 0)
			days += calendar.isLeapYear(calendar.get(Calendar.YEAR)) ? 366 : 365;
		if (days > 30 || review1.length != 30)
		{
			review1 = new int[30];
			review2 = new int[30];
			learned1 = new int[30];
			learned2 = new int[30];
		}
		else
		{
			System.arraycopy(review1, days, review1, 0, 30 - days);
			System.arraycopy(review2, days, review2, 0, 30 - days);
			System.arraycopy(learned1, days, learned1, 0, 30 - days);
			System.arraycopy(learned2, days, learned2, 0, 30 - days);
			
			for (int i = 29; i >= 30 - days; --i)
			{
				review1[i] = 0;
				review2[i] = 0;
				learned1[i] = 0;
				learned2[i] = 0;
			}
		}
		preferences.edit()
			.putLong("lastReviewDate", System.currentTimeMillis())
			.putString("review1", StringHelper.toString(review1))
			.putString("review2", StringHelper.toString(review2))
			.putString("learned1", StringHelper.toString(learned1))
			.putString("learned2", StringHelper.toString(learned2))
			.apply();

		((GraphView) v.findViewById(R.id.graph_reviewed)).setValues(review1, review2);
		((GraphView) v.findViewById(R.id.graph_learned)).setValues(learned1, learned2);
		
		calendar = new GregorianCalendar();
		
		((TextView) v.findViewById(R.id.text_next_review_values)).setText(
			review[0] + "\n"
			+ review[1] + "\n"
			+ review[24]
		);

		((TextView) v.findViewById(R.id.text_learned_values)).setText(
			learned2[29] + "\n"
			+ learned2[30 - calendar.get(Calendar.DAY_OF_WEEK)]
		);
		
		((TextView) v.findViewById(R.id.text_review_stats_values)).setText(
			String.format("%.2f", preferences.getInt("reviewSum", 0) / (float) preferences.getInt("reviewCount", 0)) + "\n"
			+ preferences.getInt("reviewMax", 0)
		);
		
		((TextView) v.findViewById(R.id.text_review_time_stats_values)).setText(
			String.format("%.2f min\n%.2f min\n%.2f min", (float) preferences.getInt("reviewTimeToday", 0), preferences.getInt("reviewTimeSum", 0) / (double) preferences.getInt("reviewTimeCount", 0) / 60F, preferences.getInt("reviewTimeMax", 0) / 60F));
		
		((TextView) v.findViewById(R.id.text_learned_stats_values)).setText(
			String.format("%.2f", preferences.getInt("learnedSum", 0) / (float) preferences.getInt("learnedCount", 0)) + "\n"
			+ preferences.getInt("learnedMax", 0)
		);
		
		calendar.add(Calendar.DAY_OF_YEAR, -30);
		String date = DateFormat.getDateFormat(getContext()).format(calendar.getTime());
		((TextView) v.findViewById(R.id.text_date)).setText(date);
		((TextView) v.findViewById(R.id.text_date2)).setText(date);
		
		((TextView) v.findViewById(R.id.text_fun_stats_values)).setText(
			kanjiSet.size() + "\n"
			+ kanaCount + "\n"
			+ maxSyno + "\n"
			+ maxHomo + "\n"
			+ maxReviews
		);
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();

		dbHelper.close();
	}
}
