package com.taraxippus.vocab.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.text.InputType;
import android.transition.TransitionInflater;
import android.transition.TransitionManager;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import com.taraxippus.vocab.ActivityDetail;
import com.taraxippus.vocab.ActivityLearn;
import com.taraxippus.vocab.ActivitySettings;
import com.taraxippus.vocab.R;
import com.taraxippus.vocab.util.JishoHelper;
import com.taraxippus.vocab.util.OnProcessSuccessListener;
import com.taraxippus.vocab.util.StringHelper;
import com.taraxippus.vocab.vocabulary.Answer;
import com.taraxippus.vocab.vocabulary.DBHelper;
import com.taraxippus.vocab.vocabulary.QuestionType;
import com.taraxippus.vocab.vocabulary.Vocabulary;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;
import java.util.Random;
import android.transition.AutoTransition;

public class FragmentActivityQuiz extends Fragment implements View.OnClickListener
{
	View card_solution, card_stroke_order;
	TextView text_question, text_type, text_solution_icon, text_solution,
	text_category, text_level_up;
	
	EditText text_answer;
	RelativeLayout layout_stroke_order;
	ProgressBar progress_quiz;
	
	ImageView button_sound, button_sound2, button_stroke_order, button_stroke_order2, button_retry;
	
	Animator disappear;
	Animator appear;
	
	Answer answer;
	QuestionType answer_type = QuestionType.MEANING;
	QuestionType question_type = QuestionType.KANJI;
	
	ArrayList<Vocabulary> vocabularies;
	ArrayList<Vocabulary> newVocabularies;
	final ArrayList<Integer> vocabularies_plus = new ArrayList<>();
	final ArrayList<Integer> vocabularies_neutral = new ArrayList<>();
	final ArrayList<Integer> vocabularies_minus = new ArrayList<>();
	int progress, maxProgress, lastStat, stat,
	timesChecked_kanji, timesChecked_reading, timesChecked_meaning,
	lastTimesChecked_kanji, lastTimesChecked_reading, lastTimesChecked_meaning,
	timesCorrect_kanji, timesCorrect_reading, timesCorrect_meaning;
	final ArrayList<Integer> history_quiz = new ArrayList<>();
	
	public Vocabulary vocabulary;
	public boolean retype;
	
	DBHelper dbHelper;
	SharedPreferences preferences;	
	
	static final Random random = new Random();
	
	public FragmentActivityQuiz() {}

