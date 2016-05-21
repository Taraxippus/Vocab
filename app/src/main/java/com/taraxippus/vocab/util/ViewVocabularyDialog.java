package com.taraxippus.vocab.util;

import android.app.*;
import android.content.*;
import android.preference.*;
import android.support.v7.view.menu.*;
import android.support.v7.widget.*;
import android.view.*;
import android.widget.*;
import com.taraxippus.vocab.*;
import com.taraxippus.vocab.vocabulary.*;

import android.support.v7.widget.PopupMenu;

public class ViewVocabularyDialog implements DialogInterface.OnDismissListener, View.OnClickListener, View.OnTouchListener, GestureDetector.OnGestureListener
{
	final MainActivity main;
	public final AlertDialog alertDialog;
	
	private GestureDetector gestureDetector; 
	
	final TextView kanji, reading, meaning, type, notes;
	final EditText practice;
	final LinearLayout layout_notes;
	final ImageButton sound;
	final ImageButton stroke_order;
	
	int index = 0;
	
	public ViewVocabularyDialog(final MainActivity main)
	{
		this.main = main;
		
		gestureDetector = new GestureDetector(main, this);
		
		alertDialog = new AlertDialog.Builder(main).create();
		alertDialog.setOnDismissListener(this);
		
		final View v = main.getLayoutInflater().inflate(R.layout.view_vocabulary_dialog, null);
		kanji = (TextView)v.findViewById(R.id.kanji_text);
		reading = (TextView)v.findViewById(R.id.reading_text);
		meaning = (TextView)v.findViewById(R.id.meaning_text);
		type = (TextView)v.findViewById(R.id.type);
		notes = (TextView)v.findViewById(R.id.notes_text);
		practice = (EditText)v.findViewById(R.id.answer_text);
		
		layout_notes = (LinearLayout) v.findViewById(R.id.layout_notes);
		
		v.findViewById(R.id.overflow_button).setOnClickListener(this);
		v.setOnTouchListener(this);
		
		sound = (ImageButton) v.findViewById(R.id.sound);
		sound.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					main.vocabulary_learned_new.get(index).playSound();
				}
			});
		
		stroke_order = (ImageButton) v.findViewById(R.id.stroke_order);
		stroke_order.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					main.vocabulary_learned_new.get(index).showStrokeOrder();
				}
			});
		
			
		alertDialog.setView(v);
		alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Next",
			new DialogInterface.OnClickListener() 
			{
				public void onClick(DialogInterface dialog, int which) {}
			});
		alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Close",
			new DialogInterface.OnClickListener() 
			{
				public void onClick(DialogInterface dialog, int which) 
				{
					dialog.cancel();
				}
			});
		alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Previous",
			new DialogInterface.OnClickListener() 
			{
				public void onClick(DialogInterface dialog, int which) {}
			});

		alertDialog.show();
		alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View p1)
				{
					index++;
					updateView();
				}
			});
	
		alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View p1)
				{
					index--;
					updateView();
				}
			});
			
		updateView();
	}
	
	public void updateView()
	{
		Vocabulary vocab = main.vocabulary_learned_new.get(index);

		kanji.setText(vocab.correctAnswer(QuestionType.KANJI));
		reading.setText(vocab.correctAnswer(QuestionType.READING));
		meaning.setText(vocab.correctAnswer(QuestionType.MEANING_INFO));
		notes.setText(vocab.notes.isEmpty() ? "" : vocab.notes);
		layout_notes.setVisibility(vocab.notes.isEmpty() ? View.GONE : View.VISIBLE);
		type.setText(vocab.getType());
		sound.setVisibility(View.GONE);
		stroke_order.setVisibility((main.jishoHelper.offlineStrokeOrder() || main.jishoHelper.isInternetAvailable()) && vocab.reading.length != 0 ? View.VISIBLE : View.GONE);

		vocab.prepareSound(new OnProcessSuccessListener()
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
		
		alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(main.vocabulary_learned_new.size() > index + 1);
		alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(index > 0);
		
		practice.getText().clear();
		
		if (PreferenceManager.getDefaultSharedPreferences(main).getBoolean("soundLearn", true))
		{
			main.vocabulary_learned_new.get(index).playSound();
		}
	}
	
	public void removeCurrent()
	{
		main.vocabulary_learned_new.remove(index);

		if (index == main.vocabulary_learned_new.size())
		{
			index--;
			if (index < 0)
			{
				alertDialog.dismiss();
				return;
			}
		}

		updateView();
	}
	
	@Override
	public void onDismiss(DialogInterface p1)
	{
		if (main.viewVocabularyDialog == this)
		{
			main.viewVocabularyDialog = null;
		}
	}
	
	@Override
	public void onClick(View v)
	{
		PopupMenu popup = new PopupMenu(main, v)
		{
			@Override
			public boolean onMenuItemSelected(MenuBuilder menu, MenuItem item)
			{
				switch (item.getItemId()) 
				{
					case R.id.detail:
						alertDialog.dismiss();
						main.vocabulary_selected = main.vocabulary.indexOf(main.vocabulary_learned_new.get(index));
		
						main.changeFragment(main.getDetailFragment(), "detail");
						
						return true;
					
					case R.id.open_jisho:
						main.jishoHelper.search(kanji.getText().toString());
						
						return true;
						
					case R.id.open_jisho_kanji:
						main.jishoHelper.search(kanji.getText().toString() + "%23kanji");

						return true;
						
					case R.id.learn_remove:
						main.vocabulary_learned_new.get(index).learned = false;
						
					case R.id.known:
						main.vocabulary_learned_new.get(index).category = 1;
						
					case R.id.exclude:
						main.vocabulary_learned.remove(main.vocabulary_learned_new.get(index));
						
						if (main.vocabulary.get(main.vocabulary_selected) == main.vocabulary_learned_new.get(index))
						{
							main.quiz.answer = Answer.SKIP;
							main.quiz.next();
							
							main.setTap(main.quiz);
						}
						
						removeCurrent();

						return true;

					default:
						return super.onMenuItemSelected(menu, item);
				}
			}
		};
		MenuInflater inflater = popup.getMenuInflater();
		inflater.inflate(R.menu.vocabulary_show, popup.getMenu());
		
		popup.show();
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
		if (p3 < -3000 && main.vocabulary_learned_new.size() > index + 1)
		{
			index++;
			updateView();

            return true;
		}
		else if (p3 > 3000 && index > 0)
		{
			index--;
			updateView();
			
			return true;
		}
		return false;
	}
}
