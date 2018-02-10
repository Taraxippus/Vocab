package com.taraxippus.vocab.fragment;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.CardView;
import android.transition.ChangeBounds;
import android.transition.ChangeTransform;
import android.transition.TransitionInflater;
import android.transition.TransitionSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.taraxippus.vocab.ActivityDetail;
import com.taraxippus.vocab.ActivitySettings;
import com.taraxippus.vocab.R;
import com.taraxippus.vocab.util.JishoHelper;
import com.taraxippus.vocab.util.OnProcessSuccessListener;
import com.taraxippus.vocab.util.StringHelper;
import com.taraxippus.vocab.vocabulary.DBHelper;
import com.taraxippus.vocab.vocabulary.QuestionType;
import com.taraxippus.vocab.vocabulary.Vocabulary;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;
import java.util.Random;
import android.transition.TransitionPropagation;

public class FragmentActivityQuizFast extends Fragment implements View.OnClickListener
{
	Random random = new Random();
	DBHelper dbHelper;
	SharedPreferences preferences;	

	String[] answers = new String[4];
	Vocabulary vocabulary;
	QuestionType question_type;
	QuestionType answer_type;
	int correctAnswer;
	
	ArrayList<Integer> vocabularies_plus;
	ArrayList<Integer> vocabularies_minus;
	int timesChecked_kanji, timesChecked_reading, timesChecked_meaning,
	timesCorrect_kanji, timesCorrect_reading, timesCorrect_meaning;
	int[] history_quiz;
	
	View card_solution, card_layout_answer;
	View card_answer[] = new View[4];
	TextView text_answer[] = new TextView[4];
	TextView text_solution_icon, text_solution, text_level_up;
	
	public FragmentActivityQuizFast() {}