	public Fragment setDefaultTransitions(Context context)
	{
		this.setEnterTransition(TransitionInflater.from(context).inflateTransition(android.R.transition.slide_top));
		this.setReturnTransition(TransitionInflater.from(context).inflateTransition(android.R.transition.slide_top));

		this.setAllowEnterTransitionOverlap(false);
		this.setAllowReturnTransitionOverlap(false);
		
		return this;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		dbHelper = new DBHelper(getContext());
	
		vocabularies = new ArrayList<>();
		newVocabularies = new ArrayList<>();
		
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor res =  db.rawQuery("SELECT * FROM vocab WHERE learned = 1 AND nextReview < ?", new String[] {"" + System.currentTimeMillis()});
	
		if (res.getCount() <= 0)
			res.close();
		
		else
		{
			res.moveToFirst();

			Vocabulary v;
			do
			{
				v = dbHelper.getVocabulary(res);
				
				if (v.lastChecked == 0)
					newVocabularies.add(v);
					
				else
					vocabularies.add(v);
			}
			while (res.moveToNext());

			res.close();
		}
		
		history_quiz.add(0);
		stat = 0;
		progress = 0;
		maxProgress = 0;
		for (Vocabulary v : vocabularies)
		{
			if (v.reading.length == 0)
				maxProgress += 2;
			else
				maxProgress += 3;
				
			if (v.answered_kanji)
				progress++;
			if (v.reading.length > 0 && v.answered_reading)
				progress++;
			if (v.answered_meaning)
				progress++;
		}
		
		Collections.shuffle(vocabularies);
		Collections.shuffle(newVocabularies);
		
		if (!newVocabularies.isEmpty())
		{
			AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
			alertDialog.setTitle("New Vocabularies");
			alertDialog.setMessage("There are some new vocabularies in this quiz, which you haven't learned yet. Do you still want to include them in this review?");
			alertDialog.setPositiveButton("Include", new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						dialog.dismiss();
						
						vocabularies.addAll(newVocabularies);
						newVocabularies.clear();
						
						stat = 0;
						progress = 0;
						for (Vocabulary v : vocabularies)
						{
							if (v.reading.length == 0)
								maxProgress += 2;
							else
								maxProgress += 3;

							if (v.answered_kanji)
								progress++;
							if (v.reading.length > 0 && v.answered_reading)
								progress++;
							if (v.answered_meaning)
								progress++;
						}
						
						setTitle();
					}
				});
			alertDialog.setNeutralButton("Learn now", new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						getContext().startActivity(new Intent(getContext(), ActivityLearn.class));
						dialog.dismiss();
						newVocabularies.clear();
					}
				});
			alertDialog.setNegativeButton("Exclude", new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						dialog.cancel();
						newVocabularies.clear();
						
						if (vocabularies.isEmpty())
						{
							answer = Answer.SKIP;
							next();
						}
					}
				});
			alertDialog.setCancelable(false);
			alertDialog.show();
		}
			
		preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
		
		setHasOptionsMenu(true);
		setTitle();
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
        return inflater.inflate(R.layout.activity_quiz, container, false);
	}

	@Override
	public void onViewCreated(View v, Bundle savedInstanceState)
	{
		progress_quiz = (ProgressBar)v.findViewById(R.id.progress_quiz);
		text_question = (TextView)v.findViewById(R.id.text_question);
		text_type = (TextView)v.findViewById(R.id.text_type);
		card_solution = v.findViewById(R.id.card_solution);
		card_solution.setVisibility(View.INVISIBLE);
		card_solution.setOnClickListener(this);
		text_solution_icon = (TextView)v.findViewById(R.id.text_solution_icon);
		text_solution = (TextView)v.findViewById(R.id.text_solution);
		text_category = (TextView)v.findViewById(R.id.text_category);
		text_level_up = (TextView)v.findViewById(R.id.text_level_up);
		
		text_question.setTextLocale(Locale.JAPANESE);
		text_solution.setTextLocale(Locale.JAPANESE);
		
		text_answer = (EditText)v.findViewById(R.id.text_answer);
		text_answer.setTextLocale(Locale.JAPANESE);
		text_answer.setOnEditorActionListener(new OnEditorActionListener()
			{
				@Override
				public boolean onEditorAction(TextView p1, int p2, KeyEvent event)
				{
					onAnswered(text_answer.getText().toString());
					return true;
				}
			});

		ImageButton button_enter = (ImageButton) v.findViewById(R.id.button_enter);
		button_enter.setOnClickListener(new View.OnClickListener() 
			{
				public void onClick(View v) 
				{
					onAnswered(text_answer.getText().toString());
				}
			});
			
		button_retry = (ImageButton) v.findViewById(R.id.button_retry);
		button_retry.setOnClickListener(new View.OnClickListener() 
			{
				public void onClick(View v) 
				{
					int index = vocabularies.indexOf(vocabulary);
					vocabulary = dbHelper.getVocabulary(dbHelper.getId(vocabulary.kanji));
					vocabularies.set(index, vocabulary);
					text_category.setText("" + vocabulary.category);
					retype = false;
					button_retry.setVisibility(View.GONE);
					stat = lastStat;
					timesChecked_kanji = lastTimesChecked_kanji;
					timesChecked_reading = lastTimesChecked_reading;
					timesChecked_meaning = lastTimesChecked_meaning;
					history_quiz.remove(history_quiz.size() - 1);
				}
			});
			
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
			
		button_sound = (ImageButton) v.findViewById(R.id.button_sound);
		button_sound.setOnClickListener(listener);
		button_sound2 = (ImageButton) v.findViewById(R.id.button_sound2);
		button_sound2.setOnClickListener(listener);
		
		layout_stroke_order = (RelativeLayout) v.findViewById(R.id.layout_stroke_order);
		card_stroke_order = v.findViewById(R.id.card_stroke_order);
		final ProgressBar progress_stroke_order = (ProgressBar) v.findViewById(R.id.progress_stroke_order);
		final ImageButton stroke_order3 = (ImageButton) v.findViewById(R.id.button_stroke_order3);
		
		card_stroke_order.setVisibility(View.INVISIBLE);
		listener = new View.OnClickListener()
		{
				@Override
				public void onClick(View p1)
				{
					next();
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
		button_stroke_order = (ImageButton) v.findViewById(R.id.button_stroke_order);
		button_stroke_order.setOnClickListener(listener);
		button_stroke_order2 = (ImageButton) v.findViewById(R.id.button_stroke_order2);
		button_stroke_order2.setOnClickListener(listener);
		
		v.findViewById(R.id.button_overflow_stroke_order).setOnClickListener(this);
		
		answer = Answer.SKIP;
		next();
    }
	
	String input;
	
	public void onAnswered(String ans)
	{
		if (card_solution.getVisibility() == View.VISIBLE || card_stroke_order.getVisibility() == View.VISIBLE)
		{
			next();
			return;
		}
		
		input = ans;
		text_answer.getText().clear();
		
		if (ans.isEmpty())
			return;
			
		else if (answer_type == answer_type.READING && !StringHelper.isKana(ans))
			answer = Answer.RETRY;
		else
			answer = vocabulary.getAnswer(dbHelper, ans, answer_type, question_type);
		
		if (answer == Answer.CORRECT)
		{
			text_solution_icon.setText("✔");
			text_solution.setText("Correct" + vocabulary.makeSuggestion(answer_type, ans));
			
			if (!retype && (answer_type == QuestionType.KANJI || vocabulary.answered_kanji) && (answer_type == QuestionType.READING || vocabulary.answered_reading || vocabulary.reading.length == 0) && (answer_type == QuestionType.MEANING || vocabulary.answered_meaning))
				text_level_up.setText(vocabulary.answered_correct ? "+1" : "+0");
			
			else
				text_level_up.setText("");
			
			if (preferences.getBoolean("soundQuiz", false) && answer_type == QuestionType.READING)
				vocabulary.playSound(dbHelper);
		}
		else if (answer == Answer.SIMILIAR)
		{
			if (answer_type == answer_type.KANJI)
			{
				text_solution_icon.setText("✖");
				text_solution.setText("Not quite:\n" + vocabulary.correctAnswer(answer_type));
				
				if (retype)
					text_level_up.setText("");
				else
					text_level_up.setText("-0");
			}
			else
			{
				text_solution_icon.setText("✔");
				text_solution.setText("Close enough");
				
				if (!retype && (answer_type == QuestionType.KANJI || vocabulary.answered_kanji) && (answer_type == QuestionType.READING || vocabulary.answered_reading || vocabulary.reading.length == 0) && (answer_type == QuestionType.MEANING || vocabulary.answered_meaning))
					text_level_up.setText(vocabulary.answered_correct ? "+1" : "+0");
				
				else
					text_level_up.setText("");
					
				if (preferences.getBoolean("soundQuiz", false) && answer_type == QuestionType.READING)
					vocabulary.playSound(dbHelper);
			}
		}
		else if (answer == Answer.DIFFERENT)
		{ 
			text_solution_icon.setText("✔");
			text_solution.setText("Try a different vocabulary");
			text_level_up.setText("");
		}
		else if (answer == Answer.WRONG)
		{
			text_solution_icon.setText("✖");
			text_solution.setText("Correct answer was:\n" + vocabulary.correctAnswer(answer_type));
			
			if (retype || vocabulary.category == 0)
				text_level_up.setText("");
			else
				text_level_up.setText("-" + (vocabulary.category - vocabulary.getLastSavePoint(getContext())));
			
			if (preferences.getBoolean("soundQuiz", false) && answer_type == QuestionType.READING)
					vocabulary.playSound(dbHelper);

		}
		else if (answer == Answer.RETRY)
		{
			text_solution_icon.setText("?");
			text_solution.setText("Enter the correct " + text_type.getText().toString() + (answer_type == QuestionType.READING ? " in Hiragana" : ""));
			
			text_level_up.setText("");
		}
		
		if (answer == Answer.RETRY || answer == Answer.DIFFERENT)
		{
			button_sound2.setVisibility(View.GONE);
			button_stroke_order2.setVisibility(View.GONE);
		}
		else
		{
			button_stroke_order2.setVisibility(JishoHelper.isStrokeOrderAvailable(getContext()) ? View.VISIBLE : View.GONE);
			button_sound2.setVisibility(View.GONE);
			if (vocabulary.answered_reading || answer_type == QuestionType.READING)
				vocabulary.prepareSound(dbHelper, new OnProcessSuccessListener()
				{
					@Override
					public void onProcessSuccess(Object[] args)
					{
						button_sound2.setVisibility(View.VISIBLE);
					}
				});
		}
		
		appear = ViewAnimationUtils.createCircularReveal(card_solution, card_solution.getWidth() / 2, card_solution.getHeight() / 2, 0, Math.max(card_solution.getWidth(), card_solution.getHeight()));

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
	}
	
	public void next()
	{
		if (card_stroke_order == null)
			return;
		
		if (card_stroke_order.getVisibility() == View.VISIBLE)
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
			return;
		}
		
		if (vocabulary != null && answer != Answer.RETRY && answer != Answer.DIFFERENT && answer != answer.SKIP && !retype)
		{
			vocabulary.answer(dbHelper, getContext(), input, answer_type, question_type);
			text_category.setText("" + vocabulary.category);
			
			lastTimesChecked_kanji = timesChecked_kanji;
			lastTimesChecked_reading = timesChecked_reading;
			lastTimesChecked_meaning = timesChecked_meaning;
			
			if (answer_type == QuestionType.KANJI)
			{
				timesChecked_kanji++;
				
				if (answer == Answer.CORRECT)
				{
					timesCorrect_kanji++;
					progress++;
					setTitle();
				}
			}
			else if (answer_type == QuestionType.READING)
			{
				timesChecked_reading++;
				
				if (answer == Answer.CORRECT)
				{
					timesCorrect_reading++;
					progress++;
					setTitle();
				}
			}
			else if (answer_type == QuestionType.MEANING)
			{
				timesChecked_meaning++;
				
				if (answer == Answer.CORRECT || answer == Answer.SIMILIAR)
				{
					timesCorrect_meaning++;
					progress++;
					setTitle();
				}
			}
		}
			
		if (answer != Answer.RETRY && answer != Answer.DIFFERENT && answer != Answer.WRONG)
		{
			retype = false;
			button_retry.setVisibility(View.GONE);
			
			if (vocabulary != null && vocabulary.answered_kanji && vocabulary.answered_meaning && (vocabulary.answered_reading || vocabulary.reading.length <= 0))
			{
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
				
				review1[29]++;
				if (vocabulary.answered_correct)
					review2[29]++;
				
				preferences.edit()
					.putLong("lastReviewDate", System.currentTimeMillis())
					.putString("review1", StringHelper.toString(review1))
					.putString("review2", StringHelper.toString(review2))
					.apply();

				if (vocabulary.answered_correct)
				{
					vocabulary.category++;
					vocabularies_plus.add(vocabulary.id);
				}
				else if (vocabulary.category_history[vocabulary.category_history.length - 1] == vocabulary.category)
					vocabularies_neutral.add(vocabulary.id);
					
				else
					vocabularies_minus.add(vocabulary.id);
					
				vocabulary.lastChecked = System.currentTimeMillis();
				vocabulary.nextReview = vocabulary.lastChecked + Vocabulary.getNextReview(vocabulary.category);
				
				vocabulary.answered_kanji = false;
				vocabulary.answered_meaning = false;
				vocabulary.answered_reading = false;
				vocabulary.answered_correct = true;

				System.arraycopy(vocabulary.category_history, 1, vocabulary.category_history, 0, vocabulary.category_history.length - 1);
				vocabulary.category_history[vocabulary.category_history.length - 1] = vocabulary.category;
				
				vocabularies.remove(vocabulary);
				setTitle();
			}
			
			if (vocabulary != null)
				dbHelper.updateVocabulary(vocabulary);
			
			if (vocabularies.isEmpty())
			{
				if (!newVocabularies.isEmpty())
				{
					vocabulary = newVocabularies.get(0);
				}
				else
				{
					int[] history_quiz = new int[this.history_quiz.size()];
					for (int i = 0; i < history_quiz.length; ++i)
						history_quiz[i] = this.history_quiz.get(i);

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
					
					tmp = new int[vocabularies_neutral.size()];
					for (int i = 0; i < tmp.length; ++i)
						tmp[i] = vocabularies_neutral.get(i);
					bundle.putIntArray("vocabularies_neutral", tmp);
					
					tmp = new int[vocabularies_minus.size()];
					for (int i = 0; i < tmp.length; ++i)
						tmp[i] = vocabularies_minus.get(i);
					bundle.putIntArray("vocabularies_minus", tmp);
					fragment.setArguments(bundle);
					getFragmentManager().beginTransaction().replace(R.id.layout_content, fragment).commit();
					return;
				}
			}
			else if (vocabularies.size() == 1)
				vocabulary = vocabularies.get(0);
			else
			{
				int i;
				while (vocabularies.get(i = random.nextInt(Math.min(vocabularies.size(), 20))) == vocabulary) {}
				
				vocabulary = vocabularies.get(i);
			}
				
			TransitionManager.beginDelayedTransition((ViewGroup) getView(), new AutoTransition().excludeTarget(TextView.class, true));
			
			ArrayList<QuestionType> tmp = new ArrayList<>();
			if (!vocabulary.answered_kanji)
				tmp.add(QuestionType.KANJI);
			if (!vocabulary.answered_reading && vocabulary.reading.length > 0)
				tmp.add(QuestionType.READING);
			if (!vocabulary.answered_meaning)
				tmp.add(QuestionType.MEANING);
				
			if (tmp.size() == 0)
				return;
				
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
			
			text_question.setText(vocabulary.question(question_type));
			
			if (question_type == question_type.KANJI)
				text_question.setTextSize(TypedValue.COMPLEX_UNIT_DIP, text_question.getText().length() > 6 ? 45 : 55);
			
			else if (question_type == question_type.READING)
				text_question.setTextSize(TypedValue.COMPLEX_UNIT_DIP, text_question.getText().length() > 10 ? 40 : text_question.getText().length() > 6 ? 45 : 50);
	
			else if (question_type == question_type.MEANING)
				text_question.setTextSize(TypedValue.COMPLEX_UNIT_DIP, text_question.getText().length() > 30 ? 35 : text_question.getText().length() > 20 ? 40 : (text_question.getText().length() > 10 ? 45 : 50));
			
			if (answer_type == QuestionType.MEANING)
				text_answer.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT);
			else
				text_answer.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
			
			text_type.setText((answer_type == QuestionType.MEANING ? "MEANING" : answer_type == QuestionType.KANJI ? (vocabulary.reading.length == 0 ? "KANA" : "KANJI") : "READING"));
			text_category.setText("" + vocabulary.category);
			
			button_sound.setVisibility(View.GONE);
			if (JishoHelper.isInternetAvailable(getContext()) && (question_type == QuestionType.READING || question_type == QuestionType.KANJI && answer_type != QuestionType.READING && vocabulary.answered_reading))
				vocabulary.prepareSound(dbHelper, new OnProcessSuccessListener()
				{
					@Override
					public void onProcessSuccess(Object[] args)
					{
						button_sound.setVisibility(View.VISIBLE);
					}
				
				});
			button_stroke_order.setVisibility(JishoHelper.isStrokeOrderAvailable(getContext()) && question_type == QuestionType.KANJI ? View.VISIBLE : View.GONE);
			
			if (JishoHelper.isInternetAvailable(getContext()) && preferences.getBoolean("soundQuiz", false))
			{
				if (question_type == QuestionType.READING || question_type == QuestionType.KANJI && answer_type != QuestionType.READING)
				{
					vocabulary.playSound(dbHelper);
				}
			}
		}
		else if (answer == Answer.WRONG)
		{
			retype = true;
			button_retry.setVisibility(View.VISIBLE);
		}
			
		if (answer != Answer.DIFFERENT && answer != Answer.RETRY && answer != Answer.SKIP)
		{
			lastStat = stat;
			
			if (answer == Answer.CORRECT)
				stat++;
			else if (answer == Answer.WRONG)
				stat = 0;
				
			history_quiz.add(stat);
		}
		
		if (card_solution != null && card_solution.getVisibility() == View.VISIBLE)
		{
			disappear = ViewAnimationUtils.createCircularReveal(card_solution, card_solution.getWidth() / 2, card_solution.getHeight() / 2, Math.max(card_solution.getWidth(), card_solution.getHeight()), 0);

			disappear.addListener(new AnimatorListenerAdapter() 
				{
					@Override
					public void onAnimationEnd(Animator animation) 
					{
						super.onAnimationEnd(animation);
						card_solution.setVisibility(View.INVISIBLE);
					}
				});

			disappear.start();
		}
		
		answer = Answer.RETRY;
	}

	@Override
	public void onClick(View view)
	{
		if (view.getId() == R.id.card_solution)
			next();

		else if (view.getId() == R.id.button_overflow)
		{
			PopupMenu popup = new PopupMenu(getActivity(), view);
			popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
			{
				@Override
				public boolean onMenuItemClick(MenuItem item)
				{
					switch (item.getItemId()) 
					{
						case R.id.item_retry:
							answer = Answer.RETRY;
							next();
							return true;
							
						case R.id.item_detail:
							Intent intent = new Intent(getContext(), ActivityDetail.class);
							intent.putExtra("id", vocabulary.id);
							startActivity(intent);
							
							return true;

						case R.id.item_show_solutions:
							AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
							alertDialog.setTitle(answer_type == QuestionType.KANJI ? (vocabulary.reading.length == 0 ? "Kana for " : "KANJI for " + vocabulary.question(question_type)) : answer_type == QuestionType.READING ? "Readings for " + vocabulary.question(question_type) : "Meanings for " + vocabulary.question(question_type));
							StringBuilder message = new StringBuilder();
							message.append("Your input: ");
							message.append(input);
							message.append("\n\nPossible solutions:\n");
							
							if (answer_type == QuestionType.KANJI)
							{
								message.append("\t- ");
								message.append(vocabulary.kanji);
								message.append("\n");
							}
							else if (answer_type == QuestionType.READING)
							{
								for (String s : vocabulary.reading)
								{
									message.append("\t- ");
									message.append(s);
									message.append("\n");
								}
							}
							else
							{
								for (String s : vocabulary.meaning)
								{
									message.append("\t- ");
									message.append(s);
									message.append("\n");
								}
							}
							
							if (question_type == QuestionType.READING && vocabulary.sameReading.length > 0
							|| question_type == QuestionType.MEANING && vocabulary.sameMeaning.length > 0)
							{
								for (int id : (question_type == QuestionType.READING ? vocabulary.sameReading : vocabulary.sameMeaning))
								{
									if (answer_type == QuestionType.KANJI)
									{
										message.append("\t- ");
										message.append(dbHelper.getString(id, "kanji"));
										message.append("\n");
									}
									else 
									{
										for (String s : dbHelper.getStringArray(id, answer_type == QuestionType.READING ? "reading" : "meaning"))
										{
											message.append("\t- ");
											message.append(s);
											message.append("\n");
										}
									}
								}
							}
							
							alertDialog.setMessage(message.toString());

							alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
								new DialogInterface.OnClickListener() 
								{
									public void onClick(DialogInterface dialog, int which) 
									{
										dialog.dismiss();

									}
								});
							
							alertDialog.show();
							return true;
							
						case R.id.item_exclude:
							vocabularies.remove(vocabulary);
							maxProgress -= vocabulary.reading.length == 0 ? 2 : 3;
							if (vocabulary.answered_kanji)
								progress--;
							if (vocabulary.answered_reading)
								progress--;
							if (vocabulary.answered_meaning)
								progress--;
							answer = Answer.SKIP;
							next();
							setTitle();
							return true;
							
						default:
							return false;
					}
				}
			});
			MenuInflater inflater = popup.getMenuInflater();
			inflater.inflate(R.menu.activity_quiz, popup.getMenu());
			popup.getMenu().findItem(R.id.item_retry).setVisible(answer == Answer.WRONG || answer == Answer.SIMILIAR);
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

	public void setTitle()
	{
		if (getActivity() instanceof AppCompatActivity && ((AppCompatActivity) getActivity()).getSupportActionBar() != null)
			((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Quiz, " + vocabularies.size() + (vocabularies.size() == 1 ? " Vocabulary" : " Vocabularies"));
		
		if (progress_quiz != null)
		{
			progress_quiz.setMax(maxProgress);
			progress_quiz.setProgress(progress);
		}
	}

	@Override
	public void onResume()
	{
		super.onResume();
		
		setTitle();
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		
		dbHelper.close();
	}
}
