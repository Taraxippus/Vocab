package com.taraxippus.vocab.fragment;

import android.animation.*;
import android.app.*;
import android.content.*;
import android.os.*;
import android.preference.*;
import android.support.v7.view.menu.*;
import android.support.v7.widget.*;
import android.text.*;
import android.transition.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import android.widget.TextView.*;
import com.taraxippus.vocab.*;
import com.taraxippus.vocab.util.*;
import com.taraxippus.vocab.vocabulary.*;
import java.util.*;

import android.support.v7.widget.PopupMenu;

public class QuizFragment extends Fragment implements View.OnClickListener
{
	TextView question_text;
	TextView type_text;
	EditText answer_text;
	CardView card_question;
	CardView card_solution;
	TextView solution_icon;
	TextView solution_text;
	TextView category_text;
	TextView level_up_text;
	CardView card_stroke_order;
	LinearLayout layout_stroke_order;
	
	ImageView sound;
	ImageView sound2;
	ImageView stroke_order;
	ImageView stroke_order2;
	
	Animator disappear;
	Animator appear;
	
	public Answer answer;
	QuestionType answer_type = QuestionType.MEANING;
	QuestionType question_type = QuestionType.KANJI;
	
	public Vocabulary vocabulary;
	public boolean retype;
	
	final Random random = new Random();
	
	MainActivity main;
	
	public QuizFragment()
	{
		super();
		
	}

