package com.taraxippus.vocab.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.ChangeBounds;
import android.transition.ChangeTransform;
import android.transition.TransitionInflater;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.taraxippus.vocab.ActivityAdd;
import com.taraxippus.vocab.ActivityAddKanji;
import com.taraxippus.vocab.ActivityDetail;
import com.taraxippus.vocab.ActivityDetailKanji;
import com.taraxippus.vocab.ActivityMain;
import com.taraxippus.vocab.ActivitySettings;
import com.taraxippus.vocab.R;
import com.taraxippus.vocab.dialog.DialogHelper;
import com.taraxippus.vocab.util.JishoHelper;
import com.taraxippus.vocab.util.NotificationHelper;
import com.taraxippus.vocab.util.OnProcessSuccessListener;
import com.taraxippus.vocab.util.StringHelper;
import com.taraxippus.vocab.view.LineGraphView;
import com.taraxippus.vocab.view.PercentageGraphView;
import com.taraxippus.vocab.vocabulary.DBHelper;
import com.taraxippus.vocab.vocabulary.QuestionType;
import com.taraxippus.vocab.vocabulary.Vocabulary;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import android.util.TypedValue;
import com.taraxippus.vocab.dialog.DialogQuizPreferences;

public class FragmentDetail extends Fragment
{
	private DBHelper dbHelper;
	private Vocabulary vocabulary;
	
	public FragmentDetail() {}

	public Fragment setDefaultTransitions(Context context)
	{
		this.setEnterTransition(TransitionInflater.from(context).inflateTransition(android.R.transition.explode));
		this.setReturnTransition(TransitionInflater.from(context).inflateTransition(android.R.transition.explode));

		TransitionSet set = new TransitionSet();
		set.addTransition(new ChangeTransform());
		ChangeBounds changeBounds = new ChangeBounds();
		changeBounds.setResizeClip(false);
		set.addTransition(changeBounds);

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
		return inflater.inflate(R.layout.fragment_detail, container, false);
	}

