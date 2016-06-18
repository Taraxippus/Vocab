package com.taraxippus.vocab.fragment;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.ChangeBounds;
import android.transition.ChangeTransform;
import android.transition.TransitionInflater;
import android.transition.TransitionSet;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.taraxippus.vocab.AddActivity;
import com.taraxippus.vocab.R;
import com.taraxippus.vocab.util.DialogHelper;
import com.taraxippus.vocab.util.JishoHelper;
import com.taraxippus.vocab.util.OnProcessSuccessListener;
import com.taraxippus.vocab.view.LineGraphView;
import com.taraxippus.vocab.view.PercentageGraphView;
import com.taraxippus.vocab.vocabulary.DBHelper;
import com.taraxippus.vocab.vocabulary.QuestionType;
import com.taraxippus.vocab.vocabulary.Vocabulary;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import android.view.Menu;
import com.taraxippus.vocab.MainActivity;
import android.view.MenuInflater;
import com.taraxippus.vocab.vocabulary.ImportType;

public class FragmentDetail extends Fragment
{
	DBHelper dbHelper;
	Vocabulary vocabulary;
	
	public FragmentDetail() {}

	public Fragment setDefaultTransitions(Context context)
	{
		this.setEnterTransition(TransitionInflater.from(context).inflateTransition(android.R.transition.slide_bottom));
		this.setReturnTransition(TransitionInflater.from(context).inflateTransition(android.R.transition.fade));

		TransitionSet set = new TransitionSet();
        set.setOrdering(TransitionSet.ORDERING_TOGETHER);

		set.addTransition(new ChangeTransform());
		set.addTransition(new ChangeBounds());

		this.setSharedElementEnterTransition(set);
		this.setSharedElementReturnTransition(set);

		this.setAllowEnterTransitionOverlap(false);
		this.setAllowReturnTransitionOverlap(false);
		
		return this;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		dbHelper = new DBHelper(getContext());
		setHasOptionsMenu(true);
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		final View v = inflater.inflate(R.layout.fragment_detail, container, false);
		
		int id = getArguments().getInt("id");
		vocabulary = dbHelper.getVocabulary(id);
		
		if (vocabulary == null)
		{
			System.out.println("Couldn`\'t find vocabulary; id=" + id);
			return v;
		}
		
		v.findViewById(R.id.card_kanji).setTransitionName("card" + id);
		
		final TextView kanji = (TextView)v.findViewById(R.id.text_kanji);
		kanji.setTransitionName("kanji" + id);
		kanji.setText(vocabulary.correctAnswer(QuestionType.KANJI));

		final TextView reading = (TextView)v.findViewById(R.id.text_reading);
		reading.setTransitionName("reading" + id);
		reading.setText(vocabulary.correctAnswer(QuestionType.READING));
		
		final TextView meaning = (TextView)v.findViewById(R.id.text_meaning);
		meaning.setTransitionName("meaning" + id);
		meaning.setText(vocabulary.correctAnswer(QuestionType.MEANING_INFO));
		
		((TextView)v.findViewById(R.id.text_type)).setText(vocabulary.getType());
		((TextView) v.findViewById(R.id.text_category)).setText("Category: " + vocabulary.category);
		((TextView) v.findViewById(R.id.text_category_number)).setText("" + vocabulary.category);
		
		final View card_stroke_order = v.findViewById(R.id.card_stroke_order);
		final View progress_stroke_order = v.findViewById(R.id.progress_stroke_order);
		final ViewGroup layout_stroke_order = (ViewGroup) v.findViewById(R.id.layout_stroke_order);
		
		final ImageButton stroke_order = (ImageButton) v.findViewById(R.id.button_stroke_order);
		stroke_order.setVisibility(JishoHelper.isStrokeOrderAvailable(getContext()) ? View.VISIBLE : View.GONE);
		stroke_order.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					if (card_stroke_order.getVisibility() == View.GONE)
					{
						card_stroke_order.setVisibility(View.VISIBLE);
						
						if (progress_stroke_order.getVisibility() == View.VISIBLE)
						{
							final RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
							params.addRule(RelativeLayout.BELOW, R.id.text_title_stroke_order);
							
							JishoHelper.addStrokeOrderView(getContext(), vocabulary.kanji, layout_stroke_order, params, progress_stroke_order, false, true);
						}
					}
					else
						card_stroke_order.setVisibility(View.GONE);
				}
			});
		
		
		final ImageButton sound = (ImageButton) v.findViewById(R.id.button_sound);
		sound.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					vocabulary.playSound(getContext());
				}
		});
		
		vocabulary.prepareSound(getContext(), new OnProcessSuccessListener()
		{
				@Override
				public void onProcessSuccess(Object... args)
				{
					sound.setVisibility(View.VISIBLE);
				}
		});
		
		
		((TextView)v.findViewById(R.id.text_notes)).setText(vocabulary.notes);
		final ImageView image_notes = (ImageView) v.findViewById(R.id.image_notes);
		final View progress_image_notes = v.findViewById(R.id.progress_image_notes);
		
		if (!vocabulary.imageFile.isEmpty() && JishoHelper.isInternetAvailable(getContext()))
			new DownloadImageTask(image_notes, progress_image_notes).execute(vocabulary.imageFile);
		
		else if (this.vocabulary.notes.isEmpty())
			v.findViewById(R.id.card_notes).setVisibility(View.GONE);
	
		if (this.vocabulary.sameReading.isEmpty())
			v.findViewById(R.id.card_same_reading).setVisibility(View.GONE);
			
		if (this.vocabulary.sameMeaning.isEmpty())
			v.findViewById(R.id.card_same_meaning).setVisibility(View.GONE);
			
		if (!this.vocabulary.sameMeaning.isEmpty() && !this.vocabulary.sameReading.isEmpty())
		{
			View card = v.findViewById(R.id.card_same_reading);
			LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) card.getLayoutParams();
			params.bottomMargin = 0;
			card.setLayoutParams(params);
			card = v.findViewById(R.id.card_same_meaning);
			params = (LinearLayout.LayoutParams) card.getLayoutParams();
			params.topMargin = 0;
			card.setLayoutParams(params);
		}
		
		RecyclerView recyclerView = (RecyclerView)v.findViewById(R.id.recycler_same_meaning);
		recyclerView.setHasFixedSize(true);
		recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
		recyclerView.setAdapter(new SynonymAdapter(dbHelper, recyclerView, vocabulary.sameReading));

		recyclerView = (RecyclerView)v.findViewById(R.id.recycler_same_reading);
		recyclerView.setHasFixedSize(true);
		recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
		recyclerView.setAdapter(new SynonymAdapter(dbHelper, recyclerView, vocabulary.sameMeaning));

		((TextView) v.findViewById(R.id.text_progress_total)).setText((vocabulary.timesCorrect_kanji + vocabulary.timesCorrect_reading + vocabulary.timesCorrect_meaning) + " / " + (vocabulary.timesChecked_kanji + vocabulary.timesChecked_reading + vocabulary.timesChecked_meaning));
		final ProgressBar progress_total = (ProgressBar)v.findViewById(R.id.progress_total);
		progress_total.setMax(vocabulary.timesChecked_kanji + vocabulary.timesChecked_reading + vocabulary.timesChecked_meaning);
		progress_total.setProgress(vocabulary.timesCorrect_kanji + vocabulary.timesCorrect_reading + vocabulary.timesCorrect_meaning);
		
		((TextView) v.findViewById(R.id.text_progress_kanji)).setText(vocabulary.timesCorrect_kanji + " / " + vocabulary.timesChecked_kanji);
		final ProgressBar progress_kanji = (ProgressBar)v.findViewById(R.id.progress_kanji);
		progress_kanji.setMax(vocabulary.timesChecked_kanji);
		progress_kanji.setProgress(vocabulary.timesCorrect_kanji);
		
		if (vocabulary.reading.length > 0)
		{
			((TextView)v.findViewById(R.id.text_progress_reading)).setText(vocabulary.timesCorrect_reading + " / " + vocabulary.timesChecked_reading);
			final ProgressBar progress_reading = (ProgressBar)v.findViewById(R.id.progress_reading);
			progress_reading.setMax(vocabulary.timesChecked_reading);
			progress_reading.setProgress(vocabulary.timesCorrect_reading);
		}
		else
		{
			v.findViewById(R.id.text_reading_progress).setVisibility(View.GONE);
			v.findViewById(R.id.text_progress_reading).setVisibility(View.GONE);
			v.findViewById(R.id.progress_reading).setVisibility(View.GONE);
		}
		
		((TextView) v.findViewById(R.id.text_progress_meaning)).setText(vocabulary.timesCorrect_meaning + " / " + vocabulary.timesChecked_meaning);
		final ProgressBar progress_meaning = (ProgressBar)v.findViewById(R.id.progress_meaning);
		progress_meaning.setMax(vocabulary.timesChecked_meaning);
		progress_meaning.setProgress(vocabulary.timesCorrect_meaning);
		
		if (vocabulary.reading.length > 0)
		{
			((TextView) v.findViewById(R.id.text_streak_category)).setText("Kanji\nReading\nMeaning");
			((TextView) v.findViewById(R.id.text_streak_values)).setText(vocabulary.streak_kanji + "\n" + vocabulary.streak_reading + "\n" + vocabulary.streak_meaning);
		
			((TextView) v.findViewById(R.id.text_streak_longest_category)).setText("Kanji\nReading\nMeaning");
			((TextView) v.findViewById(R.id.text_streak_longest_values)).setText(vocabulary.streak_kanji_best + "\n" + vocabulary.streak_reading_best + "\n" + vocabulary.streak_meaning_best);
		}
		else
		{
			((TextView) v.findViewById(R.id.text_streak_category)).setText("Kanji\nMeaning");
			((TextView) v.findViewById(R.id.text_streak_values)).setText(vocabulary.streak_kanji + "\n" + vocabulary.streak_meaning);

			((TextView) v.findViewById(R.id.text_streak_longest_category)).setText("Kanji\nMeaning");
			((TextView) v.findViewById(R.id.text_streak_longest_values)).setText(vocabulary.streak_kanji_best + "\n" + vocabulary.streak_meaning_best);
		}
		
		final LineGraphView category_history = (LineGraphView) v.findViewById(R.id.line_graph_category);
		if (vocabulary.category_history[vocabulary.category_history.length - 2] >= 0)
			category_history.setValues(vocabulary.category_history);
		else
			category_history.setVisibility(View.GONE);
		
		if (vocabulary.learned)
			((TextView)v.findViewById(R.id.text_next_review)).setText("Next Review: " + (vocabulary.lastChecked + vocabulary.getNextReview() < System.currentTimeMillis() ? "Now" : new SimpleDateFormat().format(new Date(vocabulary.lastChecked + vocabulary.getNextReview()))));
		
		((TextView) v.findViewById(R.id.text_last_checked)).setText("Last Checked: " + (vocabulary.lastChecked == 0 ? "Never" : new SimpleDateFormat().format(new Date(vocabulary.lastChecked))));
		((TextView) v.findViewById(R.id.text_added)).setText("Added: " + (new SimpleDateFormat().format(new Date(vocabulary.added))));
		
		if (this.vocabulary.lastChecked > 0)
		{
			if (this.vocabulary.meaning.length > 1)
			{
				v.findViewById(R.id.text_meaning_entered).setVisibility(View.VISIBLE);
				
				final PercentageGraphView graph_meaning = (PercentageGraphView) v.findViewById(R.id.percentage_graph_meaning);
				graph_meaning.setVisibility(View.VISIBLE);
				graph_meaning.setValues(vocabulary.meaning, vocabulary.meaning_used, true, false);
			}
			
			if (this.vocabulary.reading.length > 1)
			{
				v.findViewById(R.id.text_reading_entered).setVisibility(View.VISIBLE);

				PercentageGraphView graph_reading = (PercentageGraphView)v.findViewById(R.id.percentage_graph_reading);
				graph_reading.setVisibility(View.VISIBLE);
				graph_reading.setValues(vocabulary.reading_trimed, vocabulary.reading_used, true, false);
				
			}
		}
		
		return v;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
				case R.id.item_open_jisho:
					JishoHelper.search(getActivity(), vocabulary.kanji);

					return true;
				
				case R.id.item_open_jisho_kanji:
					JishoHelper.search(getActivity(), vocabulary.kanji + "%23kanji");

					return true;
				case R.id.item_edit:
					Intent intent = new Intent(getContext(), AddActivity.class);
					intent.putExtra("id", getArguments().getInt("id"));
					startActivity(intent);

					return true;
				case R.id.item_reset:
					DialogHelper.createDialog(getContext(), "Reset", "Do you really want to reset this vocabulary? All progress and statistics will be lost!", "Reset", new DialogInterface.OnClickListener() 
						{
							public void onClick(DialogInterface dialog, int which) 
							{
								dbHelper.resetVocabulary(getArguments().getInt("id"));
								refreshDetailView();
								dialog.dismiss();
							}
						});


					return true;
				case R.id.item_reset_category:
					DialogHelper.createDialog(getContext(), "Reset", "Do you really want to reset the category of this vocabulary? You'll have to review it again.", "Reset", new DialogInterface.OnClickListener() 
						{
							public void onClick(DialogInterface dialog, int which) 
							{
								dbHelper.resetVocabularyCategory(getArguments().getInt("id"));
								refreshDetailView();
								dialog.dismiss();
							}
						});

					return true;
					
				case R.id.item_delete:
					DialogHelper.createDialog(getContext(), "Delete", "Do you really want to delete this vocabulary?", "Delete", new DialogInterface.OnClickListener() 
						{
							public void onClick(DialogInterface dialog, int which) 
							{
								dbHelper.deleteVocabulary(getArguments().getInt("id"));
	
								getFragmentManager().popBackStack();
								dialog.dismiss();
							}
						});

					return true;
				
				case R.id.item_learn_add:
					dbHelper.updateVocabularyLearned(getArguments().getInt("id"), true);
					refreshDetailView();
					return true;
				
				case R.id.item_learn_remove:
					dbHelper.updateVocabularyLearned(getArguments().getInt("id"), false);
					refreshDetailView();
					return true;
				
				case R.id.item_cheat:
					vocabulary.answer(getContext(), vocabulary.kanji, QuestionType.KANJI, QuestionType.MEANING);
					if (vocabulary.reading.length > 0)
						vocabulary.answer(getContext(), vocabulary.reading[0], QuestionType.READING, QuestionType.KANJI);
					vocabulary.answer(getContext(), vocabulary.meaning[0], QuestionType.MEANING, QuestionType.KANJI);

					vocabulary.category++;
					vocabulary.lastChecked = System.currentTimeMillis();

					vocabulary.answered_kanji = false;
					vocabulary.answered_meaning = false;
					vocabulary.answered_reading = false;
					vocabulary.answered_correct = true;

					System.arraycopy(vocabulary.category_history, 1, vocabulary.category_history, 0, vocabulary.category_history.length - 1);
					vocabulary.category_history[vocabulary.category_history.length - 1] = vocabulary.category;

					dbHelper.updateVocabulary(vocabulary, -1, ImportType.REPLACE);
					refreshDetailView();
					return true;
				
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		inflater.inflate(R.menu.detail, menu);
		
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu)
	{
		super.onPrepareOptionsMenu(menu);
		
		menu.findItem(R.id.item_learn_add).setVisible(!vocabulary.learned);
		menu.findItem(R.id.item_learn_remove).setVisible(vocabulary.learned);
	}
	
	public void refreshDetailView()
	{
		getFragmentManager().popBackStack();
		
		this.setEnterTransition(TransitionInflater.from(getContext()).inflateTransition(android.R.transition.slide_bottom));
		getFragmentManager().beginTransaction().replace(R.id.content_frame, this).addToBackStack("refresh").commit();
	}
	
	@Override
	public void onResume()
	{
		super.onResume();

		getActivity().setTitle(vocabulary.kanji);
		
		if (getActivity() instanceof MainActivity)
			((MainActivity) getActivity()).setDisplayHomeAsUp(true);
	}

	@Override
	public void onPause()
	{
		super.onPause();
		
		if (getActivity() instanceof MainActivity)
			((MainActivity) getActivity()).setDisplayHomeAsUp(false);
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		
		dbHelper.close();
	}
	
	private class DownloadImageTask extends AsyncTask<String, Void, Bitmap>
	{
		final ImageView bmImage;
		final View progress;

		public DownloadImageTask(ImageView bmImage, View progess) 
		{
			this.bmImage = bmImage;
			this.progress = progess;
			
			progess.setVisibility(View.VISIBLE);
		}

		protected Bitmap doInBackground(String... urls) 
		{
			String urldisplay = urls[0];
			Bitmap icon = null;
			InputStream in = null;
			try
			{
				in = new java.net.URL(urldisplay).openStream();
				icon = BitmapFactory.decodeStream(in);
			} 
			catch (Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				try
				{
					if (in != null)
						in.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
			return icon;
		}

		protected void onPostExecute(Bitmap result)
		{
			if (result != null)
			{
				bmImage.setImageBitmap(result);
				bmImage.setVisibility(View.VISIBLE);
			}
			
			progress.setVisibility(View.GONE);
		}
	}
	
	public class SynonymAdapter extends RecyclerView.Adapter<SynonymAdapter.SynonymViewHolder> implements View.OnClickListener
	{
		@Override
		public void onClick(View v)
		{
			Fragment fragment = new FragmentDetail().setDefaultTransitions(getContext());
			Bundle bundle = new Bundle();
			bundle.putInt("id", dbHelper.getId(data.get(view.getChildAdapterPosition(v)).kanji));
			fragment.setArguments(bundle);
			fragment.setEnterTransition(TransitionInflater.from(getContext()).inflateTransition(android.R.transition.slide_bottom));
			
			View text_kanji = v.findViewById(R.id.text_kanji);
			FragmentTransaction ft = getActivity().getFragmentManager().beginTransaction()
				.replace(R.id.content_frame, fragment)
				.addToBackStack("detail")
				.addSharedElement(v, v.getTransitionName())
				.addSharedElement(text_kanji, text_kanji.getTransitionName());
			ft.commit();
		}

		public class SynonymViewHolder extends RecyclerView.ViewHolder 
		{
			final TextView text_kanji;

			public SynonymViewHolder(View v) 
			{
				super(v);

				text_kanji = (TextView) v.findViewById(R.id.text_kanji);
			}
		}

		final DBHelper dbHelper;
		final RecyclerView view;
		final ArrayList<Vocabulary> data;

		public SynonymAdapter(DBHelper dbHelper, RecyclerView view, ArrayList<Vocabulary> data)
		{
			this.dbHelper = dbHelper;
			this.view = view;
			this.data = data;
		}

		@Override
		public SynonymAdapter.SynonymViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
		{
			View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_synonym, parent, false);
			v.setOnClickListener(this);

			return new SynonymViewHolder(v);
		}

		@Override
		public void onBindViewHolder(SynonymViewHolder holder, int position) 
		{
			Vocabulary v = data.get(position);
			int pos = dbHelper.getId(v.kanji);

			holder.itemView.setTransitionName("card" + pos);
			holder.text_kanji.setTransitionName("kanji" + pos);
			holder.text_kanji.setText(v.kanji);
		}

		@Override
		public int getItemCount() 
		{
			return data.size();
		}
	}
}