	public void setTransitions(MainActivity main)
	{
		this.setEnterTransition(TransitionInflater.from(main).inflateTransition(android.R.transition.slide_top));
		this.setReturnTransition(TransitionInflater.from(main).inflateTransition(android.R.transition.fade));

		this.setAllowEnterTransitionOverlap(false);
		this.setAllowReturnTransitionOverlap(false);
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		this.main = (MainActivity)getActivity();
	
        View v = inflater.inflate(R.layout.quiz, container, false);

		question_text = (TextView)v.findViewById(R.id.question_text);
		type_text = (TextView)v.findViewById(R.id.type_text);
		card_question = (CardView)v.findViewById(R.id.card_question);
		card_solution = (CardView)v.findViewById(R.id.card_solution);
		card_solution.setVisibility(View.INVISIBLE);
		card_solution.setOnClickListener(this);
		solution_icon = (TextView)v.findViewById(R.id.solution_icon);
		solution_text = (TextView)v.findViewById(R.id.solution_text);
		category_text = (TextView)v.findViewById(R.id.category_text);
		level_up_text = (TextView)v.findViewById(R.id.level_up_text);
		
		answer_text = (EditText)v.findViewById(R.id.answer_text);
		answer_text.setOnEditorActionListener(new OnEditorActionListener()
			{
				@Override
				public boolean onEditorAction(TextView p1, int p2, KeyEvent event)
				{
					onAnswered(answer_text.getText().toString());
					return true;
				}
			});

		final Button button_enter = (Button) v.findViewById(R.id.button_enter);
		button_enter.setOnClickListener(new View.OnClickListener() 
			{
				public void onClick(View v) 
				{
					onAnswered(answer_text.getText().toString());
				}
			});
			
		final ImageButton button_overflow = (ImageButton) v.findViewById(R.id.overflow_button);
		button_overflow.setOnClickListener(this);
		
		View.OnClickListener listener = new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					vocabulary.playSound();
				}
			};
		sound = (ImageButton) v.findViewById(R.id.sound);
		sound.setOnClickListener(listener);
		sound2 = (ImageButton) v.findViewById(R.id.sound2);
		sound2.setOnClickListener(listener);
		
		layout_stroke_order = (LinearLayout) v.findViewById(R.id.layout_stroke_order);
		card_stroke_order = (CardView) v.findViewById(R.id.card_stroke_order);
		final ProgressBar progress_stroke_order = (ProgressBar) v.findViewById(R.id.stroke_order_progress);
		final ImageButton stroke_order3 = (ImageButton) v.findViewById(R.id.stroke_order3);
		
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
				vocabulary.showStrokeOrder(layout_stroke_order, new OnProcessSuccessListener()
				{
						@Override
						public void onProcessSuccess(Object[] args)
						{
							progress_stroke_order.setVisibility(View.GONE);
						}
					
				}, true, true);
			
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
		stroke_order = (ImageButton) v.findViewById(R.id.stroke_order);
		stroke_order.setOnClickListener(listener);
		stroke_order2 = (ImageButton) v.findViewById(R.id.stroke_order2);
		stroke_order2.setOnClickListener(listener);
		
		answer = Answer.SKIP;
		next();
		
		return v;
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
		
		if (ans.isEmpty())
			return;
		else if (answer_type == answer_type.READING && !StringHelper.isKana(ans))
			answer = Answer.RETRY;
		else
			answer = vocabulary.getAnswer(ans, answer_type, question_type);
			
		answer_text.getText().clear();
		
		if (answer == Answer.CORRECT)
		{
			solution_icon.setText("✔");
			solution_text.setText("Correct");
			
			if (!retype && (answer_type == QuestionType.KANJI || vocabulary.answered_kanji) && (answer_type == QuestionType.READING || vocabulary.answered_reading || vocabulary.reading.length == 0) && (answer_type == QuestionType.MEANING || vocabulary.answered_meaning))
			{
				level_up_text.setText(vocabulary.answered_correct ? "+1" : "+0");
			}
			else
			{
				level_up_text.setText("");
			}
		}
		else if (answer == Answer.SIMILIAR)
		{
			if (answer_type == answer_type.KANJI)
			{
				solution_icon.setText("✖");
				solution_text.setText("Not quite:\n" + vocabulary.correctAnswer(answer_type));
				
				if (retype)
					level_up_text.setText("");
				else
					level_up_text.setText("-0");
			}
			else
			{
				solution_icon.setText("✔");
				solution_text.setText("Close enough");
				
				if (!retype && (answer_type == QuestionType.KANJI || vocabulary.answered_kanji) && (answer_type == QuestionType.READING || vocabulary.answered_reading || vocabulary.reading.length == 0) && (answer_type == QuestionType.MEANING || vocabulary.answered_meaning))
				{
					level_up_text.setText(vocabulary.answered_correct ? "+1" : "+0");
				}
				else
				{
					level_up_text.setText("");
				}
			}
		}
		else if (answer == Answer.WRONG)
		{
			solution_icon.setText("✖");
			solution_text.setText("Correct answer was:\n" + vocabulary.correctAnswer(answer_type));
			
			if (retype)
				level_up_text.setText("");
			else
				level_up_text.setText("-" + (vocabulary.category - vocabulary.getLastSavePoint()));
			
			if (PreferenceManager.getDefaultSharedPreferences(main).getBoolean("soundQuiz", false) && answer_type == QuestionType.READING)
					vocabulary.playSound();

		}
		else if (answer == Answer.RETRY)
		{
			solution_icon.setText("?");
			solution_text.setText("Enter the correct " + type_text.getText().toString() + (answer_type == QuestionType.READING ? " in Hiragana" : ""));
			
			level_up_text.setText("");
		}
		
		if (answer == Answer.RETRY)
		{
			sound2.setVisibility(View.GONE);
			stroke_order2.setVisibility(View.GONE);
		}
		else
		{
			stroke_order2.setVisibility((main.jishoHelper.offlineStrokeOrder() || main.jishoHelper.isInternetAvailable()) && vocabulary.reading.length != 0 ? View.VISIBLE : View.GONE);
			sound2.setVisibility(View.GONE);
			if (vocabulary.answered_reading || answer_type == QuestionType.READING)
				vocabulary.prepareSound(new OnProcessSuccessListener()
				{
					@Override
					public void onProcessSuccess(Object[] args)
					{
						sound2.setVisibility(View.VISIBLE);
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
						layout_stroke_order.removeAllViewsInLayout();
					}
				});

			disappear.start();
			return;
		}
		
		if (vocabulary != null && answer != Answer.RETRY && answer != answer.SKIP && !retype)
		{
			vocabulary.answer(input, answer_type, question_type);
			category_text.setText("" + vocabulary.category);
		}
			
		if (answer != Answer.RETRY && answer != Answer.WRONG)
		{
			retype = false;
			
			if (vocabulary != null && vocabulary.answered_kanji && vocabulary.answered_meaning && (vocabulary.answered_reading || vocabulary.reading.length <= 0))
			{
				if (vocabulary.answered_correct)
					vocabulary.category++;
				
				vocabulary.lastChecked = System.currentTimeMillis();
					
				vocabulary.answered_kanji = false;
				vocabulary.answered_meaning = false;
				vocabulary.answered_reading = false;
				vocabulary.answered_correct = true;
				
				System.arraycopy(vocabulary.category_history, 1, vocabulary.category_history, 0, vocabulary.category_history.length - 1);
				vocabulary.category_history[vocabulary.category_history.length - 1] = vocabulary.category;
				
				main.vocabulary_learned.remove(vocabulary);
				main.saveHandler.save();
				
				main.setTap(this);
			}
			
			if (main.vocabulary_learned.isEmpty())
			{
				main.dialogHelper.createDialog("Completed Quiz!", "Finished quiz! Learn new vocabularies or come back later!");
				main.changeFragment(main.home, "quiz");
				main.updateNotification();
				
				return;
			}
			
			main.vocabulary_selected = random.nextInt(main.vocabulary_learned.size());
			vocabulary = main.vocabulary_learned.get(main.vocabulary_selected);
			main.vocabulary_selected = main.vocabulary.indexOf(vocabulary);
			
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
			
			question_text.setText(vocabulary.question(question_type));
			
			if (question_type == question_type.KANJI)
				question_text.setTextSize(TypedValue.COMPLEX_UNIT_DIP, question_text.getText().length() > 6 ? 45 : 55);
			
			else if (question_type == question_type.READING)
				question_text.setTextSize(TypedValue.COMPLEX_UNIT_DIP, question_text.getText().length() > 10 ? 40 : question_text.getText().length() > 6 ? 45 : 50);
	
			else if (question_type == question_type.MEANING)
				question_text.setTextSize(TypedValue.COMPLEX_UNIT_DIP, question_text.getText().length() > 30 ? 35 : question_text.getText().length() > 20 ? 40 : (question_text.getText().length() > 10 ? 45 : 50));
			
			if (answer_type == QuestionType.MEANING)
				answer_text.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT);
			else
				answer_text.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
			
			type_text.setText((answer_type == QuestionType.MEANING ? "MEANING" : answer_type == QuestionType.KANJI ? (vocabulary.reading.length == 0 ? "KANA" : "KANJI") : "READING"));
			category_text.setText("" + vocabulary.category);
			
			sound.setVisibility(View.GONE);
			if (main.jishoHelper.isInternetAvailable() && (question_type == QuestionType.READING || question_type == QuestionType.KANJI && answer_type != QuestionType.READING && vocabulary.answered_reading))
				vocabulary.prepareSound(new OnProcessSuccessListener()
				{
					@Override
					public void onProcessSuccess(Object[] args)
					{
						sound.setVisibility(View.VISIBLE);
					}
				
				});
			stroke_order.setVisibility((main.jishoHelper.offlineStrokeOrder() || main.jishoHelper.isInternetAvailable()) && vocabulary.reading.length != 0 && question_type == QuestionType.KANJI ? View.VISIBLE : View.GONE);
			
			if (main.jishoHelper.isInternetAvailable() && PreferenceManager.getDefaultSharedPreferences(main).getBoolean("soundQuiz", false))
			{
				if (question_type == QuestionType.READING || question_type == QuestionType.KANJI && answer_type != QuestionType.READING)
				{
					vocabulary.playSound();
				}
			}
		}
		else if (answer == Answer.WRONG)
			retype = true;
		
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
	}

	@Override
	public void onClick(View view)
	{
		if (view.getId() == R.id.card_solution)
		{
			next();
		}
		else if (view.getId() == R.id.overflow_button)
		{
			PopupMenu popup = new PopupMenu(getActivity(), view)
			{
				@Override
				public boolean onMenuItemSelected(MenuBuilder menu, MenuItem item)
				{
					switch (item.getItemId()) 
					{
						case R.id.retry:
							answer = Answer.RETRY;
							next();
							return true;
							
						case R.id.detail:
							main.changeFragment(main.getDetailFragment(), "detail");
						
							return true;

						case R.id.show_solutions:
							AlertDialog alertDialog = new AlertDialog.Builder(main).create();
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
							
							if (question_type == QuestionType.READING && vocabulary.sameReading.size() > 0
							|| question_type == QuestionType.MEANING && vocabulary.sameMeaning.size() > 0)
							{
								for (Vocabulary v : (question_type == QuestionType.READING ? vocabulary.sameReading : vocabulary.sameMeaning))
								{
									if (answer_type == QuestionType.KANJI)
									{
										message.append("\t- ");
										message.append(v.kanji);
										message.append("\n");
									}
									else if (answer_type == QuestionType.READING)
									{
										for (String s : v.reading)
										{
											message.append("\t- ");
											message.append(s);
											message.append("\n");
										}
									}
									else
									{
										for (String s : v.meaning)
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
							
						case R.id.learn_remove:
							
							vocabulary.learned = false;
							main.vocabulary_learned.remove(vocabulary);
							
							return true;
							
						default:
							return super.onMenuItemSelected(menu, item);
					}
				}
			};
			MenuInflater inflater = popup.getMenuInflater();
			inflater.inflate(R.menu.quiz, popup.getMenu());
			popup.getMenu().findItem(R.id.retry).setVisible(answer == Answer.WRONG || answer == Answer.SIMILIAR);
			popup.show();
		}
	}

	@Override
	public void onResume()
	{
		super.onResume();

		((MainActivity)getActivity()).setTap(this);
	}
}
