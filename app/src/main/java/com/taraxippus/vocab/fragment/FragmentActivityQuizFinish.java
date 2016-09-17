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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.taraxippus.vocab.ActivityDetail;
import com.taraxippus.vocab.ActivityMain;
import com.taraxippus.vocab.R;
import com.taraxippus.vocab.dialog.LearnNextDialog;
import com.taraxippus.vocab.util.NotificationHelper;
import com.taraxippus.vocab.util.StringHelper;
import com.taraxippus.vocab.view.GraphView;
import com.taraxippus.vocab.view.LineGraphView;
import com.taraxippus.vocab.view.PercentageView;
import com.taraxippus.vocab.vocabulary.DBHelper;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

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
		
		getContext().sendBroadcast(new Intent(getContext(), NotificationHelper.class));
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.activity_quiz_finish, container, false);
	}

	@Override
	public void onViewCreated(View v, Bundle savedInstanceState)
	{
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
		v.findViewById(R.id.button_learn).setOnClickListener(this);
		
		((LineGraphView) v.findViewById(R.id.line_graph_quiz)).setValues("#noNumbers", getArguments().getIntArray("history_quiz"));
		
		int[] review = new int[25];
	
		Cursor res = dbHelper.getReadableDatabase().rawQuery("SELECT nextReview FROM vocab WHERE learned = 1 AND nextReview < ? ", new String[] {"" + (System.currentTimeMillis() + 1000 * 60 * 60 * 24)});
		res.moveToFirst();

		if (res.getCount() > 0)
		{
			long vNextReview;
			
			do
			{
				vNextReview = res.getLong(res.getColumnIndex("nextReview"));

				if (vNextReview < System.currentTimeMillis())
					review[0]++;
				else
					review[1 + (int) ((vNextReview - System.currentTimeMillis()) / (1000 * 60 * 60))]++;
			}
			while (res.moveToNext());
		}
		
		res.close();

		int reviews1 = 0;
		for (int i = 0; i < review.length; i++)
			review[i] = (reviews1 += review[i]);
			
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
		
		RecyclerView recyclerView = (RecyclerView)v.findViewById(R.id.recycler_plus);
		recyclerView.setHasFixedSize(true);
		recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
		recyclerView.setAdapter(new KanjiAdapter(recyclerView, getArguments().getStringArrayList("vocabularies_plus"), Gravity.LEFT));	
		
		recyclerView = (RecyclerView)v.findViewById(R.id.recycler_neutral);
		recyclerView.setHasFixedSize(true);
		recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
		recyclerView.setAdapter(new KanjiAdapter(recyclerView, getArguments().getStringArrayList("vocabularies_neutral"), Gravity.CENTER));	
		
		recyclerView = (RecyclerView)v.findViewById(R.id.recycler_minus);
		recyclerView.setHasFixedSize(true);
		recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
		recyclerView.setAdapter(new KanjiAdapter(recyclerView, getArguments().getStringArrayList("vocabularies_minus"), Gravity.RIGHT));	
	}

	@Override
	public void onClick(View v)
	{
		if (v.getId() == R.id.button_home)
			startActivity(new Intent(getContext(), ActivityMain.class));
			
		else
			new LearnNextDialog().show(getFragmentManager(), "learn");
	}

	@Override
	public void onResume()
	{
		super.onResume();
		
		if (getActivity() != null)
			getActivity().setTitle("Finished Quiz!");
	}
	
	public class KanjiAdapter extends RecyclerView.Adapter<KanjiAdapter.ViewHolder> implements View.OnClickListener
	{
		@Override
		public void onClick(View v)
		{
			getContext().startActivity(new Intent(getContext(), ActivityDetail.class).putExtra("id", dbHelper.getId(data.get(view.getChildAdapterPosition(v)))));
		}

		public class ViewHolder extends RecyclerView.ViewHolder 
		{
			final TextView text_kanji;

			public ViewHolder(View v) 
			{
				super(v);

				text_kanji = (TextView) v;
				text_kanji.setGravity(gravity);
				text_kanji.setTextLocale(Locale.JAPANESE);
			}
		}

		final RecyclerView view;
		final ArrayList<String> data;
		final int gravity;

		public KanjiAdapter(RecyclerView view, ArrayList<String> data, int gravity)
		{
			this.view = view;
			this.data = data;
			this.gravity = gravity;
		}

		@Override
		public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
		{
			View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_kanji, parent, false);
			v.setOnClickListener(this);
			
			return new ViewHolder(v);
		}

		@Override
		public void onBindViewHolder(ViewHolder holder, int position) 
		{
			holder.text_kanji.setText(data.get(position));
		}

		@Override
		public int getItemCount() 
		{
			return data.size();
		}
	}
}
