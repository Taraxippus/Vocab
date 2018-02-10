package com.taraxippus.vocab.fragment;

import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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
import com.taraxippus.vocab.ActivityAddKanji;
import com.taraxippus.vocab.ActivityMain;
import com.taraxippus.vocab.ActivitySettings;
import com.taraxippus.vocab.R;
import com.taraxippus.vocab.dialog.DialogHelper;
import com.taraxippus.vocab.util.JishoHelper;
import com.taraxippus.vocab.util.NotificationHelper;
import com.taraxippus.vocab.util.OnProcessSuccessListener;
import com.taraxippus.vocab.view.LineGraphView;
import com.taraxippus.vocab.view.PercentageGraphView;
import com.taraxippus.vocab.vocabulary.DBHelper;
import com.taraxippus.vocab.vocabulary.Kanji;
import com.taraxippus.vocab.vocabulary.QuestionType;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FragmentDetailKanji extends Fragment
{
	private DBHelper dbHelper;
	Kanji kanji;
	boolean online;
	
	public FragmentDetailKanji() {}

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
		return inflater.inflate(R.layout.fragment_detail_kanji, container, false);
	}

	@Override
	public void onViewCreated(final View v, Bundle savedInstanceState)
	{
		if (getArguments().getChar("kanji", '\n') != '\n')
		{
			if (dbHelper.existsKanji(getArguments().getChar("kanji")))
				kanji = dbHelper.getKanji(getArguments().getChar("kanji"));
			
			else if (!online)
			{
				online = true;
				v.findViewById(R.id.card_stats_kanji).setVisibility(View.GONE);
				final View progress_jisho = v.findViewById(R.id.progress_jisho);
				progress_jisho.setVisibility(View.VISIBLE);

				kanji = new Kanji(getArguments().getChar("kanji"));
				kanji.vocabularies = dbHelper.findVocabulariesForKanji(dbHelper.getReadableDatabase(), kanji.kanji);
				JishoHelper.importKanji(getContext(), kanji, new OnProcessSuccessListener()
					{
						@Override
						public void onProcessSuccess(Object[] args)
						{
							TransitionManager.beginDelayedTransition((ViewGroup) v);
							
							progress_jisho.setVisibility(View.GONE);
							onViewCreated(getView(), null);
						}
					});
			}
		}
			
		if (kanji == null || v == null)
		{
			if (getFragmentManager() != null)
				getFragmentManager().popBackStack();
			return;
		}
		
		v.findViewById(R.id.card_kanji).setTransitionName("card" + kanji.kanji);

		final TextView text_kanji = (TextView)v.findViewById(R.id.text_kanji);
		text_kanji.setTransitionName("kanji" + kanji.kanji);
		text_kanji.setText(kanji.correctAnswer(QuestionType.KANJI));
		text_kanji.setTextLocale(Locale.JAPANESE);

		final TextView meaning = (TextView)v.findViewById(R.id.text_meaning);
		
		if (kanji.reading_kun != null)
		{
			final TextView reading = (TextView)v.findViewById(R.id.text_reading);
			reading.setTransitionName("reading" + kanji.kanji);
			reading.setText(kanji.correctAnswer(QuestionType.READING_INFO));
			
			meaning.setTransitionName("meaning" + kanji.kanji);
			meaning.setText(kanji.correctAnswer(QuestionType.MEANING_INFO));
		}
		else
			meaning.setText("Loading...");
		
		((TextView)v.findViewById(R.id.text_strokes)).setText(kanji.strokes + (kanji.strokes == 1 ? " Stroke" : " Strokes"));
		
		if (!online)
		{
			((TextView) v.findViewById(R.id.text_category)).setText("Category: " + kanji.category);
			((TextView) v.findViewById(R.id.text_category_number)).setText("" + kanji.category);
		}
		
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

							JishoHelper.addStrokeOrderView(getContext(), "" + kanji.kanji, layout_stroke_order, params, progress_stroke_order, false, true);
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
										JishoHelper.search(getContext(), kanji.kanji + " #kanji");
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

		final View card_words = v.findViewById(R.id.card_words);
		final View progress_words = v.findViewById(R.id.progress_words);
		final ViewGroup layout_words = (ViewGroup) v.findViewById(R.id.layout_words);

		final ImageButton words = (ImageButton) v.findViewById(R.id.button_words);
		words.setVisibility(JishoHelper.isInternetAvailable(getContext()) ? View.VISIBLE : View.GONE);
		words.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v1)
				{
					TransitionManager.beginDelayedTransition((ViewGroup) v);
					
					if (card_words.getVisibility() == View.GONE)
					{
						card_words.setVisibility(View.VISIBLE);

						if (progress_words.getVisibility() == View.VISIBLE)
						{
							final RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
							params.addRule(RelativeLayout.BELOW, R.id.text_title_words);

							JishoHelper.addReadingCompounds(getContext(), kanji.kanji, layout_words, params, progress_words);
						}
					}
					else
						card_words.setVisibility(View.GONE);
				}
			});
			
		final TextView text_notes = (TextView) v.findViewById(R.id.text_notes);
		text_notes.setText(kanji.notes);
		text_notes.setTextLocale(Locale.JAPANESE);
		final ImageView image_notes = (ImageView) v.findViewById(R.id.image_notes);
		final View progress_image_notes = v.findViewById(R.id.progress_image_notes);
		final View card_notes = v.findViewById(R.id.card_notes);
		
		card_notes.setVisibility(View.VISIBLE);
		
		if (kanji.imageFile != null && !kanji.imageFile.isEmpty() && JishoHelper.isInternetAvailable(getContext()))
			new FragmentDetail.DownloadImageTask(image_notes, (ViewGroup) v, progress_image_notes).execute(kanji.imageFile);

		else if (kanji.notes == null || kanji.notes.isEmpty())
			card_notes.setVisibility(View.GONE);
		
		RecyclerView recyclerView = (RecyclerView)v.findViewById(R.id.recycler_vocabularies);
		recyclerView.setHasFixedSize(true);
		recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
		recyclerView.setAdapter(new FragmentDetail.VocabularyAdapter(getActivity(), dbHelper, recyclerView, kanji.vocabularies));
		
		if (kanji.vocabularies.length == 0)
		{
			recyclerView.setVisibility(View.GONE);
			v.findViewById(R.id.text_title_vocabularies).setVisibility(View.GONE);
		}
		
		if (!online)
		{
			((TextView) v.findViewById(R.id.text_progress_total)).setText((kanji.timesCorrect_kanji + kanji.timesCorrect_reading_kun + kanji.timesCorrect_reading_on + kanji.timesCorrect_meaning) + " / " + (kanji.timesChecked_kanji + kanji.timesChecked_reading_kun + kanji.timesChecked_reading_on + kanji.timesChecked_meaning));
			final ProgressBar progress_total = (ProgressBar)v.findViewById(R.id.progress_total);
			progress_total.setMax(kanji.timesChecked_kanji + kanji.timesChecked_reading_kun + kanji.timesChecked_reading_on + kanji.timesChecked_meaning);
			progress_total.setProgress(kanji.timesCorrect_kanji + kanji.timesCorrect_reading_kun + kanji.timesCorrect_reading_on + kanji.timesCorrect_meaning);

			((TextView) v.findViewById(R.id.text_progress_kanji)).setText(kanji.timesCorrect_kanji + " / " + kanji.timesChecked_kanji);
			final ProgressBar progress_kanji = (ProgressBar)v.findViewById(R.id.progress_kanji);
			progress_kanji.setMax(kanji.timesChecked_kanji);
			progress_kanji.setProgress(kanji.timesCorrect_kanji);

			if (kanji.reading_kun.length > 0)
			{
				((TextView)v.findViewById(R.id.text_progress_reading_kun)).setText(kanji.timesCorrect_reading_kun + " / " + kanji.timesChecked_reading_kun);
				final ProgressBar progress_reading = (ProgressBar) v.findViewById(R.id.progress_reading_kun);
				progress_reading.setMax(kanji.timesChecked_reading_kun);
				progress_reading.setProgress(kanji.timesCorrect_reading_kun);
			}
			else
			{
				v.findViewById(R.id.text_reading_kun_progress).setVisibility(View.GONE);
				v.findViewById(R.id.text_progress_reading_kun).setVisibility(View.GONE);
				v.findViewById(R.id.progress_reading_kun).setVisibility(View.GONE);
			}

			if (kanji.reading_on.length > 0)
			{
				((TextView)v.findViewById(R.id.text_progress_reading_on)).setText(kanji.timesCorrect_reading_on + " / " + kanji.timesChecked_reading_on);
				final ProgressBar progress_reading = (ProgressBar) v.findViewById(R.id.progress_reading_on);
				progress_reading.setMax(kanji.timesChecked_reading_on);
				progress_reading.setProgress(kanji.timesCorrect_reading_on);
			}
			else
			{
				v.findViewById(R.id.text_reading_on_progress).setVisibility(View.GONE);
				v.findViewById(R.id.text_progress_reading_on).setVisibility(View.GONE);
				v.findViewById(R.id.progress_reading_on).setVisibility(View.GONE);
			}
			
			((TextView) v.findViewById(R.id.text_progress_meaning)).setText(kanji.timesCorrect_meaning + " / " + kanji.timesChecked_meaning);
			final ProgressBar progress_meaning = (ProgressBar) v.findViewById(R.id.progress_meaning);
			progress_meaning.setMax(kanji.timesChecked_meaning);
			progress_meaning.setProgress(kanji.timesCorrect_meaning);

			StringBuilder sb = new StringBuilder();
			StringBuilder sb2 = new StringBuilder();
			
			for (int i = 0; i < kanji.reading_kun.length; ++i)
			{
				if (i != 0)
				{
					sb.append("\n");
					sb2.append("\n");
				}
					
				sb.append(kanji.reading_kun[i]);
				sb2.append(kanji.timesCorrect_reading[i] + " / " + kanji.timesChecked_reading[i]);
			}
			
			for (int i = 0; i < kanji.reading_on.length; ++i)
			{
				if (i != 0)
				{
					sb.append("\n");
					sb2.append("\n");
				}
				else if (kanji.reading_kun.length != 0)
				{
					sb.append("\n\n");
					sb2.append("\n\n");
				}

				sb.append(kanji.reading_on[i]);
				sb2.append(kanji.timesCorrect_reading[i + kanji.reading_kun.length] + " / " + kanji.timesChecked_reading[i + kanji.reading_kun.length]);
			}
			
			((TextView) v.findViewById(R.id.text_success_reading_category)).setText(sb.toString());
			((TextView) v.findViewById(R.id.text_success_reading_values)).setText(sb2.toString());
			
			((TextView) v.findViewById(R.id.text_streak_category)).setText("Kanji\nKunyomi\nOnyomi\nMeaning");
			((TextView) v.findViewById(R.id.text_streak_values)).setText(kanji.streak_kanji + " / " + kanji.streak_kanji_best + "\n" + kanji.streak_reading_kun + " / " + kanji.streak_reading_kun_best + "\n" + kanji.streak_reading_on + " / " + kanji.streak_reading_on_best + "\n" + kanji.streak_meaning + " / " + kanji.streak_meaning_best);
			
			final LineGraphView category_history = (LineGraphView) v.findViewById(R.id.line_graph_category);
			if (kanji.category_history[kanji.category_history.length - 2] >= 0)
				category_history.setValues(kanji.category_history);
			else
				category_history.setVisibility(View.GONE);

			if (kanji.learned)
				((TextView)v.findViewById(R.id.text_next_review)).setText("Next Review: " + (kanji.nextReview < System.currentTimeMillis() ? "Now" : new SimpleDateFormat().format(new Date(kanji.nextReview))));

			((TextView) v.findViewById(R.id.text_last_checked)).setText("Last Checked: " + (kanji.lastChecked <= 1 ? "Never" : new SimpleDateFormat().format(new Date(kanji.lastChecked))));
			((TextView) v.findViewById(R.id.text_added)).setText("Added: " + (new SimpleDateFormat().format(new Date(kanji.added))));

			if (this.kanji.lastChecked > 0)
			{
				if (this.kanji.meaning.length > 1)
				{
					v.findViewById(R.id.text_meaning_entered).setVisibility(View.VISIBLE);

					final PercentageGraphView graph_meaning = (PercentageGraphView) v.findViewById(R.id.percentage_graph_meaning);
					graph_meaning.setVisibility(View.VISIBLE);
					graph_meaning.setValues(kanji.meaning, kanji.meaning_used, true, false);
				}
			}
		}
		
		super.onViewCreated(v, savedInstanceState);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.item_add:
				DialogHelper.createDialog(getContext(), "Add kanji", "Add this kanji to your database?", "Add", new DialogInterface.OnClickListener()
				{
						@Override
						public void onClick(DialogInterface p1, int p2)
						{
							dbHelper.updateKanji(kanji);
							online = false;
							refreshDetailView();
							getView().findViewById(R.id.card_stats_kanji).setVisibility(View.VISIBLE);
						}
				});
				return true;
				
			case R.id.item_open_jisho_kanji:
				JishoHelper.search(getActivity(), kanji.kanji + " #kanji");
				return true;
				
			case R.id.item_edit:
				Intent intent = new Intent(getContext(), ActivityAddKanji.class);
				intent.putExtra("kanji", kanji.kanji);
				startActivity(intent);
				return true;
				
			case R.id.item_reset:
				DialogHelper.createDialog(getContext(), "Reset", "Do you really want to reset this kanji? All progress and statistics will be lost!", "Reset", new DialogInterface.OnClickListener() 
					{
						public void onClick(DialogInterface dialog, int which) 
						{
							dbHelper.resetKanji(kanji.kanji);
							getContext().sendBroadcast(new Intent(getContext(), NotificationHelper.class));

							refreshDetailView();
							dialog.dismiss();
						}
					});


				return true;
			case R.id.item_reset_category:
				DialogHelper.createDialog(getContext(), "Reset", "Do you really want to reset the category of this kanji? You'll have to review it again.", "Reset", new DialogInterface.OnClickListener() 
					{
						public void onClick(DialogInterface dialog, int which) 
						{
							dbHelper.resetKanjiCategory(kanji.lastChecked, kanji.kanji);
							getContext().sendBroadcast(new Intent(getContext(), NotificationHelper.class));

							refreshDetailView();
							dialog.dismiss();
						}
					});

				return true;

			case R.id.item_delete:
				DialogHelper.createDialog(getContext(), "Delete", "Do you really want to delete this kanji?", "Delete", new DialogInterface.OnClickListener() 
					{
						public void onClick(DialogInterface dialog, int which) 
						{
							dbHelper.deleteKanji(kanji.kanji);
							getContext().sendBroadcast(new Intent(getContext(), NotificationHelper.class));


							dialog.dismiss();
							getFragmentManager().popBackStack();
						}
					});

				return true;

			case R.id.item_learn_add:
				dbHelper.updateKanjiLearned(kanji.kanji, true);
				getContext().sendBroadcast(new Intent(getContext(), NotificationHelper.class));

				refreshDetailView();
				return true;

			case R.id.item_learn_remove:
				dbHelper.updateKanjiLearned(kanji.kanji, false);
				getContext().sendBroadcast(new Intent(getContext(), NotificationHelper.class));

				refreshDetailView();
				return true;