	public Fragment setDefaultTransitions(Context context)
	{
		TransitionSet enter = new TransitionSet();
		enter.addTransition(TransitionInflater.from(context).inflateTransition(android.R.transition.slide_right).setStartDelay(300));
		this.setEnterTransition(enter);
		
		TransitionSet exit = new TransitionSet();
		exit.addTransition(TransitionInflater.from(context).inflateTransition(android.R.transition.slide_left));
		this.setExitTransition(exit);
		
		TransitionSet set = new TransitionSet();
		ChangeTransform changeTransform = new ChangeTransform();
		changeTransform.setReparentWithOverlay(false);
		set.addTransition(changeTransform);
		ChangeBounds changeBounds = new ChangeBounds();
		changeBounds.setResizeClip(false);
		set.addTransition(changeBounds);
		set.excludeChildren(R.id.card_answer, true);
		
		this.setSharedElementEnterTransition(set);
		this.setSharedElementReturnTransition(set);
		
		return this;
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		if (getArguments() != null)
		{
			timesChecked_kanji = getArguments().getInt("timesChecked_kanji");
			timesChecked_reading = getArguments().getInt("timesChecked_reading");
			timesChecked_meaning = getArguments().getInt("timesChecked_meaning");

			timesCorrect_kanji = getArguments().getInt("timesCorrect_kanji");
			timesCorrect_reading = getArguments().getInt("timesCorrect_reading");
			timesCorrect_meaning = getArguments().getInt("timesCorrect_meaning");

			vocabularies_plus = getArguments().getIntegerArrayList("vocabularies_plus");
			vocabularies_minus = getArguments().getIntegerArrayList("vocabularies_minus");
			
			int[] history_quiz_old = getArguments().getIntArray("history_quiz");
			history_quiz = new int[history_quiz_old.length + 1];
			System.arraycopy(history_quiz_old, 0, history_quiz, 0, history_quiz_old.length);
			history_quiz[history_quiz.length - 1] = history_quiz[history_quiz.length - 2];
		}
		else
		{
			timesChecked_kanji = timesChecked_reading = timesChecked_meaning = timesCorrect_kanji = timesCorrect_reading = timesChecked_meaning = 0;
			history_quiz = new int[2];
			vocabularies_plus = new ArrayList<>();
			vocabularies_minus = new ArrayList<>();
		}
		
		dbHelper = new DBHelper(getContext());
		preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
		
		Cursor res =  dbHelper.getReadableDatabase().rawQuery("SELECT * FROM vocab WHERE learned = 1 ORDER BY RANDOM() LIMIT 1", null);
		if (res.getCount() <= 0)
		{
			res.close();
			getActivity().finish();
			return;
		}

		res.moveToFirst();
		int id = res.getInt(0);
		vocabulary = dbHelper.getVocabulary(res);
		res.close();
		
		ArrayList<QuestionType> tmp = new ArrayList<>();
		tmp.add(QuestionType.KANJI);
		if (vocabulary.reading.length > 0)
			tmp.add(QuestionType.READING);
		tmp.add(QuestionType.MEANING);

		Collections.shuffle(tmp);
		Collections.sort(tmp, new Comparator<QuestionType>()
			{
				@Override
				public int compare(QuestionType p1, QuestionType p2)
				{
					return (int) Math.signum(vocabulary.getSuccessRate(p1) - vocabulary.getSuccessRate(p2));
				}
			});

		answer_type = tmp.get(0);

		tmp.clear();
		if (answer_type != QuestionType.KANJI)
			tmp.add(QuestionType.KANJI);
		if (answer_type != QuestionType.READING && vocabulary.reading.length > 0)
			tmp.add(QuestionType.READING);
		if (answer_type != QuestionType.MEANING)
			tmp.add(QuestionType.MEANING);

		question_type = tmp.get(random.nextInt(tmp.size()));
		correctAnswer = random.nextInt(4);
		
		StringBuilder list = new StringBuilder();
		list.append(id);
		if (answer_type == QuestionType.READING || question_type == QuestionType.READING)
			for (int i = 0; i < vocabulary.sameReading.length; ++i)
			{
				list.append(", ");
				list.append(vocabulary.sameReading[i]);
			}
		if (answer_type == QuestionType.MEANING || question_type == QuestionType.MEANING)
			for (int i = 0; i < vocabulary.sameMeaning.length; ++i)
			{
				list.append(", ");
				list.append(vocabulary.sameMeaning[i]);
			}
		
		res = dbHelper.getReadableDatabase().rawQuery("SELECT kanji, reading, meaning, additionalInfo, showInfo FROM vocab WHERE type = " + vocabulary.type.ordinal() + " AND id NOT IN (" + list.toString() + ") ORDER BY RANDOM() LIMIT 3", null);
		if (res.getCount() < 3)
		{
			res = dbHelper.getReadableDatabase().rawQuery("SELECT kanji, reading, meaning, additionalInfo, showInfo FROM vocab WHERE id NOT IN (" + list.toString() + ") ORDER BY RANDOM() LIMIT 3", null);
			if (res.getCount() < 3)
			{
				res.close();
				getActivity().finish();
				return;
			}
		}

		res.moveToFirst();
		for (int i = 0; i == 0 || res.moveToNext(); ++i)
		{
			if (i == correctAnswer)
				i++;
			
			answers[i] = Vocabulary.correctAnswer(answer_type, res.getString(0), StringHelper.toStringArray(res.getString(1)), StringHelper.toStringArray(res.getString(2)), res.getString(3), res.getInt(4) == 1);
		}
		answers[correctAnswer] = vocabulary.correctAnswer(answer_type);
		res.close();
		
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.activity_quiz_fast, container, false);
	}

	@Override
	public void onViewCreated(View v, Bundle savedInstanceState)
	{
		TextView text_question = (TextView) v.findViewById(R.id.text_question);
		text_question.setText(vocabulary.question(question_type));
		((TextView) v.findViewById(R.id.text_type)).setText(answer_type.name());
		((TextView) v.findViewById(R.id.text_category)).setText("" + vocabulary.category);
		
		if (question_type == QuestionType.KANJI)
			text_question.setTextSize(TypedValue.COMPLEX_UNIT_DIP, text_question.getText().length() > 6 ? 45 : 55);

		else if (question_type == QuestionType.READING)
			text_question.setTextSize(TypedValue.COMPLEX_UNIT_DIP, text_question.getText().length() > 10 ? 40 : text_question.getText().length() > 6 ? 45 : 50);

		else if (question_type == QuestionType.MEANING)
			text_question.setTextSize(TypedValue.COMPLEX_UNIT_DIP, text_question.getText().length() > 30 ? 35 : text_question.getText().length() > 20 ? 40 : (text_question.getText().length() > 10 ? 45 : 50));
		
		card_solution = v.findViewById(R.id.card_solution);
		card_solution.setVisibility(View.INVISIBLE);
		card_solution.setOnClickListener(this);
		text_solution_icon = (TextView) v.findViewById(R.id.text_solution_icon);
		text_solution = (TextView) v.findViewById(R.id.text_solution);
		text_level_up = (TextView) v.findViewById(R.id.text_level_up);
		card_layout_answer = v.findViewById(R.id.card_answer);
		v.findViewById(R.id.layout_answer).bringToFront();
		
		text_answer[0] = (TextView) v.findViewById(R.id.text_answer1);
		text_answer[1] = (TextView) v.findViewById(R.id.text_answer2);
		text_answer[2] = (TextView) v.findViewById(R.id.text_answer3);
		text_answer[3] = (TextView) v.findViewById(R.id.text_answer4);
		
		for (int i = 0; i < answers.length; ++i)
			text_answer[i].setText(answers[i]);
		
		card_answer[0] = v.findViewById(R.id.card_answer1);
		card_answer[1] = v.findViewById(R.id.card_answer2);
		card_answer[2] = v.findViewById(R.id.card_answer3);
		card_answer[3] = v.findViewById(R.id.card_answer4);
		
		for (View v1 : card_answer)
			v1.setOnClickListener(this);
			
		for (int i = 0; i < answers.length; ++i)
			text_answer[i].setTextSize(TypedValue.COMPLEX_UNIT_DIP, answer_type == QuestionType.KANJI ? 30 : answer_type == QuestionType.READING ? 25 : 20);
		
		text_question.setTextLocale(Locale.JAPANESE);
		text_solution.setTextLocale(Locale.JAPANESE);
		
		for (TextView v1: text_answer)
			v1.setTextLocale(Locale.JAPANESE);
		
		final ImageButton button_overflow = (ImageButton) v.findViewById(R.id.button_overflow);
		button_overflow.setOnClickListener(this);

		View.OnClickListener listener = new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				vocabulary.playSound(dbHelper);
			}
		};

		final ImageButton button_sound = (ImageButton) v.findViewById(R.id.button_sound);
		button_sound.setOnClickListener(listener);
		final ImageButton button_sound2 = (ImageButton) v.findViewById(R.id.button_sound2);
		button_sound2.setOnClickListener(listener);

		final RelativeLayout layout_stroke_order = (RelativeLayout) v.findViewById(R.id.layout_stroke_order);
		final View card_stroke_order = v.findViewById(R.id.card_stroke_order);
		final ProgressBar progress_stroke_order = (ProgressBar) v.findViewById(R.id.progress_stroke_order);
		final ImageButton stroke_order3 = (ImageButton) v.findViewById(R.id.button_stroke_order3);

		card_stroke_order.setVisibility(View.INVISIBLE);
		listener = new View.OnClickListener()
		{
			@Override
			public void onClick(View p1)
			{
				Animator disappear = ViewAnimationUtils.createCircularReveal(card_stroke_order, 0, card_stroke_order.getHeight(), Math.max(card_stroke_order.getWidth(), card_stroke_order.getHeight()), 0);

				disappear.addListener(new AnimatorListenerAdapter() 
					{
						@Override
						public void onAnimationEnd(Animator animation) 
						{
							super.onAnimationEnd(animation);
							card_stroke_order.setVisibility(View.INVISIBLE);
							layout_stroke_order.removeView(layout_stroke_order.findViewWithTag("stroke_order"));
						}
					});

				disappear.start();
			}
		};
		card_stroke_order.setOnClickListener(listener);
		stroke_order3.setOnClickListener(listener);

		listener = new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				final RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
				params.addRule(RelativeLayout.CENTER_IN_PARENT);
				params.addRule(RelativeLayout.BELOW,R.id.button_overflow_stroke_order);
				JishoHelper.addStrokeOrderView(getContext(), vocabulary.kanji, layout_stroke_order, params, progress_stroke_order, true, true);

				Animator appear = ViewAnimationUtils.createCircularReveal(card_stroke_order, 0, card_stroke_order.getHeight(), 0, Math.max(card_stroke_order.getWidth(), card_stroke_order.getHeight()));

				appear.addListener(new AnimatorListenerAdapter() 
					{
						@Override
						public void onAnimationStart(Animator animation) 
						{
							super.onAnimationStart(animation);
							card_stroke_order.setVisibility(View.VISIBLE);
							progress_stroke_order.setVisibility(View.VISIBLE);
						}
					});

				appear.start();
			}
		};
		ImageButton button_stroke_order = (ImageButton) v.findViewById(R.id.button_stroke_order);
		button_stroke_order.setOnClickListener(listener);
		ImageButton button_stroke_order2 = (ImageButton) v.findViewById(R.id.button_stroke_order2);
		button_stroke_order2.setOnClickListener(listener);

		v.findViewById(R.id.button_overflow_stroke_order).setOnClickListener(this);
		
		button_sound.setVisibility(View.GONE);
		button_sound2.setVisibility(View.GONE);
		if (JishoHelper.isInternetAvailable(getContext()))
			vocabulary.prepareSound(dbHelper, new OnProcessSuccessListener()
				{
					@Override
					public void onProcessSuccess(Object[] args)
					{
						if (question_type == QuestionType.READING || (question_type == QuestionType.KANJI && answer_type != QuestionType.READING))
							button_sound.setVisibility(View.VISIBLE);
						
						button_sound2.setVisibility(View.VISIBLE);
					}
				});
		button_stroke_order.setVisibility(JishoHelper.isStrokeOrderAvailable(getContext()) && question_type == QuestionType.KANJI ? View.VISIBLE : View.GONE);
		button_stroke_order2.setVisibility(JishoHelper.isStrokeOrderAvailable(getContext()) ? View.VISIBLE : View.GONE);
		
		final ImageButton button_skip = (ImageButton) v.findViewById(R.id.button_skip);
		button_skip.setOnClickListener(new View.OnClickListener() 
			{
				public void onClick(View v) 
				{
					
				}
			});
		button_skip.setVisibility(View.GONE);
		
		if (JishoHelper.isInternetAvailable(getContext()) && preferences.getBoolean("soundQuiz", false) && question_type == QuestionType.READING)
			vocabulary.playSound(dbHelper);
	}

	@Override
	public void onClick(View view)
	{
		if (view.getId() == R.id.card_answer1 || view.getId() == R.id.card_answer2 || view.getId() == R.id.card_answer3 || view.getId() == R.id.card_answer4)
		{
			if (card_solution.getVisibility() == View.VISIBLE)
			{
				onClick(card_solution);
				return;
			}
			
			int answer;
			
			if (view.getId() == R.id.card_answer1)
				answer = 0;
			else if (view.getId() == R.id.card_answer2)
				answer = 1;
			else if (view.getId() == R.id.card_answer3)
				answer = 2;
			else
				answer = 3;
				
			if (question_type == QuestionType.KANJI)
				timesChecked_kanji++;

			else if (question_type == QuestionType.READING)
				timesChecked_reading++;

			else
				timesChecked_meaning++;
			
			if (answer == correctAnswer)
			{
				if (question_type == QuestionType.KANJI)
					timesCorrect_kanji++;

				else if (question_type == QuestionType.READING)
					timesCorrect_reading++;

				else
					timesCorrect_meaning++;
					
				history_quiz[history_quiz.length - 1] = history_quiz[history_quiz.length - 2] + 1;
				
				if (!vocabularies_plus.contains(vocabulary.id))
					vocabularies_plus.add(vocabulary.id);
					
				text_level_up.setText("+");
				text_solution_icon.setText("✔");
				text_solution.setText("Correct");

				if (preferences.getBoolean("soundQuiz", false) && answer_type == QuestionType.READING && JishoHelper.isInternetAvailable(getContext()))
					vocabulary.playSound(dbHelper);
			}
			else
			{
				history_quiz[history_quiz.length - 1] = 0;
				
				if (!vocabularies_minus.contains(vocabulary.id))
					vocabularies_minus.add(vocabulary.id);
					
				text_solution_icon.setText("✖");
				text_solution.setText("Correct answer was:\n" + vocabulary.correctAnswer(answer_type));
				text_level_up.setText("-" + (vocabulary.category - vocabulary.getLastSavePoint(getContext())));

				if (preferences.getBoolean("soundQuiz", false) && answer_type == QuestionType.READING && JishoHelper.isInternetAvailable(getContext()))
					vocabulary.playSound(dbHelper);
			}
			
			Animator appear = ViewAnimationUtils.createCircularReveal(card_solution, card_solution.getWidth() / 2, card_solution.getHeight() / 2, 0, Math.max(card_solution.getWidth(), card_solution.getHeight()));
			appear.addListener(new AnimatorListenerAdapter() 
				{
					@Override
					public void onAnimationStart(Animator animation) 
					{
						super.onAnimationStart(animation);
						card_solution.setVisibility(View.VISIBLE);
					}
				});

			appear.start();
			
			final CardView card = (CardView) card_answer[correctAnswer];
			final float[] from = new float[3], to =   new float[3];
			Color.colorToHSV(getContext().getColor(R.color.primaryDark), from); 
			Color.colorToHSV(getContext().getColor(R.color.accent), to); 

			ValueAnimator anim = ValueAnimator.ofFloat(0, 1); 
			anim.setDuration(300);                            

			final float[] hsv  = new float[3];                
			anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
			{
					@Override
					public void onAnimationUpdate(ValueAnimator animation)
					{
						hsv[0] = from[0] + (to[0] - from[0]) * animation.getAnimatedFraction();
						hsv[1] = from[1] + (to[1] - from[1]) * animation.getAnimatedFraction();
						hsv[2] = from[2] + (to[2] - from[2]) * animation.getAnimatedFraction();

						card.setCardBackgroundColor(Color.HSVToColor(hsv));
					}
				});

			anim.start(); 
		}
		
		if (view.getId() == R.id.card_solution)
		{
			Fragment fragment = new FragmentActivityQuizFast().setDefaultTransitions(getContext());
			Bundle bundle = new Bundle();
			bundle.putIntArray("history_quiz", history_quiz);
			bundle.putInt("timesChecked_kanji", timesChecked_kanji);
			bundle.putInt("timesChecked_reading", timesChecked_reading);
			bundle.putInt("timesChecked_meaning", timesChecked_meaning);
			bundle.putInt("timesCorrect_kanji", timesCorrect_kanji);
			bundle.putInt("timesCorrect_reading", timesCorrect_reading);
			bundle.putInt("timesCorrect_meaning", timesCorrect_meaning);
			bundle.putIntegerArrayList("vocabularies_plus", vocabularies_plus);
			bundle.putIntegerArrayList("vocabularies_minus", vocabularies_minus);
			fragment.setArguments(bundle);
			getFragmentManager().beginTransaction().replace(R.id.layout_content, fragment)
			.addSharedElement(card_layout_answer, card_layout_answer.getTransitionName())
			.commit();
		}
		else if (view.getId() == R.id.button_overflow)
		{
			PopupMenu popup = new PopupMenu(getActivity(), view);
			popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
				{
					@Override
					public boolean onMenuItemClick(MenuItem item)
					{
						return FragmentActivityQuizFast.this.onOptionsItemSelected(item);
					}
				});
			MenuInflater inflater = popup.getMenuInflater();
			inflater.inflate(R.menu.fragment_quiz_random, popup.getMenu());
			popup.show();
		}
		else if (view.getId() == R.id.button_overflow_stroke_order)
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
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.item_finish:
				setExitTransition(null);
				Fragment fragment = new FragmentActivityQuizFinish().setDefaultTransitions(getContext());
				Bundle bundle = new Bundle();
				bundle.putIntArray("history_quiz", history_quiz);
				bundle.putInt("timesChecked_kanji", timesChecked_kanji);
				bundle.putInt("timesChecked_reading", timesChecked_reading);
				bundle.putInt("timesChecked_meaning", timesChecked_meaning);
				bundle.putInt("timesCorrect_kanji", timesCorrect_kanji);
				bundle.putInt("timesCorrect_reading", timesCorrect_reading);
				bundle.putInt("timesCorrect_meaning", timesCorrect_meaning);
				int[] tmp = new int[vocabularies_plus.size()];
				for (int i = 0; i < tmp.length; ++i)
					tmp[i] = vocabularies_plus.get(i);
				bundle.putIntArray("vocabularies_plus", tmp);

				bundle.putIntArray("vocabularies_neutral", new int[0]);

				tmp = new int[vocabularies_minus.size()];
				for (int i = 0; i < tmp.length; ++i)
					tmp[i] = vocabularies_minus.get(i);
				bundle.putIntArray("vocabularies_minus", tmp);				fragment.setArguments(bundle);
				getFragmentManager().beginTransaction().replace(R.id.layout_content, fragment).commit();
				return true;
				
			case R.id.item_skip:
				Fragment fragment1 = new FragmentActivityQuizFast().setDefaultTransitions(getContext());
				fragment1.setArguments(getArguments());
				getFragmentManager().beginTransaction().replace(R.id.layout_content, fragment1).addSharedElement(card_layout_answer, card_layout_answer.getTransitionName()).commit();
				return true;

			case R.id.item_detail:
				Intent intent = new Intent(getContext(), ActivityDetail.class);
				intent.putExtra("id", vocabulary.id);
				startActivity(intent);
				return true;
								
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		inflater.inflate(R.menu.fragment_quiz_random, menu);
		menu.findItem(R.id.item_detail).setVisible(false);
		menu.findItem(R.id.item_stats).setVisible(false);
		
		super.onCreateOptionsMenu(menu, inflater);
	}
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();

		dbHelper.close();
	}
}
