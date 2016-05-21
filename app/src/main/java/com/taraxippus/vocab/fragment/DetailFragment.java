package com.taraxippus.vocab.fragment;

import android.app.*;
import android.graphics.*;
import android.os.*;
import android.support.v7.widget.*;
import android.transition.*;
import android.view.*;
import android.view.ViewGroup.*;
import android.widget.*;
import com.taraxippus.vocab.*;
import com.taraxippus.vocab.util.*;
import com.taraxippus.vocab.view.*;
import com.taraxippus.vocab.vocabulary.*;
import java.io.*;
import java.text.*;
import java.util.*;
import android.view.animation.AccelerateInterpolator;

public class DetailFragment extends Fragment implements View.OnTouchListener, GestureDetector.OnGestureListener
{
	MainActivity main;
	Vocabulary vocabulary;
	
	private GestureDetector gestureDetector; 
	
	public DetailFragment()
	{
	
	}

	public void setTransitions(MainActivity main)
	{
		this.setEnterTransition(TransitionInflater.from(main).inflateTransition(android.R.transition.slide_bottom));
		this.setReturnTransition(TransitionInflater.from(main).inflateTransition(android.R.transition.fade));

		TransitionSet set = new TransitionSet();
        set.setOrdering(TransitionSet.ORDERING_TOGETHER);
		
		set.addTransition(new ChangeTransform());
		set.addTransition(new ChangeBounds());
		
		this.setSharedElementEnterTransition(set);
		this.setSharedElementReturnTransition(set);

		this.setAllowEnterTransitionOverlap(false);
		this.setAllowReturnTransitionOverlap(false);
	}

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		this.main = (MainActivity)getActivity();
		this.vocabulary = main.vocabulary.get(main.vocabulary_selected);
		
		gestureDetector = new GestureDetector(main, this);
		
		final View v = inflater.inflate(R.layout.detail, container, false);
		v.setOnTouchListener(this);
		
		final int position = main.vocabulary_filtered.indexOf(vocabulary);
		
		v.findViewById(R.id.card_kanji).setTransitionName("card" + position);
		
		final TextView kanji = (TextView)v.findViewById(R.id.kanji_text);
		kanji.setTransitionName("kanji" + position);
		kanji.setText(vocabulary.correctAnswer(QuestionType.KANJI));

		final TextView reading = (TextView)v.findViewById(R.id.reading_text);
		reading.setTransitionName("reading" + position);
		reading.setText(vocabulary.correctAnswer(QuestionType.READING));
		
		final TextView meaning = (TextView)v.findViewById(R.id.meaning_text);
		meaning.setTransitionName("meaning" + position);
		meaning.setText(vocabulary.correctAnswer(QuestionType.MEANING_INFO));
		
		final TextView type = (TextView)v.findViewById(R.id.type);
		type.setText(vocabulary.getType());
		
		final CardView card_stroke_order = (CardView) v.findViewById(R.id.card_stroke_order);
		final View progress_stroke_order = v.findViewById(R.id.stroke_order_progress);
		final LinearLayout layout_stroke_order = (LinearLayout) v.findViewById(R.id.layout_stroke_order);
		
