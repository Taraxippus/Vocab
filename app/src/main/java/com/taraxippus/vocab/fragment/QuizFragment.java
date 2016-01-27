package com.taraxippus.vocab.fragment;

import android.animation.*;
import android.app.*;
import android.content.*;
import android.os.*;
import android.support.v7.view.menu.*;
import android.support.v7.widget.*;
import android.text.*;
import android.transition.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import android.widget.TextView.*;
import com.taraxippus.vocab.*;
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
	
	Animator disappear;
	Animator appear;
	
	public Answer answer;
	QuestionType answer_type = QuestionType.MEANING;
	QuestionType question_type = QuestionType.KANJI;
	
	public Vocabulary vocabulary;
	
	final Random random = new Random();
	
	MainActivity main;
	
	public QuizFragment()
	{
		super();
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

		next();
		
		return v;
    }
	
	String input;
	
	public void onAnswered(String ans)
	{
		if (card_solution.getVisibility() == View.VISIBLE)
		{
			next();
			return;
		}
		
		input = ans;
		int category = vocabulary.category;
		
		if (ans.isEmpty())
			return;
		else if (answer_type == answer_type.READING && !Vocabulary.isKana(ans))
			answer = Answer.RETRY;
		else
			answer = vocabulary.answer(ans, answer_type, question_type);
		
		answer_text.getText().clear();
		
		if (answer == Answer.CORRECT)
		{
			solution_icon.setText("✔");
			solution_text.setText("Correct");
			
			if (vocabulary.answered_kanji && (vocabulary.answered_reading || vocabulary.reading.length <= 0) && vocabulary.answered_meaning && vocabulary.answered_correct)
			{
				level_up_text.setText("+1");
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
				
				level_up_text.setText("-0");
			}
			else
			{
				solution_icon.setText("✔");
				solution_text.setText("Close enough");
				
				if (vocabulary.answered_kanji && vocabulary.answered_reading && vocabulary.answered_meaning && vocabulary.answered_correct)
				{
					level_up_text.setText("+1");
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
			
			level_up_text.setText("-" + (category - vocabulary.category));
		}
		else if (answer == Answer.RETRY)
		{
			solution_icon.setText("?");
			solution_text.setText("Enter the correct " + type_text.getText().toString() + (answer_type == QuestionType.READING ? " in Hiragana" : ""));
			
			level_up_text.setText("");
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
		if (answer != Answer.RETRY)
		{
			if (vocabulary != null && vocabulary.answered_kanji && vocabulary.answered_meaning && (vocabulary.answered_reading || vocabulary.reading.length <= 0))
			{
				if (vocabulary.answered_correct)
					vocabulary.category++;
				
				vocabulary.lastChecked = System.currentTimeMillis();
					
				vocabulary.answered_kanji = false;
				vocabulary.answered_meaning = false;
				vocabulary.answered_reading = false;
				vocabulary.answered_correct = true;
				main.vocabulary_learned.remove(vocabulary);
				
				main.saveHandler.save();
				
				main.setTap(this);
			}
			
			if (main.vocabulary_learned.isEmpty())
			{
				main.dialogHelper.createDialog("Completed Quiz!", "Finished quiz! Learn new vocabularies or come back later!");
				main.changeFragment(main.home, null);
				
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
			
			answer_type = tmp.get(random.nextInt(tmp.size()));
			
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
			{
				question_text.setTextSize(TypedValue.COMPLEX_UNIT_DIP, question_text.getText().length() > 6 ? 45 : 55);
			}
			else if (question_type == question_type.READING)
			{
				question_text.setTextSize(TypedValue.COMPLEX_UNIT_DIP, question_text.getText().length() > 10 ? 40 : question_text.getText().length() > 6 ? 45 : 50);
			}
			else if (question_type == question_type.MEANING)
			{
				question_text.setTextSize(TypedValue.COMPLEX_UNIT_DIP, question_text.getText().length() > 30 ? 35 : question_text.getText().length() > 20 ? 40 : (question_text.getText().length() > 10 ? 45 : 50));
			}
			
			if (answer_type == QuestionType.MEANING)
			{
				answer_text.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT);
			}
			else
			{
				answer_text.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
			}
			
			type_text.setText((answer_type == QuestionType.MEANING ? "MEANING" : answer_type == QuestionType.KANJI ? (vocabulary.reading.length == 0 ? "KANA" : "KANJI") : "READING"));
			category_text.setText("" + vocabulary.category);
		}
		
		if (card_solution != null && card_solution.getVisibility() == View.VISIBLE)
		{
			disappear = ViewAnimationUtils.createCircularReveal(card_solution, card_solution.getWidth() / 2, card_solution.getHeight() / 2, card_solution.getWidth(), 0);

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
					Fragment fragment;
					FragmentManager fragmentManager;
					
					switch (item.getItemId()) 
					{
						case R.id.detail:
							fragment = new VocabularyFragment();
							fragment.setSharedElementEnterTransition(TransitionInflater.from(getActivity()).inflateTransition(R.transition.change_image_transform));
							fragment.setSharedElementReturnTransition(TransitionInflater.from(getActivity()).inflateTransition(R.transition.change_image_transform));

							fragment.setEnterTransition(TransitionInflater.from(getActivity()).inflateTransition(android.R.transition.slide_top));
							fragment.setReturnTransition(TransitionInflater.from(getActivity()).inflateTransition(android.R.transition.fade));

							fragmentManager = getFragmentManager();
							fragmentManager.beginTransaction()
								.replace(R.id.content_frame, fragment)
								.addToBackStack("detail")
								.commit();
							
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