	@Override
	public void onViewCreated(final View v, Bundle savedInstanceState)
	{
		int id = getArguments().getInt("id");
		vocabulary = dbHelper.getVocabulary(id);
		
		if (vocabulary == null)
		{
			if (getFragmentManager() != null)
				getFragmentManager().popBackStack();
			return;
		}
		
		v.findViewById(R.id.card_kanji).setTransitionName("card" + id);
		
		final TextView kanji = (TextView)v.findViewById(R.id.text_kanji);
		kanji.setTransitionName("kanji" + id);
		kanji.setText(vocabulary.correctAnswer(QuestionType.KANJI));
		kanji.setTextLocale(Locale.JAPANESE);
		
		final TextView reading = (TextView)v.findViewById(R.id.text_reading);
		reading.setTransitionName("reading" + id);
		reading.setText(vocabulary.reading.length == 0 ? "" : vocabulary.correctAnswer(QuestionType.READING_INFO));
		
		final TextView meaning = (TextView)v.findViewById(R.id.text_meaning);
		meaning.setTransitionName("meaning" + id);
		meaning.setText(vocabulary.correctAnswer(QuestionType.MEANING_INFO));
		
		((TextView)v.findViewById(R.id.text_type)).setText(vocabulary.getType());
		((TextView) v.findViewById(R.id.text_category)).setText("Category: " + vocabulary.category);
		((TextView) v.findViewById(R.id.text_category_number)).setText("" + vocabulary.category);
		
		final ImageButton info = (ImageButton) v.findViewById(R.id.button_info);
		info.setVisibility(JishoHelper.isInternetAvailable(getContext()) ? View.VISIBLE : View.GONE);
		info.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					JishoHelper.search(getActivity(), vocabulary.kanji);
				}
			});
			
		final View card_stroke_order = v.findViewById(R.id.card_stroke_order);
		final View progress_stroke_order = v.findViewById(R.id.progress_stroke_order);
		final ViewGroup layout_stroke_order = (ViewGroup) v.findViewById(R.id.layout_stroke_order);
		
		final ImageButton stroke_order = (ImageButton) v.findViewById(R.id.button_stroke_order);
		stroke_order.setVisibility(JishoHelper.isStrokeOrderAvailable(getContext()) ? View.VISIBLE : View.GONE);
		stroke_order.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v1)
				{
					TransitionManager.beginDelayedTransition((ViewGroup) v);
					
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
		
		v.findViewById(R.id.button_overflow_stroke_order).setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View view)
				{
					PopupMenu popup = new PopupMenu(getContext(), view);
					popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
						{
							@Override
							public boolean onMenuItemClick(MenuItem item)
							{
								switch (item.getItemId()) 
								{
									case R.id.item_open_jisho_kanji:
										JishoHelper.search(getContext(), vocabulary.kanji + " #kanji");
										return true;
									case R.id.item_settings:
										getContext().startActivity(new Intent(getContext(), ActivitySettings.class).setAction(ActivitySettings.ACTION_STROKE_ORDER));
										return true;
									default:
										return false;
								}
							}
						});
					MenuInflater inflater = popup.getMenuInflater();
					inflater.inflate(R.menu.item_stroke_order, popup.getMenu());
					popup.show();
				}
			});
			
		final View card_sentences = v.findViewById(R.id.card_sentences);
		final View progress_sentences = v.findViewById(R.id.progress_sentences);
		final ViewGroup layout_sentences = (ViewGroup) v.findViewById(R.id.layout_sentences);
		
		final ImageButton sentences = (ImageButton) v.findViewById(R.id.button_sentences);
		sentences.setVisibility(JishoHelper.isInternetAvailable(getContext()) ? View.VISIBLE : View.GONE);
		sentences.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v1)
				{
					TransitionManager.beginDelayedTransition((ViewGroup) v);
					
					if (card_sentences.getVisibility() == View.GONE)
					{
						card_sentences.setVisibility(View.VISIBLE);

						if (progress_sentences.getVisibility() == View.VISIBLE)
						{
							final RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
							params.addRule(RelativeLayout.BELOW, R.id.text_title_sentences);

							JishoHelper.addExampleSentences(getContext(), vocabulary.kanji, vocabulary.meaning, layout_sentences, params, progress_sentences);
						}
					}
					else
						card_sentences.setVisibility(View.GONE);
				}
			});
		
		v.findViewById(R.id.button_overflow_sentences).setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View view)
				{
					PopupMenu popup = new PopupMenu(getContext(), view);
					popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
						{
							@Override
							public boolean onMenuItemClick(MenuItem item)
							{
								switch (item.getItemId()) 
								{
									case R.id.item_open_jisho_sentences:
										JishoHelper.search(getContext(), vocabulary.kanji + " " + vocabulary.meaning[0] +  " #sentences");
										return true;
									case R.id.item_settings:
										getContext().startActivity(new Intent(getContext(), ActivitySettings.class).setAction(ActivitySettings.ACTION_SENTENCES));
										return true;
									default:
										return false;
								}
							}
						});
					MenuInflater inflater = popup.getMenuInflater();
					inflater.inflate(R.menu.item_sentences, popup.getMenu());
					popup.show();
				}
			});
			
		final ImageButton sound = (ImageButton) v.findViewById(R.id.button_sound);
		sound.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					vocabulary.playSound(dbHelper);
				}
		});
		
		vocabulary.prepareSound(dbHelper, new OnProcessSuccessListener()
		{
				@Override
				public void onProcessSuccess(Object... args)
				{
					sound.setVisibility(View.VISIBLE);
				}
		});
		
		final TextView text_notes = (TextView) v.findViewById(R.id.text_notes);
		text_notes.setText(vocabulary.notes);
		text_notes.setTextLocale(Locale.JAPANESE);
		final ImageView image_notes = (ImageView) v.findViewById(R.id.image_notes);
		final View progress_image_notes = v.findViewById(R.id.progress_image_notes);
		
		if (!vocabulary.imageFile.isEmpty() && JishoHelper.isInternetAvailable(getContext()))
			new DownloadImageTask(image_notes, (ViewGroup) v, progress_image_notes).execute(vocabulary.imageFile);
		
		else if (this.vocabulary.notes.isEmpty())
			v.findViewById(R.id.card_notes).setVisibility(View.GONE);
	
		if (StringHelper.isKana(this.vocabulary.kanji))
		{
			v.findViewById(R.id.text_title_kanji_contained).setVisibility(View.GONE);
			v.findViewById(R.id.recycler_kanji_contained).setVisibility(View.GONE);
		}
		
		if (this.vocabulary.sameReading.length == 0)
		{
			v.findViewById(R.id.text_title_same_reading).setVisibility(View.GONE);
			v.findViewById(R.id.recycler_same_reading).setVisibility(View.GONE);
		}
			
		if (this.vocabulary.sameMeaning.length == 0)
		{
			v.findViewById(R.id.text_title_same_meaning).setVisibility(View.GONE);
			v.findViewById(R.id.recycler_same_meaning).setVisibility(View.GONE);
		}
		
		RecyclerView recyclerView = (RecyclerView)v.findViewById(R.id.recycler_kanji_contained);
		recyclerView.setHasFixedSize(true);
		recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
		recyclerView.setAdapter(new KanjiAdapter(getActivity(), dbHelper, recyclerView, StringHelper.getKanji(vocabulary.kanji)));
		
		recyclerView = (RecyclerView)v.findViewById(R.id.recycler_same_reading);
		recyclerView.setHasFixedSize(true);
		recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
		recyclerView.setAdapter(new VocabularyAdapter(getActivity(), dbHelper, recyclerView, vocabulary.sameReading));

		recyclerView = (RecyclerView)v.findViewById(R.id.recycler_same_meaning);
		recyclerView.setHasFixedSize(true);
		recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
		recyclerView.setAdapter(new VocabularyAdapter(getActivity(), dbHelper, recyclerView, vocabulary.sameMeaning));

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
			((TextView) v.findViewById(R.id.text_progress_reading)).setText(vocabulary.timesCorrect_reading + " / " + vocabulary.timesChecked_reading);
			final ProgressBar progress_reading = (ProgressBar)v.findViewById(R.id.progress_reading);
			progress_reading.setMax(vocabulary.timesChecked_reading);
			progress_reading.setProgress(vocabulary.timesCorrect_reading);
		}
		else
		{
			v.findViewById(R.id.text_reading_progress).setVisibility(View.GONE);
			v.findViewById(R.id.text_progress_reading).setVisibility(View.GONE);
			v.findViewById(R.id.progress_reading).setVisibility(View.GONE);
			((TextView) v.findViewById(R.id.text_kanji_progress)).setText("Success rate for entering kana:");
		}
		
		((TextView) v.findViewById(R.id.text_progress_meaning)).setText(vocabulary.timesCorrect_meaning + " / " + vocabulary.timesChecked_meaning);
		final ProgressBar progress_meaning = (ProgressBar)v.findViewById(R.id.progress_meaning);
		progress_meaning.setMax(vocabulary.timesChecked_meaning);
		progress_meaning.setProgress(vocabulary.timesCorrect_meaning);
		
		if (vocabulary.reading.length > 0)
		{
			((TextView) v.findViewById(R.id.text_streak_category)).setText("Kanji\nReading\nMeaning");
			((TextView) v.findViewById(R.id.text_streak_values)).setText(vocabulary.streak_kanji + " / " + vocabulary.streak_kanji_best + "\n" + vocabulary.streak_reading + " / " + vocabulary.streak_reading_best + "\n" + vocabulary.streak_meaning + " / " + vocabulary.streak_meaning_best);
		}
		else
		{
			((TextView) v.findViewById(R.id.text_streak_category)).setText("Kana\nMeaning");
			((TextView) v.findViewById(R.id.text_streak_values)).setText(vocabulary.streak_kanji + " / " + vocabulary.streak_kanji_best + "\n" + vocabulary.streak_meaning + " / " + vocabulary.streak_meaning_best);
		}
		
		final LineGraphView category_history = (LineGraphView) v.findViewById(R.id.line_graph_category);
		if (vocabulary.category_history[vocabulary.category_history.length - 2] >= 0)
			category_history.setValues(vocabulary.category_history);
		else
			category_history.setVisibility(View.GONE);
		
		if (vocabulary.learned)
			((TextView)v.findViewById(R.id.text_next_review)).setText("Next Review: " + (vocabulary.nextReview < System.currentTimeMillis() ? "Now" : new SimpleDateFormat().format(new Date(vocabulary.nextReview))));
		
		((TextView) v.findViewById(R.id.text_last_checked)).setText("Last Checked: " + (vocabulary.lastChecked <= 1 ? "Never" : new SimpleDateFormat().format(new Date(vocabulary.lastChecked))));
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
		