		final ImageButton stroke_order = (ImageButton) v.findViewById(R.id.stroke_order);
		stroke_order.setVisibility((main.jishoHelper.offlineStrokeOrder() || main.jishoHelper.isInternetAvailable()) && vocabulary.reading.length != 0 ? View.VISIBLE : View.GONE);
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
							vocabulary.showStrokeOrder(layout_stroke_order, new OnProcessSuccessListener()
							{
									@Override
									public void onProcessSuccess(Object... args)
									{
										progress_stroke_order.setVisibility(View.GONE);
									}
							}, false, true);
						}
					}
					else
					{
						card_stroke_order.setVisibility(View.GONE);
					}
				}
			});
		
		
		final ImageButton sound = (ImageButton) v.findViewById(R.id.sound);
		sound.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					vocabulary.playSound();
				}
		});
		
		vocabulary.prepareSound(new OnProcessSuccessListener()
		{
				@Override
				public void onProcessSuccess(Object... args)
				{
					main.runOnUiThread(new Runnable()
						{
							@Override
							public void run()
							{
								sound.setVisibility(View.VISIBLE);
							}
						});
				}
		});
		
		
		final TextView notes = (TextView)v.findViewById(R.id.notes_text);
		final ImageView notes_image = (ImageView) v.findViewById(R.id.notes_image);
		final View progress_notes_image = v.findViewById(R.id.notes_image_progress);
		
		notes.setText(vocabulary.notes);
		
		if (!vocabulary.imageFile.isEmpty() && main.jishoHelper.isInternetAvailable())
		{
			new DownloadImageTask(notes_image, progress_notes_image).execute(vocabulary.imageFile);
		}
		else
		{
			if (this.vocabulary.notes.isEmpty())
			{
				v.findViewById(R.id.card_notes).setVisibility(View.GONE);
			}
		}
	
			
		if (this.vocabulary.sameReading.isEmpty())
			v.findViewById(R.id.card_same_reading).setVisibility(View.GONE);
			
		if (this.vocabulary.sameMeaning.isEmpty())
			v.findViewById(R.id.card_same_meaning).setVisibility(View.GONE);
			
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

		final TextView kanji_succes = (TextView)v.findViewById(R.id.kanji_progress_number);
		kanji_succes.setText(vocabulary.timesCorrect_kanji + " / " + vocabulary.timesChecked_kanji);
		
		final ProgressBar kanji_progress = (ProgressBar)v.findViewById(R.id.kanji_progress);
		kanji_progress.setMax(vocabulary.timesChecked_kanji);
		kanji_progress.setProgress(vocabulary.timesCorrect_kanji);
		
		final TextView reading_succes = (TextView)v.findViewById(R.id.reading_progress_number);
		reading_succes.setText(vocabulary.timesCorrect_reading + " / " + vocabulary.timesChecked_reading);
		
		final ProgressBar reading_progress = (ProgressBar)v.findViewById(R.id.reading_progress);
		reading_progress.setMax(vocabulary.timesChecked_reading);
		reading_progress.setProgress(vocabulary.timesCorrect_reading);
		
		final TextView meaning_succes = (TextView)v.findViewById(R.id.meaning_progress_number);
		meaning_succes.setText(vocabulary.timesCorrect_meaning + " / " + vocabulary.timesChecked_meaning);
		
		final ProgressBar meaning_progress = (ProgressBar)v.findViewById(R.id.meaning_progress);
		meaning_progress.setMax(vocabulary.timesChecked_meaning);
		meaning_progress.setProgress(vocabulary.timesCorrect_meaning);
		
		final TextView kanji_streak = (TextView)v.findViewById(R.id.kanji_streak);
		kanji_streak.setText("Kanji streak: " + vocabulary.streak_kanji + ", longest streak: " + vocabulary.streak_kanji_best);

		final TextView reading_streak = (TextView)v.findViewById(R.id.reading_streak);
		reading_streak.setText("Reading streak: " + vocabulary.streak_reading + ", longest streak: " + vocabulary.streak_reading_best);
		
		final TextView meaning_streak = (TextView)v.findViewById(R.id.meaning_streak);
		meaning_streak.setText("Meaning streak: " + vocabulary.streak_meaning + ", longest streak: " + vocabulary.streak_meaning_best);
		
		final TextView next_review = (TextView)v.findViewById(R.id.next_review);
		next_review.setText("Next Review: " + (vocabulary.lastChecked + vocabulary.getNextReview() < System.currentTimeMillis() ? "Now" : new SimpleDateFormat().format(new Date(vocabulary.lastChecked + vocabulary.getNextReview()))));

		if (!vocabulary.learned)
			next_review.setVisibility(View.INVISIBLE);
		
		final TextView last_checked = (TextView)v.findViewById(R.id.last_checked);
		last_checked.setText("Last Checked: " + (vocabulary.lastChecked == 0 ? "Never" : new SimpleDateFormat().format(new Date(vocabulary.lastChecked))));
		
		final TextView added = (TextView)v.findViewById(R.id.added);
		added.setText("Added: " + (new SimpleDateFormat().format(new Date(vocabulary.added))));
		
		if (vocabulary.reading.length == 0)
		{
			v.findViewById(R.id.layout_reading).setVisibility(View.INVISIBLE);
			v.findViewById(R.id.layout_reading).setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, 0));
			
			reading_streak.setVisibility(View.INVISIBLE);
			reading_streak.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, 0));
		}
		
		final TextView category = (TextView)v.findViewById(R.id.category);
		category.setText("Category: " + vocabulary.category);
		
		final LineGraphView category_history = (LineGraphView) v.findViewById(R.id.line_graph_category);
		if (vocabulary.category_history[vocabulary.category_history.length - 2] >= 0)
			category_history.setValues(vocabulary.category_history);
		else
			category_history.setVisibility(View.GONE);

		if (this.vocabulary.lastChecked > 0 && (this.vocabulary.reading.length > 1 || this.vocabulary.meaning.length > 1))
		{
			v.findViewById(R.id.reading_meaning_usage).setVisibility(View.VISIBLE);
			
			if (this.vocabulary.meaning.length > 1)
			{
				v.findViewById(R.id.text_meaning_entered).setVisibility(View.VISIBLE);
				
				PercentageGraphView graph_meaning = (PercentageGraphView)v.findViewById(R.id.percentage_graph_meaning);
				graph_meaning.setVisibility(View.VISIBLE);
				graph_meaning.setValues(vocabulary.meaning, vocabulary.meaning_used);
			}
			
			if (this.vocabulary.reading.length > 1)
			{
				v.findViewById(R.id.text_reading_entered).setVisibility(View.VISIBLE);

				PercentageGraphView graph_reading = (PercentageGraphView)v.findViewById(R.id.percentage_graph_reading);
				graph_reading.setVisibility(View.VISIBLE);
				graph_reading.setValues(vocabulary.reading_trimed, vocabulary.reading_used);
				
			}
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

			main.selectedVocabulary_backStack.add(main.vocabulary_selected);
				
			Fragment fragment = main.getDetailFragment();
		
			if (p3 < 0)
			{
				main.vocabulary_selected = main.vocabulary.indexOf(main.vocabulary_filtered.get((index + 1) % main.vocabulary_filtered.size()));
			
				fragment.setEnterTransition(TransitionInflater.from(main).inflateTransition(android.R.transition.slide_right));
			}
			else
			{
				main.vocabulary_selected = main.vocabulary.indexOf(main.vocabulary_filtered.get((index + main.vocabulary_filtered.size() - 1) % main.vocabulary_filtered.size()));
			
				fragment.setEnterTransition(TransitionInflater.from(main).inflateTransition(android.R.transition.slide_left));
			}
			
			main.changeFragment(fragment, "detail_swipe");

            return true;
		}
		return false;
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
			Bitmap mIcon11 = null;
			InputStream in = null;
			try
			{
				in = new java.net.URL(urldisplay).openStream();
				mIcon11 = BitmapFactory.decodeStream(in);
			} 
			catch (Exception e)
			{
				Toast.makeText(main, "Coudn't find image for url: " + urldisplay, Toast.LENGTH_LONG);
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
			return mIcon11;
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
}