//			case R.id.item_cheat:
//				vocabulary.answer(dbHelper, getContext(), vocabulary.kanji, QuestionType.KANJI, QuestionType.MEANING);
//				if (vocabulary.reading.length > 0)
//					vocabulary.answer(dbHelper, getContext(), vocabulary.reading[0], QuestionType.READING, QuestionType.KANJI);
//				vocabulary.answer(dbHelper, getContext(), vocabulary.meaning[0], QuestionType.MEANING, QuestionType.KANJI);
//
//				vocabulary.category++;
//				vocabulary.lastChecked = System.currentTimeMillis();
//				vocabulary.nextReview = vocabulary.lastChecked + Vocabulary.getNextReview(vocabulary.category);
//
//				vocabulary.answered_kanji = false;
//				vocabulary.answered_meaning = false;
//				vocabulary.answered_reading = false;
//				vocabulary.answered_correct = true;
//
//				System.arraycopy(vocabulary.category_history, 1, vocabulary.category_history, 0, vocabulary.category_history.length - 1);
//				vocabulary.category_history[vocabulary.category_history.length - 1] = vocabulary.category;
//
//				dbHelper.updateVocabulary(vocabulary);
//				getContext().sendBroadcast(new Intent(getContext(), NotificationHelper.class));
//				refreshDetailView();
//				return true;

			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		inflater.inflate(online ? R.menu.fragment_detail_kanji : R.menu.fragment_detail, menu);
		
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu)
	{
		super.onPrepareOptionsMenu(menu);

		if (!online)
		{
			menu.findItem(R.id.item_open_jisho).setVisible(false);
			menu.findItem(R.id.item_learn_add).setVisible(!kanji.learned);
			menu.findItem(R.id.item_learn_remove).setVisible(kanji.learned);
		}
	}

	public void refreshDetailView()
	{
		if (!online)
		{
			TransitionManager.beginDelayedTransition((ViewGroup) getView());
			
			onViewCreated(getView(), null);
			getActivity().invalidateOptionsMenu();
		}
	}

	@Override
	public void onResume()
	{
		super.onResume();

		refreshDetailView();
		getActivity().setTitle(kanji.kanji + " #Kanji");
		
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
}