//		ValueAnimator animator = ValueAnimator.ofFloat(35, 75);
//		animator.setDuration(300);
//		animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() 
//		{
//				@Override
//				public void onAnimationUpdate(ValueAnimator valueAnimator) 
//				{
//					kanji.setTextSize(TypedValue.COMPLEX_UNIT_SP, (float) valueAnimator.getAnimatedValue());
//				}
//			});
//		animator.start();
//		
//		animator = ValueAnimator.ofFloat(15, 20);
//		animator.setDuration(300);
//		animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() 
//			{
//				@Override
//				public void onAnimationUpdate(ValueAnimator valueAnimator) 
//				{
//					reading.setTextSize(TypedValue.COMPLEX_UNIT_SP, (float) valueAnimator.getAnimatedValue());
//				}
//			});
//		animator.start();
//		
//		animator = ValueAnimator.ofFloat(15, 20);
//		animator.setDuration(300);
//		animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() 
//			{
//				@Override
//				public void onAnimationUpdate(ValueAnimator valueAnimator) 
//				{
//					meaning.setTextSize(TypedValue.COMPLEX_UNIT_SP, (float) valueAnimator.getAnimatedValue());
//				}
//			});
//		animator.start();
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
					JishoHelper.search(getActivity(), vocabulary.kanji + " #kanji");
					return true;
					
				case R.id.item_edit:
					Intent intent = new Intent(getContext(), ActivityAdd.class);
					intent.putExtra("id", getArguments().getInt("id"));
					startActivity(intent);
					return true;
					
				case R.id.item_quiz_preferences:
					DialogQuizPreferences d = new DialogQuizPreferences();
					d.setArguments(getArguments());
					d.show(getFragmentManager(), "quiz_preferences");
					return true;
				
				case R.id.item_reset:
					DialogHelper.createDialog(getContext(), "Reset", "Do you really want to reset this vocabulary? All progress and statistics will be lost!", "Reset", new DialogInterface.OnClickListener() 
						{
							public void onClick(DialogInterface dialog, int which) 
							{
								dbHelper.resetVocabulary(getArguments().getInt("id"));
								getContext().sendBroadcast(new Intent(getContext(), NotificationHelper.class));
								
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
								dbHelper.resetVocabularyCategory(vocabulary.lastChecked, getArguments().getInt("id"));
								getContext().sendBroadcast(new Intent(getContext(), NotificationHelper.class));
								
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
								getContext().sendBroadcast(new Intent(getContext(), NotificationHelper.class));
								
								
								dialog.dismiss();
								getFragmentManager().popBackStack();
							}
						});

					return true;
				
				case R.id.item_learn_add:
					dbHelper.updateVocabularyLearned(getArguments().getInt("id"), true);
					getContext().sendBroadcast(new Intent(getContext(), NotificationHelper.class));
				
					refreshDetailView();
					return true;
				
				case R.id.item_learn_remove:
					dbHelper.updateVocabularyLearned(getArguments().getInt("id"), false);
					getContext().sendBroadcast(new Intent(getContext(), NotificationHelper.class));
				
					refreshDetailView();
					return true;
				
				case R.id.item_cheat:
					vocabulary.answer(dbHelper, getContext(), vocabulary.kanji, QuestionType.KANJI, QuestionType.MEANING);
					if (vocabulary.reading.length > 0)
						vocabulary.answer(dbHelper, getContext(), vocabulary.reading[0], QuestionType.READING, QuestionType.KANJI);
					vocabulary.answer(dbHelper, getContext(), vocabulary.meaning[0], QuestionType.MEANING, QuestionType.KANJI);

					vocabulary.category++;
					vocabulary.lastChecked = System.currentTimeMillis();
					vocabulary.nextReview = vocabulary.lastChecked + Vocabulary.getNextReview(vocabulary.category);

					vocabulary.answered_kanji = false;
					vocabulary.answered_meaning = false;
					vocabulary.answered_reading = false;
					vocabulary.answered_correct = true;

					System.arraycopy(vocabulary.category_history, 1, vocabulary.category_history, 0, vocabulary.category_history.length - 1);
					vocabulary.category_history[vocabulary.category_history.length - 1] = vocabulary.category;

					dbHelper.updateVocabulary(vocabulary);
					getContext().sendBroadcast(new Intent(getContext(), NotificationHelper.class));
					refreshDetailView();
					return true;
				
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		inflater.inflate(R.menu.fragment_detail, menu);
		
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu)
	{
		super.onPrepareOptionsMenu(menu);
		
		menu.findItem(R.id.item_open_jisho_kanji).setVisible(vocabulary.reading.length > 0);
		menu.findItem(R.id.item_learn_add).setVisible(!vocabulary.learned);
		menu.findItem(R.id.item_learn_remove).setVisible(vocabulary.learned);
	}
	
	public void refreshDetailView()
	{
		TransitionManager.beginDelayedTransition((ViewGroup) getView());
		
		onViewCreated(getView(), null);
		getActivity().invalidateOptionsMenu();
	}
	
	@Override
	public void onResume()
	{
		super.onResume();

		refreshDetailView();
		getActivity().setTitle(vocabulary.kanji);
		
		if (getActivity() instanceof ActivityMain)
			((ActivityMain) getActivity()).setDisplayHomeAsUp(true);
	}

	@Override
	public void onPause()
	{
		super.onPause();
		
		if (getActivity() instanceof ActivityMain)
			((ActivityMain) getActivity()).setDisplayHomeAsUp(false);
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		
		dbHelper.close();
	}
	
	public static class DownloadImageTask extends AsyncTask<String, Void, Bitmap>
	{
		final ImageView bmImage;
		final ViewGroup layout;
		final View progress;

		public DownloadImageTask(ImageView bmImage, ViewGroup layout, View progess) 
		{
			this.bmImage = bmImage;
			this.layout = layout;
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
			//TransitionManager.beginDelayedTransition(layout);
			
			if (result != null)
			{
				bmImage.setImageBitmap(result);
				bmImage.setVisibility(View.VISIBLE);
			}
			
			progress.setVisibility(View.GONE);
		}
	}
	
	public static class VocabularyAdapter extends RecyclerView.Adapter<VocabularyAdapter.VocabularyViewHolder> implements View.OnClickListener
	{
		@Override
		public void onClick(View v)
		{
			if (context.findViewById(R.id.layout_content) == null)
			{
				context.startActivity(new Intent(context, ActivityDetail.class).putExtra("id", data[view.getChildAdapterPosition(v)]));
			}
			else
			{
				Bundle bundle = new Bundle();
				bundle.putInt("id", data[view.getChildAdapterPosition(v)]);
				
				Fragment fragment = new FragmentDetail().setDefaultTransitions(context);
				fragment.setReturnTransition(TransitionInflater.from(context).inflateTransition(android.R.transition.fade));
				fragment.setArguments(bundle);
				fragment.setEnterTransition(TransitionInflater.from(context).inflateTransition(android.R.transition.slide_bottom));

				View text_kanji = v.findViewById(R.id.text_kanji);
				FragmentTransaction ft = context.getFragmentManager().beginTransaction()
					.replace(R.id.layout_content, fragment)
					.addToBackStack("detail")
					.addSharedElement(v, v.getTransitionName())
					.addSharedElement(text_kanji, text_kanji.getTransitionName());
				ft.commit();
			}
		}

		public class VocabularyViewHolder extends RecyclerView.ViewHolder 
		{
			final TextView text_kanji;

			public VocabularyViewHolder(final View v) 
			{
				super(v);

				text_kanji = (TextView) v.findViewById(R.id.text_kanji);
				text_kanji.setTextLocale(Locale.JAPANESE);
				
				if (vertical)
				{
					v.getLayoutParams().width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, context.getResources().getDisplayMetrics());
					v.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
					
					text_kanji.getLayoutParams().width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 84, context.getResources().getDisplayMetrics());
					text_kanji.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
				}
				
				if (canRemove)
				{
					v.setOnLongClickListener(new View.OnLongClickListener()
						{
							@Override
							public boolean onLongClick(View p1)
							{
								PopupMenu popup = new PopupMenu(context, v);
								popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
									{
										@Override
										public boolean onMenuItemClick(MenuItem item)
										{
											int index = view.getChildAdapterPosition(v);
											int[] data1 = new int[data.length - 1];
											System.arraycopy(data, 0, data1, 0, index);
											System.arraycopy(data, index + 1, data1, index, data.length - index - 1);
											data = data1;
											cache = null;
											notifyItemRemoved(index);

											return true;
										}
									});
								popup.getMenu().add("Exclude");
								popup.show();
								return true;
							}
						});
				}
			}
		}

		final Activity context;
		final DBHelper dbHelper;
		final RecyclerView view;
		public int[] data;
		String[] cache;
		final boolean vertical, canRemove;
		
		public VocabularyAdapter(Activity context, DBHelper dbHelper, RecyclerView view, int[] data)
		{
			this(context, dbHelper, view, false, false, data);
		}
		
		public VocabularyAdapter(Activity context, DBHelper dbHelper, RecyclerView view, boolean vertical, boolean canRemove, int[] data)
		{
			this.context = context;
			this.dbHelper = dbHelper;
			this.view = view;
			this.data = data;
			this.vertical = vertical;
			this.canRemove = canRemove;
			
			if (data == null)
				this.data = new int[0];
		}

		@Override
		public VocabularyAdapter.VocabularyViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
		{
			View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_synonym, parent, false);
			v.setOnClickListener(this);

			return new VocabularyViewHolder(v);
		}

		@Override
		public void onBindViewHolder(VocabularyViewHolder holder, int position) 
		{
			int id = data[position];
			
			holder.itemView.setTransitionName("card" + id);
			holder.text_kanji.setTransitionName("kanji" + id);
			holder.text_kanji.setText(getKanjiAtPosition(position));
		}
		
		public String getKanjiAtPosition(int position)
		{
			if (data == null || data.length == 0)
				return "";
				
			if (cache == null || cache.length != data.length)
				cache = new String[data.length];
				
			if (cache[position] == null)
				cache[position] = dbHelper.getString(data[position], "kanji");
			
			return cache[position];
		}

		@Override
		public int getItemCount() 
		{
			return data.length;
		}
	}
	
	public static class VocabularySpanSizeLookup extends GridLayoutManager.SpanSizeLookup
	{
		VocabularyAdapter adapter;
		
		public VocabularySpanSizeLookup(VocabularyAdapter adapter)
		{
			this.adapter = adapter;
		}
		
		@Override
		public int getSpanSize(int position)
		{
			return adapter.getKanjiAtPosition(position).length();
		}
	}
	
	public static class KanjiAdapter extends RecyclerView.Adapter<KanjiAdapter.KanjiViewHolder> implements View.OnClickListener
	{
		@Override
		public void onClick(final View v)
		{
			if (data[view.getChildAdapterPosition(v)] == '々')
			{
				DialogHelper.createDialog(context, "Info: 々", "This is no real kanji character. It just is a shortcut to show that the preceeding kanji is repeated.\n時時 -> 時々");
				return;
			}
			
			if (!dbHelper.existsKanji(data[view.getChildAdapterPosition(v)]) &&!JishoHelper.isInternetAvailable(context))
			{
				DialogHelper.createDialog(context, "Unknown Kanji", "This character is not in your kanji database. Do you want to add it now?",
					"Add", new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface p1, int p2)
						{
							context.startActivity(new Intent(context, ActivityAddKanji.class).putExtra("kanji", data[view.getChildAdapterPosition(v)]));
						}
					});
				
				return;
			}
			
			if (context.findViewById(R.id.layout_content) == null)
				context.startActivity(new Intent(context, ActivityDetailKanji.class).putExtra("kanji", data[view.getChildAdapterPosition(v)]));
			
			else
			{
				Bundle bundle = new Bundle();
				bundle.putChar("kanji", data[view.getChildAdapterPosition(v)]);

				Fragment fragment = new FragmentDetailKanji().setDefaultTransitions(context);
				fragment.setReturnTransition(TransitionInflater.from(context).inflateTransition(android.R.transition.fade));
				fragment.setArguments(bundle);
				fragment.setEnterTransition(TransitionInflater.from(context).inflateTransition(android.R.transition.slide_bottom));

				View text_kanji = v.findViewById(R.id.text_kanji);
				FragmentTransaction ft = context.getFragmentManager().beginTransaction()
					.replace(R.id.layout_content, fragment)
					.addToBackStack("detail")
					.addSharedElement(v, v.getTransitionName())
					.addSharedElement(text_kanji, text_kanji.getTransitionName());
				ft.commit();
			}
		}

		public class KanjiViewHolder extends RecyclerView.ViewHolder 
		{
			final TextView text_kanji;

			public KanjiViewHolder(final View v) 
			{
				super(v);

				text_kanji = (TextView) v.findViewById(R.id.text_kanji);
				text_kanji.setTextLocale(Locale.JAPANESE);
				
				((CardView) v).setRadius(0);
				
				if (vertical)
				{
					v.getLayoutParams().width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, context.getResources().getDisplayMetrics());
					v.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;

					text_kanji.getLayoutParams().width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 84, context.getResources().getDisplayMetrics());
					text_kanji.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
				}
				
				if (canRemove)
				{
					v.setOnLongClickListener(new View.OnLongClickListener()
					{
							@Override
							public boolean onLongClick(View p1)
							{
								PopupMenu popup = new PopupMenu(context, v);
								popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
									{
										@Override
										public boolean onMenuItemClick(MenuItem item)
										{
											int index = view.getChildAdapterPosition(v);
											char[] data1 = new char[data.length - 1];
											System.arraycopy(data, 0, data1, 0, index);
											System.arraycopy(data, index + 1, data1, index, data.length - index - 1);
											data = data1;
											notifyItemRemoved(index);

											return true;
										}
									});
								popup.getMenu().add("Exclude");
								popup.show();
								return true;
							}
					});
				}
			}
		}

		final Activity context;
		final DBHelper dbHelper;
		final RecyclerView view;
		public char[] data;
		final boolean vertical, canRemove;
		
		public KanjiAdapter(Activity context, DBHelper dbHelper, RecyclerView view, char[] data)
		{
			this(context, dbHelper, view, false, false, data);
		}
		

		public KanjiAdapter(Activity context, DBHelper dbHelper, RecyclerView view, boolean vertical, boolean canRemove, char[] data)
		{
			this.context = context;
			this.dbHelper = dbHelper;
			this.view = view;
			this.data = data;
			this.vertical = vertical;
			this.canRemove = canRemove;
			
			if (data == null)
				this.data = new char[0];
		}

		@Override
		public KanjiAdapter.KanjiViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
		{
			View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_synonym, parent, false);
			v.setOnClickListener(this);

			return new KanjiViewHolder(v);
		}

		@Override
		public void onBindViewHolder(KanjiViewHolder holder, int position) 
		{
			char id = data[position];

			holder.itemView.setTransitionName("card" + id);
			holder.text_kanji.setTransitionName("kanji" + id);
			holder.text_kanji.setText("" + id);
		}

		@Override
		public int getItemCount() 
		{
			return data.length;
		}
	}
}
